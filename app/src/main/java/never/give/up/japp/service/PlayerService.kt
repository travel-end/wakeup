package never.give.up.japp.service

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.*
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.*
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import never.give.up.japp.R
import never.give.up.japp.consts.Constants
import never.give.up.japp.consts.Extras
import never.give.up.japp.consts.SConsts
import never.give.up.japp.data.PlayHistoryLoader
import never.give.up.japp.delegate.MusicPlayerEngine
import never.give.up.japp.event.GlobalSingle
import never.give.up.japp.event.MetaChangedEvent
import never.give.up.japp.event.PlaylistEvent
import never.give.up.japp.event.StatusChangedEvent
import never.give.up.japp.model.Music
import never.give.up.japp.net.MusicApi
import never.give.up.japp.play.*
import never.give.up.japp.ui.play.PlayerActivity
import never.give.up.japp.utils.*
import never.give.up.japp.utils.FileUtils
import java.lang.ref.WeakReference
import java.util.*
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * @By Journey 2020/12/2
 * @Description
 */
class PlayerService : Service() {
    private val mBindStub = IMusicServiceStub(this)

    private lateinit var mPlayer: MusicPlayerEngine

    private var mPlayingMusic: Music? = null
    private var mPlayQueue = mutableListOf<Music>()
    private val mHistoryPos = mutableListOf<Int>()
    private var mPlayingPos: Int = -1
    private var mPlaylistId: String = Constants.PLAYLIST_QUEUE_ID
    private var playErrorTimes: Int = 0 // 错误次数 超过最大错误次数，自动停止播放
    private var percent: Int = 0// 播放缓存进度

    private lateinit var mServiceReceiver: ServiceReceiver
    private lateinit var mHeadsetReceiver: HeadsetReceiver
    private lateinit var mHeadsetPlugInReceiver: HeadsetPlugInReceiver
    private lateinit var intentFilter: IntentFilter

    private lateinit var mMainHandler: Handler
    private lateinit var mWorkThread: HandlerThread
    private lateinit var mHandler: MusicPlayerHandler

    private lateinit var powerManager: PowerManager
    lateinit var mWakeLock: PowerManager.WakeLock

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var audioAndFocusManager: AudioAndFocusManager

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mNotificationBuilder: NotificationCompat.Builder
    private lateinit var mNotification: Notification

    private var isMusicPlaying: Boolean = false
    private var isRunningForeground: Boolean = false
    private var mServiceInUse: Boolean = false
    private var mPauseByTransientLossOfFocus: Boolean = false// 暂时失去焦点，会再次回去音频焦点

    private var mNotificationPostTime: Long = 0L
    private var mServiceStartId: Int = -1


    companion object {
        private lateinit var instance: PlayerService
        fun getInstance(): PlayerService {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        "-------playService---onCreate----".log()
        instance = this
        initReceiver()
        initConfig()
        initTelephony()
        initNotify()
        initMediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder? {
        "-------playService---onBind----".log()
        return mBindStub
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        "-------playService---onStartCommand----".log()
        mServiceStartId = startId
        mServiceInUse = true
        if (intent != null) {
            val action = intent.action
            if (SConsts.SHUTDOWN == action) {
                releaseServiceUiAndStop()
                return START_NOT_STICKY
            }
            handleCommandIntent(intent)
        }
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        "-------playService---onUnbind----".log()
        mServiceInUse = false
        savePlayQueue(false)
        releaseServiceUiAndStop()
        stopSelf(mServiceStartId)
        return true
    }

    private fun initReceiver() {
        intentFilter = IntentFilter(SConsts.ACTION_SERVICE).apply {
            addAction(SConsts.ACTION_NEXT)
            addAction(SConsts.ACTION_PREV)
            addAction(SConsts.META_CHANGED)
            addAction(SConsts.SHUTDOWN)
            addAction(SConsts.ACTION_PLAY_PAUSE)
        }
        mServiceReceiver = ServiceReceiver()
        mHeadsetReceiver = HeadsetReceiver()
        mHeadsetPlugInReceiver = HeadsetPlugInReceiver()
        // 注册广播
        registerReceiver(mServiceReceiver, intentFilter)
        registerReceiver(mHeadsetReceiver, intentFilter)
        registerReceiver(mHeadsetPlugInReceiver, intentFilter)
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun initConfig() {
        mMainHandler = Handler(Looper.getMainLooper())
        PlayQueueManager.getPlayModeId()
        // 初始化工作线程
        mWorkThread = HandlerThread("MusicPlayerThread")
        mWorkThread.start()

        mHandler = MusicPlayerHandler(this, mWorkThread.looper)
        // 电源键
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PlayerWakelockTag")
        // TODO: 2020/12/4 桌面歌词

        // 初始化和设置MediaSessionCompat
        mediaSessionManager = MediaSessionManager(mBindStub, this, mMainHandler)
        audioAndFocusManager = AudioAndFocusManager(this, mHandler)
    }

    // 初始化电话监听服务
    private fun initTelephony() {
        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(ServicePhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun initNotify() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val albumName = getAlbumName()
        val artistName = getArtistName()
        val text = if (albumName.isNullOrEmpty()) artistName else "$artistName - $albumName"
        val playButtonResId = if (isMusicPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val title = if (isMusicPlaying) "暂停" else "播放"
        val playingIntent = Intent(this, PlayerActivity::class.java)
        playingIntent.action = Constants.DEFAULT_NOTIFICATION
        val clickIntent =
            PendingIntent.getActivity(this, 0, playingIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (mNotificationPostTime == 0L) {
            mNotificationPostTime = System.currentTimeMillis()
        }
        mNotificationBuilder = NotificationCompat.Builder(this, initChannelId())
            .setSmallIcon(R.drawable.ic_logo_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(clickIntent)
            .setContentTitle(getTitle())
            .setContentText(text)
            .setWhen(mNotificationPostTime)
            .addAction(
                R.drawable.ic_skip_previous,
                "上一首",
                retrievePlaybackAction(SConsts.ACTION_PREV)
            )
            .addAction(playButtonResId, title, retrievePlaybackAction(SConsts.ACTION_PLAY_PAUSE))
            .addAction(R.drawable.ic_skip_next, "下一首", retrievePlaybackAction(SConsts.ACTION_NEXT))
            .addAction(R.drawable.ic_lyric, "歌词", retrievePlaybackAction(SConsts.ACTION_LYRIC))
            .addAction(R.drawable.cha, "关闭", retrievePlaybackAction(SConsts.ACTION_CLOSE))
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mNotificationBuilder.setShowWhen(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 线控
            isRunningForeground = true
            mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            val style = androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionManager.mediaSession)
                .setShowActionsInCompactView(1, 0, 2, 3, 4)
            mNotificationBuilder.setStyle(style)
        }
        if (mPlayingMusic != null) {
            CoverLoader.loadImageViewByMusic(this, mPlayingMusic) {
                mNotificationBuilder.setLargeIcon(it)
                mNotification = mNotificationBuilder.build()
                mNotificationManager.notify(SConsts.NOTIFICATION_ID, mNotification)

            }
        }
        mNotification = mNotificationBuilder.build()
    }

    // 释放通知栏
    private fun releaseServiceUiAndStop() {
        if (isPlaying || mHandler.hasMessages(SConsts.TRACK_PLAY_ENDED)) {
            return
        }
        cancelNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager.release()
        }
        if (!mServiceInUse) {
            savePlayQueue(false)
            stopSelf(mServiceStartId)
        }

    }

    // 初始化音乐播放服务
    private fun initMediaPlayer() {
        mPlayer = MusicPlayerEngine(this)
        mPlayer.setHandler(mHandler)
        reloadPlayQueue()
    }


    private inner class ServicePhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            "TelephonyManager state=" + state + ",incomingNumber = " + phoneNumber.log()
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> pause()
                else -> {
                }
            }
        }
    }

    fun pause() {
        synchronized(this) {
            mHandler.removeMessages(SConsts.VOLUME_FADE_UP)
            mHandler.sendEmptyMessage(SConsts.VOLUME_FADE_DOWN)
            if (isPlaying) {
                isMusicPlaying = false
                notifyChange(SConsts.PLAY_STATE_CHANGED)
                updateNotification(false)
                val task = object : TimerTask() {
                    override fun run() {
                        val intent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
                        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId())
                        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                        sendBroadcast(intent)
                        mPlayer.pause()
                    }
                }
                val timer = Timer()
                timer.schedule(task, 200)
            }
        }
    }

    fun playPause() {
        if (isPlaying) {
            pause()
        } else {
            if (mPlayer.isInitialized) {
                play()
            } else {
                playCurrentAndNext()
            }
        }
    }

    fun play() {
        if (mPlayer.isInitialized) {
            mPlayer.start()
            isMusicPlaying = true
            notifyChange(SConsts.PLAY_STATE_CHANGED)
            audioAndFocusManager.requestAudioFocus()
            mHandler.removeMessages(SConsts.VOLUME_FADE_DOWN)
            mHandler.sendEmptyMessage(SConsts.VOLUME_FADE_UP)// 组件调到正常音量
            updateNotification(false)
        } else {
            playCurrentAndNext()
        }
    }

    fun play(musicList: List<Music>?, id: Int, pid: String?) {
        if (isNotNullOrEmpty(musicList)) {
            if (musicList!!.size <= id) return
            if (mPlaylistId == pid && id == mPlayingPos) return
            if (mPlaylistId != pid || mPlayQueue.size == 0 || mPlayQueue.size != musicList.size) {
                setPlayQueue(musicList)
                mPlaylistId = pid ?: Constants.PLAYLIST_QUEUE_ID
            }
            mPlayingPos = id
            playCurrentAndNext()
        }
    }

    // 播放在线音乐【搜索的音乐】  加入播放队列 并播放音乐
    fun play(music: Music?) {
        if (music == null) return
        if (mPlayingPos == -1 || mPlayQueue.size == 0) {
            mPlayQueue.add(music)
            mPlayingPos = 0
        } else if (mPlayingPos < mPlayQueue.size) {
            mPlayQueue.add(mPlayingPos, music)
        } else {
            mPlayQueue.add(mPlayQueue.size, music)
        }
        // 播放列表改变
        notifyChange(SConsts.PLAY_QUEUE_CHANGE)
//        "当前播放的音乐：${music.toString()}".log()
        mPlayingMusic = music
        playCurrentAndNext()
    }

    fun next(isAuto: Boolean) {
        synchronized(this) {
            mPlayingPos = PlayQueueManager.getNextPosition(isAuto, mPlayQueue.size, mPlayingPos)
            "next playingPos:$mPlayingPos".log()
            stop(false)
            playCurrentAndNext()
        }
    }

    // 下一首播放
    fun nextPlay(music: Music?) {
        if (mPlayQueue.size == 0) {
            play(music)
        } else if (mPlayingPos < mPlayQueue.size) {
            if (music != null) {
                mPlayQueue.add(mPlayingPos + 1, music)
                notifyChange(SConsts.PLAY_QUEUE_CHANGE)
            }
        }
    }

    fun prev() {
        synchronized(this) {
            mPlayingPos = PlayQueueManager.getPreviousPosition(mPlayQueue.size, mPlayingPos)
            stop(false)
            playCurrentAndNext()
        }
    }

    // 总时长
    fun getDuration(): Int {
        if (mPlayer.isInitialized && mPlayer.isPrepared) {
            return mPlayer.duration
        }
        return 0
    }

    fun getPlayQueue(): List<Music> {
        if (mPlayQueue.size > 0) {
            return mPlayQueue
        }
        return mPlayQueue
    }

    // 获取正在播放的歌曲
    fun removeFromQueue(position: Int) {
        try {
            if (position == mPlayingPos) {
                mPlayQueue.removeAt(position)
                if (mPlayQueue.size == 0) {
                    clearQueue()
                } else {
                    playMusic(position)
                }
            } else if (position > mPlayingPos) {
                mPlayQueue.removeAt(position)
            } else if (position < mPlayingPos) {
                mPlayQueue.removeAt(position)
                mPlayingPos -= 1
            }
            notifyChange(SConsts.PLAY_QUEUE_CLEAR)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearQueue() {
        mPlayingMusic = null
        isMusicPlaying = false
        mPlayingPos = -1
        mPlayQueue.clear()
        mHistoryPos.clear()
        savePlayQueue(true)
        stop(true)
        notifyChange(SConsts.META_CHANGED)
        notifyChange(SConsts.PLAY_STATE_CHANGED)
        notifyChange(SConsts.PLAY_QUEUE_CLEAR)
    }

    val isPlaying get() = isMusicPlaying

    val isPrepared get() = mPlayer.isPrepared

    private fun updateWidget(action: String) {
        val intent = Intent(action)
        intent.putExtra(SConsts.ACTION_IS_WIDGET,true)
        intent.putExtra(Extras.PLAY_STATUS,isPlaying)
        if (action == SConsts.META_CHANGED) {
            intent.putExtra(Extras.SONG,mPlayingMusic)
        }
        sendBroadcast(intent)
    }

    // 播放当前歌曲
    private fun playCurrentAndNext() {
        synchronized(this) {
            if (mPlayingPos >= mPlayQueue.size || mPlayingPos < 0) {
                return
            }
            mPlayingMusic = mPlayQueue[mPlayingPos]
            // 更新当前歌曲
            notifyChange(SConsts.META_CHANGED)
            updateNotification(true)
            // 更新播放状态
            isMusicPlaying = false
            notifyChange(SConsts.PLAY_STATE_CHANGED)
            updateNotification(false)
            "当前播放歌曲：${mPlayingMusic.toString()}".log()
            mPlayingMusic?.let { music ->
                if (music.uri.isNullOrEmpty() ||
                    music.type != Constants.LOCAL ||
                    music.uri == "null"
                ) {
                    if (!isNetworkAvailable(this)) {
                        "${R.string.net_error}".toast()
                    } else {
                        mPlayingMusic = MusicApi.getMusicInfo(mPlayingMusic)
                        val playUri = mPlayingMusic?.uri
                        "获取播放地址：uri:$playUri".log()
                        if (playUri.isNullOrEmpty()) {
                            checkPlayErrorTimes()
                        } else {
                            saveHistory()
                            playErrorTimes = 0
                            mPlayer.setDataSource(playUri)
                        }

                    }
                }
                saveHistory()
                mHistoryPos.add(mPlayingPos)
                if (mPlayingMusic?.uri != null) {
                    val uri = mPlayingMusic?.uri
                    if (!uri.isNullOrEmpty()) {
                        if (!uri.startsWith(Constants.IS_URL_HEADER) && !FileUtils.exists(uri)) {
                            checkPlayErrorTimes()
                        } else {
                            playErrorTimes = 0
                            mPlayer.setDataSource(uri)
                        }
                    }
                }
                mediaSessionManager.updateMetaData(mPlayingMusic)
                audioAndFocusManager.requestAudioFocus()
                val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
                intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId())
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                sendBroadcast(intent)
                if (mPlayer.isInitialized) {
                    mHandler.removeMessages(SConsts.VOLUME_FADE_DOWN)
                    mHandler.sendEmptyMessage(SConsts.VOLUME_FADE_UP)
                }
            }
        }
    }


    fun stop(removeStatusIcon: Boolean) {
        if (mPlayer.isInitialized) {
            mPlayer.stop()
        }
        if (removeStatusIcon) {
            cancelNotification()
        }
        if (removeStatusIcon) {
            isMusicPlaying = false
        }
    }

    private fun getAlbumName(): String? {
        if (mPlayingMusic != null) {
            return mPlayingMusic!!.artist
        }
        return null
    }

    fun getArtistName(): String? {
        return mPlayingMusic?.artist
    }

    fun getPlayingMusic(): Music? {
        return mPlayingMusic
    }

    fun getPlayPosition(): Int {
        return if (mPlayingPos >= 0) {
            mPlayingPos
        } else 0
    }

    fun getTitle(): String? {
        return mPlayingMusic?.title
    }

    fun getAudioId(): String? {
        return if (mPlayingMusic != null) mPlayingMusic!!.mid else null
    }

    fun getCurrentPosition(): Int {
        return if (mPlayer.isInitialized) {
            mPlayer.getCurrentPosition()
        } else {
            0
        }
    }

    // 重新加载当前进度
    fun reloadPlayQueue() {
        mPlayQueue.clear()
        mHistoryPos.clear()
        mPlayQueue = PlayQueueLoader.getPlayQueue()
        mPlayingPos = SpUtil.getInt(Constants.KEY_PLAY_POSITION)
        if (mPlayingPos >= 0 && mPlayingPos < mPlayQueue.size) {
            mPlayingMusic = mPlayQueue[mPlayingPos]
            updateNotification(true)
            seekTo(SpUtil.getInt(Constants.KEY_POSITION), true)
            notifyChange(SConsts.META_CHANGED)
        }
        notifyChange(SConsts.META_CHANGED)
    }

    private fun saveHistory() {
        mPlayingMusic?.let {
            PlayHistoryLoader.addSongToHistory(it)
            savePlayQueue(false)
        }
    }

    // 保存播放队列
    private fun savePlayQueue(full: Boolean) {
        if (full) {
            PlayQueueLoader.updateQueue(mPlayQueue)
        }
        if (mPlayingMusic != null) {
            SpUtil.saveValue(Constants.KEY_MUSIC_ID, mPlayingMusic!!.mid ?: "")
        }
        SpUtil.saveValue(Constants.KEY_PLAY_POSITION, mPlayingPos)
        SpUtil.saveValue(Constants.KEY_POSITION, getCurrentPosition())
    }

    fun setPlayQueue(playQueue: List<Music>) {
        mPlayQueue.clear()
        mHistoryPos.clear()
        mPlayQueue.addAll(playQueue)
        notifyChange(SConsts.PLAY_QUEUE_CHANGE)
        savePlayQueue(true)
    }

    private fun updateNotification(isChange: Boolean) {

    }

    private fun checkPlayErrorTimes() {
        if (playErrorTimes > SConsts.MAX_ERROR_TIMES) {
            pause()
        } else {
            playErrorTimes++
            "播放地址异常，自动切换至下一首".toast()
            next(false)
        }
    }

    private fun getNextPosition(isAuto: Boolean): Int {
        val playModeId = PlayQueueManager.getPlayModeId()
        if (mPlayQueue.isEmpty()) {
            return -1
        }
        if (mPlayQueue.size == 1) {
            return 0
        }
        if (playModeId == PlayQueueManager.PLAY_MODE_REPEAT && isAuto) {
            if (mPlayingPos < 0) {
                return 0
            } else {
                return mPlayingPos
            }
        } else if (playModeId == PlayQueueManager.PLAY_MODE_RANDOM) {
            return Random.nextInt(mPlayQueue.size)
        } else {
            if (mPlayingPos == mPlayQueue.size - 1) {
                return 0
            } else if (mPlayingPos < mPlayQueue.size - 1) {
                return mPlayingPos + 1
            }
        }
        return mPlayingPos
    }

    private fun getPreviousPosition(): Int {
        val playModeId = PlayQueueManager.getPlayModeId()
        if (mPlayQueue.isEmpty()) {
            return -1
        }
        if (mPlayQueue.size == 1) {
            return 0
        }
        if (playModeId == PlayQueueManager.PLAY_MODE_REPEAT) {
            if (mPlayingPos < 0) {
                return 0
            }
        } else if (playModeId == PlayQueueManager.PLAY_MODE_RANDOM) {
            mPlayingPos = Random.nextInt(mPlayQueue.size)
            return Random.nextInt(mPlayQueue.size)
        } else {
            if (mPlayingPos == 0) {
                return mPlayQueue.size - 1
            } else if (mPlayingPos > 0) {
                return mPlayingPos - 1
            }
        }
        return mPlayingPos
    }

    // 根据位置播放
    fun playMusic(position: Int) {
        mPlayingPos = if (position >= mPlayQueue.size || position == -1) {
            PlayQueueManager.getNextPosition(true, mPlayQueue.size, position)
        } else {
            position
        }
        if (mPlayingPos == -1) return
        playCurrentAndNext()
    }

    private fun notifyChange(what: String) {
        when (what) {
            SConsts.META_CHANGED -> {
                GlobalSingle.metaChanged.value = MetaChangedEvent(mPlayingMusic)
            }
            SConsts.PLAY_STATE_CHANGED -> {
                mediaSessionManager.updatePlaybackState()
                GlobalSingle.stateChanged.value =
                    StatusChangedEvent(isPrepared, isPlaying, percent * getDuration())
            }
            SConsts.PLAY_QUEUE_CHANGE -> {
                GlobalSingle.playListChanged.value = PlaylistEvent(Constants.PLAYLIST_QUEUE_ID)
            }
            SConsts.PLAY_STATE_LOADING_CHANGED->{
                GlobalSingle.stateChanged.value =
                    StatusChangedEvent(isPrepared, isPlaying, percent * getDuration())
            }
        }
    }

    fun seekTo(pos: Int, isInit: Boolean) {
        "pos:$pos".log()
        if (mPlayer != null && mPlayer.isInitialized && mPlayingMusic != null) {
            mPlayer.seek(pos)
        } else if (isInit) {
            // TODO: 2020/12/5
        }
    }

    fun getAudioSessionId(): Int {
        synchronized(this) {
            return mPlayer.getAudioSessionId()
        }
    }

    private fun cancelNotification() {
        stopForeground(true)
        mNotificationManager.cancel(SConsts.NOTIFICATION_ID)
        isRunningForeground = false
    }

    private fun handleCommandIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val command =
                if (SConsts.SERVICE_CMD == action) intent.getStringExtra(SConsts.CMD_NAME) else null
            if (command != null) {
                if (SConsts.CMD_NEXT == command || SConsts.ACTION_NEXT == action) {
                    next(false)
                } else if (SConsts.CMD_PREVIOUS == command || SConsts.ACTION_PREV == action) {
                    prev()
                } else if (SConsts.CMD_TOGGLE_PAUSE == command || SConsts.PLAY_STATE_CHANGED == command || SConsts.ACTION_PLAY_PAUSE == command) {
                    if (isPlaying) {
                        pause()
                        mPauseByTransientLossOfFocus = false
                    } else {
                        play()
                    }
                } else if (SConsts.UNLOCK_DESKTOP_LYRIC == command) {

                } else if (SConsts.CMD_PAUSE == command) {
                    pause()
                    mPauseByTransientLossOfFocus = false
                } else if (SConsts.CMD_PLAY == command) {
                    play()
                } else if (SConsts.CMD_STOP == command) {
                    pause()
                    mPauseByTransientLossOfFocus = false
                    seekTo(0, false)
                    releaseServiceUiAndStop()
                } else if (SConsts.ACTION_LYRIC == action) {

                } else if (SConsts.ACTION_CLOSE == action) {
                    stop(true)
                    stopSelf()
                    releaseServiceUiAndStop()
                    exitProcess(0)
                }
            }
        }
    }

    class MusicPlayerHandler(service: PlayerService, looper: Looper) : Handler(looper) {
        private val mService: WeakReference<PlayerService> = WeakReference(service)
        private var mCurrentVolume: Float = 1.0f
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val service = mService.get()
            service?.let { s ->
                synchronized(mService) {
                    when (msg.what) {
                        SConsts.VOLUME_FADE_DOWN -> {
                            mCurrentVolume -= 0.05f
                            if (mCurrentVolume > 0.2f) {
                                sendEmptyMessageDelayed(SConsts.VOLUME_FADE_DOWN, 10)
                            } else {
                                mCurrentVolume = 0.2f
                            }
                            s.mPlayer.setVolume(mCurrentVolume)
                        }
                        SConsts.VOLUME_FADE_UP -> {
                            mCurrentVolume += 0.01f
                            if (mCurrentVolume < 1.0f) {
                                sendEmptyMessageDelayed(SConsts.VOLUME_FADE_UP, 10)
                            } else {
                                mCurrentVolume = 1.0f
                            }
                            s.mPlayer.setVolume(mCurrentVolume)
                        }
                        //mplayer播放完毕切换到下一首
                        SConsts.TRACK_WENT_TO_NEXT -> {
                            s.mMainHandler.post {
                                s.next(true)
                            }
                        }
                        //mPlayer播放完毕且暂时没有下一首
                        SConsts.TRACK_PLAY_ENDED -> {
                            if (PlayQueueManager.getPlayModeId() == PlayQueueManager.PLAY_MODE_REPEAT) {
                                s.seekTo(0, false)
                                s.mMainHandler.post(s::play)
                            } else {
                                s.mMainHandler.post { s.next(true) }
                            }
                        }
                        // mPlayer播放错误
                        SConsts.TRACK_PLAY_ERROR -> {
                            "歌曲播放地址异常，请切换其他歌曲".toast()
                            s.playErrorTimes++
                            if (s.playErrorTimes < SConsts.MAX_ERROR_TIMES) {
                                s.mMainHandler.post { s.next(true) }
                            } else {
                                s.mMainHandler.post(s::pause)
                            }
                        }
                        // 释放电源锁
                        SConsts.RELEASE_WAKELOCK -> {
                            s.mWakeLock.release()
                        }
                        SConsts.PREPARE_ASYNC_UPDATE -> {
                            s.percent = msg.obj as Int
                            s.notifyChange(SConsts.PLAY_STATE_LOADING_CHANGED)
                        }
                        SConsts.PLAYER_PREPARED -> {
                            // 准备完毕可以播放
                            s.isMusicPlaying = true
                            s.updateNotification(false)
                            s.notifyChange(SConsts.PLAY_STATE_CHANGED)
                        }
                        SConsts.AUDIO_FOCUS_CHANGE -> {
                            when (msg.arg1) {
                                // 失去音频焦点、暂时失去焦点
                                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                    if (s.isPlaying) {
                                        s.mPauseByTransientLossOfFocus =
                                            msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                                    } else {
                                    }
                                    s.mMainHandler.post(s::pause)
                                }
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                                    removeMessages(SConsts.VOLUME_FADE_UP)
                                    sendEmptyMessage(SConsts.VOLUME_FADE_DOWN)
                                }
                                // 重新获取焦点
                                AudioManager.AUDIOFOCUS_GAIN -> {
                                    // 重新获得焦点 且符合播放条件 开始播放
                                    if (!s.isPlaying && s.mPauseByTransientLossOfFocus) {
                                        s.mPauseByTransientLossOfFocus = false
                                        mCurrentVolume = 0f
                                        s.mPlayer.setVolume(mCurrentVolume)
                                        s.mMainHandler.post(s::play)
                                    } else {
                                        removeMessages(SConsts.VOLUME_FADE_DOWN)
                                        sendEmptyMessage(SConsts.VOLUME_FADE_UP)
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                        else -> {

                        }
                    }
                }
            }
        }
    }

    private inner class ServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.getBooleanExtra(SConsts.ACTION_IS_WIDGET, false)) {
                    handleCommandIntent(intent)
                }
            }
        }
    }

    // 耳机拔出广播接收器
    private inner class HeadsetReceiver : BroadcastReceiver() {
        private var bluetoothAdapter: BluetoothAdapter

        init {
            // 有线耳机拔出变化
            intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            // 蓝牙耳机拔出变化
            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (isRunningForeground) {
                    when (intent.action) {
                        BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                            "蓝牙耳机拔出状态改变".log()
                            if (BluetoothProfile.STATE_DISCONNECTED == bluetoothAdapter.getProfileConnectionState(
                                    BluetoothProfile.HEADSET
                                ) && isPlaying
                            ) {
                                // 蓝牙耳机断开连接，如果当前音乐正在播放 将其暂停
                                pause()
                            }
                        }
                        AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                            "有线耳机拔出状态改变".log()
                            if (isPlaying) {
                                pause()
                            }
                        }
                    }
                }
            }
        }

    }

    // 耳机插入广播接收器
    private inner class HeadsetPlugInReceiver : BroadcastReceiver() {
        init {
            if (Build.VERSION.SDK_INT >= 21) {
                intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG)
            } else {
                intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra("state")) {
                val isPlugIn = intent.extras?.getInt("state") == 1
                "耳机插入状态：$isPlugIn".log()
            }
        }
    }

    private fun initChannelId(): String {
        // 通知渠道的id
        val id = "music_lake_01"
        val name: CharSequence = "音乐胡"
        val description = "通知栏播放控制"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel: NotificationChannel = NotificationChannel(id, name, importance)
            mChannel.description = description
            mChannel.enableLights(false)
            mChannel.enableVibration(false)
            mNotificationManager.createNotificationChannel(mChannel)
        }
        return id
    }

    private fun retrievePlaybackAction(action: String): PendingIntent {
        val intent = Intent(action)
        intent.component = ComponentName(this, PlayerService::class.java)
        return PendingIntent.getService(this, 0, intent, 0)
    }

    override fun onDestroy() {
        "-----PlayService----onDestroy---".log()
        super.onDestroy()
        val audioEffectsIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId())
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(audioEffectsIntent)
        savePlayQueue(false)
        mPlayer.stop()
        isMusicPlaying = false
        mPlayer.release()
        mHandler.removeCallbacksAndMessages(null)
        if (mWorkThread.isAlive) {
            mWorkThread.quitSafely()
            mWorkThread.interrupt()
        }
        audioAndFocusManager.abandonAudioFocus()
        cancelNotification()
        unregisterReceiver(mServiceReceiver)
        unregisterReceiver(mHeadsetReceiver)
        unregisterReceiver(mHeadsetPlugInReceiver)
        if (mWakeLock.isHeld) {
            mWakeLock.release()
        }
    }
}
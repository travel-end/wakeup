package never.give.up.japp.service

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.*
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import never.give.up.japp.R
import never.give.up.japp.consts.Constants
import never.give.up.japp.consts.SConsts
import never.give.up.japp.data.PlayHistoryLoader
import never.give.up.japp.delegate.MusicPlayerEngine
import never.give.up.japp.model.Music
import never.give.up.japp.net.RetrofitClient
import never.give.up.japp.play.*
import never.give.up.japp.ui.player.PlayerActivity
import never.give.up.japp.utils.*
import java.lang.ref.WeakReference
import kotlin.random.Random

/**
 * @By Journey 2020/12/2
 * @Description
 */
class PlayerService:Service() {
    private val mBindStub = IMusicServiceStub(this)

    private lateinit var mPlayer:MusicPlayerEngine

    private var mPlayingMusic:Music?=null
    private var mPlayQueue = mutableListOf<Music>()
    private val mHistoryPos = mutableListOf<Int>()
    private var mPlayingPos:Int = -1
    private var mNextPlayPos:Int = -1
    private var mPlaylistId:String = Constants.PLAYLIST_QUEUE_ID
    private var playErrorTimes:Int =0 // 错误次数 超过最大错误次数，自动停止播放

    private lateinit var mServiceReceiver:ServiceReceiver
    private lateinit var mHeadsetReceiver: HeadsetReceiver
    private lateinit var mHeadsetPlugInReceiver: HeadsetPlugInReceiver
    private lateinit var intentFilter: IntentFilter

    private lateinit var mMainHandler:Handler
    private lateinit var mWorkThread:HandlerThread
    private lateinit var mHandler:MusicPlayerHandler

    private lateinit var powerManager: PowerManager
    private lateinit var mWakeLock: PowerManager.WakeLock

    private lateinit var mediaSessionManager:MediaSessionManager
    private lateinit var audioAndFocusManager: AudioAndFocusManager

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mNotificationBuilder: NotificationCompat.Builder
    private lateinit var mNotification:Notification

    private var isMusicPlaying:Boolean =  false
    private var isRunningForeground:Boolean = false

    private val NOTIFICATION_ID:Int = 0x123
    private var mNotificationPostTime:Long = 0L
    private var mServiceStartId:Int = -1




    companion object {
        private lateinit var instance:PlayerService
        fun getInstance():PlayerService {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initReceiver()
        initConfig()
        initTelephony()
        initNotify()
        initMediaPlayer()
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
        registerReceiver(mServiceReceiver,intentFilter)
        registerReceiver(mHeadsetReceiver,intentFilter)
        registerReceiver(mHeadsetPlugInReceiver,intentFilter)
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun initConfig() {
        mMainHandler = Handler(Looper.getMainLooper())
        PlayQueueManager.getPlayModeId()
        // 初始化工作线程
        mWorkThread = HandlerThread("MusicPlayerThread")
        mWorkThread.start()

        mHandler = MusicPlayerHandler(this,mWorkThread.looper)
        // 电源键
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"PlayerWakelockTag")
        // TODO: 2020/12/4 桌面歌词

        // 初始化和设置MediaSessionCompat
        mediaSessionManager = MediaSessionManager(mBindStub,this,mMainHandler)
        audioAndFocusManager = AudioAndFocusManager(this,mHandler)
    }

    // 初始化电话监听服务
    private fun initTelephony() {
        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(ServicePhoneStateListener(),PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun initNotify() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val albumName = getAlbumName()
        val artistName = getArtistName()
        val text = if (albumName.isNullOrEmpty()) artistName else "$artistName - $albumName"
        val playButtonResId = if (isMusicPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val title = if (isMusicPlaying) "暂停" else "播放"
        val playingIntent = Intent(this,PlayerActivity::class.java)
        playingIntent.action = Constants.DEFAULT_NOTIFICATION
        val clickIntent = PendingIntent.getActivity(this, 0, playingIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (mNotificationPostTime == 0L) {
            mNotificationPostTime = System.currentTimeMillis()
        }
        mNotificationBuilder = NotificationCompat.Builder(this,initChannelId())
            .setSmallIcon(R.drawable.ic_logo_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(clickIntent)
            .setContentTitle(getTitle())
            .setContentText(text)
            .setWhen(mNotificationPostTime)
            .addAction(R.drawable.ic_skip_previous,"上一首",retrievePlaybackAction(SConsts.ACTION_PREV))
            .addAction(playButtonResId,title,retrievePlaybackAction(SConsts.ACTION_PLAY_PAUSE))
            .addAction(R.drawable.ic_skip_next,"下一首",retrievePlaybackAction(SConsts.ACTION_NEXT))
            .addAction(R.drawable.ic_lyric,"歌词",retrievePlaybackAction(SConsts.ACTION_LYRIC))
            .addAction(R.drawable.cha,"关闭",retrievePlaybackAction(SConsts.ACTION_CLOSE))
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,PlaybackStateCompat.ACTION_STOP))
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mNotificationBuilder.setShowWhen(false)
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            // 线控
            isRunningForeground = true
            mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            val style = androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionManager.mediaSession)
                .setShowActionsInCompactView(1,0,2,3,4)
            mNotificationBuilder.setStyle(style)
        }
        if (mPlayingMusic != null) {
            CoverLoader.loadImageViewByMusic(this,mPlayingMusic) {
                mNotificationBuilder.setLargeIcon(it)
                mNotification = mNotificationBuilder.build()
                mNotificationManager.notify(NOTIFICATION_ID,mNotification)

            }
        }
        mNotification = mNotificationBuilder.build()
    }

    // 初始化音乐播放服务
    private fun initMediaPlayer() {
        mPlayer = MusicPlayerEngine(this)
        mPlayer.setHandler(mHandler)
        reloadPlayQueue()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBindStub
    }

    private inner class ServicePhoneStateListener:PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            "TelephonyManager state=" + state + ",incomingNumber = " + phoneNumber.log()
            when(state) {
                TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING->pause()
                else->{
                }
            }
        }


    }

    fun pause() {

    }

    fun next(isAuto:Boolean) {
        synchronized(this) {
            mPlayingPos = PlayQueueManager.getNextPosition(isAuto,mPlayQueue.size,mPlayingPos)
            "next playingPos:$mPlayingPos".log()
            stop(false)
            playCurrentAndNext()
        }
    }



    // 播放当前歌曲
    private fun playCurrentAndNext() {
        synchronized(this){
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
            mPlayingMusic?.let {music->
                if (music.uri.isNullOrEmpty() ||
                        music.type != Constants.LOCAL||
                        music.uri=="null"){
                    if (!isNetworkAvailable(this)) {
                        "${R.string.net_error}".toast()
                    } else {
                        var playUrl:String?=null
                        if (music.type == Constants.QQ) {
                            val songUrl = "${Constants.SONG_URL_DATA_LEFT}${music.mid}${Constants.SONG_URL_DATA_RIGHT}"
                            execute {
                                val result = RetrofitClient.instance.songUrlApiService.getSongUrl(songUrl)
                                if (result.code == 0) {
                                    val sipList = result.req_0?.data?.sip
                                    var sip = ""
                                    if (sipList != null) {
                                        if (sipList.isNotEmpty()) {
                                            sip = sipList[0]
                                        }
                                    }
                                    val purlList = result.req_0?.data?.midurlinfo
                                    var pUrl: String? = ""
                                    if (purlList != null) {
                                        if (purlList.isNotEmpty()) {
                                            pUrl = purlList[0].purl
                                        }
                                    }
                                    playUrl = "$sip$pUrl"
                                }
                            }
                            mPlayingMusic!!.uri = playUrl
                            playErrorTimes = 0
                            mPlayer.setDataSource(playUrl!!)
                        } else {

                        }
                        saveHistory()
                    }
                }

            }
        }
    }

    fun stop(removeStatusIcon:Boolean) {
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

    private fun getAlbumName() :String?{
        if (mPlayingMusic != null) {
            return mPlayingMusic!!.artist
        }
        return null
    }

    fun getArtistName():String? {
        return mPlayingMusic?.artist
    }

    fun getPlayingMusic():Music? {
        return mPlayingMusic
    }

    fun getTitle():String? {
        return mPlayingMusic?.title
    }

    fun getCurrentPosition():Int {
        return if (mPlayer.isInitialized){
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
            seekTo(SpUtil.getInt(Constants.KEY_POSITION),true)
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
    private fun savePlayQueue(full:Boolean) {
        if (full) {
            PlayQueueLoader.updateQueue(mPlayQueue)
        }
        if (mPlayingMusic != null) {
            SpUtil.saveValue(Constants.KEY_MUSIC_ID,mPlayingMusic!!.mid?:"")
        }
        SpUtil.saveValue(Constants.KEY_PLAY_POSITION,mPlayingPos)
        SpUtil.saveValue(Constants.KEY_POSITION,getCurrentPosition())


    }

    private fun updateNotification(isChange:Boolean) {

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

    private fun getNextPosition(isAuto:Boolean):Int {
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
            if (mPlayingPos == mPlayQueue.size -1) {
                return 0
            } else if (mPlayingPos < mPlayQueue.size - 1) {
                return mPlayingPos+1
            }
        }
        return mPlayingPos
    }

    private fun getPreviousPosition():Int {
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
                return mPlayQueue.size -1
            } else if (mPlayingPos > 0) {
                return mPlayingPos -1
            }
        }
        return mPlayingPos
    }

    fun playMusic(position:Int) {


    }

    private fun notifyChange(what:String) {
        when(what) {
            SConsts.META_CHANGED->{

            }
            SConsts.PLAY_STATE_CHANGED->{

            }
        }
    }

    fun seekTo(pos:Int,isInit:Boolean) {
        "pos:$pos".log()
        if (mPlayer != null && mPlayer.isInitialized && mPlayingMusic != null) {
            mPlayer.seek(pos)
        } else if (isInit) {
            // TODO: 2020/12/5
        }
    }

    private fun cancelNotification() {
        stopForeground(true)
        mNotificationManager.cancel(NOTIFICATION_ID)
        isRunningForeground = false
    }

    class MusicPlayerHandler(service:PlayerService,looper: Looper):Handler(looper) {
        private val mService:WeakReference<PlayerService> = WeakReference(service)
        private var mCurrentVolume:Float = 1.0f
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val service = mService.get()
            service?.let {s->
                synchronized(mService) {
                    when(msg.what) {
                        SConsts.VOLUME_FADE_DOWN->{
                            mCurrentVolume -= 0.05f
                            if (mCurrentVolume > 0.2f) {
                                sendEmptyMessageDelayed(SConsts.VOLUME_FADE_DOWN,10)
                            } else {
                                mCurrentVolume = 0.2f
                            }
                            s.mPlayer.setVolume(mCurrentVolume)
                        }
                        SConsts.VOLUME_FADE_UP->{
                           mCurrentVolume += 0.01f
                            if (mCurrentVolume < 1.0f) {
                                sendEmptyMessageDelayed(SConsts.VOLUME_FADE_UP,10)
                            } else {
                                mCurrentVolume = 1.0f
                            }
                            s.mPlayer.setVolume(mCurrentVolume)
                        }
                        SConsts.TRACK_WENT_TO_NEXT->{
                            s.mMainHandler.post {

                            }
                        }
                        else->{

                        }
                    }
                }
            }
        }
    }

    class ServiceReceiver:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {


        }

    }

    private class HeadsetReceiver:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("Not yet implemented")
        }

    }
    // 耳机插入广播接收器
    class HeadsetPlugInReceiver:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("Not yet implemented")
        }

    }

    private fun initChannelId():String {
        // 通知渠道的id
        val id = "music_lake_01"
        val name:CharSequence = "音乐胡"
        val description = "通知栏播放控制"
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel:NotificationChannel = NotificationChannel(id,name,importance)
            mChannel.description = description
            mChannel.enableLights(false)
            mChannel.enableVibration(false)
            mNotificationManager.createNotificationChannel(mChannel)
        }
        return id
    }

    private fun retrievePlaybackAction(action:String) :PendingIntent{
        val intent = Intent(action)
        intent.component = ComponentName(this,PlayerService::class.java)
        return PendingIntent.getService(this,0,intent,0)
    }
}
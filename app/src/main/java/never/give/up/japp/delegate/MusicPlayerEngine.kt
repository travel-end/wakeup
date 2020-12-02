package never.give.up.japp.delegate

import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.PowerManager
import never.give.up.japp.Japp
import never.give.up.japp.consts.SConsts
import never.give.up.japp.service.PlayerService
import never.give.up.japp.utils.SpUtil
import never.give.up.japp.utils.log
import java.lang.ref.WeakReference

/**
 * @By Journey 2020/11/24
 * @Description
 */
class MusicPlayerEngine(service: PlayerService) : MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener {
    companion object {
        private const val TAG = "MusicPlayerEngine"
    }
    private var mIsInitialized: Boolean = false // 是否已经初始化
    private var mIsPrepared: Boolean = false
    private var mService: WeakReference<PlayerService> = WeakReference(service)
    private var mMediaPlayer = MediaPlayer()
    private var mHandler:Handler?=null

    init {
        mMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK)
    }

    fun setDataSource(path: String) {
    }

    private fun setDataSourceImpl(mediaPlayer: MediaPlayer, path: String?): Boolean {
        if (path == null) return false
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mIsPrepared = false
            mediaPlayer.reset()
            val cacheSetting = SpUtil.getBoolean("key_cache_mode", true)
            // 本地歌曲无需缓存
            if (path.startsWith("content://") || path.startsWith("/storage")) {
                mService.get()?.let { mediaPlayer.setDataSource(it, Uri.parse(path)) }
            } else if (cacheSetting) {
                // 开启缓存，读取缓存
                val proxy = Japp.getProxy()
                val proxyUrl = proxy.getProxyUrl(path)
                "缓存地址proxyUrl：$proxyUrl".log(TAG)
                mediaPlayer.setDataSource(proxyUrl)
            } else {
                // 不缓存
                mediaPlayer.setDataSource(path)
            }
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnBufferingUpdateListener(this)
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            "exception:${e.message}".log(TAG)
            e.printStackTrace()
            return false
        }
        return true
    }

    fun setHandler(handler: Handler?) {
        this.mHandler = handler
    }

    val  isInitialized get() = mIsInitialized

    val isPrepared get() = mIsPrepared

    fun start() {
        mMediaPlayer.start()
    }
    fun stop() {
        try {
            mMediaPlayer.reset()
            mIsInitialized = false
            mIsPrepared = false
        } catch (e:IllegalStateException) {
            e.printStackTrace()
        }
    }
    fun release() {
        mMediaPlayer.release()
    }
    fun pause() {
        mMediaPlayer.pause()
    }

    val isPlaying get() = mMediaPlayer.isPlaying

    /*获取播放时长，getDuration只能在prepared之后才能调用，否则会报-38错误*/
    val duration get() = if (mIsPrepared) mMediaPlayer.duration else 0

    /*获取播放进度*/
    fun getCurrentPosition():Int {
        return try {
            mMediaPlayer.currentPosition
        } catch (e:IllegalStateException) {
            -1
        }
    }
    fun seek(whereto:Int) {
        mMediaPlayer.seekTo(whereto)
    }
    fun setVolume(vol:Float) {
        try {
            mMediaPlayer.setVolume(vol,vol)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }


    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when(what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN,MediaPlayer.MEDIA_ERROR_SERVER_DIED->{
                val service = mService.get()
                // TODO: 2020/11/24 上传错误songId和SongTitle
                val errorInfo = TrackErrorInfo("","")
                mIsInitialized = false
                mMediaPlayer.release()
                mMediaPlayer = MediaPlayer()
                mMediaPlayer.setWakeMode(service,PowerManager.PARTIAL_WAKE_LOCK)
                val msg = mHandler?.obtainMessage(SConsts.TRACK_PLAY_ERROR,errorInfo)
                if (msg != null) {
                    mHandler?.sendMessageDelayed(msg,500)
                }
                return true
            }
        }
        return true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mp == mMediaPlayer) {
            mHandler?.sendEmptyMessage(SConsts.TRACK_WENT_TO_NEXT)
        } else {
            mService.get()
            mHandler?.sendEmptyMessage(SConsts.TRACK_PLAY_ENDED)
            mHandler?.sendEmptyMessage(SConsts.RELEASE_WAKELOCK)
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        val msg = mHandler?.obtainMessage(SConsts.PREPARE_ASYNC_UPDATE,percent)
        if (msg != null) {
            mHandler?.sendMessage(msg)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        if (mp != null) {
            mp.start()
            if (!mIsPrepared) {
                mIsPrepared = true
                val msg = mHandler?.obtainMessage(SConsts.PLAYER_PREPARED)
                if (msg != null) {
                    mHandler?.sendMessage(msg)
                }
            }
        }
    }

    data class TrackErrorInfo(
        val audioId:String,
        val trackName:String
    )
}
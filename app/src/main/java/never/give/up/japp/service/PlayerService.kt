package never.give.up.japp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import never.give.up.japp.consts.SConsts
import never.give.up.japp.delegate.MusicPlayerEngine
import never.give.up.japp.model.Music
import never.give.up.japp.play.AudioAndFocusManager
import never.give.up.japp.play.IMusicServiceStub
import never.give.up.japp.play.MediaSessionManager
import never.give.up.japp.play.PlayQueueManager
import never.give.up.japp.utils.log
import java.lang.ref.WeakReference

/**
 * @By Journey 2020/12/2
 * @Description
 */
class PlayerService:Service() {
    private val mBindStub = IMusicServiceStub(this)

    private lateinit var mPlayer:MusicPlayerEngine

    private var mPlayingMusic:Music?=null

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
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
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

    class MusicPlayerHandler(service:PlayerService,looper: Looper):Handler(looper) {
        private val mService:WeakReference<PlayerService> = WeakReference(service)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val service = mService.get()
            service?.let {s->
                when(msg.what) {
                    SConsts.VOLUME_FADE_DOWN->{

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
}
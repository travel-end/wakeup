package never.give.up.japp.play

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import never.give.up.japp.consts.SConsts
import never.give.up.japp.service.PlayerService

/**
 * @By Journey 2020/12/4
 * @Description
 */
class MediaButtonIntentReceiver:BroadcastReceiver() {
    companion object {
        const val MSG_LONGPRESS_TIMEOUT= 1
        const val MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2
        const val LONG_PRESS_DELAY = 1000
        const val DOUBLE_CLICK = 800
        private var mClickCounter :Int= 0
        private var mLastClickTime:Long = 0
        private var mDown:Boolean = false
        private var mLaunched:Boolean = false
        private var mWakeLock:PowerManager.WakeLock?=null

        private val mHandler = object :Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when(msg.what) {

                }
            }
        }

        fun startService(context: Context,command:String) {
            val i = Intent(context,PlayerService::class.java)
            i.action = SConsts.SERVICE_CMD
            i.putExtra(SConsts.CMD_NAME,command)
            i.putExtra(SConsts.FROM_MEDIA_BUTTON,true)
            context.startService(i)
        }
        @SuppressLint("InvalidWakeLockTag")
        fun acquireWakeLockAndSendMessage(context: Context, msg:Message, delay:Long) {
            if (mWakeLock == null) {
                val appContext = context.applicationContext
                val pm = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Listener headset button")
            }
            mWakeLock?.setReferenceCounted(false)
            mWakeLock?.acquire(10000)
            mHandler.sendMessageDelayed(msg,delay)
        }

        /**
         * 如果handler的消息队列中没有待处理的消息，就释放receiver
         */
        private fun releaseWakeLockIfHandlerIdle() {
            if (mHandler.hasMessages(MSG_LONGPRESS_TIMEOUT) || mHandler.hasMessages(
                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
                return
            }
            if (mWakeLock != null) {
                mWakeLock!!.release()
                mWakeLock = null
            }
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { i->
            val intentAction = i.action
            // 耳机拔出暂停播放
            if (intentAction == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (isMusicServiceRunning(context)) {
                    val startIntent = Intent(context,PlayerService::class.java)
                    startIntent.action = SConsts.SERVICE_CMD
                    startIntent.putExtra(SConsts.CMD_NAME,SConsts.CMD_PAUSE)
                    context?.startService(startIntent)
                }
            }
        }
    }
    @SuppressLint("ServiceCast")
    private fun isMusicServiceRunning(context: Context?):Boolean {
        if (context == null) return false
        var isServiceRunning = false
        val am = context.getSystemService(Context.ACCOUNT_SERVICE) as ActivityManager
        val maxServiceNum = 100
        val list = am.getRunningServices(maxServiceNum)
        for (info in list) {
            if (PlayerService::class.java.name == info.service.className) {
                isServiceRunning = true
                break
            }
        }
        return  isServiceRunning
    }

}
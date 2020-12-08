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
import android.view.KeyEvent
import never.give.up.japp.consts.SConsts
import never.give.up.japp.service.PlayerService
import never.give.up.japp.ui.MainActivity
import never.give.up.japp.utils.log

/**
 * @By Journey 2020/12/4
 * @Description
 */
class MediaButtonIntentReceiver : BroadcastReceiver() {
    companion object {
        const val MSG_LONGPRESS_TIMEOUT = 1
        const val MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2
        const val LONG_PRESS_DELAY = 1000
        const val DOUBLE_CLICK = 800
        private var mClickCounter: Int = 0
        private var mLastClickTime: Long = 0
        private var mDown: Boolean = false
        private var mLaunched: Boolean = false
        private var mWakeLock: PowerManager.WakeLock? = null

        private val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_LONGPRESS_TIMEOUT -> {
                        if (!mLaunched) {
                            if (msg.obj is Context) {
                                val context = msg.obj as Context
                                val i = Intent()
                                i.putExtra("autoshuffle", "true")
                                i.setClass(context, MainActivity::class.java)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                context.startActivity(i)
                                mLaunched = true
                            }
                        }
                    }
                    // 双击时间阈值内
                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT -> {
                        val clickCount = msg.arg1
                        val command: String?
                        command = when (clickCount) {
                            1 -> SConsts.CMD_TOGGLE_PAUSE
                            2 -> SConsts.CMD_NEXT
                            3 -> SConsts.CMD_PREVIOUS
                            else -> null
                        }
                        if (command != null) {
                            val context1 = msg.obj as Context
                            startService(context1, command)
                        }
                    }
                }
            }
        }

        fun startService(context: Context, command: String) {
            val i = Intent(context, PlayerService::class.java)
            i.action = SConsts.SERVICE_CMD
            i.putExtra(SConsts.CMD_NAME, command)
            i.putExtra(SConsts.FROM_MEDIA_BUTTON, true)
            context.startService(i)
        }

        @SuppressLint("InvalidWakeLockTag")
        fun acquireWakeLockAndSendMessage(context: Context?, msg: Message, delay: Long) {
            if (mWakeLock == null) {
                val appContext = context?.applicationContext
                if (appContext != null) {
                    val pm = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                    mWakeLock =
                        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Listener headset button")
                }
            }
            mWakeLock?.setReferenceCounted(false)
            mWakeLock?.acquire(10000)
            mHandler.sendMessageDelayed(msg, delay)
        }

        /**
         * 如果handler的消息队列中没有待处理的消息，就释放receiver
         */
        private fun releaseWakeLockIfHandlerIdle() {
            if (mHandler.hasMessages(MSG_LONGPRESS_TIMEOUT) || mHandler.hasMessages(
                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT
                )
            ) {
                return
            }
            if (mWakeLock != null) {
                mWakeLock!!.release()
                mWakeLock = null
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { i ->
            val intentAction = i.action
            // 耳机拔出暂停播放
            if (intentAction == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (isMusicServiceRunning(context)) {
                    val startIntent = Intent(context, PlayerService::class.java)
                    startIntent.action = SConsts.SERVICE_CMD
                    startIntent.putExtra(SConsts.CMD_NAME, SConsts.CMD_PAUSE)
                    context?.startService(startIntent)
                }
            } else if (intentAction == Intent.ACTION_MEDIA_BUTTON) {// 耳机按钮事件
                val event = i.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                "keycode=${event?.keyCode}".log()
                if (event != null) {
                    val keycode = event.keyCode
                    val action = event.action
                    val eventTime = event.eventTime
                    var command: String? = null
                    when (keycode) {
                        KeyEvent.KEYCODE_MEDIA_STOP -> command = SConsts.CMD_STOP
                        KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> command =
                            SConsts.CMD_TOGGLE_PAUSE
                        KeyEvent.KEYCODE_MEDIA_NEXT -> command = SConsts.CMD_NEXT
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> command = SConsts.CMD_PREVIOUS
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> command = SConsts.CMD_PAUSE
                        KeyEvent.KEYCODE_MEDIA_PLAY -> command = SConsts.CMD_PLAY
                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> command = SConsts.CMD_FORWARD
                        KeyEvent.KEYCODE_MEDIA_REWIND -> command = SConsts.CMD_REWIND
                    }
                    if (command != null) {
                        if (action == KeyEvent.ACTION_DOWN) {
                            if (mDown) {
                                if (SConsts.CMD_TOGGLE_PAUSE == command || SConsts.CMD_PLAY == command) {
                                    if (mLastClickTime != 0L && eventTime - mLastClickTime > LONG_PRESS_DELAY) {
                                        acquireWakeLockAndSendMessage(
                                            context, mHandler.obtainMessage(
                                                MSG_LONGPRESS_TIMEOUT, context
                                            ), 0
                                        )
                                    }
                                }
                            } else if (event.repeatCount == 0) {
                                if (keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                                    if (eventTime - mLastClickTime >= DOUBLE_CLICK) {
                                        mClickCounter = 0
                                    }
                                    mClickCounter++
                                    mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)
                                    val msg = mHandler.obtainMessage(
                                        MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context
                                    )
                                    val delay = if (mClickCounter < 3) DOUBLE_CLICK else 0
                                    if (mClickCounter >= 3) {
                                        mClickCounter = 0
                                    }
                                    mLastClickTime = eventTime
                                    acquireWakeLockAndSendMessage(context, msg, delay.toLong())
                                } else {
                                    if (context != null)
                                        startService(context, command)
                                }
                                mLaunched = false
                                mDown = true
                            }
                        } else {
                            mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT)
                            mDown = false
                        }
                        if (isOrderedBroadcast) {
                            abortBroadcast()
                        }
                        releaseWakeLockIfHandlerIdle()
                    }
                }
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun isMusicServiceRunning(context: Context?): Boolean {
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
        return isServiceRunning
    }

}
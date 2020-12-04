package never.give.up.japp.play

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import never.give.up.japp.consts.SConsts
import never.give.up.japp.service.PlayerService
import never.give.up.japp.utils.log

/**
 * @By Journey 2020/12/4
 * @Description
 */
class AudioAndFocusManager(context: Context, handler: PlayerService.MusicPlayerHandler) {
    private lateinit var mAudioManager: AudioManager

    init {
        initAudioManager(context)
    }

    private fun initAudioManager(context: Context) {
        val mediaSession = MediaSession(context, "AudioAndFocusManager")
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mediaButtonReceiverComponent =
            ComponentName(context.packageName, MediaButtonIntentReceiver::class.java.name)
        context.packageManager.setComponentEnabledSetting(
            mediaButtonReceiverComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        mAudioManager.registerMediaButtonEventReceiver(mediaButtonReceiverComponent)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            mediaButtonIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        mediaSession.setMediaButtonReceiver(pendingIntent)
    }

    // 请求音频焦点
    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            val res = mAudioManager.requestAudioFocus(audioFocusRequest)
            "res=$res".log()
        } else {
            val result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            "requestAudioFocus=$result".log()
        }
    }

    // 关闭音频焦点
    fun abandonAudioFocus() {
        val result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(
            audioFocusChangeListener
        )
        "requestAudioFocus=$result".log()
    }


    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        handler.obtainMessage(SConsts.AUDIO_FOCUS_CHANGE,it,0).sendToTarget()
    }
}
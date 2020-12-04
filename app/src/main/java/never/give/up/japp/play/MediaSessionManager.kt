package never.give.up.japp.play

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import never.give.up.japp.model.Music
import never.give.up.japp.utils.loadBigImageView
import never.give.up.japp.utils.log

/**
 * @By Journey 2020/12/4
 * @Description
 */
class MediaSessionManager(
    private val control: IMusicServiceStub,
    private val context: Context,
    private val handler: Handler
) {
    private val tag = "MediaSessionManager"

    //指定可以接收的来自锁屏页面的按键信息
    private val MEDIA_SESSION_ACTIONS: Long = (PlaybackStateCompat.ACTION_PLAY
            or PlaybackStateCompat.ACTION_PAUSE
            or PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_SEEK_TO)
    private lateinit var mMediaSession: MediaSessionCompat

    init {
        setupMediaSession()
    }
    // 初始化并激活MediaSession
    private fun setupMediaSession() {
        mMediaSession = MediaSessionCompat(context,tag)
        mMediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mMediaSession.setCallback(callback,handler)
        mMediaSession.isActive = true
    }

    // 更新播放状态 播放/暂停/拖动进度条时条用
    fun updatePlaybackState() {
        val state = if (isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mMediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(state, getCurrentPosition(), 1f)
                .build())
    }
    // 更新正在播放的音乐，切换歌曲时调用
    fun updateMetaData(songInfo:Music?) {
        if (songInfo == null) {
            mMediaSession.setMetadata(null)
            return
        }
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songInfo.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,songInfo.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,songInfo.album)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,songInfo.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,songInfo.duration)

        loadBigImageView(context,songInfo) { bitmap ->
            metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,bitmap)
            mMediaSession.setMetadata(metaData.build())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS,getCount().toLong())
        }
        mMediaSession.setMetadata(metaData.build())
    }

    private fun isPlaying():Boolean {
        return try {
            control.isPlaying
        } catch (e:RemoteException) {
            e.printStackTrace()
            false
        }
    }
    private fun getCurrentPosition():Long {
        return try {
            control.currentPosition.toLong()
        } catch (e:RemoteException) {
            e.printStackTrace()
            0
        }
    }

    private fun getCount():Int {
        return try {
            control.playList.size
        } catch (e:RemoteException) {
            e.printStackTrace()
            0
        }
    }

    val mediaSession get() = mMediaSession.sessionToken

    fun release() {
        mMediaSession.setCallback(null)
        mMediaSession.isActive =false
        mMediaSession.release()
    }

    private val callback = object :MediaSessionCompat.Callback(){
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            "mediaButtonEvent$mediaButtonEvent".log()
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onPlay() {
            try {
                control.playPause()
            } catch (e:RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onPause() {
            super.onPause()
            try {
                control.playPause()
            } catch (e:RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onSkipToNext() {
            try {
                control.next()
            } catch (e:RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onSkipToPrevious() {
            try {
                control.prev()
            } catch (e:RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onStop() {
            try {
                control.playPause()
            } catch (e:RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onSeekTo(pos: Long) {
            try {
                control.seekTo(pos.toInt())
            } catch (e:RemoteException) {
                e.printStackTrace()
            }
        }
    }
}
package never.give.up.japp.play

import never.give.up.japp.IMusicService
import never.give.up.japp.model.Music
import never.give.up.japp.service.PlayerService
import java.lang.ref.WeakReference

/**
 * @By Journey 2020/12/4
 * @Description
 */
class IMusicServiceStub(service:PlayerService):IMusicService.Stub() {
    private var mService:WeakReference<PlayerService> = WeakReference(service)
    override fun AudioSessionId(): Int {
        TODO("Not yet implemented")
    }

    override fun playPlaylist(playlist: MutableList<Music>?, id: Int, pid: String?) {
        TODO("Not yet implemented")
    }

    override fun getDuration(): Int {
        TODO("Not yet implemented")
    }

    override fun seekTo(ms: Int) {
        TODO("Not yet implemented")
    }

    override fun showDesktopLyric(show: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isPause(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition(): Int {
        TODO("Not yet implemented")
    }

    override fun removeFromQueue(position: Int) {
        TODO("Not yet implemented")
    }

    override fun play(id: Int) {
        TODO("Not yet implemented")
    }

    override fun next() {
        TODO("Not yet implemented")
    }

    override fun getPlayingMusic(): Music {
        TODO("Not yet implemented")
    }

    override fun setLoopMode(loopmode: Int) {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearQueue() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun playPause() {
        TODO("Not yet implemented")
    }

    override fun getPlayList(): MutableList<Music> {
        TODO("Not yet implemented")
    }

    override fun nextPlay(music: Music?) {
        TODO("Not yet implemented")
    }

    override fun prev() {
        TODO("Not yet implemented")
    }

    override fun getSongArtist(): String {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun position(): Int {
        TODO("Not yet implemented")
    }

    override fun getSongName(): String {
        TODO("Not yet implemented")
    }

    override fun playMusic(music: Music?) {
        TODO("Not yet implemented")
    }
}
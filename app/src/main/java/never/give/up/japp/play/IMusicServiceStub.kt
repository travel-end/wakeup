package never.give.up.japp.play

import never.give.up.japp.IMusicService
import never.give.up.japp.model.Music
import never.give.up.japp.service.PlayerService
import java.lang.ref.WeakReference

/**
 * @By Journey 2020/12/4
 * @Description
 */
class IMusicServiceStub(service: PlayerService) : IMusicService.Stub() {
    private var mService: WeakReference<PlayerService> = WeakReference(service)

    override fun AudioSessionId() = mService.get()?.getAudioSessionId()?:0

    override fun playPlaylist(playlist: MutableList<Music>?, id: Int, pid: String?) {
        mService.get()?.play(playlist,id,pid)
    }

    override fun getDuration()=mService.get()?.getDuration()?:0

    override fun seekTo(ms: Int) {
        mService.get()?.seekTo(ms,false)
    }

    override fun showDesktopLyric(show: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isPause()=!mService.get()!!.isPlaying

    override fun getCurrentPosition(): Int {
        return mService.get()?.getCurrentPosition()?:0
    }

    override fun removeFromQueue(position: Int) {
        mService.get()?.removeFromQueue(position)
    }

    override fun play(id: Int) {
        mService.get()?.playMusic(id)
    }

    override fun next() {
        mService.get()?.next(false)
    }

    override fun getPlayingMusic(): Music? {
        return mService.get()?.getPlayingMusic()
    }

    override fun setLoopMode(loopmode: Int) {
    }

    override fun isPlaying(): Boolean {
        return mService.get()?.isPlaying?:false
    }

    override fun clearQueue() {
        mService.get()?.clearQueue()
    }

    override fun pause() {
        mService.get()?.pause()
    }

    override fun playPause() {
        mService.get()?.playPause()
    }

    override fun getPlayList(): List<Music> {
        return mService.get()?.getPlayQueue()?: emptyList()
    }

    override fun nextPlay(music: Music?) {
        mService.get()?.nextPlay(music)
    }

    override fun prev() {
        mService.get()?.prev()
    }

    override fun getSongArtist(): String {
        return mService.get()?.getArtistName()?:""
    }

    override fun stop() {
        mService.get()?.stop(true)
    }

    override fun position(): Int {
        return mService.get()?.getPlayPosition()?:0
    }

    override fun getSongName(): String {
        return mService.get()?.getTitle()?:""
    }

    override fun playMusic(music: Music?) {
        mService.get()?.play(music)
    }
}
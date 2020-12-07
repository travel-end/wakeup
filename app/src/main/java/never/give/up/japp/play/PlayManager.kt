package never.give.up.japp.play

import android.app.Activity
import android.content.*
import android.os.IBinder
import android.os.RemoteException
import never.give.up.japp.IMusicService
import never.give.up.japp.model.Music
import never.give.up.japp.service.PlayerService
import java.util.WeakHashMap

/**
 * @By Journey 2020/12/4
 * @Description
 */
object PlayManager {
    var mService:IMusicService?=null
    var mConnectionMap:WeakHashMap<Context,ServiceBinder>
    init {
        mConnectionMap = WeakHashMap<Context,ServiceBinder>()
    }

    fun bindToService(context: Context,callback: ServiceConnection):ServiceToken? {
        var realActivity = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context as Activity
        }
        try {
            val contextWrapper = ContextWrapper(realActivity)
            contextWrapper.startService(Intent(contextWrapper,PlayerService::class.java))
            val binder = ServiceBinder(callback)
            if (contextWrapper.bindService(Intent().setClass(contextWrapper,PlayerService::class.java),binder,0)) {
                mConnectionMap[contextWrapper] = binder
                return ServiceToken(contextWrapper)
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap[mContextWrapper] ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            mService = null
        }
    }

    fun nextPlay(music: Music) {
        try {
            mService?.nextPlay(music)
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun playOnline(music: Music) {
        try {
            mService?.playMusic(music)
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun play(id:Int) {
        try {
            mService?.play(id)
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun play(id:Int,musicList:List<Music>,pid:String) {
        try {
            mService?.playPlaylist(musicList,id,pid)
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun getAudioSessionId():Int {
        try {
            return mService?.AudioSessionId()?:0
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    fun playPause() {
        try {
            mService?.playPause()
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun prev() {
        try {
            mService?.prev()
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun next() {
        try {
            mService?.next()
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun setLoopMode(mode:Int) {
        try {
            mService?.setLoopMode(mode)
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun seekTo(ms:Int) {
        try {
            mService?.seekTo(ms)
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun position():Int {
        try {
            return mService?.position()?:0
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getCurrentPosition():Int {
        try {
            return mService?.currentPosition?:0
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getDuration():Int {
        try {
            return mService?.duration?:0
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getSongName():String {
        try {
            return mService?.songName?:"麦浪音乐"
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return "麦浪音乐"
    }

    fun getSongArtist():String {
        try {
            return mService?.songArtist?:"麦浪音乐"
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return "麦浪音乐"
    }

    fun isPlaying():Boolean {
        try {
            return mService?.isPlaying?:false
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    fun isPause():Boolean {
        try {
            return mService?.isPause?:false
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    fun getPlayingMusic():Music? {
        try {
            return mService?.playingMusic
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    fun getPlayingId():String {
        try {
            if (mService != null && mService!!.playingMusic != null)  {
                return mService!!.playingMusic!!.mid?:"-1"
            }
        } catch (e:RemoteException) {
            e.printStackTrace()
        }
        return "-1"
    }

    fun getPlayList():List<Music> {
        try {
            return mService?.playList?:emptyList()
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun clearQueue() {
        try {
            mService?.clearQueue()
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    fun removeFromQueue(adapterPosition:Int) {
        try {
            mService?.removeFromQueue(adapterPosition)
        }catch (e:RemoteException) {
            e.printStackTrace()
        }
    }

    class ServiceBinder(private val callback:ServiceConnection?):ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            callback?.onServiceDisconnected(name)
            mService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = IMusicService.Stub.asInterface(service)
            callback?.onServiceConnected(name,service)
        }

    }
    class ServiceToken(var mWrappedContext: ContextWrapper)
}
package never.give.up.japp

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.multidex.MultiDexApplication
import com.danikula.videocache.HttpProxyCacheServer
import never.give.up.japp.utils.CacheFileNameGenerator
import never.give.up.japp.utils.FileUtils
import org.litepal.LitePal
import java.io.File

/**
 * @By Journey 2020/12/2
 * @Description
 */
class Japp:MultiDexApplication(),ViewModelStoreOwner {
    private lateinit var mAppViewModelStore:ViewModelStore
    companion object {
        private var instances:Japp?=null
        fun getInstance():Japp {
            if (instances == null) {
                synchronized(Japp::class.java) {
                    if (instances == null) {
                        instances = Japp()
                    }
                }
            }
            return instances!!
        }
        fun getProxy(): HttpProxyCacheServer {
            return if (getInstance().proxy == null) {
                getInstance().proxy = getInstance().newProxy()
                getInstance().proxy!!
            } else {
                getInstance().proxy!!
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        instances= this
        LitePal.initialize(this)
        mAppViewModelStore = ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore
    }

    private var proxy: HttpProxyCacheServer?=null

    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer
            .Builder(this)
            .cacheDirectory(File(FileUtils.getMusicCacheDir()!!))
            .fileNameGenerator(CacheFileNameGenerator())
            .build()
    }
}
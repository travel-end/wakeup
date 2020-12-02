package never.give.up.japp.utils

import android.os.Environment
import java.io.File

/**
 * @By Journey 2020/12/2
 * @Description
 */
object FileUtils {
    private fun getAppDir(): String {
        return Environment.getExternalStorageDirectory().toString() + "/jAppMusic/"
    }

    fun getMusicDir(): String? {
        val dir: String = getAppDir() + "Music/"
        return mkdirs(dir)
    }
    fun getMusicCacheDir(): String? {
        val dir: String = getAppDir() + "MusicCache/"
        return mkdirs(dir)
    }

    private fun mkdirs(dir: String): String? {
        val file = File(dir)
        if (!file.exists()) {
            file.mkdirs()
        }
        return dir
    }
}
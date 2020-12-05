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

    fun exists(filePath:String?):Boolean {
        if (filePath==null) return false
        if (File(filePath).exists()) {
            return true
        }
        return false
    }

    fun delFile(dirPath:String,fileName:String):Boolean {
        val file = File(dirPath, fileName)
        var delete = false
        delete = if (file == null || !file.exists() || file.isDirectory) {
            false
        } else {
            file.delete()
        }
        return delete
    }

    /**
     * 删除文件
     * filepath 文件路径
     */
    fun delFile(filepath: String?): Boolean {
        if (filepath==null) return false
        val file = File(filepath)
        var delete = false
        delete = if (file == null || !file.exists() || file.isDirectory) {
            false
        } else {
            file.delete()
        }
        return delete
    }
}
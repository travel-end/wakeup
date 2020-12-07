package never.give.up.japp.utils

import android.text.TextUtils
import never.give.up.japp.R
import never.give.up.japp.model.core.AlbumListBean
import never.give.up.japp.model.core.ListBean
import java.util.regex.Pattern

/**
 * @By Journey 2020/10/28
 * @Description
 */
object StringUtil {
    // TODO: 2020/11/18 正则表达式待完善
    fun checkPhone(phone: String): Boolean {
        if (TextUtils.isDigitsOnly(phone) && phone.matches(Regex("^1[0-9]{10}\$"))) {
            return true
        }
        return false
    }

    fun passwordCheck(pwd: String): Boolean {
        if (pwd.length >= 6) {
            return true
        }
        return false
    }

    fun formatSinger(singer: String): String {
        return if (singer.contains("/")) singer.split("/")[0].trim() else singer.trim()
    }

    fun formatProgress(time: Long): String {//263593
        val min = "${time / 60}"
        var sec = "${time % 60}"
        if (sec.length < 2) {
            sec = "0$sec"
        }
        return "$min:$sec"
    }

    fun getSinger(data: ListBean): String? {
        val singerList = data.singer
        singerList?.let {
            if (it.isNotEmpty()) {
                val singer = StringBuilder(it[0].name ?: "")
                if (it.size > 1) {
                    for (i in it.indices + 1) {
                        val bean = it[i]
                        singer.append("、").append(bean.name)
                    }
                }
                return singer.toString()
            }
        }
        return null
    }

    fun getSinger(data: AlbumListBean): String? {
        val singerList = data.singer
        singerList?.let {
            if (it.isNotEmpty()) {
                val singer = StringBuilder(it[0].name ?: "")
                if (it.size > 1) {
                    for (i in it.indices + 1) {
                        val bean = it[i]
                        singer.append("、").append(bean.name)
                    }
                }
                return singer.toString()
            }
        }
        return null
    }

    fun getTitle(title: String?): String? {
        var t = strFilter(title)
        if (t.isNullOrEmpty()) {
            t = R.string.unknown.getResString()
        }
        return t
    }


    fun getArtistAndAlbum(artist: String?, album: String?): String {
        val ar = strFilter(artist)
        val al = strFilter(album)
        return if (TextUtils.isEmpty(ar) && TextUtils.isEmpty(al)) {
            R.string.unknown_singer.getResString()
        } else if (!TextUtils.isEmpty(ar) && TextUtils.isEmpty(al)) {
            ar!!
        } else if (TextUtils.isEmpty(ar) && !TextUtils.isEmpty(al)) {
            al!!
        } else {
            "$artist - $album"
        }
    }

    // 过滤特殊字符
    fun strFilter(str: String?): String? {
        if (str == null) return null
        val regex = "<[^>]+>"
        val p = Pattern.compile(regex)
        val m = p.matcher(str)
        return m.replaceAll("").trim()
    }
}
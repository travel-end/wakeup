package never.give.up.japp.net

import never.give.up.japp.consts.Constants
import never.give.up.japp.model.Music
import never.give.up.japp.utils.FileUtils
import never.give.up.japp.utils.SpUtil
import never.give.up.japp.utils.execute
import never.give.up.japp.utils.log

/**
 * @By Journey 2020/12/7
 * @Description
 */
object MusicApi {
    fun getMusicInfo(music: Music?):Music? {
        if (music == null) return null
        var quality = SpUtil.getInt(Constants.KEY_SONG_QUALITY,music.quality)
        if (music.quality != quality) {
            quality = when {
                quality >= 999000 && music.sq -> 999000
                quality >= 320000 && music.hq -> 320000
                quality >= 192000 && music.high -> 192000
                quality >= 128000 -> 128000
                else -> 128000
            }
        }
        val cachePath = FileUtils.getMusicCacheDir() + music.artist + " - " + music.title + "(" + quality + ")"
        val downloadPath = FileUtils.getMusicDir() + music.artist + " - " + music.title + ".mp3"
        if (FileUtils.exists(cachePath)) {
            music.uri = cachePath
            return music
        } else if (FileUtils.exists(downloadPath)) {
            music.uri = downloadPath
            return music
        }
        var playUrl: String? = null
        if (music.type == Constants.QQ) {
            val songUrl =
                "${Constants.SONG_URL_DATA_LEFT}${music.mid}${Constants.SONG_URL_DATA_RIGHT}"
            execute {
                val result =
                    RetrofitClient.instance.songUrlApiService.getSongUrl(songUrl)
                if (result.code == 0) {
                    val sipList = result.req_0?.data?.sip
                    var sip = ""
                    if (sipList != null) {
                        if (sipList.isNotEmpty()) {
                            sip = sipList[0]
                        }
                    }
                    val purlList = result.req_0?.data?.midurlinfo
                    var pUrl: String? = ""
                    if (purlList != null) {
                        if (purlList.isNotEmpty()) {
                            pUrl = purlList[0].purl
                        }
                    }
                    playUrl = "$sip$pUrl"
                }
            }
            music.uri = playUrl
            return music
        } else {
            val uri = MusicApiServiceImpl.getMusicUrl(music.type?:Constants.LOCAL,music.mid?:"",quality)
            music.uri = playUrl
            return music
        }
    }
}
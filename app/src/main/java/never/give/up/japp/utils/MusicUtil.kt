package never.give.up.japp.utils

import com.cyl.musicapi.playlist.MusicInfo
import never.give.up.japp.consts.Constants
import never.give.up.japp.model.core.ListBean
import never.give.up.japp.model.Music

/**
 * @By Journey 2020/12/3
 * @Description
 */
object MusicUtil {
    val PIC_SIZE_SMALL = 0
    val PIC_SIZE_NORMAL = 1
    val PIC_SIZE_BIG = 2
    fun getMusic(musicInfo: MusicInfo): Music {
        val music = Music()
        if (musicInfo.songId != null) {
            music.mid = musicInfo.songId
        } else if (musicInfo.id != null) {
            music.mid = musicInfo.id
        }
        music.collectId = musicInfo.id
        music.title = musicInfo.name
        music.isOnline = true
        music.type = musicInfo.vendor
        music.album = musicInfo.album?.name
        music.albumId = musicInfo.album?.id
        music.isCp = musicInfo.cp
        music.isDl = musicInfo.dl
        musicInfo.quality?.let {
            music.sq = it.sq
            music.hq = it.hq
            music.high = it.high
        }
        if (musicInfo.artists != null) {
            var artistIds = musicInfo.artists?.get(0)?.id
            var artistNames = musicInfo.artists?.get(0)?.name
            for (j in 1 until musicInfo.artists?.size!! - 1) {
                artistIds += ",${musicInfo.artists?.get(j)?.id}"
                artistNames += ",${musicInfo.artists?.get(j)?.name}"
            }
            music.artist = artistNames
            music.artistId = artistIds
        }
        music.coverUri = getAlbumPic(musicInfo.album?.cover, musicInfo.vendor, PIC_SIZE_NORMAL)
        music.coverBig = getAlbumPic(musicInfo.album?.cover, musicInfo.vendor, PIC_SIZE_BIG)
        music.coverSmall = getAlbumPic(musicInfo.album?.cover, musicInfo.vendor, PIC_SIZE_SMALL)
        return music
    }

    fun getMusic(bean: ListBean): Music {
        val music = Music()
        music.mid = bean.songmid
        val singers = bean.singer
        if (singers != null) {
            var artistIds = singers[0].id.toString()
            var artistNames = singers[0].name
            for (j in 1 until singers.size - 1) {
                artistIds += ",${singers[j].id}"
                artistNames += ",${singers[j].name}"
            }
            music.artist = artistNames
            music.artistId = artistIds
            music.title = bean.songname
//            music.coverUri = "${Constants.ALBUM_PIC}${bean.albummid}${Constants.JPG}"
            music.coverUri = getAlbumPic(bean.albummid, bean.mainType, PIC_SIZE_NORMAL)
//            music.coverBig = getAlbumPic(musicInfo.album?.cover, musicInfo.vendor, PIC_SIZE_BIG)
//            music.coverSmall = getAlbumPic(musicInfo.album?.cover, musicInfo.vendor, PIC_SIZE_SMALL)
            music.duration = bean.interval.toLong()
            music.isOnline = true
            music.album = bean.albumname
            music.albumId = bean.albummid
            music.type = bean.mainType
            music.lyric = bean.lyric
        }
        return music
    }

    fun getAlbumPic(url: String?, type: String?, size: Int): String? {
        println(url)
        return when (type) {
            Constants.QQ -> {
//                when (size) {
//                    PIC_SIZE_SMALL -> {
//                        url?.replace("150x150", "90x90")
//                    }
//                    PIC_SIZE_NORMAL -> {
//                        url?.replace("150x150", "150x150")
//                    }
//                    else -> {
//                        url?.replace("150x150", "300x300")
//                    }
//                }
                "${Constants.ALBUM_PIC}$url${Constants.JPG}"
            }
            Constants.XIAMI -> {
                val tmp = url?.split("@")?.get(0) ?: url
                when (size) {
                    PIC_SIZE_SMALL -> "$tmp@1e_1c_100Q_90w_90h"
                    PIC_SIZE_NORMAL -> "$tmp@1e_1c_100Q_150w_150h"
                    else -> "$tmp@1e_1c_100Q_450w_450h"
                }
            }
            Constants.NETEASE -> {
                val temp = url?.split("?")?.get(0) ?: url
                when (size) {
                    PIC_SIZE_SMALL -> "$temp?param=90y90"
                    PIC_SIZE_NORMAL -> "$temp?param=150y150"
                    else -> "$temp?param=450y450"
                }
            }
            Constants.BAIDU -> {
                val tmp = url?.split("@")?.get(0) ?: url
                when (size) {
                    PIC_SIZE_SMALL -> "$tmp@s_1,w_90,h_90"
                    PIC_SIZE_NORMAL -> "$tmp@s_1,w_150,h_150"
                    else -> "$tmp@s_1,w_450,h_450"
                }
            }
            else -> {
                url
            }
        }
    }

}
package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.consts.Constants
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist
import never.give.up.japp.utils.MusicUtil
import never.give.up.japp.utils.log

class MdMusicViewModel:BaseViewModel() {
    val musicList:MutableLiveData<MutableList<Music>> = MutableLiveData()
    fun loadPlaylistSongs(playlist: Playlist) {
        when(playlist.type) {
            // 每日推荐
            Constants.PLAYLIST_WY_RECOMMEND_ID->{

            }
        }
    }

    private fun loadRecommendSongs() {
        request {
           val result =  neteaseApiService.getRecommendSongs()
            if (result.code==200) {
                val list = mutableListOf<Music>()
                result.data?.dailySongs?.forEach {
                    val music = Music()
                    music.mid = it.id
                    music.title = it.name
                    music.type = Constants.NETEASE
                    music.album = it.al?.name
                    music.isOnline = true
                    music.albumId = it.al?.id
                    if (it.ar != null) {
                        var artistIds = it.ar?.get(0)?.id.toString()
                        var artistNames = it.ar?.get(0)?.name
                        for (j in 1 until it.ar?.size!! - 1) {
                            artistIds += ",${it.ar?.get(j)?.id}"
                            artistNames += ",${it.ar?.get(j)?.name}"
                        }
                        music.artist = artistNames
                        music.artistId = artistIds
                    }
                    music.coverUri = MusicUtil.getAlbumPic(it.al?.picUrl, Constants.NETEASE, MusicUtil.PIC_SIZE_NORMAL)
                    music.coverBig = MusicUtil.getAlbumPic(it.al?.picUrl, Constants.NETEASE, MusicUtil.PIC_SIZE_BIG)
                    music.coverSmall = MusicUtil.getAlbumPic(it.al?.picUrl, Constants.NETEASE, MusicUtil.PIC_SIZE_SMALL)
                    list.add(music)
                }
                musicList.value = list
                "推荐音乐：${list.size}".log()
            }
        }
    }
}
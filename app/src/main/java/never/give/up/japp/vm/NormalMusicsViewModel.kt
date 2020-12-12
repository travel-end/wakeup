package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import never.give.up.japp.Japp
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.consts.Constants
import never.give.up.japp.data.PlayHistoryLoader
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist
import never.give.up.japp.utils.PlaylistLoader
import never.give.up.japp.utils.SongLoader

/**
 * @By Journey 2020/12/11
 * @Description
 */
class NormalMusicsViewModel:BaseViewModel() {
    val localMusics:MutableLiveData<MutableList<Music>> = MutableLiveData()
    val historyMusics:MutableLiveData<Playlist> = MutableLiveData()
    fun loadMusics(isReload:Boolean,listType:String) {
        if (listType=="") return
        request {
            when(listType){
                Constants.PLAYLIST_LOCAL_ID->{
                    val musicList = SongLoader.getLocalMusic(Japp.getInstance(),isReload)
                    localMusics.value =musicList
                }
                Constants.PLAYLIST_HISTORY_ID->{
//                    val playList = PlaylistLoader.getHistoryPlaylist()
                    val playList = PlayHistoryLoader.getPlayHistory()
//                    historyMusics.value = playList
                    if (playList.size >0) {
                        localMusics.value =playList
                    }
                }
            }

        }
    }
}
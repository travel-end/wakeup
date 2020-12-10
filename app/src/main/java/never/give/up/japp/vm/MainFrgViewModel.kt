package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import never.give.up.japp.Japp
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.data.PlayHistoryLoader
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist
import never.give.up.japp.utils.DownloadLoader
import never.give.up.japp.utils.PlaylistLoader
import never.give.up.japp.utils.SongLoader

/**
 * @By Journey 2020/12/2
 * @Description
 */
class MainFrgViewModel:BaseViewModel() {
    val localMusics :MutableLiveData<MutableList<Music>> = MutableLiveData()
    val historyMusics:MutableLiveData<MutableList<Music>> = MutableLiveData()
    val loveMusics:MutableLiveData<MutableList<Music>> = MutableLiveData()
    val downloadMusics:MutableLiveData<MutableList<Music>> = MutableLiveData()
    val localPlaylist:MutableLiveData<MutableList<Playlist>> = MutableLiveData()
    fun loadSongs() {
        viewModelScope.launch {
            val localDef = async { SongLoader.getLocalMusic(Japp.getInstance()) }
            localMusics.value = localDef.await()

            val historyDef = async { PlayHistoryLoader.getPlayHistory() }
            historyMusics.value = historyDef.await()

            val loveDef = async { SongLoader.getFavoriteSong() }
            loveMusics.value = loveDef.await()

            val downloadDef = async { DownloadLoader.getDownloadList() }
            downloadMusics.value = downloadDef.await()
        }
    }
    fun getLocalPlaylist() {
        request {
            val playlist = PlaylistLoader.getAllPlaylist()
            playlist.forEach {
                it.pid?.let { it1->
                    val list = PlaylistLoader.getMusicForPlaylist(it1)
                    it.total = list.size.toLong()
                }
            }
            localPlaylist.value = playlist
        }
    }
}
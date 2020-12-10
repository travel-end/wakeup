package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.model.Music
import never.give.up.japp.model.core.OnlineSongLrc

/**
 * @By Journey 2020/12/10
 * @Description
 */
class PlayLrcViewModel:BaseViewModel() {
    val onlineLyric: MutableLiveData<OnlineSongLrc?> = MutableLiveData()
    fun getPlayingLrc(music:Music) {
        val songId = music.mid ?: return
        viewModelScope.launch {
            runCatching {
                apiService.getOnlineSongLrc(songId)
            }.onSuccess {
                if (it != null) {
                    if (it.code == 0) {
                        onlineLyric.value = it
                    } else {
                        onlineLyric.value = null
                    }
                }
            }.onFailure {
                onlineLyric.value = null
            }
        }
    }
}
package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.model.Music
import never.give.up.japp.model.TasksManagerModel
import never.give.up.japp.utils.DownloadLoader

/**
 * @By Journey 2020/12/14
 * @Description
 */
class DownloadViewModel:BaseViewModel() {
    val musicList:MutableLiveData<MutableList<Music>> = MutableLiveData()
    val downloadingMusics:MutableLiveData<MutableList<TasksManagerModel>> = MutableLiveData()
    fun loadDownloadedMusics(isCache:Boolean) {
        request {
            val data = DownloadLoader.getDownloadList(isCache)
            musicList.value = data
        }
    }

    fun loadDownloadingMusics() {
        request {
            val data = DownloadLoader.getDownloadingList()
            downloadingMusics.value = data
        }
    }
}
package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.model.core.SearchSong
import never.give.up.japp.utils.toast

/**
 * @By Journey 2020/12/3
 * @Description
 */
class SearchMusicViewModel:BaseViewModel() {
    val searchResult: MutableLiveData<SearchSong> = MutableLiveData()
    fun searchMusic(searchContent: String, page: Int) {
//        loadStatus.value = State(StateType.LOADING_SONG)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    apiService.searchMusic(searchContent, page)
                }
            }.onSuccess {
                searchResult.value = it
            }.onFailure {
                "搜索失败".toast()
//                handleException(it, State(StateType.ERROR,msg = R.string.empty.getStringRes()))// TODO: 2020/10/29 处理搜索结果空
            }
//            loadStatus.value = State(StateType.DISMISSING_SONG)
        }
    }
}
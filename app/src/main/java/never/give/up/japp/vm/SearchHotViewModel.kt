package never.give.up.japp.vm

import androidx.lifecycle.MutableLiveData
import never.give.up.japp.R
import never.give.up.japp.base.BaseViewModel
import never.give.up.japp.model.ContentTitle
import never.give.up.japp.model.SearchHistory
import never.give.up.japp.utils.getResString

/**
 * @By Journey 2020/12/2
 * @Description
 */
class SearchHotViewModel : BaseViewModel() {
    val mData: MutableLiveData<MutableList<Any>> = MutableLiveData()
    val data = mutableListOf<Any>()
    val deleteHistory: MutableLiveData<Boolean> = MutableLiveData()
    fun initHotSearchData() {
//        val searchDao = db.searchDao
        request {
            val hotSearch = neteaseApiService.getHotSearchInfo()
            data.add(hotSearch.result!!)
            data.add(ContentTitle(R.string.search_history.getResString()))
//            val historyTags = searchDao.findRecentSearchHistory(10)
//            if (historyTags.isNotEmpty()) {
//                data.add(SearchHistory(historyTags))
//            }
            mData.value = data
        }
    }

    fun deleteSearchHistoryTag() {
//        val searchDao = db.searchDao
//        request {
//            val result = searchDao.deleteSearchHistoryTag()
//            if (result != 0) {
//                deleteHistory.value = true
//            }
//        }
    }
}
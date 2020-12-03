package never.give.up.japp.ui.search

import android.os.Bundle
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.vm.SearchMusicViewModel

/**
 * @By Journey 2020/12/3
 * @Description
 */
class SearchAlbumFragment:BaseVmFragment<SearchMusicViewModel>() {
    companion object {
        fun newInstance(searchText:String):SearchAlbumFragment {
            val fragment = SearchAlbumFragment()
            val bundle = Bundle().apply {
                putString(Constants.KEY_SEARCH_CONTENT,searchText)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
    override fun layoutResId()=R.layout.frg_rv_container
}
package never.give.up.japp.ui.search

import android.os.Bundle
import never.give.up.japp.R
import never.give.up.japp.base.BaseVpFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.utils.getResString

/**
 * @By Journey 2020/12/2
 * @Description
 */
class SearchResultFragment : BaseVpFragment() {
    companion object {
        fun newInstance(searchText: String): SearchResultFragment {
            val fragment = SearchResultFragment()
            fragment.arguments = Bundle().apply {
                putString(Constants.KEY_SEARCH_CONTENT, searchText)
            }
            return fragment
        }
    }

    override fun layoutResId() = R.layout.frg_tab_vp

    override fun initView() {
        super.initView()
        val searchText = arguments?.getString(Constants.KEY_SEARCH_CONTENT) ?: ""
        initVpTitle(
            arrayOf(
                R.string.single_song.getResString(),
                R.string.album.getResString()
            )
        )
        initVpFragments(
            arrayOf(
                SearchMusicFragment.newInstance(searchText),
                SearchAlbumFragment.newInstance(searchText)
            )
        )
    }
}
package never.give.up.japp.ui.search

import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import never.give.up.japp.R
import never.give.up.japp.base.BaseFragment
import never.give.up.japp.utils.*
import never.give.up.japp.widget.ClearEditText

/**
 * @By Journey 2020/12/2
 * @Description
 */
class SearchMainFragment : BaseFragment() {
    private lateinit var searchEditText: ClearEditText
    private lateinit var searchTv: TextView
    private lateinit var searchBackIv: ImageView
    private lateinit var searchResultFragment: SearchResultFragment
    private var isSearchVp: Boolean = false
    override fun layoutResId() = R.layout.frg_main_search
    override fun initView() {
        super.initView()
        searchEditText = mRootView.findViewById(R.id.view_search_et)
        searchTv = mRootView.findViewById(R.id.view_search_tv)
        searchBackIv = mRootView.findViewById(R.id.view_search_iv_back)
//        val searchRootLayout = mRootView.findViewById<LinearLayout>(R.id.main_search_search)
//        if (searchRootLayout != null) {
//            val lp = searchRootLayout.layoutParams as LinearLayout.LayoutParams
//            lp.topMargin = ImUtils.getStatusBarHeight(requireContext())
//        }
        searchEditText.hint = R.string.search_content_hint.getResString()
    }

    override fun initData() {
        super.initData()
//        requireLazyInit()
        replaceFragment(SearchHotFragment.newInstance())
    }

    //    override fun lazyInitData() {
//        super.lazyInitData()
//        replaceFragment(SearchHotFragment.newInstance())
//    }
//
    override fun initAction() {
        super.initAction()
        searchEditText.showKeyBoard(requireContext())
        searchTv.fastClickListener {
            requireActivity().hideKeyboards()
            val content = searchEditText.editableText.toString()
            if (content.isNotNullOrEmpty()) {
                isSearchVp = true
                searchResultFragment = SearchResultFragment.newInstance(content)
                replaceFragment(searchResultFragment)
            }
        }
        searchBackIv.fastClickListener {
            if (isSearchVp) {
                searchEditText.text = "".getEditableStr()
//                removeFragment(searchVpFragment)
                replaceFragment(SearchHotFragment.newInstance())
                isSearchVp = false
            } else {
                requireActivity().hideKeyboards()
//                NavUtil.navUp(it)
            }
        }
        searchEditText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                searchTv.performClick()
                true
            } else {
                false
            }
        }
    }

    fun replaceFragment(fragment: Fragment, searchContent: String = "") {
        if (searchContent.isNotNullOrEmpty()) {
            isSearchVp = true
            searchEditText.text = searchContent.getEditableStr()
            searchEditText.setSelection(searchContent.length)
//            searchEditText.isCursorVisible = true
            requireActivity().hideKeyboards()
        }
        childFragmentManager.beginTransaction().replace(R.id.main_search_fl_container, fragment)
            .commitAllowingStateLoss()
    }

    fun removeFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }

}
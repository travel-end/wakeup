package never.give.up.japp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.model.ContentTitle
import never.give.up.japp.model.HistoryTag
import never.give.up.japp.model.RecommendSearchList
import never.give.up.japp.model.SearchHistory
import never.give.up.japp.rv.*
import never.give.up.japp.utils.getResString
import never.give.up.japp.utils.gone
import never.give.up.japp.utils.isNotNullOrEmpty
import never.give.up.japp.vm.SearchHotViewModel
import never.give.up.japp.widget.flowlayout.FlowLayout
import never.give.up.japp.widget.flowlayout.TagAdapter
import never.give.up.japp.widget.flowlayout.TagFlowLayout

/**
 * @By Journey 2020/12/2
 * @Description
 */
class SearchHotFragment:BaseVmFragment<SearchHotViewModel>() {
    private lateinit var rvSearchHot: RecyclerView
    companion object {
        fun newInstance(): SearchHotFragment {
            return SearchHotFragment()
        }
    }
    override fun layoutResId()=R.layout.view_rv
    override fun initView() {
        super.initView()
        rvSearchHot = mRootView.findViewById(R.id.search_hot_rv)
        rvSearchHot.setup<Any> {
            adapter {
                /*推荐搜索*/
                addItem(R.layout.item_flow_layout) {
                    isForViewType { data, position -> data is SearchHistory }
                    bindViewHolder { data, position, holder ->
                        val searchHistory = (data as SearchHistory).historyList
                        setText(R.id.item_history_title_tv,R.string.search_history.getResString())
                        if (isNotNullOrEmpty(searchHistory)) {
                            itemView?.findViewById<TagFlowLayout>(R.id.item_sh_flow_layout)
                                ?.let { flow ->
                                    flow.adapter =
                                        object : TagAdapter<HistoryTag>(searchHistory) {
                                            override fun getView(
                                                parent: FlowLayout?,
                                                position: Int,
                                                t: HistoryTag?
                                            ): View {
                                                return LayoutInflater.from(parent?.context)
                                                    .inflate(
                                                        R.layout.item_round_text,
                                                        parent,
                                                        false
                                                    )
                                                    .apply { (this as TextView).text = t?.name }
                                            }
                                        }
                                    flow.setOnTagClickListener { _, position, _ ->
                                        val historyTag = searchHistory!![position].name
                                        if (historyTag.isNotNullOrEmpty()) {
//                                            val parent = (parentFragment as SearchMainFragment)
//                                            parent.removeFragment(this@SearchHotFragment)
//                                            parent.replaceFragment(SearchVpFragment.newInstance(historyTag!!),historyTag)
//                                            LogUtil.e("searchHistory name:$historyTag")
                                        }
                                        false
                                    }
                                    setVisible(R.id.item_sh_flow_layout, true)
                                }
                        } else {
                            visible(R.id.item_sh_tv_no)
                            invisible(R.id.item_history_end_iv)
                            itemView?.findViewById<TagFlowLayout>(R.id.item_sh_flow_layout)?.removeAllViews()
                            itemView?.findViewById<TagFlowLayout>(R.id.item_sh_flow_layout)?.gone()
                        }
                        clicked(R.id.item_history_end_iv, View.OnClickListener {
                            mViewModel.deleteSearchHistoryTag()
                        })
                    }
                }

                // 5
                addItem(R.layout.view_content_title) {
                    isForViewType { data, _ -> data is ContentTitle }
                    bindViewHolder { data, position, holder ->
                        invisible(R.id.item_history_end_iv)
                        setText(R.id.item_history_title_tv, (data as ContentTitle).title)
                    }
                }
                // 6
                addItem(R.layout.view_gradient_bg) {
                    isForViewType { data, position -> data is RecommendSearchList }
                    bindViewHolder { data, position, holder ->
                        val songList = (data as RecommendSearchList).recommendSearch
                        val rootView = itemView as LinearLayout
                        if (isNotNullOrEmpty(songList)) {
                            rootView.removeAllViews()
                            val lps = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            lps.setMargins(0, 10, 0, 10)
                            for (song in songList) {
                                val itemSongView = View.inflate(
                                    requireContext(),
                                    R.layout.item_num_content,
                                    null
                                )
                                val tvSongName: TextView =
                                    itemSongView.findViewById(R.id.item_search_hot_song_tv_song_name)
                                val tvDesc: TextView =
                                    itemSongView.findViewById(R.id.item_search_hot_song_tv_description)
                                val tvIndex: TextView =
                                    itemSongView.findViewById(R.id.item_search_hot_song_tv_index)
//                                tvSongName.text = song.songName
//                                tvDesc.text = song.desc
                                tvIndex.text = "${song.id ?: 0}"
                                rootView.addView(itemSongView, lps)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mViewModel.initHotSearchData()
    }
    override fun observe() {
        super.observe()
        mViewModel.mData.observe(this, Observer {
            it?.let {
                rvSearchHot.submitList(it)
            }
        })
        mViewModel.deleteHistory.observe(this,Observer{
            if (it == true) {
                rvSearchHot.updateData(0, SearchHistory())
            }
        })
    }
}
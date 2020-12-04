package never.give.up.japp.ui.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.frg_rv_container.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.listener.OnMusicItemClickListener
import never.give.up.japp.model.Music
import never.give.up.japp.play.PlayManager
import never.give.up.japp.rv.setLoadMoreListener
import never.give.up.japp.ui.adapter.MusicAdapter
import never.give.up.japp.utils.MusicUtil
import never.give.up.japp.utils.isNotNullOrEmpty
import never.give.up.japp.utils.log
import never.give.up.japp.utils.toast
import never.give.up.japp.vm.SearchMusicViewModel

/**
 * @By Journey 2020/12/3
 * @Description
 */
class SearchMusicFragment : BaseVmFragment<SearchMusicViewModel>() {
    private var searchText: String = ""
    private var pageOffset: Int = 1
    private val musicList = mutableListOf<Music>()
    private lateinit var musicAdapter: MusicAdapter
    private var listSize :Int = 0

    companion object {
        fun newInstance(searchText: String): SearchMusicFragment {
            val fragment = SearchMusicFragment()
            val bundle = Bundle().apply {
                putString(Constants.KEY_SEARCH_CONTENT, searchText)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun layoutResId() = R.layout.frg_rv_container

    override fun initView() {
        super.initView()
        rvContainer.setHasFixedSize(true)
        musicAdapter = MusicAdapter(requireContext(), searchText)
        rvContainer.adapter = musicAdapter
    }

    override fun initData() {
        super.initData()
        searchText = arguments?.getString(Constants.KEY_SEARCH_CONTENT) ?: ""
        if (searchText.isNotEmpty()) {
            mViewModel.searchMusic(searchText, pageOffset)
        }
    }

    override fun observe() {
        super.observe()
        mViewModel.searchResult.observe(this, Observer {
            it?.let { music ->
                val song = music.data?.song
                if (song != null) {
                    val songList = song.list
                    if (isNotNullOrEmpty(songList)) {
                        listSize = songList!!.size
                        try {
                            songList.forEach { s ->
                                s.mainType = Constants.QQ
                                musicList.add(MusicUtil.getMusic(s))
                            }
                            musicAdapter.setData(musicList)
                            musicAdapter.notifyDataSetChanged()
                        } catch (e: Exception) {
                            "添加歌曲错误：${e.message}".log()
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    override fun initAction() {
        super.initAction()
        rvContainer.setLoadMoreListener {
            "加载更多".log()
            if (listSize < Constants.SEARCH_SONG_PAGE_SIZE) {
                "没有更多了".toast()
            } else {
                pageOffset++
                mViewModel.searchMusic(searchText,pageOffset)
            }
        }
        musicAdapter.setOnMusicItemClickListener(object : OnMusicItemClickListener {
            override fun onMusicItemClick(music: Music, position: Int) {
                "name:${music.title} position,$position".log()
                if (musicList.size <= position) return
                PlayManager.playOnline(music)
            }
            override fun onMoreClick(view: View) {
            }
        })
    }
}
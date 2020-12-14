package never.give.up.japp.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.frag_normal_musics.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.listener.OnMusicItemClickListener
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist
import never.give.up.japp.play.PlayManager
import never.give.up.japp.rv.*
import never.give.up.japp.ui.adapter.NormalMusicAdapter
import never.give.up.japp.utils.StringUtil
import never.give.up.japp.utils.fastClickListener
import never.give.up.japp.utils.getResColor
import never.give.up.japp.utils.toast
import never.give.up.japp.vm.NormalMusicsViewModel
import never.give.up.japp.widget.RippleView

/**
 * @By Journey 2020/12/11
 * @Description
 */
class NormalMusicsFragment : BaseVmFragment<NormalMusicsViewModel>() {
    private var musicList = mutableListOf<Music>()
    private var listType: String = ""
    private var playList: Playlist? = null
    private lateinit var mLm: LinearLayoutManager
    private var mAdapter: NormalMusicAdapter? = null
    override fun layoutResId(): Int {
        return R.layout.frag_normal_musics
    }

    override fun getBundle(bundle: Bundle) {
        super.getBundle(bundle)
        listType = bundle.getString(Constants.KEY_MUSIC_TYPE, "")
    }

    override fun initView() {
        super.initView()
        mLm = LinearLayoutManager(requireContext())
        normalRvMusicList.layoutManager = mLm
    }

    override fun initAction() {
        super.initAction()
        mAdapter?.setOnMusicItemClickListener(object : OnMusicItemClickListener {
            override fun onMusicItemClick(music: Music, position: Int) {
                when (listType) {
                    Constants.PLAYLIST_LOCAL_ID -> {
                        PlayManager.play(position, musicList, Constants.PLAYLIST_LOCAL_ID)
                    }
                    Constants.PLAYLIST_HISTORY_ID -> {
                        if (playList != null) {
                            PlayManager.play(position, musicList, playList!!.pid ?: "")
                        }
                    }
                }
            }
            override fun onMoreClick(view: View) {
            }

        })
    }

    override fun initData() {
        super.initData()
        mViewModel.loadMusics(true, listType)
    }

    override fun observe() {
        super.observe()
        mViewModel.localMusics.observe(this, Observer {
            it?.let {
                musicList.clear()
                musicList.addAll(it)
                if (mAdapter == null) {
                    mAdapter = NormalMusicAdapter(musicList)
                    normalRvMusicList.adapter = mAdapter
                } else {
                    mAdapter?.notifyDataSetChanged()
                }
            }
        })
        mViewModel.historyMusics.observe(this, Observer {
            playList = it
        })
    }

}
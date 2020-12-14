package never.give.up.japp.ui

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.frg_main.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.utils.fastClickListener
import never.give.up.japp.utils.getResString
import never.give.up.japp.utils.log
import never.give.up.japp.vm.MainFrgViewModel

/**
 * @By Journey 2020/12/2
 * @Description
 */
class MainFragment:BaseVmFragment<MainFrgViewModel>() {
    override fun layoutResId()=R.layout.frg_main
    override fun initView() {
        super.initView()
    }

    override fun initData() {
        super.initData()
        mViewModel.loadSongs()
        mViewModel.getLocalPlaylist()
    }

    override fun initAction() {
        super.initAction()
        val bundle = Bundle()
        main_title_search.fastClickListener {
            nav(R.id.action_mainFragment_to_searchMainFragment)
        }
        layoutLocalMusic.fastClickListener {
            bundle.putString(Constants.KEY_MUSIC_TYPE,Constants.PLAYLIST_LOCAL_ID)
            nav(R.id.action_mainFragment_to_normalMusicsFragment,bundle)
        }
        layoutHistoryMusics.fastClickListener {
            bundle.putString(Constants.KEY_MUSIC_TYPE,Constants.PLAYLIST_HISTORY_ID)
            nav(R.id.action_mainFragment_to_normalMusicsFragment,bundle)
        }
        layoutDownloadMusics.fastClickListener {
            nav(R.id.action_mainFragment_to_downloadVpFragment)
        }
    }

    override fun observe() {
        super.observe()
        mViewModel.localMusics.observe(this,Observer{
            it?.let {
                "本地音乐：${it.size}首".log()
                mRootView.findViewById<TextView>(R.id.mainTvLocalMusicCount).text = "${it.size} 首dd"
//                    R.string.song_num.getResString(it.size)
            }
        })
        mViewModel.historyMusics.observe(this,Observer{
            it?.let {
                mainTvRecentMusicCount.text = R.string.song_num.getResString(it.size)
            }
        })

        mViewModel.loveMusics.observe(this,Observer{
            it?.let {
                mainTvLocalMusicCount.text = R.string.song_num.getResString(it.size)
            }
        })
        mViewModel.downloadMusics.observe(this,Observer{
            it?.let {
                mainTvDownloadedMusicCount.text = R.string.song_num.getResString(it.size)
            }
        })

        mViewModel.localPlaylist.observe(this,Observer{
            it?.let {

            }
        })

    }
}
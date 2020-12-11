package never.give.up.japp.ui

import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.frg_main.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
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
        main_title_search.fastClickListener {
            nav(R.id.action_mainFragment_to_searchMainFragment)
        }
        layoutLocalMusic.fastClickListener {

        }
    }

    override fun observe() {
        super.observe()
        mViewModel.localMusics.observe(this,Observer{
            it?.let {
                mainTvLocalMusicCount.text = "${it.size} 首"
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
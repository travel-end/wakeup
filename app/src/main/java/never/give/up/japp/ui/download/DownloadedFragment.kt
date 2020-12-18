package never.give.up.japp.ui.download

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.frag_normal_musics.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.listener.OnMusicItemClickListener
import never.give.up.japp.model.Music
import never.give.up.japp.play.PlayManager
import never.give.up.japp.ui.adapter.NormalMusicAdapter
import never.give.up.japp.vm.DownloadViewModel

/**
 * @By Journey 2020/12/14
 * @Description
 */
class DownloadedFragment :BaseVmFragment<DownloadViewModel>(){
    private var isCache:Boolean = false
    private val musics = mutableListOf<Music>()
    private lateinit var mLm:LinearLayoutManager
    private var musicAdapter:NormalMusicAdapter?=null
    companion object {
        fun newInstance(isCache:Boolean) :Fragment{
            val fragment  = DownloadedFragment()
            val args = Bundle().apply {
                putBoolean(Constants.KEY_IS_CACHE,isCache)
            }
            fragment.arguments =args
            return fragment
        }
    }
    override fun layoutResId()= R.layout.frag_normal_musics

    override fun getBundle(bundle: Bundle) {
        super.getBundle(bundle)
        isCache = bundle.getBoolean(Constants.KEY_IS_CACHE)
    }

    override fun initView() {
        super.initView()

    }
    override fun initData() {
        super.initData()
        mLm= LinearLayoutManager(requireContext())
        normalRvMusicList.layoutManager = mLm
        mViewModel.loadDownloadedMusics(isCache)
    }

    override fun initAction() {
        super.initAction()
        musicAdapter?.setOnMusicItemClickListener(object :OnMusicItemClickListener{
            override fun onMoreClick(view: View) {
                TODO("Not yet implemented")
            }

            override fun onMusicItemClick(music: Music, position: Int) {
                PlayManager.play(position,musics,Constants.PLAYLIST_DOWNLOAD_ID)
            }
        })
    }

    override fun observe() {
        super.observe()
        mViewModel.musicList.observe(this,Observer{
            it?.let {
                musics.clear()
                musics.addAll(it)
                if (musicAdapter==null) {
                    musicAdapter = NormalMusicAdapter(musics)
                    normalRvMusicList.adapter = musicAdapter
                } else {
                    musicAdapter?.setMusics(it)
                }
            }
        })
    }
}
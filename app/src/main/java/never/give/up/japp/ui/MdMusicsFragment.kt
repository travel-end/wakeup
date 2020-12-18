package never.give.up.japp.ui

import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.frag_md_musics.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Extras
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist
import never.give.up.japp.ui.adapter.NormalMusicAdapter
import never.give.up.japp.utils.fastClickListener
import never.give.up.japp.utils.intPx
import never.give.up.japp.vm.MdMusicViewModel

/**
 * @By Journey 2020/12/11
 * @Description
 */
class MdMusicsFragment:BaseVmFragment<MdMusicViewModel>() {
    private var alphaFlag: Boolean = true
    private var playlist:Playlist?=null
    private var musicList= mutableListOf<Music>()
    private var normalMusicAdapter:NormalMusicAdapter?=null
    override fun layoutResId()=R.layout.frag_md_musics

    override fun getBundle(bundle: Bundle) {
        super.getBundle(bundle)
        playlist = bundle.getParcelable(Extras.PLAYLIST)
    }

    override fun initView() {
        super.initView()
        initToolBar()
        normalMusicAdapter = NormalMusicAdapter(musicList)
        song_list_rv.adapter =normalMusicAdapter
        fab.fastClickListener {

        }
    }

    override fun initData() {
        super.initData()
        playlist?.let {
            if (it.musicList.size>0) {
                showPlaylistSongs(it.musicList)
            } else {
                mViewModel.loadPlaylistSongs(it)
            }
        }
    }

    override fun observe() {
        super.observe()
        mViewModel.musicList.observe(this,Observer{
            it?.let {
                showPlaylistSongs(it)
            }
        })
    }

    private fun showPlaylistSongs(songList:MutableList<Music>?) {
        songList?.let {
            musicList.addAll(it)
            normalMusicAdapter?.setMusics(musicList)
        }

    }


    private fun initToolBar() {
        val alphaMaxOffset = 150f.intPx
        md_song_list_toolbar?.background?.alpha = 0
        md_song_list_tv_title_name?.alpha = 0f
        md_song_list_appbar_layout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (verticalOffset > -alphaMaxOffset) {
                md_song_list_toolbar?.background?.alpha = 255 * -verticalOffset / alphaMaxOffset
                var textAlpha = -verticalOffset.toFloat() / 1000
                if (textAlpha > 1) {
                    textAlpha = 1.00f
                }
                md_song_list_tv_title_name?.alpha = textAlpha
                if (alphaFlag) {
                    alphaFlag = false
                    md_song_list_iv_title_back?.setImageResource(R.drawable.ic_arrow_left_white)
//                    immersionBar?.statusBarDarkFont(false)?.init()
                }
            } else {
                if (!alphaFlag) {
                    alphaFlag = true
                    md_song_list_toolbar?.background?.alpha = 255
                    md_song_list_tv_title_name?.alpha = 1.0f
                    md_song_list_iv_title_back?.setImageResource(R.drawable.ic_arrow_left)
//                    immersionBar?.statusBarDarkFont(true)?.init()
                }
            }
        })
    }
}
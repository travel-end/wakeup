package never.give.up.japp.ui

import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.frag_normal_musics.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist
import never.give.up.japp.play.PlayManager
import never.give.up.japp.rv.*
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
class NormalMusicsFragment:BaseVmFragment<NormalMusicsViewModel>() {
    private var musicList = mutableListOf<Music>()
    private var listType:String = ""
    private var playList:Playlist?=null
    private lateinit var mLm: LinearLayoutManager
    override fun layoutResId(): Int {
        return R.layout.frag_normal_musics
    }

    override fun getBundle(bundle: Bundle) {
        super.getBundle(bundle)
        listType = bundle.getString(Constants.KEY_MUSIC_TYPE,"")
    }

    override fun initView() {
        super.initView()
        mLm = LinearLayoutManager(requireContext())
        normalRvMusicList.setup<Music> {
            withLayoutManager {
                return@withLayoutManager mLm
            }
            adapter {
                addItem(R.layout.item_normal_music) {
                    bindViewHolder { data, position, holder ->
                        data?.let {m->
                            setText(R.id.item_search_song_list_tv_song_singer,StringUtil.getArtistAndAlbum(m.artist,m.album))
                            setText(R.id.item_search_song_list_tv_song_name,m.title)
                            if (PlayManager.getPlayingId()==m.mid) {
                                setVisible(R.id.v_playing,true)
                                setTextColor(R.id.item_search_song_list_tv_song_singer,R.color.colorPrimary.getResColor())
                                setTextColor(R.id.item_search_song_list_tv_song_name,R.color.colorPrimary.getResColor())
                                normalRvMusicList.scrollToPosition(position)
                            } else {
                                setVisible(R.id.v_playing,false)
                                setTextColor(R.id.item_search_song_list_tv_song_singer,R.color.text_color.getResColor())
                                setTextColor(R.id.item_search_song_list_tv_song_name,R.color.black2.getResColor())
                            }
                            if (m.isCp) {
                                setTextColor(R.id.item_search_song_list_tv_song_singer,R.color.text_color.getResColor())
                                setTextColor(R.id.item_search_song_list_tv_song_name,R.color.text_color.getResColor())
                            }
                            itemView?.findViewById<RippleView>(R.id.ripple_view)?.fastClickListener {
                                if (m.isCp) {
                                    "歌曲无法播放".toast()
                                } else {
                                    when(listType) {
                                        Constants.PLAYLIST_LOCAL_ID->{
                                            PlayManager.play(position,musicList, Constants.PLAYLIST_LOCAL_ID)
                                        }
                                        Constants.PLAYLIST_HISTORY_ID->{
                                            if (playList != null) {
                                                PlayManager.play(position,musicList, playList!!.pid?:"")
                                            }
                                        }
                                    }
                                }
                            }
                            itemView?.findViewById<ImageView>(R.id.item_search_song_list_iv_more)?.fastClickListener {

                            }
                        }
                    }
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mViewModel.loadMusics(true,listType)
    }

    override fun observe() {
        super.observe()
        mViewModel.localMusics.observe(this,Observer{
            it?.let {
                musicList.clear()
                musicList.addAll(it)
                normalRvMusicList.submitList(musicList)
            }
        })
        mViewModel.historyMusics.observe(this,Observer{
            playList = it
        })
    }

}
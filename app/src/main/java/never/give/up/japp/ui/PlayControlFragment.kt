package never.give.up.japp.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.frg_play_control.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.consts.Constants
import never.give.up.japp.event.GlobalSingle
import never.give.up.japp.model.Music
import never.give.up.japp.play.PlayManager
import never.give.up.japp.rv.addItem
import never.give.up.japp.rv.setText
import never.give.up.japp.rv.setup
import never.give.up.japp.rv.submitList
import never.give.up.japp.utils.StringUtil
import never.give.up.japp.utils.loadImg
import never.give.up.japp.utils.log
import never.give.up.japp.vm.PlayControlViewModel

/**
 * @By Journey 2020/11/25
 * @Description
 */
class PlayControlFragment: BaseVmFragment<PlayControlViewModel>() {
    private var ivCover:ImageView?=null
    private val musicList = mutableListOf<Music>()
    override fun layoutResId()= R.layout.frg_play_control
    override fun initView() {
        super.initView()
        updatePlayStatus(PlayManager.isPlaying())
        musicList.clear()
        musicList.addAll(PlayManager.getPlayList())
        initSongList()
    }

    override fun initData() {
        super.initData()
        val music = PlayManager.getPlayingMusic()
        mRootView.isVisible = music != null
    }

    override fun observe() {
        super.observe()
        GlobalSingle.playListChanged.observeInFragment(this,Observer{
            "---PlayControlFragment---PlaylistEvent---${it?.type}".log()
            it?.let {event->
                if (event.type == Constants.PLAYLIST_QUEUE_ID) {
                    musicList.clear()
                    musicList.addAll(PlayManager.getPlayList())
                    bottomPlayRv.submitList(musicList)
                    bottomPlayRv.scrollToPosition(PlayManager.position())
                }
            }
        })
    }

    private val rotationAnim by lazy {
        ObjectAnimator.ofFloat(ivCover!!, "rotation", 0.0f, 360.0f).apply {
            duration = 30000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
    }

    fun clearAnim() {
        if (rotationAnim.isRunning) {
            rotationAnim.cancel()
        }
    }

    private fun initSongList() {
        bottomPlayRv.onFlingListener=null
        val snap = PagerSnapHelper()
        snap.attachToRecyclerView(bottomPlayRv)
        bottomPlayRv.setup<Music> {
            adapter {
                addItem(R.layout.item_bottom_music) {
                    ivCover = itemView?.findViewById(R.id.iv_cover)
                    "---initSongList--".log()
                    bindViewHolder { data, position, holder ->
                        data?.let {
                            it.coverUri?.let {uri->
                                ivCover?.loadImg(uri)
                            }
                            setText(R.id.tv_title,StringUtil.getTitle(it.title))
                            setText(R.id.tv_artist,StringUtil.getArtistAndAlbum(it.artist,it.album))
                        }
                    }
                }
            }
        }
        bottomPlayRv.submitList(musicList)
        bottomPlayRv.scrollToPosition(PlayManager.position())
    }

    private fun updatePlayStatus(isPlaying:Boolean) {
        if (isPlaying && !playPauseView.isPlaying) {
            playPauseView.play()
        } else if (!isPlaying && playPauseView.isPlaying) {
            playPauseView.pause()
        }
    }
}
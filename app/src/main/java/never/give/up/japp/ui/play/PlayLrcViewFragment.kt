package never.give.up.japp.ui.play

import androidx.lifecycle.Observer
import never.give.up.japp.R
import never.give.up.japp.base.BaseFragment
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.model.Music
import never.give.up.japp.model.core.OnlineSongLrc
import never.give.up.japp.play.PlayManager
import never.give.up.japp.utils.getResString
import never.give.up.japp.vm.PlayLrcViewModel
import never.give.up.japp.widget.lrc.LrcView

/**
 * @By Journey 2020/12/8
 * @Description
 */
class PlayLrcViewFragment:BaseVmFragment<PlayLrcViewModel>() {
    private val lrcView by lazy { mRootView.findViewById<LrcView>(R.id.lrc_view) }
    override fun layoutResId()=R.layout.frag_lrc

    override fun initView() {
        super.initView()
        initLyric()
    }

    override fun initData() {
        super.initData()
        val music = PlayManager.getPlayingMusic()
        music?.let {
            mViewModel.getPlayingLrc(music)
        }
    }

    override fun observe() {
        super.observe()
        mViewModel.onlineLyric.observe(this,Observer{
            val lyric = it?.lyric
            if (lyric.isNullOrEmpty()) {
                lrcView.setLabel(R.string.lrc_empty.getResString())
            } else {
                lrcView.loadLrc(lyric)
            }
        })
    }

    fun updateLrcTime(currentTime:Long) {
        if (lrcView.hasLrc()) {
            lrcView.updateTime(currentTime)
        }
    }

    fun initLyric() {
        lrcView.setDraggable(true) { _, time ->
            PlayManager.seekTo(time.toInt())
            if (!PlayManager.isPlaying()) {
                PlayManager.playPause()
                // todo updatePlayProgress
            }
            true
        }

    }

    override fun initAction() {
        super.initAction()
        // 点击歌词
        lrcView.setOnTapListener { view, x, y ->

        }
    }
}
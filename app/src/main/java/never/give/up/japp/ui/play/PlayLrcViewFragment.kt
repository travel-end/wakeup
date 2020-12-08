package never.give.up.japp.ui.play

import never.give.up.japp.R
import never.give.up.japp.base.BaseFragment
import never.give.up.japp.play.PlayManager
import never.give.up.japp.utils.getResString
import never.give.up.japp.widget.lrc.LrcView

/**
 * @By Journey 2020/12/8
 * @Description
 */
class PlayLrcViewFragment:BaseFragment() {
    private val lrcView by lazy { mRootView.findViewById<LrcView>(R.id.lrc_view) }
    override fun layoutResId()=R.layout.frag_lrc

    fun updateLrcTime(currentTime:Long) {
        if (lrcView.hasLrc()) {
            lrcView.updateTime(currentTime)
        }
    }

    fun showLyric(lyric:String?,init:Boolean) {
        lrcView.setDraggable(true) { _, time ->
            PlayManager.seekTo(time.toInt())
            if (!PlayManager.isPlaying()) {
                PlayManager.playPause()
                // todo updatePlayProgress
            }
            true
        }
        if (lyric.isNullOrEmpty()) {
            lrcView.setLabel(R.string.lrc_empty.getResString())
        } else {
            lrcView.loadLrc(lyric)
        }
    }

    override fun initAction() {
        super.initAction()
        // 点击歌词
        lrcView.setOnTapListener { view, x, y ->

        }
    }
}
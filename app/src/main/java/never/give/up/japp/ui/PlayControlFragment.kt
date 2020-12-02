package never.give.up.japp.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import never.give.up.japp.R
import never.give.up.japp.base.BaseFragment

/**
 * @By Journey 2020/11/25
 * @Description
 */
class PlayControlFragment: BaseFragment() {
    override fun layoutResId()= R.layout.frg_play_control
    override fun initView() {
        super.initView()
    }

    override fun initData() {
        super.initData()
    }

//    private val rotationAnim by lazy {
//        ObjectAnimator.ofFloat(ivSongCover, "rotation", 0.0f, 360.0f).apply {
//            duration = 30000
//            interpolator = LinearInterpolator()
//            repeatCount = ValueAnimator.INFINITE
//            repeatMode = ValueAnimator.RESTART
//        }
//    }

//    fun clearAnim() {
//        if (rotationAnim.isRunning) {
//            rotationAnim.cancel()
//        }
//    }
}
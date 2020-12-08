package never.give.up.japp.ui.play

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.frag_play_cover.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseFragment
import never.give.up.japp.play.PlayManager
import never.give.up.japp.utils.fastClickListener
import never.give.up.japp.utils.gone
import never.give.up.japp.utils.visible

/**
 * @By Journey 2020/12/8
 * @Description
 */
class PlayCoverFragment:BaseFragment() {
    // 当前专辑图片
    var currentBitmap:Bitmap?=null
    // 是否初始化 第一次进入界面不播放切换动画
    var isInitAnimator:Boolean = false

    //动画
    private var coverAnimator: ObjectAnimator? = null
    private var objectAnimator1: ObjectAnimator? = null
    private var objectAnimator3: ObjectAnimator? = null
    private var objectAnimator2: ObjectAnimator? = null
    private var animatorSet: AnimatorSet? = null
    private var clickListener: (() -> Unit)? = null

    override fun layoutResId()=R.layout.frag_play_cover

    override fun initView() {
        super.initView()
    }

    override fun initData() {
        super.initData()
    }

    override fun initAction() {
        super.initAction()
        cover2View.fastClickListener {
            clickListener?.invoke()
        }
        coverView.fastClickListener {
            clickListener?.invoke()
        }
    }

    fun setImageBitmap(bm:Bitmap?) {
        coverView.setImageBitmap(bm)
        coverView.visible()
        if (currentBitmap == null) {
            cover2View.gone()
            cover2View.setImageBitmap(bm)
        }
        currentBitmap = bm
    }

    // 初始化动画
    fun initAnimator() {
        // 旋转动画
        coverAnimator = ObjectAnimator.ofFloat(cover2View, "rotation", 0F, 359F).apply {
            duration = (20 * 1000).toLong()
            repeatCount = -1
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                //同时更新civ_cover_2
                coverView?.rotation = it.animatedValue as Float
                cover2View?.rotation = it.animatedValue as Float
            }
        }
        // 缩放动画
        objectAnimator1 = ObjectAnimator.ofFloat(cover2View, "scaleX", 1f, 0.7f).apply {
            duration = 500L
            interpolator = AccelerateInterpolator()
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                cover2View.scaleY = it.animatedValue as Float
                cover2View.scaleX = it.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
//                    LogUtil.d("objectAnimator", "objectAnimator1 动画结束")
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    //开始时，初始化状态
                    coverView?.alpha = 0f
                    cover2View?.translationY = 0f
                    cover2View?.visibility = View.VISIBLE
//                    LogUtil.d("objectAnimator", "objectAnimator1 动画开始")
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }
        //coverView 由透明变成不透明
        objectAnimator3 = ObjectAnimator.ofFloat(coverView, "alpha", 0f, 1F).apply {
            duration = 300L
            addUpdateListener {
                coverView.alpha = it.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    coverView?.alpha = 1f
//                    LogUtil.d("objectAnimator", "objectAnimator3 onAnimationEnd")
                }

                override fun onAnimationCancel(animation: Animator?) {
                    coverView?.alpha = 1f
                }

                override fun onAnimationStart(animation: Animator?) {
//                    LogUtil.d("objectAnimator", "objectAnimator3 动画开始")
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }
        //civ_cover_2 上移动画
        objectAnimator2 = ObjectAnimator.ofFloat(cover2View, "translationY", 0f, -1000f).apply {
            duration = 300L
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                cover2View.translationY = it.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    cover2View?.translationY = 0f
                    cover2View?.setImageBitmap(currentBitmap)
                    cover2View?.visibility = View.GONE
//                    LogUtil.d("objectAnimator", "objectAnimator2 onAnimationEnd")
                }

                override fun onAnimationCancel(animation: Animator?) {
                    coverView?.alpha = 1f
                }

                override fun onAnimationStart(animation: Animator?) {
//                    LogUtil.d("objectAnimator", "objectAnimator2 动画开始")
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }
        animatorSet = AnimatorSet()
        animatorSet?.play(objectAnimator2)?.with(objectAnimator3)?.after(objectAnimator1)
    }
    // 切换歌曲 开始旋转动画
    fun startRotateAnimation(isPlaying: Boolean = false) {
//        LogUtil.d(TAG, "startRotateAnimation ，isInitAnimator=$isInitAnimator isPlaying =$isPlaying")
        if (isPlaying) {
            coverAnimator?.pause()
            coverAnimator?.start()
        }
        if (isInitAnimator) {
            cover2View?.visibility = View.VISIBLE
            //组合动画
            animatorSet?.pause()
            animatorSet?.start()
        } else {
            //第一次进入不播放切换动画
            isInitAnimator = true
            cover2View?.visibility = View.GONE
        }
    }

    // 停止旋转
    fun stopRotateAnimation() {
        coverAnimator?.pause()
    }
    // 继续旋转
    fun resumeRotateAnimation() {
        coverAnimator?.isStarted?.let {
            if (it) coverAnimator?.resume() else coverAnimator?.start()
        }
    }
    override fun onResume() {
        super.onResume()
        if (coverAnimator != null && coverAnimator?.isPaused!! && PlayManager.isPlaying()) {
            coverAnimator?.resume()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        coverAnimator?.cancel()
        coverAnimator = null
    }

    override fun onStop() {
        super.onStop()
        coverAnimator?.pause()
        animatorSet?.pause()
    }
    fun setOnclickAlbumListener(clickListener: (() -> Unit)?) {
        this.clickListener = clickListener;
    }
}
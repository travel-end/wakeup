package never.give.up.japp.utils

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView

object TransitionAnimationUtils {
    /**
     * 图片渐变切换动画
     */
    fun startChangeAnimation(iv:ImageView,drawable: Drawable?) {
        if (drawable==null) return
        val oldDrawable = iv.drawable
        var oldBitmapDrawable:Drawable?= null
        oldBitmapDrawable = if (oldDrawable == null) {
            ColorDrawable(Color.TRANSPARENT)
        } else if (oldDrawable is TransitionDrawable){
            oldDrawable.getDrawable(1)
        } else {
            oldDrawable
        }
        val td =
            TransitionDrawable(
                arrayOf(
                    oldBitmapDrawable!!,
                    drawable
                )
            )
        iv.setImageDrawable(td)
        td.startTransition(1000)
    }

    /**
     * 颜色渐变动画
     */
    fun startColorAnimation(mView: View, newColor: Int) {
        val olderColor = (mView.background as ColorDrawable).color
        val objectAnimator: ObjectAnimator
        objectAnimator = ObjectAnimator.ofInt(
            mView,
            "backgroundColor", olderColor, newColor
        )
            .setDuration(800)
        objectAnimator.setEvaluator(ArgbEvaluator())
        objectAnimator.start()
    }

    /**
     * 旋转动画
     */
    fun startCoverChangeAnimation(
        mView: ImageView,
        bitmap: Bitmap?
    ) {
        val startY = mView.bottom.toFloat()
        val endY = mView.height.toFloat()
        val objectAnimator: ObjectAnimator
        objectAnimator = ObjectAnimator.ofFloat(
            mView,
            "y", startY, endY
        )
            .setDuration(1000)
        objectAnimator.interpolator = AccelerateInterpolator()
        objectAnimator.start()
    }
}
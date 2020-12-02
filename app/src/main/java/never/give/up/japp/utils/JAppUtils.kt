package never.give.up.japp.utils

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import never.give.up.japp.Japp
import java.lang.reflect.ParameterizedType

/**
 * @By Journey 2020/12/2
 * @Description
 */
@Suppress("UNCHECKED_CAST")
fun <T> getClass(t: Any): Class<T> =
    (t.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

val Float.fpx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.intPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt()


fun Int.getDrawable()=ActivityCompat.getDrawable(Japp.getInstance(),this)

fun Int.getResString() = Japp.getInstance().resources.getString(this)

fun Int.getResDimen()=Japp.getInstance().resources.getDimension(this)

fun Int.getResColor() = ContextCompat.getColor(Japp.getInstance(),this)

fun Any?.isNull() = this ==null

fun Any?.isNotNull() = !isNull()

fun String?.isNotNullOrEmpty(): Boolean = !(this == null || this.trim().isBlank())

fun isNotNullOrEmpty(list: List<Any>?): Boolean {
    return !list.isNullOrEmpty()
}

fun Boolean?.truely() = this != null && this

fun Boolean?.falsely() = !truely()

fun String?.getEditableStr(): Editable {
    val value = this ?: ""
    return SpannableStringBuilder(value)
}

fun TextView?.setDiffColor(appointStr: String?, originalStr: String?) {
    if (this != null) {
        if (appointStr != null && originalStr != null) {
            val ori = originalStr.replace(
                appointStr.toRegex(),
                "<font color='#FF4081'>$appointStr</font>"
            )
            text = Html.fromHtml(ori)
        }
    }
}

fun CharSequence.toast(duration: Int = Toast.LENGTH_SHORT) {
    if (this.isNotEmpty()) {
        Toast.makeText(Japp.getInstance(), this, duration).show()
    }
}

fun Int.inflate(parent: ViewGroup, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(parent.context).inflate(this, parent, attachToRoot)
}

fun String?.log(tag:String = "Japp") {
    if (this.isNotNullOrEmpty()) {
        Log.e(tag,this!!)
    }
}

fun View?.visible() {
    if (this?.visibility == View.GONE) {
        this.visibility = View.VISIBLE
    }
}

fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

fun View?.gone() {
    if (this?.visibility == View.VISIBLE) {
        this.visibility = View.GONE
    }
}

val screenWidth get() =
    Resources.getSystem().displayMetrics.widthPixels

val screenHeight get() =
    Resources.getSystem().displayMetrics.heightPixels

fun getStatusBarHeight(context: Context): Int {
    var height = 20f.intPx
    return try {
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            height = context.resources.getDimensionPixelOffset(resId)
        }
        "statusBarHeight:$height".log()
        height
    } catch (e: Exception) {
        e.printStackTrace()
        height
    }
}

package never.give.up.japp.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
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

fun Int.getResString(size:Int) = Japp.getInstance().resources.getString(this,size)

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

fun String?.logi(tag:String = "Japp") {
    if (this.isNotNullOrEmpty()) {
        Log.i(tag,this!!)
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

fun isNetworkAvailable(context: Context):Boolean{
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = cm.activeNetworkInfo
    return info != null && info.isAvailable
}


fun EditText?.showKeyBoard(context: Context) {
    this?.let { et ->
        // 设置可获得焦点
        et.isFocusable = true
        et.isFocusableInTouchMode = true
        // 获取焦点
        et.requestFocus()
        // 调用系统输入法
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}

fun Activity.hideKeyboards() {
    // 当前焦点的 View
    val imm =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
}

//是否是Android 8.0
val isAndroidO get() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun <T> execute(block:suspend () -> T) :T{
    return runBlocking {
        withContext(Dispatchers.IO) {
            block.invoke()
        }
    }
}

// TODO: 2020/11/19 处理协程泄露的问题
fun <T> async(block:suspend () -> T) :T?{
    var result:T?=null
    GlobalScope.launch {
        result = withContext(Dispatchers.IO) {
            block.invoke()
        }
    }
    return result
}

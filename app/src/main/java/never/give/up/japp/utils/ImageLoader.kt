package never.give.up.japp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import never.give.up.japp.R
import never.give.up.japp.model.Music

/**
 * @By Journey 2020/10/26
 * @Description
 */
fun ImageView.loadImg(
    url: String,
    placeholder: Int = R.drawable.ic_default_cover,
    error: Int = R.drawable.ic_default_cover
) {
    val option = RequestOptions()
        .placeholder(placeholder)
        .error(error)
    Glide
        .with(this.context)
        .load(url)
        .apply(option)
        .into(this)
}

fun loadImgOfReady(
    context: Context,
    url: String,
    placeholder: Int = R.drawable.ic_default_cover,
    error: Int = R.drawable.ic_default_cover,
    block: (resource: Drawable) -> Unit
) {
    Glide
        .with(context)
        .load(url)
        .apply(RequestOptions.placeholderOf(placeholder).error(error))
        .into(object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                block.invoke(resource)
            }
        })
}

// 显示播放页大图
fun loadBigImageView(context: Context?, music: Music?, callBack: ((Bitmap) -> Unit)?) {
    if (music == null) return
    if (context == null) return
    val url = MusicUtil.getAlbumPic(music.coverUri, music.type, MusicUtil.PIC_SIZE_BIG)
    Glide.with(context)
        .asBitmap()
        .load(url ?: R.drawable.ic_default_cover)
        .error(R.drawable.ic_default_cover)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into<CustomTarget<Bitmap>>(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {

            }
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (callBack != null && resource != null) {
                    callBack.invoke(resource)
                }
            }
        })

}

fun ImageView.loadImgOfError(url: String, errorBlock: () -> Unit) {
    Glide.with(this.context)
        .load(url)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                errorBlock.invoke()
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        })
        .into(this)
}
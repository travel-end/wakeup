package never.give.up.japp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import never.give.up.japp.R
import never.give.up.japp.model.Music

object CoverLoader {
    val coverUriByRandom: Int = R.drawable.ic_default_cover

    /**
     * 显示小图
     *
     * @param mContext
     * @param music
     * @param callBack
     */
    fun loadImageViewByMusic(mContext: Context, music: Music?, callBack: ((Bitmap) -> Unit)?) {
        if (music == null) return
        val url = getCoverUriByMusic(music, false)
        loadBitmap(mContext, url, callBack)
    }

    /**
     * 返回bitmap
     *
     * @param mContext
     * @param url
     * @param callBack
     */
    fun loadBitmap(mContext: Context?, url: String?, callBack: ((Bitmap) -> Unit)?) {
        if (mContext == null) return
        Glide.with(mContext)
            .asBitmap()
            .load(url ?: coverUriByRandom)
            .error(coverUriByRandom)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into<CustomTarget<Bitmap>>(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callBack?.invoke(resource)
                }
            })
    }

    /**
     * 获取专辑图url，
     *
     * @param music 音乐
     * @param isBig 是否是大图
     * @return
     */
    private fun getCoverUriByMusic(music: Music, isBig: Boolean): String? {
        return if (music.coverBig != null && isBig) {
            music.coverBig
        } else if (music.coverUri != null) {
            music.coverUri
        } else {
            music.coverSmall
        }
    }

    fun loadBigImageView(context: Context, music: Music?, callBack: ((Bitmap) -> Unit)?) {
        if (music == null) return
        Glide.with(context)
            .asBitmap()
            .load(music.coverUri ?: R.drawable.disk)
            .error(R.drawable.disk)
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
    fun getCoverUri(context: Context, albumId: String): String? {
        if (albumId == "-1") {
            return null
        }
        var uri: String? = null
        try {
            val cursor = context.contentResolver.query(
                Uri.parse("content://media/external/audio/albums/$albumId"),
                arrayOf("album_art"), null, null, null)
            if (cursor != null) {
                cursor.moveToNext()
                uri = cursor.getString(0)
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }


}
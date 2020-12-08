package never.give.up.japp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.ImageView
import never.give.up.japp.Japp
import never.give.up.japp.R
import never.give.up.japp.play.PlayQueueManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object PlayUtil {
    fun updatePlayMode(iv:ImageView?,isChange:Boolean=false) {
        if (iv==null) return
        try {
            var playMode = PlayQueueManager.getPlayModeId()
            if (isChange) playMode = PlayQueueManager.updatePlayMode()
            when(playMode) {
                PlayQueueManager.PLAY_MODE_LOOP->{
                    if (isChange) R.string.order_play.getResString().toast()
                }
                PlayQueueManager.PLAY_MODE_REPEAT->{
                    if (isChange) R.string.single_play.getResString().toast()
                }
                PlayQueueManager.PLAY_MODE_RANDOM->{
                    if (isChange) R.string.random_play.getResString().toast()
                }
            }
            iv.setImageLevel(playMode)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    fun blurBitmap(bitmap: Bitmap,inSampleSize:Int):Drawable {
        val rs= RenderScript.create(Japp.getInstance())
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,30,baos)
        val byte = baos.toByteArray()
        val bais = ByteArrayInputStream(byte)
        val blurTemplate = BitmapFactory.decodeStream(bais,null,options)
        val input = Allocation.createFromBitmap(rs,blurTemplate)
        val output = Allocation.createTyped(rs,input.type)
        val script = ScriptIntrinsicBlur.create(rs,Element.U8_4(rs))
        script.setRadius(20f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(blurTemplate)
        return BitmapDrawable(Japp.getInstance().resources,blurTemplate)
    }

}
package never.give.up.japp.listener

import android.view.View
import never.give.up.japp.model.Music

/**
 * @By Journey 2020/11/13
 * @Description
 */
interface OnMusicItemClickListener {
    fun onMusicItemClick(music: Music, position:Int)
    fun onMoreClick(view:View)
}
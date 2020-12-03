package never.give.up.japp.listener

import android.view.View
import never.give.up.japp.model.core.ListBean

/**
 * @By Journey 2020/11/13
 * @Description
 */
interface OnMusicItemClickListener {
    fun onMusicItemClick(song: ListBean, position:Int)
    fun onMoreClick(view:View)
}
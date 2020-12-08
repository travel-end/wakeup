package never.give.up.japp.event

import never.give.up.japp.consts.Constants
import never.give.up.japp.model.Music
import never.give.up.japp.model.Playlist

/**
 * @By Journey 2020/12/7
 * @Description
 */

//歌单改变
data class PlaylistEvent(
    var type:String?=Constants.PLAYLIST_CUSTOM_ID,
    val playList:Playlist?=null
)

data class StatusChangedEvent(
    var isPrepared:Boolean,
    var isPlaying:Boolean,
    var percent:Int = 0
)

// 歌曲变化
data class MetaChangedEvent(
    var music:Music?
)


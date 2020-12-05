package never.give.up.japp.play

import never.give.up.japp.consts.Constants
import never.give.up.japp.data.dao.DaoLitepal
import never.give.up.japp.model.Music
import never.give.up.japp.utils.execute

object PlayQueueLoader {
    const val tag = "PlayQueueLoader"
    /**
     * 获取播放队列
     */
    fun getPlayQueue(): MutableList<Music> {
        return DaoLitepal.getMusicList(Constants.PLAYLIST_QUEUE_ID)
    }

    /**
     * 添加歌曲到歌单
     */
    fun updateQueue(musics:List<Music>) {
        execute {
            clearQueue()
            musics.forEach {
                DaoLitepal.addToPlaylist(it, Constants.PLAYLIST_QUEUE_ID)
            }
        }
    }
    /**
     * 清空播放列表
     */
    fun clearQueue() {
        try {
            DaoLitepal.clearPlaylist(Constants.PLAYLIST_QUEUE_ID)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
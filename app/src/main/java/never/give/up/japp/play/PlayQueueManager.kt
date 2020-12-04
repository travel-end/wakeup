package never.give.up.japp.play

import never.give.up.japp.Japp
import never.give.up.japp.R
import never.give.up.japp.consts.Constants
import never.give.up.japp.utils.SpUtil
import never.give.up.japp.utils.log

/**
 * @By Journey 2020/12/4
 * @Description
 */
object PlayQueueManager {
    /**
     * 播放模式 0：顺序播放，1：单曲循环，2：随机播放
     */
    const val PLAY_MODE_LOOP = 0
    const val PLAY_MODE_REPEAT = 1
    const val PLAY_MODE_RANDOM = 2
    //播放模式
    private var playingModeId = 0
    private val playingMode = arrayOf(R.string.order_play, R.string.single_play, R.string.random_play)

    // 总共多少首歌曲
    private var total = 0
    private var orderList = mutableListOf<Int>()
    private var saveList = mutableListOf<Int>()
    private var randomPosition = 0

    fun updatePlayMode():Int {
        playingModeId = (playingModeId + 1) % 3
        SpUtil.saveValue(Constants.KEY_PLAY_MODE, playingModeId)
        // TODO: 2020/12/4 EventBus

        return playingModeId
    }

    fun getPlayModeId():Int {
        return SpUtil.getInt(Constants.KEY_PLAY_MODE)
    }
    /**
     * 获取播放模式
     */
    fun getPlayMode(): String {
        return Japp.getInstance().getString(playingMode[playingModeId]);
    }

    private fun initOrderList(total: Int) {
        this.total = total
        orderList.clear()
        for (i in 0 until total) {
            orderList.add(i)
        }

        /**
         * 更新
         */
        if (getPlayModeId() == PLAY_MODE_RANDOM) {
            orderList.shuffle()
            randomPosition = 0
            printOrderList(-1)
        }
    }

    /**
     * 获取下一首位置
     *
     * @return isAuto 是否自动下一曲
     */
    fun getNextPosition(isAuto: Boolean?, total: Int, cuePosition: Int): Int {
        if (total == 1) {
            return 0
        }
        initOrderList(total)
        if (playingModeId == PlayQueueManager.PLAY_MODE_REPEAT && isAuto!!) {
            return if (cuePosition < 0) {
                0
            } else {
                cuePosition
            }
        } else if (playingModeId == PlayQueueManager.PLAY_MODE_RANDOM) {
            printOrderList(orderList[randomPosition])
            saveList.add(orderList[randomPosition])
            return orderList[randomPosition]
        } else {
            if (cuePosition == total - 1) {
                return 0
            } else if (cuePosition < total - 1) {
                return cuePosition + 1
            }
        }
        return cuePosition
    }

    /**
     * 获取下一首位置
     *
     * @return isAuto 是否自动下一曲
     */
    fun getPreviousPosition(total: Int, cuePosition: Int): Int {
        if (total == 1) {
            return 0
        }
        getPlayModeId()
        if (playingModeId == PlayQueueManager.PLAY_MODE_REPEAT) {
            return if (cuePosition < 0) {
                0
            } else {
                cuePosition
            }
        } else if (playingModeId == PlayQueueManager.PLAY_MODE_RANDOM) {
            randomPosition = if (saveList.size > 0) {
                saveList.last()
                saveList.removeAt(saveList.lastIndex)
            } else {
                randomPosition--
                if (randomPosition < 0) {
                    randomPosition = total - 1
                }
                orderList[randomPosition]
            }
            printOrderList(randomPosition)
            return randomPosition
        } else {
            if (cuePosition == 0) {
                return total - 1
            } else if (cuePosition > 0) {
                return cuePosition - 1
            }
        }
        return cuePosition
    }

    /**
     * 打印当前顺序
     */
    private fun printOrderList(cur: Int) {
        orderList.toString() + " --- $cur".log()
    }

}
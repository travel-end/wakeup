package never.give.up.japp.consts

/**
 * @By Journey 2020/11/24
 * @Description
 */
object SConsts {
    const val TRACK_WENT_TO_NEXT = 2//下一首
    const val RELEASE_WAKELOCK = 3//播放完成
    const val TRACK_PLAY_ENDED = 4//播放完成
    const val TRACK_PLAY_ERROR = 5//播放出错
    const val PREPARE_ASYNC_UPDATE = 7//PrepareAsync装载进程
    const val PLAYER_PREPARED = 8//mediaplayer准备完成
    const val AUDIO_FOCUS_CHANGE = 12//音频焦点改变
    const val VOLUME_FADE_DOWN = 13//音量改变减少
    const val VOLUME_FADE_UP = 14//音量改变增加
    const val NOTIFICATION_ID =0x123
    const val ACTION_SERVICE = "never.give.up.japp.consts.service"//广播标志
    const val ACTION_NEXT = "never.give.up.japp.consts.notify.next" // 下一首广播标志
    const val ACTION_PREV = "never.give.up.japp.consts.notify.prev" // 上一首广播标志
    const val ACTION_PLAY_PAUSE = "never.give.up.japp.consts.notify.play_state" // 播放暂停广播
    const val ACTION_CLOSE = "never.give.up.japp.consts.notify.close" // 播放暂停广播
    const val ACTION_IS_WIDGET = "ACTION_IS_WIDGET" // 播放暂停广播
    const val ACTION_LYRIC = "never.give.up.japp.consts.notify.lyric" // 播放暂停广播
    const val PLAY_STATE_CHANGED = "never.give.up.japp.consts.play_state" // 播放暂停广播
    const val PLAY_STATE_LOADING_CHANGED = "never.give.up.japp.consts.play_state_loading" // 播放loading
    const val SHUTDOWN = "never.give.up.japp.consts.shutdown"
    const val PLAY_QUEUE_CLEAR = "never.give.up.japp.consts.play_queue_clear" //清空播放队列
    const val PLAY_QUEUE_CHANGE = "never.give.up.japp.consts.play_queue_change" //播放队列改变
    const val META_CHANGED = "never.give.up.japp.consts.metachanged" //状态改变(歌曲替换)
    const val CMD_TOGGLE_PAUSE = "toggle_pause" //按键播放暂停
    const val CMD_NEXT = "next" //按键下一首
    const val CMD_PREVIOUS = "previous" //按键上一首
    const val CMD_PAUSE = "pause" //按键暂停
    const val CMD_PLAY = "play" //按键播放
    const val CMD_STOP = "stop" //按键停止
    const val CMD_FORWARD = "forward" //按键停止
    const val CMD_REWIND = "reward" //按键停止
    const val SERVICE_CMD = "cmd_service" //状态改变
    const val FROM_MEDIA_BUTTON = "media" //状态改变
    const val CMD_NAME = "name" //状态改变
    const val UNLOCK_DESKTOP_LYRIC = "unlock_lyric" //音量改变增加
}
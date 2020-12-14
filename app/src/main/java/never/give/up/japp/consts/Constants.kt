package never.give.up.japp.consts

/**
 * @By Journey 2020/12/2
 * @Description
 */
object Constants {
    /**
     * config
     */
    const val SINGER_PIC_BASE_URL = "http://music.163.com/"
    // Fiddler抓包qq音乐网站后的地址
    const val FIDDLER_BASE_QQ_URL = "https://c.y.qq.com/"
    //获取播放地址的baseUrl
    const val FIDDLER_BASE_SONG_URL = "https://u.y.qq.com/"
    // 古诗文网
    const val GUSHI_BASE_URL = "https://app.gushiwen.cn/api/"
    const val BASE_NETEASE_URL = "http://musiclake.leanapp.cn"
    const val ALBUM_PIC="http://y.gtimg.cn/music/photo_new/T002R180x180M000"
    const val JPG=".jpg"
    const val SONG_URL_DATA_LEFT="%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22"
    const val SONG_URL_DATA_RIGHT="%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%221443481947%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D"

    /**
     * key
     */
    const val KEY_SEARCH_CONTENT = "key_search_content"// 搜索内容
    const val KEY_PLAY_MODE = "key_play_mode"
    const val KEY_PLAY_POSITION = "key_play_position"
    const val KEY_POSITION = "key_position"
    const val KEY_MUSIC_ID = "key_music_id"
    const val KEY_FIRST_LAUNCH = "key_first_launch"
    const val KEY_SONG_QUALITY = "key_song_quality"
    const val KEY_MUSIC_TYPE = "key_music_type"
    const val KEY_IS_CACHE = "key_is_cache"

    // config
    const val SEARCH_SONG_PAGE_SIZE = 20//每页搜索（单曲、专辑）显示的条数

    // flag
    const val LOCAL = "local"
    const val QQ = "qq"
    const val XIAMI = "xiami"
    const val BAIDU = "baidu"
    const val NETEASE = "netease"
    const val DEFAULT_NOTIFICATION = "notification"
    const val IS_URL_HEADER = "http"

    //特殊歌单类型
    const val PLAYLIST_LOVE_ID = "love"
    const val PLAYLIST_HISTORY_ID = "history"
    const val PLAYLIST_LOCAL_ID = "local"
    const val PLAYLIST_QUEUE_ID = "queue"
    const val PLAYLIST_DOWNLOAD_ID = "download"

    // 在线歌单
    const val PLAYLIST_CUSTOM_ID = "custom_online"
    //百度歌单
    const val PLAYLIST_BD_ID = "playlist_bd"
    //网易云歌单
    const val PLAYLIST_WY_ID = "playlist_wy"
    const val PLAYLIST_WY_RECOMMEND_ID = "playlist_wy_recommend_songs"
    //QQ歌单
    const val PLAYLIST_QQ_ID = "playlist_qq"
    //虾米歌单
    const val PLAYLIST_XIA_MI_ID = "playlist_xm"
}
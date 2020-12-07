package never.give.up.japp.net

import com.cyl.musicapi.BaseApiImpl
import never.give.up.japp.consts.Constants
import never.give.up.japp.utils.execute
import never.give.up.japp.utils.log

/**
 * @By Journey 2020/12/7
 * @Description
 */
object MusicApiServiceImpl {

    // 获取播放地址（）
    fun getMusicUrl(vendor: String, mid: String, br: Int = 128000): String? {
        var url:String?=null
        execute {
            BaseApiImpl.getSongUrl(vendor, mid, br, {
                if (it.status) {
                    url =
                        if (vendor == Constants.XIAMI) {
                            if (it.data.url.startsWith("http")) it.data.url
                            else "http:${it.data.url}"
                        } else it.data.url

                } else {
                    "----MusicApiServiceImpl---onError:获取播放地址异常${it.msg}".log()
                }
            },{})
        }
        return url
    }
}
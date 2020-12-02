package never.give.up.japp.utils

import com.danikula.videocache.file.Md5FileNameGenerator

/**
 * 作者：yonglong
 * 包名：com.cyl.musiclake.player.cache
 * 时间：2019/7/23 19:48
 * 描述：处理相同歌曲不同的播放地址，生成同一个缓存文件名
 */
class CacheFileNameGenerator : Md5FileNameGenerator() {
    override fun generate(url: String): String {
        val len = url.split("/".toRegex()).toTypedArray().size
        //分割"/"获得xxx.mp3... 字符串
        val newUrl =
            url.split("/".toRegex()).toTypedArray()[len - 1].replace(".mp3", "")
        //分割"?"获得xxx.mp3... 字符串
        val newUrl1 = newUrl.split("\\?".toRegex()).toTypedArray()[0]
        //        LogUtil.e("MusicPlayerEngine", "cache oldUrl =" + url);
//        LogUtil.d("MusicPlayerEngine", "cache newUrl =" + newUrl);
//        LogUtil.d("MusicPlayerEngine", "cache newUrl1 =" + newUrl1);
        return super.generate(newUrl1)
    }
}
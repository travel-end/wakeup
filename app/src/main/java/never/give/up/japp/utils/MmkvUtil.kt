package never.give.up.japp.utils

import com.tencent.mmkv.MMKV

/**
 * @By Journey 2020/12/2
 * @Description
 */
object MmkvUtil {
    private var kv = MMKV.defaultMMKV()

    fun clearAll() {
        kv.clearAll()
    }

    fun encodeString(key:String,value:String) {
        kv.encode(key,value)
    }
    fun decodeString(key:String):String {
        return kv.decodeString(key)
    }
}
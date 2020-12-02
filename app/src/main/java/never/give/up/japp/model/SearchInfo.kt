package never.give.up.japp.model

import com.google.gson.annotations.SerializedName

/**
 * @By Journey 2020/12/2
 * @Description
 */
data class SearchInfo(
    @SerializedName("result")
    val result: HotItems?,
    @SerializedName("code")
    val code: Int = 0
)

data class HotItems(
    val hots: List<HotItem>?
)

data class HotItem(
    val first: String?,
    val second: String?,
    val third: String?,
    val iconType: String?
)
package never.give.up.japp.model

import org.litepal.crud.LitePalSupport


/**
 * @By Journey 2020/10/27
 * @Description
 */
data class HistoryTag(
    val id: Int? = null,
    val name: String? = null,
    val tagId: String? = null
):LitePalSupport()

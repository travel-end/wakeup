package never.give.up.japp.model


/**
 * @By Journey 2020/12/2
 * @Description
 */
data class RecommendSearch(
    val id:Int?=null,
    val tagName:String,
    val tagId:Int
)
data class RecommendSearchList(
    val recommendSearch:List<RecommendSearch>
)
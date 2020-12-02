package never.give.up.japp.net

import never.give.up.japp.model.SearchInfo
import retrofit2.http.GET

/**
 * @By Journey 2020/12/2
 * @Description
 */
interface ApiService {

    @GET("search/hot")
    suspend fun getHotSearchInfo(): SearchInfo
}
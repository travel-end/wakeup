//package never.give.up.japp.data.dao
//
//import androidx.room.*
//import never.give.up.japp.model.HistoryTag
//
///**
// * @By Journey 2020/11/11
// * @Description
// */
//@Dao
//interface SearchSongDao {
//    @Insert(entity = HistoryTag::class, onConflict = OnConflictStrategy.REPLACE)
//    suspend fun addOneSearchTag(tag: HistoryTag): Long
//
//    @Query("SELECT * FROM t_search_history ORDER BY id DESC LIMIT(:count)")
//    suspend fun findRecentSearchHistory(count: Int): List<HistoryTag>
//
//    @Query("SELECT * FROM t_search_history WHERE name=(:tagName) ORDER BY id")
//    suspend fun findSearchTagByName(tagName: String): List<HistoryTag>
//
//    @Query("DELETE FROM t_search_history")
//    suspend fun deleteSearchHistoryTag(): Int
//
//}
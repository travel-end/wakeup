package never.give.up.japp.data

import androidx.room.Room
import androidx.room.RoomDatabase
import never.give.up.japp.Japp
import never.give.up.japp.data.dao.SearchSongDao

/**
 * @By Journey 2020/12/2
 * @Description
 */
abstract class JAppDatabase : RoomDatabase() {
    abstract val searchDao:SearchSongDao
    companion object {
        @Volatile
        private var instance: JAppDatabase? = null
        fun getInstance() = instance ?: synchronized(JAppDatabase::class.java) {
            instance ?: buildDatabase().also { instance = it }
        }

        private fun buildDatabase(): JAppDatabase =
            Room.databaseBuilder(Japp.getInstance(), JAppDatabase::class.java, "JAppDatabase.db")
                .allowMainThreadQueries().build()
    }
}
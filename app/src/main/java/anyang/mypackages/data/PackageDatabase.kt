package anyang.mypackages.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PackageEntity::class], version = 1)
abstract class PackageDatabase : RoomDatabase() {
    abstract fun packageDao(): PackageDao

    companion object {
        @Volatile
        private var INSTANCE: PackageDatabase? = null

        fun getDatabase(context: Context): PackageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PackageDatabase::class.java,
                    "package_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

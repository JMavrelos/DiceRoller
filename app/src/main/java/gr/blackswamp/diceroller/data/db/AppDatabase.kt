package gr.blackswamp.diceroller.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import gr.blackswamp.damagereports.data.db.converters.UUIDConverter

@Database(entities = [DieSetEntity::class], version = 1)
@TypeConverters(UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val dieSetDao: DieSetDao
}
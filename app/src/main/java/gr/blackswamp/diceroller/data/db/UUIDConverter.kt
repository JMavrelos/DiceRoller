package gr.blackswamp.damagereports.data.db.converters

import androidx.room.TypeConverter
import java.util.*

class UUIDConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun toUUID(text: String?) = text?.let { UUID.fromString(it) }

        @TypeConverter
        @JvmStatic
        fun toText(uuid: UUID?) = uuid?.toString()
    }
}
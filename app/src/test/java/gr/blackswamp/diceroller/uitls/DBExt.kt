package gr.blackswamp.diceroller.uitls

import androidx.annotation.VisibleForTesting
import androidx.room.RoomDatabase

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun RoomDatabase.count(table: String): Int {
    this.query("select count(*) from $table ", null).use { cursor ->
        cursor.moveToFirst()
        return cursor.getInt(0)
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun RoomDatabase.countWhere(table: String, condition: String): Int {
    this.query("select count(*) from $table where $condition", null).use { cursor ->
        cursor.moveToFirst()
        return cursor.getInt(0)
    }
}

fun RoomDatabase.get(query: String): String {
    this.query(query, null).use { cursor ->
        cursor.moveToFirst()
        return cursor.getString(0)
    }
}

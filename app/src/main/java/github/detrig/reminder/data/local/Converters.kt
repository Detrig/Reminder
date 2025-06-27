package github.detrig.reminder.data.local

import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    private val type = object : TypeToken<List<DayTimePair>>() {}.type

    @TypeConverter
    fun fromDaysWithTime(set: Set<Pair<Int, String>>): String {
        val list = set.map { DayTimePair(it.first, it.second) }
        return gson.toJson(list)
    }

    @TypeConverter
    fun toDaysWithTime(json: String): Set<Pair<Int, String>> {
        val list: List<DayTimePair> = gson.fromJson(json, type) ?: emptyList()
        return list.map { it.day to it.time }.toSet()
    }
}

data class DayTimePair(
    val day: Int,
    val time: String
)
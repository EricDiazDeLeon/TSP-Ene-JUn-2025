package com.example.mirutadigital.data.local

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLatLngListToJson(latLngList: List<LatLng>?): String? {
        return latLngList?.let { list ->
            val latLngData = list.map { LatLngData(it.latitude, it.longitude) }
            gson.toJson(latLngData)
        }
    }

    @TypeConverter
    fun fromJsonToLatLngList(json: String?): List<LatLng>? {
        return json?.let {
            val type = object : TypeToken<List<LatLngData>>() {}.type
            val latLngDataList: List<LatLngData> = gson.fromJson(it, type)
            latLngDataList.map { data -> LatLng(data.latitude, data.longitude) }
        }
    }

    private data class LatLngData(
        val latitude: Double,
        val longitude: Double
    )
}

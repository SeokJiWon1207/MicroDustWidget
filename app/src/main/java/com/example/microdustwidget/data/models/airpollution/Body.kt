package com.example.microdustwidget.data.models.airpollution


import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("items")
    val airPollutionValues: List<AirPollutionValues>?,
    @SerializedName("numOfRows")
    val numOfRows: Int?,
    @SerializedName("pageNo")
    val pageNo: Int?,
    @SerializedName("totalCount")
    val totalCount: Int?
)
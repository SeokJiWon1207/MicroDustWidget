package com.example.microdustwidget.data.models.airpollution


import com.google.gson.annotations.SerializedName

data class AirPollutionResponse(
    @SerializedName("response")
    val response: Response?
)
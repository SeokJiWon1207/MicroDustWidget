package com.example.microdustwidget.data.service

import com.example.microdustwidget.BuildConfig
import com.example.microdustwidget.data.models.monitoringstation.MonitoringStationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AirKoreaApiService {

    @GET("B552584/MsrstnInfoInqireSvc/getMsrstnList" +
        "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
        "&returnType=json"
    )
    suspend fun getNearbyMonitoringStation(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ): Response<MonitoringStationsResponse>
}
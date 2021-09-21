package com.example.microdustwidget.data

import com.example.microdustwidget.BuildConfig
import com.example.microdustwidget.data.Url.AIRKOREA_API_BASE_URL
import com.example.microdustwidget.data.Url.KAKAO_API_BASE_URL
import com.example.microdustwidget.data.models.monitoringstation.MonitoringStation
import com.example.microdustwidget.data.service.AirKoreaApiService
import com.example.microdustwidget.data.service.KakaoApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {

    // 가장 가까운 측정소
    suspend fun getNearbyMonitoringStation(latitude: Double, longitude: Double): MonitoringStation? {
        val tmCoordinates = kakaoApiService
            .getTmCoordinates(longitude, latitude)
            .body()
            ?.documents
            ?.firstOrNull()

        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y

        return airKoreaApiService
            .getNearbyMonitoringStation(tmX!!, tmY!!)
            .body()
            ?.response
            ?.body
            ?.monitoringStations
            ?.minByOrNull {
                it.tm ?: Double.MAX_VALUE
            }
    }


    private val kakaoApiService: KakaoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private val airKoreaApiService: AirKoreaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AIRKOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            ).build()
}
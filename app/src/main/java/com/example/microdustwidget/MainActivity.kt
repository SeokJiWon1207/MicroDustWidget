package com.example.microdustwidget

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.microdustwidget.data.Repository
import com.example.microdustwidget.data.models.airpollution.AirPollutionValues
import com.example.microdustwidget.data.models.airpollution.Grade
import com.example.microdustwidget.data.models.monitoringstation.MonitoringStation
import com.example.microdustwidget.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_BACKGROUND_ACCESS_PERMISSION = 100
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    private var scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isInstallGooglePlayService()) installPlayService(this)

        bindView()
        initVariable()
        requsetLocationPermission()
    }


    // 마지막에 저장된 위치 정보
    private fun initVariable() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun bindView() {
        binding.refresh.setOnRefreshListener {
            fetchAirData()
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!locationPermissionGranted) {
                finish()
            } else {
                val backgroundLocationPermissionGranted =
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                val shouldShowBackgroundPermissionRationale =
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                if (!backgroundLocationPermissionGranted && shouldShowBackgroundPermissionRationale) {
                    showBackgroundLocationPermissionRationaleDialog()
                } else {
                    fetchAirData()
                }
            }
        } else {
            if (!locationPermissionGranted) {
                finish()
            } else {
                fetchAirData()
            }
        }
    }

    // 앱 시작과 동시에 권한 요청
    private fun requsetLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                // ACCESS_COARSE_LOCATION : 도시 Block 단위의 정밀도의 위치 정보를 얻을 수 있습니다.
                // ACCESS_FINE_LOCATION : ACCESS_COARSE_LOCATION보다 더 정밀한 위치 정보를 얻을 수 있습니다.
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            REQUEST_BACKGROUND_ACCESS_PERMISSION
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showBackgroundLocationPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("홈 위젯을 사용하려면 위치 접근 권한이 ${packageManager.backgroundPermissionOptionLabel} 상태여야 합니다.")
            .setPositiveButton("설정하기") { dialog, _ ->
                requestBackgroundLocationPermission()
                dialog.dismiss()
            }
            .setNegativeButton("그냥두기") { dialog, _ ->
                fetchAirData()
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirData() {
        cancellationTokenSource = CancellationTokenSource()

        // LocationService는 위치 정보를 캐시하며,
        // lastLocation.addOnSuccessListener()으로 마지막에 캐시된 위치 정보를 가져옵니다.
        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->
            scope.launch {
                try {
                    val monitoringStation =
                        Repository.getNearbyMonitoringStation(location.latitude, location.longitude)

                    val airPollutionValue =
                        Repository.getLatestAirPollution(monitoringStation!!.stationName!!)

                    displayAirPollution(monitoringStation, airPollutionValue!!)
                } catch (e: Exception) {
                    binding.errorDescriptionTextView.visibility = View.VISIBLE
                    binding.contentsLayout.alpha = 0F
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.refresh.isRefreshing = false
                }
            }
        }
    }

    fun displayAirPollution(
        monitoringStation: MonitoringStation,
        airPollutionValues: AirPollutionValues
    ) {
        binding.contentsLayout.animate()
            .alpha(1F)
            .start()

        binding.measuringStationNameTextView.text = monitoringStation.stationName
        binding.measuringStationAddressTextView.text = monitoringStation.addr

        (airPollutionValues.khaiGrade ?: Grade.UNKNOWN).let {
            binding.root.setBackgroundResource(it.colorResId)
            binding.totalGradeLabelTextView.text = it.label
            binding.totalGradleEmojiTextView.text = it.emoji
        }

        with(airPollutionValues) {
            binding.fineDustInformationTextView.text =
                "미세먼지: $pm10Value ㎍/㎥ ${(pm10Grade ?: Grade.UNKNOWN).emoji}"
            binding.ultraFineDustInformationTextView.text =
                "초미세먼지: $pm25Value ㎍/㎥ ${(pm25Grade ?: Grade.UNKNOWN).emoji}"

            with(binding.so2Item) {
                labelTextView.text = "아황산가스"
                gradeTextView.text = (so2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$so2Value ppm"
            }

            with(binding.coItem) {
                labelTextView.text = "일산화탄소"
                gradeTextView.text = (coGrade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$coValue ppm"
            }

            with(binding.o3Item) {
                labelTextView.text = "오존"
                gradeTextView.text = (o3Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$o3Value ppm"
            }

            with(binding.no2Item) {
                labelTextView.text = "이산화질소"
                gradeTextView.text = (no2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$no2Value ppm"
            }
        }
    }

    private fun isInstallGooglePlayService(): Boolean {
        var services = false

        try {
            // 설치가 되어 있는 경우
            packageManager.getApplicationInfo("com.google.android.gms", 0)
            services = true
        } catch (e: PackageManager.NameNotFoundException) {
            // 설치가 되어 있지 않은 경우
            services = false
        }
        return services

    }

    private fun installPlayService(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Google Play Service")
            .setMessage("Required Google Play Service")
            .setCancelable(false)
            .setPositiveButton("Install") { dialog, id ->
                dialog.dismiss()
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")
                    )
                    intent.setPackage("com.android.vending")
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")
                    )
                    startActivity(intent)
                }
            }.setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.let { it.cancel() }
        scope.cancel()
    }

}
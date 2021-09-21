package com.example.microdustwidget

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.microdustwidget.data.Repository
import com.example.microdustwidget.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    private var scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!isInstallGooglePlayService()) installPlayService(this)


        initVariable()
        requsetLocationPermission()

    }


    // 마지막에 저장된 위치 정보
    private fun initVariable() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionGranted =
            requestCode == REQUEST_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (!locationPermissionGranted) {
            finish() // 권한 거부시 종료
        } else {
            // fetchData
            fetchAirData()
        }
    }

    // 앱 시작과 동시에 권한 요청
    private fun requsetLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                // ACCESS_COARSE_LOCATION : 도시 Block 단위의 정밀도의 위치 정보를 얻을 수 있습니다.
                // ACCESS_FINE_LOCATION : ACCESS_COARSE_LOCATION보다 더 정밀한 위치 정보를 얻을 수 있습니다.
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
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
                val monitoringstation =
                    Repository.getNearbyMonitoringStation(location.latitude, location.longitude)

                binding.textView.text = monitoringstation?.stationName
            }
        }
    }

    private fun isInstallGooglePlayService(): Boolean {
        var services = false

        try {
            // 설치가 되어 있는 경우
            packageManager.getApplicationInfo("com.google.android.gms", 0)
            services = true
        } catch(e: PackageManager.NameNotFoundException) {
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
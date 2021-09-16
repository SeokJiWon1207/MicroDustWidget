package com.example.microdustwidget

import android.Manifest
import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.microdustwidget.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVariable()
        requsetLocationPermission()

    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.let { it.cancel() }
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
            cancellationTokenSource = CancellationTokenSource()

            // LocationService는 위치 정보를 캐시하며,
            // lastLocation.addOnSuccessListener()으로 마지막에 캐시된 위치 정보를 가져옵니다.
            fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            ).addOnSuccessListener { location ->
                binding.textView.text = "${location.latitude}, ${location.latitude}"
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
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }
}
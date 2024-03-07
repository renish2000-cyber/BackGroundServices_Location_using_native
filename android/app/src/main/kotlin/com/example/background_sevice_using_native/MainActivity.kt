package com.example.background_sevice_using_native

import LocationEvent
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : FlutterActivity() {

    // native channel to bind flutter
    private val CHANNEL = "background_chanel"


    /*
    * Event channel variable
    * */
    private var mEventSink: EventChannel.EventSink? = null
    var EVENTCHANNEL = "background_chanel/eventChannel"

    /*
    * Required variable
    * */
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val PERMISSION_ID = 1010
    lateinit var methodChannel: MethodChannel
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "SuspiciousIndentation")
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "getCurrentLocation") {
                if (checkPermission()) {
                    val binaryMessenger: BinaryMessenger =
                        flutterEngine.dartExecutor.binaryMessenger
                    methodChannel = MethodChannel(binaryMessenger, CHANNEL)
                    val serviceIntent = Intent(this, locationService::class.java)
                    startService(serviceIntent)
                } else {
                    requestPermission()
                }
            } else {
                result.notImplemented()
            }
        }
        /*
        * Register Event channel
        * */
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENTCHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink) {
                    // Start sending continuous data here
                    // You can use a timer, sensor data, etc.
                    mEventSink = eventSink
                }

                override fun onCancel(arguments: Any?) {
                    // Stop sending continuous data here
                    mEventSink = null
                }
            })


    }


    /*
    * this function will return a boolean
    * this function is used to check permission given in manifiest
    * */
    fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false

    }

    /*
    * after check permission if it is not there then this method call to request permission
    * */
    fun requestPermission() {
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun receiveLocationEvent(locationEvent: LocationEvent) {
        println("location receiver${locationEvent.latitude}")
        println("location receiver${locationEvent.longitude}")
        val data = mapOf(
            "latitude" to locationEvent.latitude,
            "longitude" to locationEvent.longitude
        )
        sendContinuousData(data)
    }

    // Simulate continuous data, you can replace this with your actual data source
    private fun sendContinuousData(data: Map<String, Double?>) {
        mEventSink?.success(data)
    }
}
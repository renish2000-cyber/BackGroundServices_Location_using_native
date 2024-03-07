package com.example.background_sevice_using_native

import LocationEvent
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterJNI
import org.greenrobot.eventbus.EventBus

class locationService: Service() {


   // private var methodChannel: MethodChannel? = null



    override fun onBind(intent: Intent): IBinder? = null


    /*
        * variable used for get location
        * */
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location?=null

    /*
    * create variable for send data to ui using method channel
    * */

    /*
    * This variable used to display notification when service is start
    * */
    private var notificationManager: NotificationManager? = null

    /*
    * static variable taken for channel and notification id
    * */
     val CHANNEL_ID = "12345"
     val NOTIFICATION_ID=12345


/*    val flutterJNI = FlutterJNI()
    val channelName = "background_chanel"
    val flutterEngine = FlutterEngine(applicationContext)*/

    //  val binaryMessenger: BinaryMessenger = DartExecutor(flutterJNI, applicationContext.assets)

    /*
    * This method is an entry point in service life cycle
    * execute only one time
    * */
    override fun onCreate() {
        super.onCreate()

        // initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // create lcoationRequest on interval of 1 second
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()


        /*
        *  This method is a call back function when location get
        *  This method bind with another method (onNewLocation) every interval this method called with location
        * */
        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                onNewLocation(locationResult)
            }
        }

        // initialize notificationManager
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // create channel version is greater than
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, "locations", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        // start foreground service to run service on background
        startForeground(NOTIFICATION_ID,getNotification())
    }

    /*
    * This method called after onCreate method
    * Work of this method to get location or call method which is used to get location
    * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    /*
    * This Method create request to get location
    * This method actually bind locationCallback & locationRequest
    * */
    @Suppress("MissingPermission")
    fun createLocationRequest(){
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!,locationCallback!!,null
            )
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    /*
    * When location get this method called
    *  */
    @SuppressLint("SuspiciousIndentation")
    private fun onNewLocation(locationResult: LocationResult) {

        location = locationResult.lastLocation

        EventBus.getDefault().post(
            LocationEvent(
                latitude = location?.latitude,
                longitude = location?.longitude
            )
        )
    }

    /*
    * create notification
    * */
    fun getNotification(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Background Location Services")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            notification.setChannelId(CHANNEL_ID)
        }
        return notification.build()
    }


    /*
    * this method is override method call when service destroy
    * */
    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }

    /*
    * when destroy method call at that time
    * */
    private fun removeLocationUpdates(){
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

}


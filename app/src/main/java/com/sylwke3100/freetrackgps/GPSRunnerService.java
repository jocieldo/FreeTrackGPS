package com.sylwke3100.freetrackgps;


import android.app.Activity;
import android.app.Service;
import android.content.*;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GPSRunnerService extends Service {
    public static final String ACTION = "GPSRunnerService";
    private final IBinder mBinder = new LocalBinder();
    private BroadcastReceiver gPSRunnerServiceReceiver;
    private RouteManager currentRoute;
    private SharedPreferences sharedPrefs;
    private boolean gpsCurrentStatus = false;
    private GPSRunnerServiceMessageController messageController;
    private LocationManager service;

    private void onCreateGPSConnection() {
        if (service != null) {
            service.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Integer.valueOf(sharedPrefs.getString("time", DefaultValues.defaultMinSpeedIndex)),
                    Integer.valueOf(
                            sharedPrefs.getString("distance", DefaultValues.defaultMinDistanceIndex)),
                    new GPSListener(currentRoute, this));
            messageController.sendMessageToGUI(MainActivityReceiver.COMMANDS.GPS_ON);
            Location lastLocation =
                    (Location) service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            gpsCurrentStatus = true;
            if (lastLocation != null) {
                if (currentRoute.getStatus() != DefaultValues.routeStatus.stop) {
                    Intent gpsPosIntent = new Intent();
                    gpsPosIntent.putExtra("lat", lastLocation.getLatitude());
                    gpsPosIntent.putExtra("lon", lastLocation.getLongitude());
                    messageController.sendMessageToGUI(MainActivityReceiver.COMMANDS.GPS_POS, gpsPosIntent);
                }
            }
        } else {
            messageController.sendMessageToGUI(MainActivityReceiver.COMMANDS.GPS_OFF);
            service.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Integer.valueOf(sharedPrefs.getString("time", DefaultValues.defaultMinSpeedIndex)),
                    Integer.valueOf(
                            sharedPrefs.getString("distance", DefaultValues.defaultMinDistanceIndex)),
                    new GPSListener(currentRoute, this));
        }
        Intent message = new Intent();
        message.putExtra("dist", "0,0");
        messageController.sendMessageToGUI(MainActivityReceiver.COMMANDS.WORKOUT_DISTANCE, message);
    }

    public void onCreate() {
        messageController = new GPSRunnerServiceMessageController(this);
        currentRoute = new RouteManager(this);
        service = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPrefs = this.getSharedPreferences(DefaultValues.prefs, Activity.MODE_PRIVATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(new GPSRunnerServiceReceiver(currentRoute, this), intentFilter);
        onCreateGPSConnection();
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GPSRunnerService", "Received start id " + startId + ": " + intent);
        return START_STICKY_COMPATIBILITY;
    }

    public void onDestroy() {
        Toast.makeText(this, "Service Stop", Toast.LENGTH_SHORT).show();
        unregisterReceiver(gPSRunnerServiceReceiver);
        if (currentRoute.getStatus() != DefaultValues.routeStatus.stop)
            currentRoute.stop();
        super.onDestroy();
    }


    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public static class SERVICE_ACTION {
        public static final int WORKOUT_STOP = 0;
        public static final int WORKOUT_START = 1;
        public static final int WORKOUT_PAUSE = 2;
        public static final int WORKOUT_STATUS = 3;
        public static final int WORKOUT_UNPAUSE = 4;
        public static final int WORKOUT_ID = 5;
    }


    public class LocalBinder extends Binder {
        GPSRunnerService getService() {
            return GPSRunnerService.this;
        }
    }

}

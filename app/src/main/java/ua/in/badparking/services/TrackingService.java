package ua.in.badparking.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import ua.in.badparking.receivers.UserLocationListener;
import ua.in.badparking.utils.LogHelper;

public class TrackingService extends Service implements Handler.Callback {
    public static final long WAITING_TIME_MILLIS = 3000L;
    public static final float ACCURANCY_IN_METERS = 3f;

    //Start tracking service
    public final static String ACTION_START_MONITORING = "ua.in.badparking.START_MONITORING";
    //Stop tracking service
    public final static String ACTION_STOP_MONITORING = "ua.in.badparking.STOP_MONITORING";
    private final static String HANDLER_THREAD_NAME = "MyLocationThread";

    private LocationListener listener;
    private Looper looper;
    private android.os.Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread(HANDLER_THREAD_NAME);
        thread.start();

        looper = thread.getLooper();
        handler = new Handler(looper, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        doStopTracking();

        if (looper != null)
            looper.quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String threadId = LogHelper.threadId();
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "Location Monitoring Service onStartCommand - " + threadId);

        handler.sendMessage(handler.obtainMessage(0, intent));
        return START_STICKY;
    }

    public boolean handleMessage(Message message) {
        String threadId = LogHelper.threadId();
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "Location Monitoring Service onStartCommand - " + threadId);

        Intent intent = (Intent)message.obj;

        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        Log.d(LogHelper.LOCATION_MONITORING_TAG, "Location Service onStartCommand Action:" + action);

        if (action.equals(ACTION_START_MONITORING)) {
            doStartTracking();
        } else if (action.equals(ACTION_STOP_MONITORING)) {
            doStopTracking();
            stopSelf();
        }

        return true;
    }

    private void doStartTracking() {
        doStopTracking();

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        listener = new UserLocationListener(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission logic
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, WAITING_TIME_MILLIS, ACCURANCY_IN_METERS, listener, looper);
    }

    private void doStopTracking() {
        if (listener != null) {
            LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //permission logic
            }

            lm.removeUpdates(listener);
            listener = null;
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}

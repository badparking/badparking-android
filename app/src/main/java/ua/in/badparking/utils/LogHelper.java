package ua.in.badparking.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import com.crashlytics.android.Crashlytics;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Dima Kovalenko on 10/5/16.
 */
public class LogHelper {
    private static final String _timeStampFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String _timeStampTimeZoneId = "UTC";
    public  static final String LOCATION_MONITORING_TAG = "Location Monitoring";
    private static final boolean DEBUG = false;

    /**
     * Log an error. If not in debug mode, the error will be logged to Crashlytics.
     *
     * @param tag     Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The message to log.
     */
    public static void e(String tag, String message) {
        if (DEBUG) {
            android.util.Log.e(tag, message);
        } else {
            Crashlytics.log(tag + ": " + message);
        }
    }

    /**
     * Log an error with an exception. If not in debug mode, the error will be logged to Crashlytics.
     *
     * @param tag       Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message   The message to log.
     * @param exception The exception which was thrown, and should be logged.
     */
    public static void e(String tag, String message, Throwable exception) {
        if (DEBUG) {
            android.util.Log.e(tag, message, exception);
        } else {
            if (exception instanceof ConnectException) {
                return;
            }
            Exception wrapperException = new Exception(tag + ": " + message, exception);
            Crashlytics.logException(wrapperException);
        }
    }

    public static String formatLocationInfo(String provider, double lat, double lng, float accuracy, long time) {
//        SimpleDateFormat timeStampFormatter = new SimpleDateFormat(_timeStampFormat);
//        timeStampFormatter.setTimeZone(TimeZone.getTimeZone(_timeStampTimeZoneId));

        String timeStamp = formatTime(time);

        String logMessage = String.format("%s | lat/lng=%f/%f | accuracy=%f | Time=%s",
                provider, lat, lng, accuracy, timeStamp);

        return logMessage;
    }

    public static String formatTime(long time) {
        SimpleDateFormat timeStampFormatter = new SimpleDateFormat(_timeStampFormat);
        timeStampFormatter.setTimeZone(TimeZone.getTimeZone(_timeStampTimeZoneId));

        return timeStampFormatter.format(time);

    }

    public static String formatCalendar(Calendar when) {
        return formatTime(when.getTimeInMillis());
//        SimpleDateFormat timeStampFormatter = new SimpleDateFormat(_timeStampFormat);
//        timeStampFormatter.setTimeZone(TimeZone.getTimeZone(_timeStampTimeZoneId));
//
//        return timeStampFormatter.format(when);
//
    }

    public static String formatLocationInfo(Location location) {
        String provider = location.getProvider();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();

        return LogHelper.formatLocationInfo(provider, lat, lng, accuracy, time);
    }

    public static String formationLocationProvider(Context context, LocationProvider provider) {
        String name = provider.getName();
        int horizontalAccuracy = provider.getAccuracy();
        int powerRequirements = provider.getPowerRequirement();
        boolean hasMonetaryCost = provider.hasMonetaryCost();
        boolean requiresCell = provider.requiresCell();
        boolean requiresNetwork = provider.requiresNetwork();
        boolean requiresSatellite = provider.requiresSatellite();
        boolean supportsAltitude = provider.supportsAltitude();
        boolean supportsBearing = provider.supportsBearing();
        boolean supportsSpeed = provider.supportsSpeed();

        String enabledMessage = "UNKNOWN";
        if (context != null) {
            LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            enabledMessage = yOrN(lm.isProviderEnabled(name));
        }

        String horizontalAccuracyDisplay = translateAccuracyFineCourse(horizontalAccuracy);
        String powerRequirementsDisplay = translatePower(powerRequirements);

        String logMessage = String.format("%s | enabled:%s | horizontal accuracy:%s | power:%s | " +
                        "cost:%s | uses cell:%s | uses network:%s | uses satellite:%s | " +
                        "has altitude:%s | has bearing:%s | has speed:%s |",
                name, enabledMessage, horizontalAccuracyDisplay, powerRequirementsDisplay,
                yOrN(hasMonetaryCost), yOrN(requiresCell), yOrN(requiresNetwork), yOrN(requiresSatellite),
                yOrN(supportsAltitude), yOrN(supportsBearing),yOrN(supportsSpeed));

        return logMessage;
    }

    public static String threadId() {
        long id = Thread.currentThread().getId();

        return String.format("Thread ID:%d", id);
    }

    public  static String yOrN(boolean value) {
        return value ? "Y" : "N";
    }

    public static String translateStatus(int value) {
        String message = "UNDEFINED";
        switch(value) {
            case LocationProvider.AVAILABLE:
                message = "AVAILABLE";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                message = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                message = "TEMPORARILY_UNAVAILABLE";
                break;
        }

        return message;
    }

    public static String translateAccuracyFineCourse(int value) {
        String message = "UNDEFINED";
        switch(value) {
            case Criteria.ACCURACY_COARSE:
                message = "COARSE";
                break;
            case Criteria.ACCURACY_FINE:
                message = "FINE";
                break;

        }
        return message;
    }

    public static String translateAccuracyHighMediumLow(int value) {
        String message = "UNDEFINED";
        switch(value) {
            case Criteria.ACCURACY_HIGH:
                message = "HIGH";
                break;
            case Criteria.ACCURACY_MEDIUM:
                message = "MEDIUM";
                break;
            case Criteria.ACCURACY_LOW:
                message = "LOW";
                break;
        }

        return message;
    }

    public static String translatePower(int value) {
        String message = "NO_REQUIREMENT";
        switch(value) {
            case Criteria.POWER_HIGH:
                message = "HIGH";
                break;
            case Criteria.POWER_MEDIUM:
                message = "MEDIUM";
                break;
            case Criteria.POWER_LOW:
                message = "LOW";
                break;
        }

        return message;
    }

    public static String translateGpsEvent(int value) {
        String message = "UNDEFINED";
        switch(value) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                message = "GPS_EVENT_FIRST_FIX";
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                message = "GPS_EVENT_SATELLITE_STATUS";
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                message = "GPS_EVENT_STARTED";
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                message = "GPS_EVENT_STOPPED";
                break;

        }
        return message;
    }

}

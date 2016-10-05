package ua.in.badparking;

import com.crashlytics.android.Crashlytics;

import java.net.ConnectException;

/**
 * Created by Dima Kovalenko on 10/5/16.
 */
public class Log {

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

}

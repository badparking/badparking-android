package ua.in.badparking.utils;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import ua.in.badparking.R;

public class Utils {

    private static final String TAG = "Utils";

    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.S");

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file.");
        }

        return null;
    }

    @NonNull
    public static String getFileName() {
        return "snapshot_" + format.format(new Date()) + "jpg";
    }

    public static void shootSound(Context context) {
        AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        switch (audio.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                MediaActionSound sound = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    sound = new MediaActionSound();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                }
                break;
            case AudioManager.RINGER_MODE_SILENT:
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                break;
        }
    }
}

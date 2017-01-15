package ua.in.badparking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PhotoUtils {

    private static final int PHOTO_MAX_HEIGHT = 1024;
    private static final int PHOTO_MAX_WIDTH = 1024;
    private static final int QUALITY_PHOTO = 40;
    public static final int DEGREE_0 = 0;
    public static final int DEGREE_90 = 90;
    public static final int DEGREE_180 = 180;
    public static final int DEGREE_270 = 270;

    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    @NonNull
    public static String getFileName() {
        return "snapshot_" + format.format(new Date()) + ".jpg";
    }

    public static void shootSound(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (audio.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                MediaActionSound sound = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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

    private static Bitmap getScaledBitmap(Bitmap bm, int bmOriginalWidth, int bmOriginalHeight, double originalWidthToHeightRatio, double originalHeightToWidthRatio, int maxHeight, int maxWidth) {
        if (bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {

            if (bmOriginalWidth > bmOriginalHeight) {
                bm = scaleDeminsFromWidth(bm, maxWidth, bmOriginalHeight, originalHeightToWidthRatio);
            } else if (bmOriginalHeight > bmOriginalWidth) {
                bm = scaleDeminsFromHeight(bm, maxHeight, bmOriginalHeight, originalWidthToHeightRatio);
            }

        }
        return bm;
    }

    private static Bitmap scaleDeminsFromHeight(Bitmap bm, int maxHeight, int bmOriginalHeight, double originalWidthToHeightRatio) {
        int newHeight = (int) Math.max(maxHeight, bmOriginalHeight * .11);
        int newWidth = (int) (newHeight * originalWidthToHeightRatio);
        bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return bm;
    }

    private static Bitmap scaleDeminsFromWidth(Bitmap bm, int maxWidth, int bmOriginalWidth, double originalHeightToWidthRatio) {
        int newWidth = (int) Math.max(maxWidth, bmOriginalWidth * .15);
        int newHeight = (int) (newWidth * originalHeightToWidthRatio);
        bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return bm;
    }

    public static void resize(String currentPhotoPath, int orientationDegree) {
        Bitmap resizedBitmap = resizeBitmap(currentPhotoPath);
        Bitmap rotatedBitmap = rotateBitmap(resizedBitmap, orientationDegree);

        File file = new File(currentPhotoPath);

        saveBitmap(rotatedBitmap, file);
    }

    public static Bitmap resizeBitmap(String photoPath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int bmOriginalWidth = bmOptions.outWidth;
        int bmOriginalHeight = bmOptions.outHeight;

        int scaleFactor = 2;
        if ((bmOriginalWidth > 0) || (bmOriginalHeight > 0)) {
            scaleFactor = Math.min(bmOriginalWidth / PHOTO_MAX_WIDTH, bmOriginalHeight / PHOTO_MAX_HEIGHT);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        double originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight;
        double originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth;

        Bitmap originalBitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        Bitmap photoBm = getScaledBitmap(originalBitmap, bmOriginalWidth, bmOriginalHeight,
                originalWidthToHeightRatio, originalHeightToWidthRatio,
                PHOTO_MAX_HEIGHT, PHOTO_MAX_WIDTH);
        return photoBm;
    }

    private static Bitmap resizeBitmap(final byte[] data) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, bmOptions);
        int bmOriginalWidth = bmOptions.outWidth;
        int bmOriginalHeight = bmOptions.outHeight;

        int scaleFactor = 2;
        if ((bmOriginalWidth > 0) || (bmOriginalHeight > 0)) {
            scaleFactor = Math.min(bmOriginalWidth / PHOTO_MAX_WIDTH, bmOriginalHeight / PHOTO_MAX_HEIGHT);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        double originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight;
        double originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth;

        Bitmap originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bmOptions);
        Bitmap photoBm = getScaledBitmap(originalBitmap, bmOriginalWidth, bmOriginalHeight,
                originalWidthToHeightRatio, originalHeightToWidthRatio,
                PHOTO_MAX_HEIGHT, PHOTO_MAX_WIDTH);
        return photoBm;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientationDegree) {

        int savePhotoCorrectionDegree = DEGREE_0;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            switch (orientationDegree) {
                case DEGREE_0:
                    savePhotoCorrectionDegree = DEGREE_90;
                    break;

                case DEGREE_90:
                    savePhotoCorrectionDegree = DEGREE_180;
                    break;

                case DEGREE_180:
                    savePhotoCorrectionDegree = DEGREE_270;
                    break;

                case DEGREE_270:
                    savePhotoCorrectionDegree = DEGREE_0;
                    break;
            }
        } else {
            switch (orientationDegree) {
                case DEGREE_90:
                    savePhotoCorrectionDegree = DEGREE_90;
                    break;

                case DEGREE_180:
                    savePhotoCorrectionDegree = DEGREE_180;
                    break;

                case DEGREE_0:
                    savePhotoCorrectionDegree = DEGREE_0;
                    break;

                case DEGREE_270:
                    savePhotoCorrectionDegree = DEGREE_270;
                    break;
            }
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(savePhotoCorrectionDegree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    private static void saveBitmap(Bitmap bitmap, final File file) {
        FileOutputStream out = null;
        try {
            file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_PHOTO, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void takePhoto(final byte[] data, int orientationDegree, final File file){
        Bitmap resizedBitmap = resizeBitmap(data);
        Bitmap rotatedBitmap = rotateBitmap(resizedBitmap, orientationDegree);
        saveBitmap(rotatedBitmap,file);
    }
}
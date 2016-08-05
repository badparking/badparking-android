package ua.in.badparking.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Volodymyr Dranyk on 8/5/2016.
 */
@SuppressWarnings("deprecation")
public enum CameraState {
    INST;

    private static final int FOCUS_AREA_SIZE = 300;
    private static final boolean FULL_SCREEN = true;
    private static final String TAG = CameraState.class.getName();

    private Context context;

    public void init(Context context) {
        this.context = context;
    }

    public void focusOnTouch(MotionEvent event, Camera camera, SurfaceView surfaceView) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Log.i(TAG, "fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY(), surfaceView);

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                camera.setParameters(parameters);
                camera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                camera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y, SurfaceView surfaceView) {
        int left = clamp(Float.valueOf((x / surfaceView.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / surfaceView.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                Log.i("tap_to_focus", "success!");
            } else {
                Log.i("tap_to_focus", "fail!");
            }
        }
    };

    public String takePicture(byte[] data, FragmentActivity activity) {
        Uri imageFileUri = activity.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        try {
            OutputStream imageFileOS = null;
            if (imageFileUri != null) {
                imageFileOS = activity.getContentResolver().openOutputStream(
                        imageFileUri);
            }

            if (imageFileOS != null) {
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();
            }
        } catch (Exception e) {
            Toast t = Toast.makeText(context, "Помилка", Toast.LENGTH_SHORT);
            t.show();
        }
        return getPathFromUri(imageFileUri, activity);
    }

    private String getPathFromUri(Uri uri, FragmentActivity activity) {
        String selected = null;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            selected = cursor.getString(columnIndex);
            cursor.close();
        }
        return selected;
    }

    public void setPreviewSize(FragmentActivity activity, Camera camera, SurfaceView surfaceView) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        if (widthIsMax) {
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        if (!FULL_SCREEN) {
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        matrix.mapRect(rectPreview);

        surfaceView.getLayoutParams().height = (int) (rectPreview.bottom);
        surfaceView.getLayoutParams().width = (int) (rectPreview.right);
    }

    public void setCameraDisplayOrientation(Camera camera, FragmentActivity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        result = ((360 - degrees) + info.orientation);
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

    public Camera getCameraInstance(boolean flashMode) {
        Camera camera = null;
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            setFlashMode(camera, flashMode);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return camera;
    }

    private void setFlashMode(Camera camera, boolean enabled) {
        List<String> mSupportedFlashModes = camera.getParameters().getSupportedFlashModes();
        if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            Camera.Parameters parameters = camera.getParameters();
            if (enabled) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            camera.setParameters(parameters);
        }
    }
}

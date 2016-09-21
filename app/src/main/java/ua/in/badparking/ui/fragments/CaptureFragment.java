package ua.in.badparking.ui.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.CameraWrapper;
import ua.in.badparking.R;
import ua.in.badparking.events.ShowHeaderEvent;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;

/**
 * @author Dima Kovalenko
 * @author Vadik Kovalsky
 * @author Volodymyr Dranyk
 */
@SuppressWarnings("deprecation")
public class CaptureFragment extends BaseFragment implements View.OnClickListener, PhotoAdapter.PhotosUpdatedListener, SensorEventListener {

    private static final String TAG = CaptureFragment.class.getName();

    @BindView(R.id.surface_container)
    protected FrameLayout surfaceContainer;
    private SurfaceView surfaceView;

    @BindView(R.id.recyclerView)
    protected RecyclerView recyclerView;

    private PhotoAdapter photoAdapter;

    @BindView(R.id.message)
    protected TextView messageView;

    @BindView(R.id.platesPreviewImage)
    protected ImageView platesPreviewImage;

    @BindView(R.id.platesEditText)
    protected EditText platesEditText;

    @BindView(R.id.snap)
    protected View snapButton;

    @BindView(R.id.next_button)
    protected View nextButton;
    private Unbinder unbinder;
    private CameraWrapper cameraWrapper;
    private SensorManager sensorManager;
    private Sensor mySensor;
    private int m_nOrientation = 0;

    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_capture, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        surfaceView = new SurfaceView(inflater.getContext());
        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snapButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        photoAdapter = new PhotoAdapter(getActivity());
        photoAdapter.setListener(this);
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setHasFixedSize(true);
        onPhotosUpdated();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPhotosUpdated() {
        int photosTaken = ClaimState.INST.getClaim().getPhotoFiles().size();
        nextButton.setVisibility(photosTaken > 1 ? View.VISIBLE : View.GONE);
        snapButton.setVisibility(photosTaken > 1 ? View.GONE : View.VISIBLE);
        if (photosTaken == 0) {
            messageView.setText(R.string.capture_claim);
        } else if (photosTaken == 1) {
            messageView.setText(R.string.capture_plates);
            messageView.setVisibility(View.VISIBLE);
            platesEditText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            EventBus.getDefault().post(new ShowHeaderEvent(false));
            messageView.setVisibility(View.GONE);
            platesEditText.setVisibility(View.VISIBLE);
            platesPreviewImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
//            surfaceView.setVisibility(View.GONE);
            setPic(platesPreviewImage, ClaimState.INST.getClaim().getPhotoFiles().get(1).getPath());
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    // TODO use Glide here
    private void setPic(ImageView view, String currentPhotoPath) {
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        view.setImageBitmap(bitmap);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        if (surfaceView != null) {
            surfaceView.getHolder().removeCallback(cameraWrapper.getSurfaceCamCallback());
            cameraWrapper.setSurfaceCamCallback(null);
        }

        if (cameraWrapper.getCamera() != null) {
            cameraWrapper.getCamera().setPreviewCallback(null);
            surfaceContainer.removeView(surfaceView);
        }

        releaseCamera();
        removePhoneKeypad();
    }

    private void releaseCamera() {
        if (cameraWrapper.getCamera() != null) {
            cameraWrapper.getCamera().release();
            cameraWrapper.setCamera(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (surfaceView != null) {
            cameraWrapper = new CameraWrapper(getActivity());
            if (cameraWrapper.getCamera() != null) {

                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                surfaceHolder.addCallback(cameraWrapper.getSurfaceCamCallback());

                Camera.Size containerSize = cameraWrapper.getCameraContainerSize();
                surfaceContainer.addView(surfaceView,
                        new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(
                                containerSize.height, containerSize.width)));
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snap:
                cameraWrapper.getCamera().takePicture(null, null, jpegCallback);
                cameraWrapper.shootSound();
                break;
            case R.id.next_button:
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                ClaimState.INST.getClaim().setLicensePlates(platesEditText.getText().toString());
                EventBus.getDefault().post(new ShowHeaderEvent(true));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)getActivity()).moveToNext();
                    }
                }, 300);

                break;
        }
    }

    private void onImageFileCreated(String photoPath) {
        ClaimState.INST.getClaim().addPhoto(photoPath);
        int numberOfPhotosTaken = ClaimState.INST.getClaim().getPhotoFiles().size();
        snapButton.setVisibility(numberOfPhotosTaken > 2 ? View.GONE : View.VISIBLE);
        photoAdapter.notifyDataSetChanged();
        onPhotosUpdated();
    }

    public Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            SaveImageTask saveImageTask = (SaveImageTask)new SaveImageTask().execute(data);

            try {
                onImageFileCreated(saveImageTask.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            //The coordinate-system is defined relative to the screen of the phone in its default orientation
            int orientation = 0;
            float roll = 0;
            float pitch = 0;
            switch (getActivity().getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    roll = event.values[2];
                    pitch = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    roll = event.values[1];
                    pitch = -event.values[2];
                    break;
                case Surface.ROTATION_180:
                    roll = -event.values[2];
                    pitch = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    roll = -event.values[1];
                    pitch = event.values[2];
                    break;
            }
            if (pitch >= -45 && pitch < 45 && roll >= 45) orientation = 0;
            else if (pitch < -45 && roll >= -45 && roll < 45) orientation = 90;
            else if (pitch >= -45 && pitch < 45 && roll < -45) orientation = 180;
            else if (pitch >= 45 && roll >= -45 && roll < 45) orientation = 270;

            if (m_nOrientation != orientation) {
                m_nOrientation = orientation;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {

        private final int QUALITY_PHOTO = 40;

        @Override
        protected String doInBackground(byte[]... data) {
            Uri imageFileUri = getActivity().getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            try {
                OutputStream imageFileOS = null;
                if (imageFileUri != null) {
                    imageFileOS = getActivity().getContentResolver().openOutputStream(
                            imageFileUri);
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap photoBm = BitmapFactory.decodeByteArray(data[0], 0, data[0].length, options);

                int bmOriginalWidth = photoBm.getWidth();
                int bmOriginalHeight = photoBm.getHeight();
                double originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight;
                double originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth;

                photoBm = getScaledBitmap(photoBm, bmOriginalWidth, bmOriginalHeight,
                        originalWidthToHeightRatio, originalHeightToWidthRatio,
                        CameraWrapper.PHOTO_MAX_HEIGHT, CameraWrapper.PHOTO_MAX_WIDTH);

                Matrix matrix = new Matrix();
                matrix.postRotate(m_nOrientation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(photoBm, 0, 0, photoBm.getWidth(), photoBm.getHeight(), matrix, true);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_PHOTO, bytes);

                if (imageFileOS != null) {
                    imageFileOS.write(bytes.toByteArray());
                    imageFileOS.flush();
                    imageFileOS.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return getPathFromUri(imageFileUri);
        }

        private Bitmap getScaledBitmap(Bitmap bm, int bmOriginalWidth, int bmOriginalHeight, double originalWidthToHeightRatio, double originalHeightToWidthRatio, int maxHeight, int maxWidth) {
            if (bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {

                if (bmOriginalWidth > bmOriginalHeight) {
                    bm = scaleDeminsFromWidth(bm, maxWidth, bmOriginalHeight, originalHeightToWidthRatio);
                } else if (bmOriginalHeight > bmOriginalWidth) {
                    bm = scaleDeminsFromHeight(bm, maxHeight, bmOriginalHeight, originalWidthToHeightRatio);
                }

            }
            return bm;
        }

        private Bitmap scaleDeminsFromHeight(Bitmap bm, int maxHeight, int bmOriginalHeight, double originalWidthToHeightRatio) {
            int newHeight = (int)Math.max(maxHeight, bmOriginalHeight * .55);
            int newWidth = (int)(newHeight * originalWidthToHeightRatio);
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            return bm;
        }

        private Bitmap scaleDeminsFromWidth(Bitmap bm, int maxWidth, int bmOriginalWidth, double originalHeightToWidthRatio) {
            int newWidth = (int)Math.max(maxWidth, bmOriginalWidth * .75);
            int newHeight = (int)(newWidth * originalHeightToWidthRatio);
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            return bm;
        }
    }

    private String getPathFromUri(Uri uri) {
        String selected = null;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            selected = cursor.getString(columnIndex);
            cursor.close();
        }
        return selected;
    }
}
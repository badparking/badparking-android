package ua.in.badparking.ui.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.events.ShowHeaderEvent;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;
import ua.in.badparking.utils.CameraWrapper;

/**
 * @author Dima Kovalenko
 * @author Vadik Kovalsky
 * @author Volodymyr Dranyk
 */
@SuppressWarnings("deprecation")
public class CaptureFragment extends BaseFragment implements View.OnClickListener, PhotoAdapter.PhotosUpdatedListener, SensorEventListener {

    private static final String TAG = CaptureFragment.class.getName();

    @BindView(R.id.camera)
    protected CameraView cameraView;

    @BindView(R.id.recyclerView)
    protected RecyclerView recyclerView;

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

    private PhotoAdapter photoAdapter;

    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_capture, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snapButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        if (cameraView != null) {
            cameraView.addCallback(mCallback);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        photoAdapter = new PhotoAdapter(getActivity());
        photoAdapter.setListener(this);
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setHasFixedSize(true);
        onPhotosUpdated();

        platesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int photosTaken = ClaimService.INST.getClaim().getPhotoFiles().size();
                nextButton.setVisibility(photosTaken > 1 && platesEditText.getText().length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPhotosUpdated() {
        int photosTaken = ClaimService.INST.getClaim().getPhotoFiles().size();
        nextButton.setVisibility(photosTaken > 1 && platesEditText.getText().length() > 0 ? View.VISIBLE : View.GONE);
        snapButton.setVisibility(photosTaken > 1 ? View.GONE : View.VISIBLE);
        if (photosTaken == 0) {
            messageView.setText(R.string.capture_claim);
        } else if (photosTaken == 1) {
            messageView.setText(R.string.capture_plates);
            messageView.setVisibility(View.VISIBLE);
            platesEditText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else if (TextUtils.isEmpty(ClaimService.INST.getClaim().getLicensePlates())) {
            EventBus.getDefault().post(new ShowHeaderEvent(false));
            messageView.setText("Введiть номернi знаки...");
            platesEditText.setVisibility(View.VISIBLE);
            platesPreviewImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
//            surfaceView.setVisibility(View.GONE); // TODO
            setPic(platesPreviewImage, ClaimService.INST.getClaim().getPhotoFiles().get(0).getPath());
            if (TextUtils.isEmpty(ClaimService.INST.getClaim().getLicensePlates())) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }, 800);
            }
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
        cameraView.stop();
        removePhoneKeypad();
        super.onPause();

    }


    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode,
                                                             @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snap:
//                if (cameraWrapper.isSafeToTakePicture()) {
//                    cameraWrapper.getCamera().takePicture(null, null, jpegCallback);
//                    cameraWrapper.shootSound();
//                    cameraWrapper.setSafeToTakePicture(false);
//                }
                if (cameraView != null) {
                    cameraView.takePicture();
                }
                break;
            case R.id.next_button:
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                ClaimService.INST.getClaim().setLicensePlates(platesEditText.getText().toString());
                EventBus.getDefault().post(new ShowHeaderEvent(true));
                platesEditText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                platesPreviewImage.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)getActivity()).showPage(MainActivity.PAGE_CLAIM_TYPES);
                    }
                }, 1000);

                break;
        }
    }

    private void onImageFileCreated(String photoPath) {
        ClaimService.INST.getClaim().addPhoto(photoPath);
        int numberOfPhotosTaken = ClaimService.INST.getClaim().getPhotoFiles().size();
        snapButton.setVisibility(numberOfPhotosTaken > 2 ? View.GONE : View.VISIBLE);
        photoAdapter.notifyDataSetChanged();
        onPhotosUpdated();
    }

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
            if (pitch < 45 && roll >= 45) orientation = 0;
            else if (roll >= -45 && roll < 45) orientation = 90;
            else if (pitch < 45 && roll < -45) orientation = 180;
            else if (roll >= -45 && roll < 45) orientation = 270;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private Handler mBackgroundHandler;
    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
//            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT)
//                    .show();
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    // This demo app saves the taken picture to a constant file.
                    // $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
                    File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "picture.jpg");
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(file);
                        os.write(data);
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Cannot write to " + file, e);
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                    }
                }
            });
        }

    };

//    private class SaveImageTask extends AsyncTask<byte[], Void, String> {
//
//        private final int QUALITY_PHOTO = 40;
//
//        @Override
//        protected String doInBackground(byte[]... data) {
//            Uri imageFileUri = getActivity().getContentResolver().insert(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//            try {
//                OutputStream imageFileOS = null;
//                if (imageFileUri != null) {
//                    imageFileOS = getActivity().getContentResolver().openOutputStream(
//                            imageFileUri);
//                }
//
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                Bitmap photoBm = BitmapFactory.decodeByteArray(data[0], 0, data[0].length, options);
//
//                int bmOriginalWidth = photoBm.getWidth();
//                int bmOriginalHeight = photoBm.getHeight();
//                double originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight;
//                double originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth;
//
//                photoBm = getScaledBitmap(photoBm, bmOriginalWidth, bmOriginalHeight,
//                        originalWidthToHeightRatio, originalHeightToWidthRatio,
//                        CameraWrapper.PHOTO_MAX_HEIGHT, CameraWrapper.PHOTO_MAX_WIDTH);
//
//                Matrix matrix = new Matrix();
//                matrix.postRotate(m_nOrientation);
//                Bitmap rotatedBitmap = Bitmap.createBitmap(photoBm, 0, 0, photoBm.getWidth(), photoBm.getHeight(), matrix, true);
//
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_PHOTO, bytes);
//
//                if (imageFileOS != null) {
//                    imageFileOS.write(bytes.toByteArray());
//                    imageFileOS.flush();
//                    imageFileOS.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return getPathFromUri(imageFileUri);
//        }
//
//        private Bitmap getScaledBitmap(Bitmap bm, int bmOriginalWidth, int bmOriginalHeight, double originalWidthToHeightRatio, double originalHeightToWidthRatio, int maxHeight, int maxWidth) {
//            if (bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {
//
//                if (bmOriginalWidth > bmOriginalHeight) {
//                    bm = scaleDeminsFromWidth(bm, maxWidth, bmOriginalHeight, originalHeightToWidthRatio);
//                } else if (bmOriginalHeight > bmOriginalWidth) {
//                    bm = scaleDeminsFromHeight(bm, maxHeight, bmOriginalHeight, originalWidthToHeightRatio);
//                }
//
//            }
//            return bm;
//        }
//
//        private Bitmap scaleDeminsFromHeight(Bitmap bm, int maxHeight, int bmOriginalHeight, double originalWidthToHeightRatio) {
//            int newHeight = (int)Math.max(maxHeight, bmOriginalHeight * .55);
//            int newWidth = (int)(newHeight * originalWidthToHeightRatio);
//            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
//            return bm;
//        }
//
//        private Bitmap scaleDeminsFromWidth(Bitmap bm, int maxWidth, int bmOriginalWidth, double originalHeightToWidthRatio) {
//            int newWidth = (int)Math.max(maxWidth, bmOriginalWidth * .75);
//            int newHeight = (int)(newWidth * originalHeightToWidthRatio);
//            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
//            return bm;
//        }
//    }

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
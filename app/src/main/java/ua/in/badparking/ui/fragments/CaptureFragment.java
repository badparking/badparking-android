package ua.in.badparking.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.cameraview.CameraView;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.events.ShowHeaderEvent;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;
import ua.in.badparking.utils.ConfirmationDialogFragment;
import ua.in.badparking.utils.Utils;

/**
 * @author Dima Kovalenko
 * @author Vadik Kovalsky
 * @author Volodymyr Dranyk
 */
@SuppressWarnings("deprecation")
public class CaptureFragment extends BaseFragment implements View.OnClickListener, PhotoAdapter.PhotosUpdatedListener {

    private static final String TAG = CaptureFragment.class.getName();

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int PHOTO_MAX_WIDTH = 1024;
    private static final int PHOTO_MAX_HEIGHT = 1024;
    private static final int QUALITY_PHOTO = 40;
    private static final String FRAGMENT_DIALOG = "dialog";

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
            setPic(platesPreviewImage, ClaimService.INST.getClaim().getPhotoFiles().get(1).getPath());
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

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[] {Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snap:

                if (cameraView != null) {
                    cameraView.takePicture();
                    Utils.shootSound(getActivity());
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

    private Handler mBackgroundHandler;

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(final CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    // This demo app saves the taken picture to a constant file.
                    // $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
                    final File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), Utils.getFileName());
                    OutputStream os = null;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    Bitmap photoBm = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                    int bmOriginalWidth = photoBm.getWidth();
                    int bmOriginalHeight = photoBm.getHeight();
                    double originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight;
                    double originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth;

                    photoBm = getScaledBitmap(photoBm, bmOriginalWidth, bmOriginalHeight,
                            originalWidthToHeightRatio, originalHeightToWidthRatio,
                            PHOTO_MAX_HEIGHT, PHOTO_MAX_WIDTH);

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photoBm.compress(Bitmap.CompressFormat.JPEG, QUALITY_PHOTO, bytes);

                    try {
                        os = new FileOutputStream(file);
                        os.write(bytes.toByteArray());
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
                    cameraView.post(new Runnable() {
                        @Override
                        public void run() {
                            onImageFileCreated(file.getPath());
                        }
                    });

                }
            });
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
            int newHeight = (int)Math.max(maxHeight, bmOriginalHeight * .11);
            int newWidth = (int)(newHeight * originalWidthToHeightRatio);
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            return bm;
        }

        private Bitmap scaleDeminsFromWidth(Bitmap bm, int maxWidth, int bmOriginalWidth, double originalHeightToWidthRatio) {
            int newWidth = (int)Math.max(maxWidth, bmOriginalWidth * .15);
            int newHeight = (int)(newWidth * originalHeightToWidthRatio);
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            return bm;
        }
    };
}
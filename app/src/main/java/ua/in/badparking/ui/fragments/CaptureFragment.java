package ua.in.badparking.ui.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.CameraWrapper;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;

/**
 * @author Dima Kovalenko
 * @author Vadik Kovalsky
 * @author Volodymyr Dranyk
 */
@SuppressWarnings("deprecation")
public class CaptureFragment extends BaseFragment implements View.OnClickListener, PhotoAdapter.PhotosUpdatedListener{

    private static final String TAG = CaptureFragment.class.getName();

    @BindView(R.id.surface_container)
    protected FrameLayout surfaceContainer;
    private SurfaceView surfaceView;
    @BindView(R.id.recyclerView)
    protected RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    @BindView(R.id.message)
    protected TextView messageView;
    @BindView(R.id.snap)
    protected View snapButton;
    @BindView(R.id.next_button)
    protected View nextButton;
    private Unbinder unbinder;
    private CameraWrapper cameraWrapper;

    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_capture, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        surfaceView = new SurfaceView(inflater.getContext());
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
            messageView.setText(R.string.capture_claim_another_angle);
        } else {
            messageView.setText("");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (surfaceView != null) {
            surfaceView.getHolder().removeCallback(cameraWrapper.getSurfaceCamCallback());
            cameraWrapper.setSurfaceCamCallback(null);
        }

        if (cameraWrapper.getCamera() != null) {
            cameraWrapper.getCamera().setPreviewCallback(null);
            surfaceContainer.removeView(surfaceView);
        }

        releaseCamera();
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
        if (surfaceView != null) {
            cameraWrapper = new CameraWrapper(getActivity());
            if (cameraWrapper.getCamera() != null) {

                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                surfaceHolder.addCallback(cameraWrapper.getSurfaceCamCallback());

                Camera.Size containerSize = cameraWrapper.getCameraContainerSize();
                surfaceContainer.addView(surfaceView,
                        new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(
                                containerSize.height,containerSize.width)));
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snap:
                cameraWrapper.getCamera().takePicture(null, null, jpegCallback);
                break;
            case R.id.next_button:
                ((MainActivity)getActivity()).moveToNext();
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
            SaveImageTask saveImageTask = (SaveImageTask) new SaveImageTask().execute(data);

            try {
                onImageFileCreated(saveImageTask.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {

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

                if (imageFileOS != null) {
                    imageFileOS.write(data[0]);
                    imageFileOS.flush();
                    imageFileOS.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            return getPathFromUri(imageFileUri);
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
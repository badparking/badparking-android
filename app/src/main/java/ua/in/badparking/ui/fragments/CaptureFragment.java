package ua.in.badparking.ui.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.services.CameraState;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;

/**
 * @author Dima Kovalenko
 * @author Vadik Kovalsky
 * @author Volodymyr Dranyk
 */
@SuppressWarnings("deprecation")
public class CaptureFragment extends BaseFragment  implements View.OnClickListener,
        SurfaceHolder.Callback, Camera.PictureCallback, PhotoAdapter.PhotosUpdatedListener {

    private static final String TAG = CaptureFragment.class.getName();
    private static final boolean FLASH_MODE = true;

    @BindView(R.id.surfaceView)
    protected SurfaceView surfaceView;
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
    private Camera camera;

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

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        snapButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    CameraState.INST.focusOnTouch(event, camera, surfaceView);
                }
                return true;
            }
        });

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snap:
                camera.takePicture(null, null, CaptureFragment.this);
                break;
            case R.id.next_button:
                ((MainActivity)getActivity()).moveToNext();
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            releaseCameraAndPreview(); //important
            camera = CameraState.INST.getCameraInstance(FLASH_MODE);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            camera.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        CameraState.INST.setCameraDisplayOrientation(camera, getActivity());
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera!=null) {
            camera.stopPreview();
            camera.release();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String photoPath = CameraState.INST.takePicture(data, getActivity());
        onImageFileCreated(photoPath);
        camera.startPreview();
    }

    private void onImageFileCreated(String photoPath) {
        ClaimState.INST.getClaim().addPhoto(photoPath);
        int numberOfPhotosTaken = ClaimState.INST.getClaim().getPhotoFiles().size();
        snapButton.setVisibility(numberOfPhotosTaken > 2 ? View.GONE : View.VISIBLE);
        photoAdapter.notifyDataSetChanged();
        onPhotosUpdated();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(camera==null) {
            camera = CameraState.INST.getCameraInstance(FLASH_MODE);
        }
        CameraState.INST.setPreviewSize(getActivity(), camera, surfaceView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
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
        } else {
            messageView.setText("");
        }
    }
}
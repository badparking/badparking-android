package ua.in.badparking.ui.fragments;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import ua.in.badparking.R;
import ua.in.badparking.ui.MainActivity;
import ua.in.badparking.ui.utils.CameraPreview;

/**
 * @author Dima Kovalenko
 * @author Vadik Kovalsky
 */
public class CaptureFragment extends BaseFragment implements View.OnClickListener {

//    private static final int REQUEST_IMAGE_CAPTURE = 356;
//    private static final int PICK_IMAGE = 357;

    private Camera mCamera;
    private CameraPreview mPreview;

    private static final String TAG = CaptureFragment.class.getName();

    // photos
    private ImageView firstImageView;
    private ImageView secondImageView;
    private View firstHolder;
    private View secondHolder;
    private View takePhotoButton;
    private boolean isFirstHasImage;
    private boolean isSecondHasImage;
    private File firstImage;
    private File secondImage;
    private View nextButton;


    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_capture, container, false);

        Resources res = getResources();
        String[] trespassTypes = res.getStringArray(R.array.report_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.listitem_report_type, Arrays.asList(trespassTypes));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ImageView closeFirst = (ImageView)rootView.findViewById(R.id.close_first);
        ImageView closeSecond = (ImageView)rootView.findViewById(R.id.close_second);
        firstImageView = (ImageView)rootView.findViewById(R.id.first_image);
        secondImageView = (ImageView)rootView.findViewById(R.id.second_image);
        firstHolder = rootView.findViewById(R.id.first_image_holder);
        secondHolder = rootView.findViewById(R.id.second_image_holder);
        takePhotoButton = rootView.findViewById(R.id.snap);
        nextButton = rootView.findViewById(R.id.next_button);

        firstImageView.setOnClickListener(this);
        secondImageView.setOnClickListener(this);
        closeFirst.setOnClickListener(this);
        closeSecond.setOnClickListener(this);
        takePhotoButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getContext(), mCamera);
        FrameLayout preview = (FrameLayout)rootView.findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        return rootView;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            if (!isFirstHasImage) {
//                if (isSecondHasImage) {
//                    takePhotoButton.setVisibility(View.GONE);
//                }
//                firstHolder.setVisibility(View.VISIBLE);
//                setPic(firstImageView, firstImage.getPath());
//                isFirstHasImage = true;
//                Toast.makeText(getActivity(), R.string.capture_plates, Toast.LENGTH_LONG).show();
//
//            } else {
//                takePhotoButton.setVisibility(View.GONE);
//                secondHolder.setVisibility(View.VISIBLE);
//                setPic(secondImageView, secondImage.getPath());
//                isSecondHasImage = true;
//
//            }
//        } else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
//            if (getPathFromUri(data.getData()) != null) {
//                File file = new File(getPathFromUri(data.getData()));
//                if (!isFirstHasImage) {
//                    firstHolder.setVisibility(View.VISIBLE);
//                    firstImage = file;
//                    setPic(firstImageView, firstImage.getPath());
//                    isFirstHasImage = true;
//                    Toast.makeText(getActivity(), R.string.capture_plates, Toast.LENGTH_LONG).show();
//                } else {
//                    secondHolder.setVisibility(View.VISIBLE);
//                    secondImage = file;
//                    setPic(secondImageView, secondImage.getPath());
//                    isSecondHasImage = true;
//                }
//            } else
//                Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_LONG).show();
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAll();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_first:
                releaseFirstImage();
                break;
            case R.id.close_second:
                releaseSecondImage();
                break;
            case R.id.snap:
                capture();
//                openGallery();
                break;
            case R.id.next_button:
                ((MainActivity)getActivity()).moveToNext();
                break;
        }
    }

    private void capture() {

    }

    // Photo stuff

//    private void openCamera() {
//        if (!isFirstHasImage || !isSecondHasImage) {
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                try {
//                    if (!isFirstHasImage) {
//                        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                        firstImage = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jpg", storageDir);
//                    } else {
//                        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                        secondImage = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jpg", storageDir);
//                    }
//                } catch (IOException ignored) {
//                }
//                if (firstImage != null || secondImage != null) {
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(!isFirstHasImage ? firstImage : secondImage));
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                }
//            }
//        } else {
//            Toast.makeText(getActivity(), "You can send only 2 pics", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void openGallery() {
//        if (!isFirstHasImage || !isSecondHasImage) {
//            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(galleryIntent, PICK_IMAGE);
//        } else {
//            Toast.makeText(getActivity(), getString(R.string.only_2_pics), Toast.LENGTH_LONG).show();
//        }
//    }

    private void releaseFirstImage() {
        firstImageView.setImageBitmap(null);
        firstImage = null;
        isFirstHasImage = false;
        firstHolder.setVisibility(View.GONE);
        takePhotoButton.setVisibility(View.VISIBLE);
    }

    private void releaseSecondImage() {
        secondImageView.setImageBitmap(null);
        secondImage = null;
        isSecondHasImage = false;
        secondHolder.setVisibility(View.GONE);
        takePhotoButton.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void releaseAll() {
        if (firstImage != null) firstImage.delete();
        if (secondImage != null) secondImage.delete();
        firstImage = null;
        secondImage = null;
        isFirstHasImage = false;
        isSecondHasImage = false;
    }


    // TODO move all of this to helper class
    private void setPic(ImageView view, String currentPhotoPath) {
        int targetW = getResources().getDimensionPixelSize(R.dimen.photo_side);
        int targetH = getResources().getDimensionPixelSize(R.dimen.photo_side);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        //noinspection deprecation
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        view.setImageBitmap(bitmap);
    }

    private String getPathFromUri(Uri uri) {
        String selected;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        selected = cursor.getString(columnIndex);
        cursor.close();
        return selected;
    }

}
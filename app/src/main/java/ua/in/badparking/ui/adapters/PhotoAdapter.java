package ua.in.badparking.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.model.MediaFile;
import ua.in.badparking.services.ClaimState;

/**
 * @author Dima Kovalenko
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MovieViewHolder> {

    private static final String TAG = PhotoAdapter.class.getName();
    private final LayoutInflater _layoutInflater;
    private Context _context;

    /**
     * Constructor
     *
     * @param context {@link Context}
     */
    public PhotoAdapter(Context context) {
        _context = context;
        _layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MovieViewHolder(_layoutInflater.inflate(R.layout.photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.setViewData(getItems().get(position));
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    private List<MediaFile> getItems() {
        return ClaimState.INST.getClaim().getPhotoFiles();
    }


    /**
     * {@link android.support.v7.widget.RecyclerView.ViewHolder}
     */
    class MovieViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        protected ImageView _photoView;

        @InjectView(R.id.deleteCross)
        protected ImageView _deleteCross;

        /**
         * Constructor
         *
         * @param itemView {@link View}
         */
        public MovieViewHolder(View itemView) {
            super(itemView);
            _photoView = (ImageView)itemView.findViewById(R.id.image);
            _deleteCross = (ImageView)itemView.findViewById(R.id.deleteCross);
        }

        /**
         * Set the data for the View.
         *
         * @param mediaFile {@link MediaFile}
         */
        public void setViewData(final MediaFile mediaFile) {

            setPic(_photoView, mediaFile.getFilePath());
            _deleteCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClaimState.INST.getClaim().removePhoto(mediaFile);
                    notifyDataSetChanged();
                }
            });
        }


        // TODO use Glide here
        private void setPic(ImageView view, String currentPhotoPath) {
            int targetW = _context.getResources().getDimensionPixelSize(R.dimen.photo_side);
            int targetH = _context.getResources().getDimensionPixelSize(R.dimen.photo_side);

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
            Cursor cursor = _context.getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            selected = cursor.getString(columnIndex);
            cursor.close();
            return selected;
        }

    }
}

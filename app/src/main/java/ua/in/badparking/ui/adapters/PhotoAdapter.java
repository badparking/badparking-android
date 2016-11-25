package ua.in.badparking.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.badparking.R;
import ua.in.badparking.model.MediaFile;
import ua.in.badparking.services.ClaimService;

/**
 * @author Dima Kovalenko
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MovieViewHolder> {

    public interface PhotosUpdatedListener {
        void onPhotosUpdated();
    }

    private static final String TAG = PhotoAdapter.class.getName();
    private final LayoutInflater mLayoutInflater;
    private final boolean hideCross;
    private Context mContext;

    private PhotosUpdatedListener mListener;

    /**
     * Constructor
     *
     * @param context {@link Context}
     */
    public PhotoAdapter(Context context, boolean hideCross) {
        mContext = context;
        this.hideCross = hideCross;
        mLayoutInflater = LayoutInflater.from(context);

    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MovieViewHolder(mLayoutInflater.inflate(R.layout.photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.setViewData(getItems().get(position));
        if(hideCross) {
            holder._deleteCross.setVisibility(View.GONE);
        } else holder._deleteCross.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    private List<MediaFile> getItems() {
        return ClaimService.INST.getClaim().getPhotoFiles();
    }

    public void setListener(PhotosUpdatedListener listener) {
        mListener = listener;
    }

    /**
     * {@link RecyclerView.ViewHolder}
     */
    class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        protected ImageView _photoView;
        @BindView(R.id.deleteCross)
        protected ImageView _deleteCross;

        /**
         * Constructor
         *
         * @param itemView {@link View}
         */
        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /**
         * Set the data for the View.
         *
         * @param mediaFile {@link MediaFile}
         */
        public void setViewData(final MediaFile mediaFile) {

            setPic(_photoView, mediaFile.getPath());
            _deleteCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClaimService.INST.getClaim().removePhoto(mediaFile);
                    notifyDataSetChanged();
                    if (mListener != null) {
                        mListener.onPhotosUpdated();
                    }
                }
            });
        }

        // TODO use Glide here
        private void setPic(ImageView view, String currentPhotoPath) {
            int targetW = mContext.getResources().getDimensionPixelSize(R.dimen.photo_side);
            int targetH = mContext.getResources().getDimensionPixelSize(R.dimen.photo_side);

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
    }
}

package nl.fourtress.flickrclient.flickr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.SizeModel;

/**
 * @author Rick Slinkman
 */
public class FlickrDownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private final PhotoMetaModel mItem;
    private final SizeModel[] mSizes;
    private final FlickrImageDownloadCompletedListener mListener;

    public interface FlickrImageDownloadCompletedListener {
        void onFlickrImageDownloaded(PhotoMetaModel item, SizeModel[] sizes, Bitmap result);
    }
    private static final String TAG = "FlickrDownloadImageTask";
    private String mImageURL;

    public FlickrDownloadImageTask(String imgURL, PhotoMetaModel item, SizeModel[] sizes, FlickrImageDownloadCompletedListener listener) {
        this.mImageURL = imgURL;
        this.mItem = item;
        this.mSizes = sizes;
        this.mListener = listener;
    }

    protected Bitmap doInBackground(String... urls)
    {
        Bitmap bitmap = null;
        Log.d(TAG, "doInBackground: download " + mImageURL);
        try {
            InputStream in = new URL(mImageURL).openStream();
            bitmap = BitmapFactory.decodeStream(in);
            Log.d(TAG, "doInBackground: bitmap loaded " + (bitmap != null));
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result)
    {
        if(mListener == null) {
            // Nobody interested in result
            return;
        }

        mListener.onFlickrImageDownloaded(mItem, mSizes, result);
        // TODO
        Log.d(TAG, "onPostExecute: bitmap found " + (result != null));
    }
}
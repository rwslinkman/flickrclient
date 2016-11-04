package nl.fourtress.flickrclient.flickr;

import android.util.Log;

import com.google.gson.Gson;

import nl.fourtress.flickrclient.flickr.model.FlickrErrorResponse;
import nl.fourtress.flickrclient.flickr.model.ImageSizesResponseModel;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.SizeModel;

/**
 * @author Rick Slinkman
 */
public class FlickrImageTask extends HttpRequestTask
{
    public interface FlickerImageTaskCompletedListener {
        void onFlickrImageTaskCompleted(SizeModel[] sizes, PhotoMetaModel item);
        void onFlickrImageTaskError(FlickrErrorResponse error);
    }

    private static final String TAG = "FlickrImageTask";
    private final FlickerImageTaskCompletedListener mCompletedListener;
    private final PhotoMetaModel mItem;

    public FlickrImageTask(String endpoint, PhotoMetaModel item, FlickerImageTaskCompletedListener listener)
    {
        super(endpoint, GET);
        this.mCompletedListener = listener;
        this.mItem = item;
    }

    @Override
    protected void onRequestComplete()
    {
        if(mCompletedListener == null) {
            // Nobody interested in result
            return;
        }

        Gson converter = new Gson();
        ImageSizesResponseModel model = converter.fromJson(getResponseBody(), ImageSizesResponseModel.class);
        if(model != null && model.getSizes() != null && model.getStat().equals("ok"))
        {
            mCompletedListener.onFlickrImageTaskCompleted(model.getSizes().getSize(), mItem);
        }
        else {
            FlickrErrorResponse error = converter.fromJson(getResponseBody(), FlickrErrorResponse.class);
            mCompletedListener.onFlickrImageTaskError(error);
            Log.e(TAG, "onRequestComplete: error " + error.getMessage());
        }
    }
}

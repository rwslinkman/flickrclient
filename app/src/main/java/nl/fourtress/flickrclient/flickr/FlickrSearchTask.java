package nl.fourtress.flickrclient.flickr;

import android.util.Log;

import com.google.gson.Gson;

import nl.fourtress.flickrclient.flickr.model.SearchResponseModel;

/**
 * @author Rick Slinkman
 */
public class FlickrSearchTask extends HttpRequestTask
{
    public interface FlickrSearchCompletedListener {
        void onFlickrSearchCompleted(SearchResponseModel response);
        void onFlickrSearchError();
    }

    private static final String TAG = "FlickrSearchTask";
    private FlickrSearchCompletedListener mCompletedListener;

    public FlickrSearchTask(String endpoint, FlickrSearchCompletedListener listener)
    {
        super(endpoint, GET);
        this.mCompletedListener = listener;
    }

    @Override
    protected void onRequestComplete()
    {
        if(getResponseCode() == 200) {
            Log.d(TAG, "onRequestComplete: request succesfull");
            Log.d(TAG, "onRequestComplete: body " + getResponseBody());

            Gson converter = new Gson();
            SearchResponseModel model = converter.fromJson(getResponseBody(), SearchResponseModel.class);
            if(mCompletedListener != null)
            {
                if(model != null) {
                    Log.d(TAG, "onRequestComplete: model with stat: " + model.getStat());
                    mCompletedListener.onFlickrSearchCompleted(model);
                } else {
                    mCompletedListener.onFlickrSearchError();
                }
            }
        }
        else if(mCompletedListener != null) {
            Log.e(TAG, "onRequestComplete: Flickr API error");
            mCompletedListener.onFlickrSearchError();
        }
    }
}

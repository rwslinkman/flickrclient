package nl.fourtress.flickrclient.flickr;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import nl.fourtress.flickrclient.BuildConfig;
import nl.fourtress.flickrclient.flickr.model.FlickrErrorResponse;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.SearchResponseModel;
import nl.fourtress.flickrclient.flickr.model.SizeModel;

/**
 * @author Rick Slinkman
 */
public class FlickrSearchTask extends HttpRequestTask implements FlickrImageTask.FlickerImageTaskCompletedListener
{
    public interface FlickrSearchCompletedListener extends FlickrImageTask.FlickerImageTaskCompletedListener
    {
        void onFlickrSearchCompleted(SearchResponseModel response);
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
        if(mCompletedListener == null) {
            // Nobody is interested
            return;
        }

        if(getResponseCode() == 200) {
            Log.d(TAG, "onRequestComplete: request succesfull");
            Log.d(TAG, "onRequestComplete: body " + getResponseBody());

            Gson converter = new Gson();
            SearchResponseModel model = converter.fromJson(getResponseBody(), SearchResponseModel.class);

            if(model != null && model.getStat().equals("ok")) {
                Log.d(TAG, "onRequestComplete: model with stat: " + model.getStat());
                mCompletedListener.onFlickrSearchCompleted(model);
                getSizes(model);
            } else {
                // There seems to be an error
                FlickrErrorResponse errorResponse = converter.fromJson(getResponseBody(), FlickrErrorResponse.class);
                mCompletedListener.onFlickrImageTaskError(errorResponse);
            }
        }
        else {
            Log.e(TAG, "onRequestComplete: Flickr API error");
            mCompletedListener.onFlickrImageTaskError(null);
        }
    }

    private void getSizes(SearchResponseModel model)
    {
        List<PhotoMetaModel> modelsToAdd = Arrays.asList(model.getPhotos().getPhoto());
        for(PhotoMetaModel item : modelsToAdd)
        {
            // GET sizes for each photo
            String url = String.format("https://api.flickr.com/services/rest/?method=flickr.photos.getSizes" +
                            "&api_key=%s" +
                            "&photo_id=%s" +
                            "&format=json" +
                            "&nojsoncallback=1",
                    BuildConfig.FLICKR_API_KEY, item.getId());
            FlickrImageTask task = new FlickrImageTask(url, item, mCompletedListener);
            task.execute();
        }
    }

    @Override
    public void onFlickrImageTaskCompleted(SizeModel[] sizes, PhotoMetaModel item) {
        if(mCompletedListener != null) {
            mCompletedListener.onFlickrImageTaskCompleted(sizes, item);
        }
    }

    @Override
    public void onFlickrImageTaskError(FlickrErrorResponse error)
    {
        if(mCompletedListener != null) {
            mCompletedListener.onFlickrImageTaskError(error);
        }
    }
}

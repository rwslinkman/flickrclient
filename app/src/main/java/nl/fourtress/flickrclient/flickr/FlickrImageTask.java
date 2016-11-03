package nl.fourtress.flickrclient.flickr;

import android.util.Log;

import com.google.gson.Gson;

import nl.fourtress.flickrclient.flickr.model.FlickrErrorResponse;
import nl.fourtress.flickrclient.flickr.model.ImageSizesResponseModel;

/**
 * @author Rick Slinkman
 */
public class FlickrImageTask extends HttpRequestTask
{
    private static final String TAG = "FlickrImageTask";
    public FlickrImageTask(String endpoint)
    {
        super(endpoint, GET);
    }

    @Override
    protected void onRequestComplete()
    {
        Gson converter = new Gson();
        ImageSizesResponseModel model = converter.fromJson(getResponseBody(), ImageSizesResponseModel.class);
        Log.d(TAG, "onRequestComplete: body " + getResponseBody());
        Log.d(TAG, "onRequestComplete: code " + getResponseCode());
        if(model != null && model.getSizes() != null && model.getStat().equals("ok")) {

            Log.d(TAG, "onRequestComplete: request successful");
            Log.d(TAG, "onRequestComplete: image has " + model.getSizes().getSize().length+ " sizes");
        }
        else {
            FlickrErrorResponse error = converter.fromJson(getResponseBody(), FlickrErrorResponse.class);
            Log.e(TAG, "onRequestComplete: error " + error.getMessage());
        }
    }
}

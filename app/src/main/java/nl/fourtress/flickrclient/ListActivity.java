package nl.fourtress.flickrclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import nl.fourtress.flickrclient.flickr.FlickrSearchTask;
import nl.fourtress.flickrclient.flickr.model.SearchErrorResponse;
import nl.fourtress.flickrclient.flickr.model.SearchResponseModel;

/**
 * @author Rick Slinkman
 */
public class ListActivity extends AppCompatActivity implements View.OnClickListener, FlickrSearchTask.FlickrSearchCompletedListener
{
    private static final String TAG = "ListActivity";
    private RecyclerView mPhotoList;
    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;
    private EditText mSearchField;
    private String mQueryURL;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mPhotoList = (RecyclerView) findViewById(R.id.flickr_photo_list);
        mProgress = (ProgressBar) findViewById(R.id.flickr_search_progress);
        mSearchButton = (FloatingActionButton) findViewById(R.id.flickr_search_btn);
        mSearchField = (EditText) findViewById(R.id.flickr_search_field);

        String url = "https://api.flickr.com/services/rest/?method=flickr.photos.search" +
                    "&api_key=%s" +
                    "&format=json" +
                    "&nojsoncallback=1";
        this.mQueryURL = String.format(url, BuildConfig.FLICKR_API_KEY);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mSearchButton.setOnClickListener(this);
    }

    @Override
    protected void onPause()
    {
        mSearchButton.setOnClickListener(null);
        super.onPause();
    }

    @Override
    public void onClick(View view)
    {
        // Some simple validation
        String query = mSearchField.getText().toString().trim();
        if(query.length() == 0) {
            Toast.makeText(this, getString(R.string.no_keywords_entered), Toast.LENGTH_SHORT).show();
            return;
        }

        // Tags limited by Flickr API
        String[] tags = query.split("\\W+");
        if(tags.length >= 20) {
            Toast.makeText(this, getString(R.string.too_many_keywords), Toast.LENGTH_SHORT).show();
            return;
        }

        // UI
        Utils.closeKeyboard(this);
        mProgress.setVisibility(View.VISIBLE);
        mPhotoList.setItemViewCacheSize(View.GONE);

        // Execute API request
        String tagsParam = Utils.joinTags(tags, "%2C");
        String flickrQuery = mQueryURL + "&tags=" + tagsParam;
        FlickrSearchTask task = new FlickrSearchTask(flickrQuery, this);
        task.execute();
    }

    @Override
    public void onFlickrSearchCompleted(SearchResponseModel response)
    {
        String resultCount = response.getPhotos().getTotal();
        Log.d(TAG, "onFlickrSearchCompleted: response stat" + response.getStat());
        Toast.makeText(this, "Found " + resultCount + " results on Flickr", Toast.LENGTH_SHORT).show();

        // TODO Convert response model to RecyclerView dataset

        mProgress.setVisibility(View.GONE);
        mPhotoList.setItemViewCacheSize(View.VISIBLE);
        mSearchField.getText().clear();
    }

    @Override
    public void onFlickrSearchError(SearchErrorResponse errorResponse)
    {
        Log.e(TAG, "onFlickrSearchError: Request was not successful");
        String msg = (errorResponse == null) ? getString(R.string.flickr_search_error) : errorResponse.getMessage();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // UI updates after request
        mProgress.setVisibility(View.GONE);
        mPhotoList.setItemViewCacheSize(View.GONE);
        mSearchField.getText().clear();
    }
}

package nl.fourtress.flickrclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.fourtress.flickrclient.flickr.FlickrImageTask;
import nl.fourtress.flickrclient.flickr.FlickrSearchTask;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.PhotosResponseModel;
import nl.fourtress.flickrclient.flickr.model.FlickrErrorResponse;
import nl.fourtress.flickrclient.flickr.model.SearchResponseModel;
import nl.fourtress.flickrclient.presenter.PhotoItemPresenter;
import nl.rwslinkman.presentable.PresentableAdapter;

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
    private PresentableAdapter<PhotoMetaModel> mPhotoListAdapter;
    private PhotosResponseModel mCurrentSearch;
    private String mSearchTags;
    private boolean mIsQuerying;
    private ArrayList<PhotoMetaModel> mVisiblePhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mPhotoList = (RecyclerView) findViewById(R.id.flickr_photo_list);
        LinearLayoutManager mgr = new LinearLayoutManager(this);
        mPhotoList.setLayoutManager(mgr);
        mPhotoList.addOnScrollListener(new InfiniteScrollListener(100, mgr) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition)
            {
                Log.d(TAG, "onScrolledToEnd: first visible position " + firstVisibleItemPosition);
                if(mIsQuerying) return;
                // Only load when not busy
                loadAdditionalItems(mCurrentSearch);
            }
        });

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

        mVisiblePhotos = new ArrayList<>();
        mPhotoListAdapter = new PresentableAdapter<>(new PhotoItemPresenter(), mVisiblePhotos);
        mPhotoList.setAdapter(mPhotoListAdapter);
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

        // Reset values for new search
        mCurrentSearch = null;
        mSearchTags = null;

        // UI
        Utils.closeKeyboard(this);
        mProgress.setVisibility(View.VISIBLE);
        mPhotoList.setItemViewCacheSize(View.GONE);

        // Execute API request
        mIsQuerying = true;
        mSearchTags = Utils.joinTags(tags, "%2C");
        String flickrQuery = mQueryURL + "&tags=" + mSearchTags;
        FlickrSearchTask task = new FlickrSearchTask(flickrQuery, this);
        task.execute();
    }

    @Override
    public void onFlickrSearchCompleted(SearchResponseModel response)
    {
        mIsQuerying = false;
        int resultCount = response.getPhotos().getPerPage();
        Toast.makeText(this, "Loaded " + resultCount + " photos from Flickr", Toast.LENGTH_SHORT).show();

        if(resultCount > 0)
        {
            mCurrentSearch = response.getPhotos();

            PhotoMetaModel[] metaModels = mCurrentSearch.getPhoto();
            List<PhotoMetaModel> modelsToAdd = Arrays.asList(metaModels);
            for(PhotoMetaModel item : modelsToAdd)
            {
                // GET sizes for each photo
                String url = String.format("https://api.flickr.com/services/rest/?method=flickr.photos.getSizes" +
                                "&api_key=%s" +
                                "&photo_id=%s" +
                                "&format=json" +
                                "&nojsoncallback=1",
                                BuildConfig.FLICKR_API_KEY, item.getId());
                FlickrImageTask task = new FlickrImageTask(url);
                task.execute();
            }
            mVisiblePhotos.addAll(modelsToAdd);
            mPhotoListAdapter.notifyDataSetChanged();

            Log.d(TAG, "onFlickrSearchCompleted: photos " + metaModels.length);

            mProgress.setVisibility(View.GONE);
            mPhotoList.setVisibility(View.VISIBLE);
            mSearchField.getText().clear();
        }
        else {
            // Hide views when showing Toast message
            mProgress.setVisibility(View.GONE);
            mPhotoList.setVisibility(View.GONE);
            mSearchField.getText().clear();
        }
    }

    @Override
    public void onFlickrSearchError(FlickrErrorResponse errorResponse)
    {
        mIsQuerying = false;
        Log.e(TAG, "onFlickrSearchError: Request was not successful");
        String msg = (errorResponse == null) ? getString(R.string.flickr_search_error) : errorResponse.getMessage();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // UI updates after request
        mProgress.setVisibility(View.GONE);
        mPhotoList.setItemViewCacheSize(View.GONE);
        mSearchField.getText().clear();
    }

    private void loadAdditionalItems(PhotosResponseModel search)
    {
        int newPage = search.getPage() + 1;
        Log.d(TAG, "loadAdditionalItems: load items from page " + newPage);

        mIsQuerying = true;

        String flickrQuery = mQueryURL +
                "&tags=" + mSearchTags +
                "&page=" + newPage;
        FlickrSearchTask task = new FlickrSearchTask(flickrQuery, this);
        task.execute();
    }
}

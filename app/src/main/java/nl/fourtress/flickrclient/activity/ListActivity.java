package nl.fourtress.flickrclient.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.fourtress.flickrclient.BuildConfig;
import nl.fourtress.flickrclient.FlickrClient;
import nl.fourtress.flickrclient.R;
import nl.fourtress.flickrclient.flickr.FlickrAPI;
import nl.fourtress.flickrclient.flickr.FlickrDownloadImageTask;
import nl.fourtress.flickrclient.flickr.model.ImageSizesResponseModel;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.PhotosResponseModel;
import nl.fourtress.flickrclient.flickr.model.SearchResponseModel;
import nl.fourtress.flickrclient.flickr.model.SizeModel;
import nl.fourtress.flickrclient.presenter.PhotoItemPresenter;
import nl.fourtress.flickrclient.util.ListItem;
import nl.fourtress.flickrclient.util.ShakeListener;
import nl.fourtress.flickrclient.util.Utils;
import nl.rwslinkman.presentable.PresentableAdapter;
import nl.rwslinkman.presentable.PresentableItemClickListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Rick Slinkman
 */
public class ListActivity extends AppCompatActivity implements View.OnClickListener,
        FlickrDownloadImageTask.FlickrImageDownloadCompletedListener,
        PresentableItemClickListener<ListItem>,
        View.OnKeyListener,
        ShakeListener.OnShakeListener, Callback<SearchResponseModel> {
    private static final String TAG = "ListActivity";
    private static final int PHOTOS_PER_PAGE = 15;

    private RecyclerView mPhotoList;
    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;
    private EditText mSearchField;
//    private String mQueryURL;
    private PresentableAdapter<ListItem> mPhotoListAdapter;
    private ArrayList<ListItem> mVisiblePhotos;
    private PhotosResponseModel mCurrentSearch;
    private String mSearchTags;
    private boolean mIsQuerying;
    private int mExpected;
    private ShakeListener shakeListener;
    private FlickrAPI mFlickerAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mPhotoList = (RecyclerView) findViewById(R.id.flickr_photo_list);
        LinearLayoutManager mgr = new LinearLayoutManager(this);
        mPhotoList.setLayoutManager(mgr);
        mPhotoList.addOnScrollListener(new InfiniteScrollListener(PHOTOS_PER_PAGE, mgr) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition)
            {
                if(mIsQuerying) return;
                // Only load when not busy
                loadAdditionalItems(mCurrentSearch);
            }
        });
        mPhotoList.setItemAnimator(new DefaultItemAnimator());
        mVisiblePhotos = new ArrayList<>();
        mPhotoListAdapter = new PresentableAdapter<>(new PhotoItemPresenter(), mVisiblePhotos);
        mPhotoListAdapter.setItemClickListener(this);
        mPhotoList.setAdapter(mPhotoListAdapter);

        mProgress = (ProgressBar) findViewById(R.id.flickr_search_progress);
        mSearchButton = (FloatingActionButton) findViewById(R.id.flickr_search_btn);
        mSearchField = (EditText) findViewById(R.id.flickr_search_field);
        mSearchField.setOnKeyListener(this);

//        https://api.flickr.com/services/rest/?method=
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.flickr.com/services/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // prepare call in Retrofit 2.0
        mFlickerAPI = retrofit.create(FlickrAPI.class);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mSearchButton.setOnClickListener(this);

        shakeListener = new ShakeListener(this);
        shakeListener.startListening(this);
    }

    @Override
    protected void onPause()
    {
        mSearchButton.setOnClickListener(null);
        shakeListener.stopListening();
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
        mVisiblePhotos.clear();
        mPhotoListAdapter.notifyDataSetChanged();

        // UI
        Utils.closeKeyboard(this);
        mProgress.setVisibility(View.VISIBLE);
        mPhotoList.setVisibility(View.GONE);

        Toast.makeText(this, getString(R.string.flickr_search_loading), Toast.LENGTH_LONG).show();

        // Execute API request
        mIsQuerying = true;
        mSearchTags = Utils.joinTags(tags, "%2C");

        Call<SearchResponseModel> call = mFlickerAPI.searchImages(BuildConfig.FLICKR_API_KEY, PHOTOS_PER_PAGE, mSearchTags);
        call.enqueue(this);
    }

    private void loadAdditionalItems(PhotosResponseModel search)
    {
        int newPage = search.getPage() + 1;

        mIsQuerying = true;
        Toast.makeText(this, getString(R.string.flickr_search_loading), Toast.LENGTH_LONG).show();

        Call<SearchResponseModel> call = mFlickerAPI.searchImages(BuildConfig.FLICKR_API_KEY, PHOTOS_PER_PAGE, mSearchTags, newPage);
        call.enqueue(this);
    }

    @Override
    public void onFlickrImageDownloaded(PhotoMetaModel item, SizeModel[] sizes, Bitmap result)
    {
        ListItem photoItem = new ListItem(item, result, sizes);
        mVisiblePhotos.add(photoItem);
        // Redraw list
        mPhotoListAdapter.notifyDataSetChanged();

        // Show list
        mProgress.setVisibility(View.GONE);
        mPhotoList.setVisibility(View.VISIBLE);

        // Finalize last loading
        if(mVisiblePhotos.size() == mExpected) {
            Toast.makeText(this, getString(R.string.flickr_search_loaded), Toast.LENGTH_SHORT).show();
            mExpected = 0;
            mIsQuerying = false;
        }
    }

    @Override
    public void onItemClicked(ListItem item)
    {
        FlickrClient app = (FlickrClient) getApplication();
        app.setTempItem(item);

        Log.d(TAG, "onItemClicked: item " + item.getMeta().getId());
        Intent detailIntent = new Intent(this, DetailActivity.class);
        startActivity(detailIntent);
    }

    @Override
    public void onItemSelected(ListItem item) {
        // NOP
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    // Simulate button click
                    onClick(mSearchButton);
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public void onShake()
    {
        if(mVisiblePhotos.size() > 0) {
            Toast.makeText(this, getString(R.string.easter_egg_shuffle), Toast.LENGTH_SHORT).show();
            Collections.shuffle(mVisiblePhotos);
            mPhotoListAdapter.notifyDataSetChanged();
            Log.d(TAG, "Device shaked; shuffle pictures");
        }
    }

    @Override
    public void onResponse(Call<SearchResponseModel> call, Response<SearchResponseModel> response)
    {
        Log.d(TAG, "onResponse: ");

        if(response.isSuccessful())
        {
            SearchResponseModel body = response.body();
            int resultCount = body.getPhotos().getPerPage();
            if(resultCount > 0) {
                mCurrentSearch = body.getPhotos();
                PhotoMetaModel[] metaModels = mCurrentSearch.getPhoto();
                mExpected = mVisiblePhotos.size() + metaModels.length;
            } else {
                Toast.makeText(this, getString(R.string.flickr_search_none_found), Toast.LENGTH_SHORT).show();
            }

            List<PhotoMetaModel> modelsToAdd = Arrays.asList(body.getPhotos().getPhoto());
            for (final PhotoMetaModel item : modelsToAdd)
            {
                Log.d(TAG, "onResponse: photometa item " + item.getId());

                Call<ImageSizesResponseModel> sizeCall = mFlickerAPI.getItemSizes(BuildConfig.FLICKR_API_KEY, item.getId());
                sizeCall.enqueue(new Callback<ImageSizesResponseModel>() {
                    @Override
                    public void onResponse(Call<ImageSizesResponseModel> call, Response<ImageSizesResponseModel> response) {
                        if(response.isSuccessful())
                        {
                            ImageSizesResponseModel sizeResponse = response.body();

                            int desiredThumbWidth = 650;
                            SizeModel thumbNail = null;
                            SizeModel[] sizes = sizeResponse.getSizes().getSize();
                            for(SizeModel size : sizes)
                            {
                                if(size.getWidth() <= desiredThumbWidth)
                                {
                                    thumbNail = size;
                                }
                            }

                            if(thumbNail != null) {
                                // Download image
                                FlickrDownloadImageTask downloadTask = new FlickrDownloadImageTask(thumbNail.getSource(), item, sizes, ListActivity.this);
                                downloadTask.execute();
                            } else {
                                Log.e(TAG, "onFlickrImageTaskCompleted: Size unavailable for item " + item.getId());
                            }
                            Log.d(TAG, "onResponse: check 't uut " );
                        }
                        else {
                            mIsQuerying = false;
                            Log.e(TAG, "onFlickrSearchError: Request was not successful");
                            String msg = getString(R.string.flickr_search_error);
                            Toast.makeText(ListActivity.this, msg, Toast.LENGTH_SHORT).show();

                            // UI updates after request
                            mProgress.setVisibility(View.GONE);
                            mPhotoList.setItemViewCacheSize(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageSizesResponseModel> call, Throwable t)
                    {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onFailure(Call<SearchResponseModel> call, Throwable t)
    {
        Log.d(TAG, "onFailure: " + t.getMessage());
    }
}

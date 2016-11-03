package nl.fourtress.flickrclient.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import nl.fourtress.flickrclient.FlickrClient;
import nl.fourtress.flickrclient.ListItem;
import nl.fourtress.flickrclient.R;
import nl.fourtress.flickrclient.flickr.FlickrDownloadImageTask;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.SizeModel;

/**
 * @author Rick Slinkman
 */
public class DetailActivity extends AppCompatActivity implements FlickrDownloadImageTask.FlickrImageDownloadCompletedListener {
    private static final String TAG = "DetailActivity";
    private ImageView mLargePhoto;
    private ProgressBar mLoader;
    private ScrollView mContent;
    private TextView mInfoOwner, mInfoServer, mInfoSize, mTitleView, mLinkView;
    private SizeModel mUsedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        FlickrClient app = (FlickrClient) getApplication();
        ListItem clickedItem = app.getTempItem();
        if(clickedItem == null) {
            Toast.makeText(this, getString(R.string.detail_no_clicked_image), Toast.LENGTH_SHORT).show();
            return;
        }

        mLoader = (ProgressBar) findViewById(R.id.detail_loading_progress);
        mLargePhoto = (ImageView) findViewById(R.id.detail_large_photo);

        mContent = (ScrollView) findViewById(R.id.detail_content);
        mTitleView = (TextView) findViewById(R.id.detail_info_title);
        mLinkView = (TextView) findViewById(R.id.detail_info_url);
        mInfoOwner = (TextView) findViewById(R.id.detail_info_owner);
        mInfoServer = (TextView) findViewById(R.id.detail_info_server);
        mInfoSize = (TextView) findViewById(R.id.detail_info_size);

        String biggestPictureURL = "";
        int biggestWidth = 0;
        for(SizeModel size : clickedItem.getSizes())
        {
            int width = size.getWidth();
            if(width > biggestWidth) {
                biggestWidth = width;
                biggestPictureURL = size.getSource();
                mUsedSize = size;
            }
        }

        mLoader.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);

        new FlickrDownloadImageTask(biggestPictureURL, clickedItem.getMeta(), null, this).execute();
        Log.d(TAG, "onCreate: Picture with width " + biggestWidth + " found at " + biggestPictureURL);
    }

    @Override
    public void onFlickrImageDownloaded(PhotoMetaModel item, SizeModel[] sizes, Bitmap result)
    {
        mLargePhoto.setImageBitmap(result);
        mTitleView.setText(item.getTitle());
        mLinkView.setText(mUsedSize.getSource());
        mInfoServer.setText(item.getServer());
        mInfoSize.setText(mUsedSize.getLabel());
        mInfoOwner.setText(item.getOwner());

        mLoader.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
    }
}

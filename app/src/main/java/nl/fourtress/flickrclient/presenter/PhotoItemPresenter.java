package nl.fourtress.flickrclient.presenter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import nl.fourtress.flickrclient.BuildConfig;
import nl.fourtress.flickrclient.ListItem;
import nl.fourtress.flickrclient.R;
import nl.fourtress.flickrclient.flickr.FlickrImageTask;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.rwslinkman.presentable.Presenter;

/**
 * @author Rick Slinkman
 */
public class PhotoItemPresenter implements Presenter<ListItem, PhotoItemPresenter.ViewHolder>
{
    private static final String TAG = "PhotoItemPresenter";

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater.from(parent.getContext()));
        View v = inflater.inflate(R.layout.item_photo, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.thumbNail = (ImageView) v.findViewById(R.id.item_photo_image);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, ListItem item)
    {
        viewHolder.thumbNail.setImageBitmap(item.getThumbnail());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // NOP
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView thumbNail;

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

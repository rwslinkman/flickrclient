package nl.fourtress.flickrclient.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nl.fourtress.flickrclient.BuildConfig;
import nl.fourtress.flickrclient.R;
import nl.fourtress.flickrclient.flickr.FlickrImageTask;
import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.rwslinkman.presentable.Presenter;

/**
 * @author Rick Slinkman
 */
public class PhotoItemPresenter implements Presenter<PhotoMetaModel, PhotoItemPresenter.ViewHolder>
{
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater.from(parent.getContext()));
        View v = inflater.inflate(R.layout.item_photo, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.test = (TextView) v.findViewById(R.id.item_photo_test);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, PhotoMetaModel item)
    {
        viewHolder.test.setText(item.getId());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // NOP
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView test;

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

package nl.fourtress.flickrclient;

import android.graphics.Bitmap;

import nl.fourtress.flickrclient.flickr.model.PhotoMetaModel;
import nl.fourtress.flickrclient.flickr.model.SizeModel;

/**
 * @author Rick Slinkman
 */

public class ListItem
{
    private SizeModel[] mSizes;
    private PhotoMetaModel mMeta;
    private Bitmap mThumbnail;

    public ListItem(PhotoMetaModel item, Bitmap thumb, SizeModel[] sizes)
    {
        this.mMeta = item;
        this.mThumbnail = thumb;
        this.mSizes = sizes;
    }

    public PhotoMetaModel getMeta() {
        return mMeta;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public SizeModel[] getSizes() {
        return mSizes;
    }
}

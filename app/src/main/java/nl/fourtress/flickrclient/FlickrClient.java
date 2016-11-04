package nl.fourtress.flickrclient;

import android.app.Application;

import nl.fourtress.flickrclient.util.ListItem;

/**
 * @author Rick Slinkman
 */
public class FlickrClient extends Application
{
    private ListItem mTempItem;

    public ListItem getTempItem() {
        return mTempItem;
    }

    public void setTempItem(ListItem mTempItem) {
        this.mTempItem = mTempItem;
    }
}

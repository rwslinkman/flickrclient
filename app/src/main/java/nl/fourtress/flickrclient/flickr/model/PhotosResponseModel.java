package nl.fourtress.flickrclient.flickr.model;

/**
 * Created by rick.slinkman on 02-Nov-16.
 */

public class PhotosResponseModel
{
    private int page;
    private int pages;
    private int perpage;
    private String total;
    private PhotoMetaModel[] photo;


    public String getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public int getPerpage() {
        return perpage;
    }

    public PhotoMetaModel[] getPhoto() {
        return photo;
    }
}

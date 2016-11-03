package nl.fourtress.flickrclient.flickr.model;

/**
 * @author Rick Slinkman
 */
public class ImageSizesModel
{
    private int canblog;
    private int canprint;
    private int candownload;
    private SizeModel[] size;

    public boolean canBlog() {
        return canblog == 1;
    }

    public boolean canPrint() {
        return canprint == 1;
    }

    public boolean canDownload() {
        return candownload == 1;
    }

    public SizeModel[] getSize() {
        return size;
    }
}

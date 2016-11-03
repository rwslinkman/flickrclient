package nl.fourtress.flickrclient.flickr.model;

/**
 * @author Rick Slinkman
 */
public class ImageSizesResponseModel
{
    private String stat;
    private ImageSizesModel sizes;

    public String getStat() {
        return stat;
    }

    public ImageSizesModel getSizes() {
        return sizes;
    }
}

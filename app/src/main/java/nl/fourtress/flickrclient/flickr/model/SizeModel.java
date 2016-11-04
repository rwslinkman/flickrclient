package nl.fourtress.flickrclient.flickr.model;

/**
 * @author Rick Slinkman
 */
public class SizeModel
{
    private String label;
    private int width;
    private int height;
    private String source;
    private String url;
    private String media;

    public String getLabel() {
        return label;
    }

    public String getURL() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

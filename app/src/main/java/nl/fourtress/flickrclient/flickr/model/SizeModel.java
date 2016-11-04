package nl.fourtress.flickrclient.flickr.model;

/**
 * @author Rick Slinkman
 */
public class SizeModel
{
    private String label;
    private Object width;
    private Object height;
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

    public int getWidth()
    {
        String w = width.toString();
        return (int) Double.parseDouble(w);
    }

    public int getHeight()
    {
        String h = height.toString();
        return (int) Double.parseDouble(h);
    }
}

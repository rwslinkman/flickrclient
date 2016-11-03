package nl.fourtress.flickrclient.flickr.model;

/**
 * Created by rick.slinkman on 03-Nov-16.
 */

public class SearchErrorResponse
{
    private String stat;
    private int code;
    private String message;

    public String getMessage() {
        return message;
    }
}

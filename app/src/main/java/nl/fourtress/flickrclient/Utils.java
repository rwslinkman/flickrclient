package nl.fourtress.flickrclient;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author Rick Slinkan
 */
public class Utils
{
    public static String joinTags(String[] tags, String delim)
    {
        StringBuilder sb = new StringBuilder();
        String loopDelim = "";
        for(String tag : tags) {
            sb.append(loopDelim);
            sb.append(tag);
            loopDelim = delim;
        }
        return sb.toString();
    }

    public static void closeKeyboard(Activity a)
    {
        View view = a.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

package nl.fourtress.flickrclient.flickr;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rick Slinkman
 */
abstract class HttpRequestTask extends AsyncTask<Void, Void, Void>
{
    public enum Result {
        SUCCESS,
        TIMEOUT,
        ERROR
    }
    static final String GET = "GET";
    static final String POST = "POST";
    static final String PUT = "PUT";
    static final String DELETE = "DELETE";
    static final List<String> ALLOWED_METHODS = Arrays.asList(GET, POST, PUT, DELETE);
    private static final String TAG = "TelevisionApiTask";
    private static final int TIMEOUT = 4000; // milliseconds
    // Request variables
    private Map<String,String> headers;
    private String url;
    private String method;
    // Response variables
    private int responseCode;
    private String responseBody;
    private Result result;

    HttpRequestTask(String endpoint, String method)
    {
        if(!ALLOWED_METHODS.contains(method)) {
            throw new IllegalArgumentException("Method '" + method + "' is not allowed");
        }

        this.url = endpoint;
        this.headers = new HashMap<>();
        this.method = method;
    }

    @Override
    final protected Void doInBackground(Void... aVoid)
    {
        this.responseCode = 0;
        this.responseBody = "";
        String json = "";
        boolean hasBody = this.method.equals(POST) || this.method.equals(PUT);
        if(hasBody) {
            try {
                json = getRequestBody();
                if(json == null || json.isEmpty()) {
                    // JSON is required when using POST or PUT
                    throw new IllegalArgumentException("Method was '" + this.method + "' but no JSON request body was given");
                }
            }
            catch(JSONException jEx)
            {
                throw new IllegalArgumentException("Request body is not valid JSON");
            }
        }

        Log.d(TAG, "doInBackground: " + this.method + " " + this.url);

        // Create request
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(hasBody);
            connection.setDoInput(true);
            connection.setRequestMethod(this.method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            for(Map.Entry<String, String> entry : this.headers.entrySet())
            {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            if(hasBody) {
                Log.d(TAG, "doInBackground: JSON Body " + json);
                // Define request body
                OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
                streamWriter.write(json);
                streamWriter.flush();
            }

            // Handle response
            this.responseCode = connection.getResponseCode();
            Log.d(TAG, "doInBackground: API response code [" + Integer.toString(this.responseCode) + "]");
            InputStream inputStream = isSuccessful() ? connection.getInputStream() : connection.getErrorStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            // Response body into buffer
            StringBuilder stringBuilder = new StringBuilder();
            String response;
            while ((response = bufferedReader.readLine()) != null) {
                stringBuilder.append(response).append("\n");
            }
            // Close up and to JSON string
            bufferedReader.close();
            this.responseBody = stringBuilder.toString();
            this.result = isSuccessful() ? Result.SUCCESS : Result.ERROR;
        }
        catch (Exception exception) {
            // Error happened while executing request
            Log.e(TAG, "doInBackground: API Request failed " + exception.getMessage());
            result = Result.TIMEOUT;
            this.responseCode = -1;
        }
        finally {
            // Finalize
            if (connection != null){
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    final protected void onPostExecute(Void aVoid)
    {
        this.onRequestComplete();
    }

    public String getRequestBody() throws JSONException {
        // To be overridden by children
        return null;
    }
    abstract protected void onRequestComplete();

    // Method to check response
    protected String getResponseBody()
    {
        return this.responseBody;
    }

    protected boolean isSuccessful()
    {
        return this.getResponseCode() >= 0 && ((this.getResponseCode() - 200) < 100);
    }

    protected int getResponseCode()
    {
        return this.responseCode;
    }

    protected Result getResult()
    {
        return this.result;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void addHeader(String header, String headerValue)
    {
        this.headers.put(header, headerValue);
    }
}

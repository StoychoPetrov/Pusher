package eu.mobile.pusher;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Stoycho Petrov on 17.2.2018 г..
 */

public class ConnectionHttp extends AsyncTask<String, Void, String> {

    private JSONObject mPostData;

    public ConnectionHttp(JSONObject postData){
        mPostData = postData;
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            StringBuilder dataGetChanges = new StringBuilder();
            URL urlForData = new URL(strings[0]);

            HttpURLConnection urlConnectionGetChanges = (HttpURLConnection) urlForData.openConnection();
            urlConnectionGetChanges.setDoInput(true);
            urlConnectionGetChanges.setDoOutput(true);
            urlConnectionGetChanges.setRequestProperty("Accept", "application/json");
            urlConnectionGetChanges.setRequestMethod("POST");

            if (mPostData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnectionGetChanges.getOutputStream());
                writer.write(mPostData.toString());
                writer.flush();
            }

            int statusCode = urlConnectionGetChanges.getResponseCode();

            BufferedReader bufferedReaderGetChanges = new BufferedReader(new InputStreamReader(urlConnectionGetChanges.getInputStream()));
            String nextGetChanges = null;

            nextGetChanges = bufferedReaderGetChanges.readLine();

            while (nextGetChanges != null) {
                dataGetChanges.append(nextGetChanges);
                nextGetChanges = bufferedReaderGetChanges.readLine();
            }
            urlConnectionGetChanges.disconnect();

            return dataGetChanges.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}

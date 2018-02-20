package eu.mobile.fashionpoint;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Stoycho Petrov on 17.2.2018 г..
 */

public class ConnectionHttp extends AsyncTask<String, Void, String> {

    private String              mUserName;
    private String              mPassword;
    private View                mProgress;
    private OnAnswerReceived    mListener;

    public interface OnAnswerReceived {
        void onAnswerReceived(JSONObject answer);
    }

    public ConnectionHttp(String username, String password){
        mUserName   = username;
        mPassword   = password;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(mProgress != null)
            mProgress.setVisibility(View.VISIBLE);
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

            String data = URLEncoder.encode("_username", "UTF-8") + "=" + URLEncoder.encode(mUserName, "UTF-8");
            data += "&" + URLEncoder.encode("_password", "UTF-8") + "=" + URLEncoder.encode(mPassword, "UTF-8");

            OutputStreamWriter writer = new OutputStreamWriter(urlConnectionGetChanges.getOutputStream());
            writer.write(data);
            writer.flush();

            int statusCode = urlConnectionGetChanges.getResponseCode();

            BufferedReader bufferedReaderGetChanges = new BufferedReader(new InputStreamReader(urlConnectionGetChanges.getInputStream()));
            String nextGetChanges = null;

            nextGetChanges = bufferedReaderGetChanges.readLine();

            while (nextGetChanges != null) {
                dataGetChanges.append(nextGetChanges);
                nextGetChanges = bufferedReaderGetChanges.readLine();
            }
            urlConnectionGetChanges.disconnect();

            Log.d("login", dataGetChanges.toString());

            return dataGetChanges.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if(mProgress != null)
            mProgress.setVisibility(View.GONE);

        try {
            if(mListener != null && s != null)
                mListener.onAnswerReceived(new JSONObject(s));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public View getmProgress() {
        return mProgress;
    }

    public void setmProgress(View mProgress) {
        this.mProgress = mProgress;
    }

    public OnAnswerReceived getmListener() {
        return mListener;
    }

    public void setmListener(OnAnswerReceived mListener) {
        this.mListener = mListener;
    }
}
package eu.mobile.fashionpoint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;

/**
 * Created by Stoycho Petrov on 18.2.2018 г..
 */

public class DownloadImage extends AsyncTask<Void, Void, Bitmap> {

    private String              mDownloadUrl;
    private DownloadCompleted   mDownloadListener;

    public interface DownloadCompleted {
        void onDownloadSuccess(Bitmap bitmap);
        void onDownloadFailed();
    }

    public DownloadImage(String downloadUrl){
        mDownloadUrl = downloadUrl;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        Bitmap bitmap = null;
        try {
            // Download Image from URL
            InputStream input = new java.net.URL(mDownloadUrl).openStream();
            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            if(mDownloadListener != null)
                mDownloadListener.onDownloadFailed();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if(bitmap != null)
            mDownloadListener.onDownloadSuccess(bitmap);
    }

    public DownloadCompleted getmDownloadListener() {
        return mDownloadListener;
    }

    public void setmDownloadListener(DownloadCompleted mDownloadListener) {
        this.mDownloadListener = mDownloadListener;
    }
}

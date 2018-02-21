package eu.mobile.fashionpoint;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class WebViewActivity extends AppCompatActivity implements View.OnClickListener{

    private WebView                 mWebView;
    private FloatingActionButton    mExitFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        if(getIntent() != null) {
            mWebView    = (WebView)                 findViewById(R.id.webview);
            mExitFab    = (FloatingActionButton)    findViewById(R.id.exit_icon);

            mExitFab.setOnClickListener(this);

            loadUrl();
            receivedMessageListener();
        }
    }

    private void loadUrl(){
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                view.loadUrl(url);
                Log.d("ShouldOverrideUrl", url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("onPageFinished", url);

                super.onPageFinished(view, url);

                findViewById(R.id.progress_layout).setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                Log.d("onPageStarted", url);

                findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        mWebView.postUrl("https://fashionpoint.bg/profile/login_check", generatePostRequest().getBytes());
    }

    private void receivedMessageListener(){
        PushNotifications.setOnMessageReceivedListener(new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                RemoteMessage.Notification notification = remoteMessage.getNotification();
                if(notification != null) {
                    Drawable drawable = ContextCompat.getDrawable(WebViewActivity.this, R.drawable.ic_push);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(WebViewActivity.this)
                            .setSmallIcon(R.drawable.ic_push2)
                            .setLargeIcon(((BitmapDrawable) drawable).getBitmap())
                            .setContentTitle(notification.getTitle())

                            .setContentText(notification.getBody());

                    Intent intent = new Intent(WebViewActivity.this, WebViewActivity.class);
                    intent.putExtra("url_to_open", remoteMessage.getData().get("url_to_open"));

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    WebViewActivity.this,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    builder.setAutoCancel(true);
                    builder.setContentIntent(resultPendingIntent);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (manager != null) {
                        manager.notify(0, builder.build());
                    }
                }
            }
        });
    }

    private String generatePostRequest(){
        String postRequest = "";
        try {
            postRequest += "_username="  + URLEncoder.encode(PreferenceManager.getDefaultSharedPreferences(this).getString("username", ""), "UTF-8");
            postRequest += "&_password=" + URLEncoder.encode(PreferenceManager.getDefaultSharedPreferences(this).getString("password", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return postRequest;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mExitFab.getId()){
            SharedPreferences           sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor    editor            = sharedPreferences.edit();

            PushNotifications.start(getApplicationContext(), "f1373bb0-1b5f-4939-8a25-5d730bd0037a");
            PushNotifications.unsubscribe("mobile_" + sharedPreferences.getInt("user_id", -1));

            editor.putString("username", "");
            editor.putString("password", "");
            editor.putInt("user_id", -1);

            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            Log.d("processHTML", html);
        }
    }
}

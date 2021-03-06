package eu.mobile.fashionpoint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;


public class WebViewActivity extends AppCompatActivity{

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private WebView                 mWebView;
    private SwipeRefreshLayout      mSwipeRefreshLayout;

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    private Handler                 mHandler;
    private Runnable                mRunnable;

    private Intent                 mChatHeadServiceIntent;

    private boolean                 mUrlFromNotLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        requestPermissions();

        if(isTablet()){
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if(getIntent() != null) {
//            mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
            mWebView            = (WebView)                 findViewById(R.id.webview);

            mChatHeadServiceIntent = new Intent(WebViewActivity.this, ChatHeadService.class);

            setListeners();
            createChannel();
            loadUrl();
            receivedMessageListener();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createShortcuts();
        }
    }

    private void setListeners(){
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mWebView.reload();
//            }
//        });
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createShortcuts(){
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("LOG_OUT");

        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "id1")
                .setShortLabel("Log out")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_exit))
                .setIntent(intent)
                .build();

        if (shortcutManager != null) {
            shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
        }
    }

    private void requestPermissions() {
// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waitinиg for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private boolean isTablet() {
        try {
            // Compute screen size
            DisplayMetrics dm = getResources().getDisplayMetrics();
            float screenWidth  = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2) +
                    Math.pow(screenHeight, 2));
            // Tablet devices should have a screen size greater than 6 inches
            return size >= 6;
        } catch(Throwable t) {
            return false;
        }

    }

    private void createChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier("bell_small", "raw", getPackageName()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            // The user-visible name of th
            // e channel.
            CharSequence name = "Product";
            // The user-visible description of the channel.
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel mChannel = new NotificationChannel(getString(R.string.channel_id), name, NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            mChannel.enableLights(true);
            mChannel.setSound(sound, audioAttributes);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setVibrationPattern(new long[]{300, 200, 300, 200, 300});
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);

            notificationManager.createNotificationChannel(mChannel);
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

                if(getIntent() != null && getIntent().hasExtra("url_to_open") && !mUrlFromNotLoaded) {
                    mWebView.loadUrl(getIntent().getStringExtra("url_to_open"));
                    mUrlFromNotLoaded = true;
                }

//                mSwipeRefreshLayout.setRefreshing(false);

//                if(mHandler != null && mRunnable != null)
//                    mHandler.removeCallbacks(mRunnable);
//
//                if()
//                setHandler();

//                if(getIntent() != null
//                        && getIntent().getStringExtra("url") != null
//                        && !getIntent().getStringExtra("url").isEmpty()
//                        && url.equalsIgnoreCase())
//                    mWebView.loadUrl(getIntent().getStringExtra("url"));
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                Log.d("onPageStarted", url);

//                if(!mSwipeRefreshLayout.isRefreshing())
                    findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        mWebView.postUrl("https://fashionpoint.bg/profile/login_check", generatePostRequest().getBytes());
    }

    private void setHandler(){
        mHandler    = new Handler();
        mRunnable   = new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(getIntent().getStringExtra("url"));
            }
        };

        mHandler.postDelayed(mRunnable, 1000);
    }

    private void wakeUp(){
        // Turn on the screen for notification
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean result= Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isInteractive()|| Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isScreenOn();

        if (!result){
            PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
            wl_cpu.acquire(10000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        mSwipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
//                new ViewTreeObserver.OnScrollChangedListener() {
//                    @Override
//                    public void onScrollChanged() {
//                        if (mWebView.getScrollY() == 0)
//                            mSwipeRefreshLayout.setEnabled(true);
//                        else
//                            mSwipeRefreshLayout.setEnabled(false);
//                    }
//                });
    }

    @Override
    protected void onStop() {
        super.onStop();

//        mSwipeRefreshLayout.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }

    private void receivedMessageListener(){
        PushNotifications.setOnMessageReceivedListener(new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                wakeUp();
                RemoteMessage.Notification notification = remoteMessage.getNotification();
                createNotification(notification, remoteMessage.getData().get("url_to_open"));

//                stopService(new Intent(WebViewActivity.this, ChatHeadService.class));

                Log.d("background_notification", "receive");
            }
        });
    }

    private void createNotification(RemoteMessage.Notification notification, String url){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(notification.getSound(), "raw", getPackageName()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            // The user-visible name of the channel.
            CharSequence name = "Product";
            // The user-visible description of the channel.
            String description = notification.getBody();
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel mChannel = new NotificationChannel(getString(R.string.channel_id), name, NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setSound(sound, audioAttributes);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setVibrationPattern(new long[] { 300, 200, 300, 200, 300});
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);

            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent1 = new Intent(this, WebViewActivity.class);
        intent1.putExtra("url_to_open", url);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),getString(R.string.channel_id))
                .setSmallIcon(R.drawable.notification_icon) //your app icon
                .setBadgeIconType(R.drawable.ic_push) //your app icon
                .setChannelId(getString(R.string.channel_id))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setSound(sound)
                .setAutoCancel(true)
                .setVibrate(new long[]{300, 200, 300, 200, 300})
                .setContentIntent(pendingIntent)
                .setNumber(1)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis());

        notificationManager.notify(1, notificationBuilder.build());
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

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            Log.d("processHTML", html);
        }
    }
}

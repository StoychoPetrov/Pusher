package eu.mobile.pusher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.messaging.RemoteMessage;
import com.pusher.android.PusherAndroid;
import com.pusher.android.notifications.ManifestValidator;
import com.pusher.android.notifications.PushNotificationRegistration;
import com.pusher.android.notifications.fcm.FCMPushNotificationReceivedListener;
import com.pusher.android.notifications.tokens.PushNotificationRegistrationListener;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ConnectionHttp.OnAnswerReceived, View.OnClickListener {

    private static final int    PLAY_SERVICES_RESOLUTION_REQUEST    = 9000;
    private static final String STATUS_KEY                          = "status";
    private static final String ID_KEY                              = "id";

    private Button      mLoginBtn;
    private EditText    mUsernameEdt;
    private EditText    mPasswordEdt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        setListerners();

        connectPusherAndSubscribe("mobile_" + 4, "mobile_" + 4);
    }

    private void initUI(){
        mLoginBtn       = (Button)      findViewById(R.id.log_in);
        mUsernameEdt    = (EditText)    findViewById(R.id.user_name_edt);
        mPasswordEdt    = (EditText)    findViewById(R.id.password_edt);
    }

    private void setListerners(){
        mLoginBtn.setOnClickListener(this);
    }

    private void login(){
        ConnectionHttp connectionHttp = new ConnectionHttp(mUsernameEdt.getText().toString(), mPasswordEdt.getText().toString());
        connectionHttp.setmProgress(findViewById(R.id.progress_layout));
        connectionHttp.setmListener(this);
        connectionHttp.execute("http://fashionpoint.bg/profile/login_check");
    }

    private void connectPusherAndSubscribe(String channelName, String eventName){

        PusherAndroid pusher = new PusherAndroid("f1373bb0-1b5f-4939-8a25-5d730bd0037a");
        PushNotificationRegistration nativePusher = pusher.nativePusher();
        try {
            nativePusher.registerFCM(this, new PushNotificationRegistrationListener() {
                @Override
                public void onSuccessfulRegistration() {
                    Log.d("hello", "success");
                }

                @Override
                public void onFailedRegistration(int statusCode, String response) {
                    Log.d("hello", "failed");
                }
            });
        } catch (ManifestValidator.InvalidManifestException e) {
            e.printStackTrace();
        }

        PushNotificationRegistration push = pusher.nativePusher();
        push.subscribe("hello");

        push.setFCMListener(new FCMPushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                Log.d("hello", "hello");
            }
        });
//        PushNotifications.start(getApplicationContext(), "f1373bb0-1b5f-4939-8a25-5d730bd0037a");
//
//        PushNotifications.subscribe("hello");
//        PushNotifications.setOnMessageReceivedListener(new PushNotificationReceivedListener() {
//            @Override
//            public void onMessageReceived(RemoteMessage remoteMessage) {
//                Log.i("hello", "hello");
//            }
//        });
    }

    private void showPush(JSONObject data){
        try {
            String title    = data.getString("title");
            String url      = data.getString("url");

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, browserIntent, 0);

            Notification notification = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).build();

            NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if(notifyManager != null) {
                notifyManager.notify(0, notification);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAnswerReceived(JSONObject answer) {
        try {
            if(answer.getBoolean(STATUS_KEY)){
                Toast.makeText(this, getString(R.string.you_are_logged_successful), Toast.LENGTH_SHORT).show();
                connectPusherAndSubscribe("mobile_" + answer.getInt(ID_KEY), "mobile_" + answer.getInt(ID_KEY));
            }
            else
                Toast.makeText(this, getString(R.string.wrong_email_or_password), Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mLoginBtn.getId()) {
            if(!mUsernameEdt.getText().toString().isEmpty() && !mPasswordEdt.getText().toString().isEmpty())
                login();
            else
                Toast.makeText(this, getString(R.string.enter_username_and_password), Toast.LENGTH_SHORT).show();
        }
    }
}

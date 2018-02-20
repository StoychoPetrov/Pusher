package eu.mobile.fashionpoint;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ConnectionHttp.OnAnswerReceived, View.OnClickListener {

    private static final String STATUS_KEY                          = "status";
    private static final String ID_KEY                              = "id";
    private static final String PREFERENCES_USERNAME                = "username";
    private static final String PREFERENCES_PASSWORD                = "password";
    private static final String PREFERENCES_USER_ID                 = "user_id";

    private Button      mLoginBtn;
    private EditText    mUsernameEdt;
    private EditText    mPasswordEdt;

    private SharedPreferences           mSharedPreferences;
    private SharedPreferences.Editor    mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        setListerners();

        if(mSharedPreferences.getInt(PREFERENCES_USER_ID, -1) != -1){
            receivedMessageListener();
        }
    }

    private void initUI(){
        mLoginBtn           = (Button)      findViewById(R.id.log_in);
        mUsernameEdt        = (EditText)    findViewById(R.id.user_name_edt);
        mPasswordEdt        = (EditText)    findViewById(R.id.password_edt);

        mSharedPreferences  = PreferenceManager.getDefaultSharedPreferences(this);
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

    private void connectPusherAndSubscribe(String interest){

        PushNotifications.start(getApplicationContext(), "f1373bb0-1b5f-4939-8a25-5d730bd0037a");

        PushNotifications.subscribe(interest);
        receivedMessageListener();
    }

    private void receivedMessageListener(){
        PushNotifications.setOnMessageReceivedListener(new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                RemoteMessage.Notification notification = remoteMessage.getNotification();
                if(notification != null) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.drawable.ic_push)
                            .setContentTitle(notification.getTitle())
                            .setContentText(notification.getBody());

                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra("url_to_open", remoteMessage.getData().get("url_to_open"));

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    MainActivity.this,
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

    @Override
    public void onAnswerReceived(JSONObject answer) {
        try {
            if(answer.getBoolean(STATUS_KEY)){
                Toast.makeText(this, getString(R.string.you_are_logged_successful), Toast.LENGTH_SHORT).show();
                connectPusherAndSubscribe("mobile_" + answer.getInt(ID_KEY));

                mEditor = mSharedPreferences.edit();
                mEditor.putString(PREFERENCES_USERNAME, mUsernameEdt.getText().toString());
                mEditor.putString(PREFERENCES_PASSWORD, mPasswordEdt.getText().toString());
                mEditor.putInt(PREFERENCES_USER_ID,  answer.getInt(ID_KEY));

                mEditor.apply();
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

package eu.mobile.fashionpoint;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements ConnectionHttp.OnAnswerReceived, View.OnClickListener {

    private Button      mLoginBtn;
    private EditText    mUsernameEdt;
    private EditText    mPasswordEdt;

    private SharedPreferences           mSharedPreferences;
    private SharedPreferences.Editor    mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equalsIgnoreCase("LOG_OUT")) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                    ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                    if (shortcutManager != null) {
                        shortcutManager.removeAllDynamicShortcuts();
                    }
                }
            }

            logOut();
        }

        initUI();
        setListerners();

        if(isTablet()){
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            findViewById(R.id.root_view).setBackgroundResource(R.drawable.tablet_background);
        }

        if(mSharedPreferences.getInt(Utils.PREFERENCES_USER_ID, -1) != -1){
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            findViewById(R.id.root_view).setBackgroundResource(R.drawable.tablet_background);
        }

        if(mSharedPreferences.getInt(Utils.PREFERENCES_USER_ID, -1) != -1){
            openWebViewActivity();
        }
        else {
            mUsernameEdt.setText(mSharedPreferences.getString(Utils.PREFERENCES_USERNAME, ""));
            mPasswordEdt.setText(mSharedPreferences.getString(Utils.PREFERENCES_PASSWORD, ""));
        }
    }

    private void initUI(){
        mLoginBtn           = (Button)      findViewById(R.id.log_in);
        mUsernameEdt        = (EditText)    findViewById(R.id.user_name_edt);
        mPasswordEdt        = (EditText)    findViewById(R.id.password_edt);

        mSharedPreferences  = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void logOut(){
        SharedPreferences           sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor    editor            = sharedPreferences.edit();

        PushNotifications.start(getApplicationContext(), "f1373bb0-1b5f-4939-8a25-5d730bd0037a");
        PushNotifications.unsubscribe("mobile_" + sharedPreferences.getInt("user_id", -1));

        editor.putInt("user_id", -1);

        editor.apply();
    }

    private void setListerners(){
        mLoginBtn.setOnClickListener(this);

        mPasswordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!mUsernameEdt.getText().toString().isEmpty() && !mPasswordEdt.getText().toString().isEmpty()) {
                        try {
                            login();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        Toast.makeText(MainActivity.this, getString(R.string.enter_username_and_password), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
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

    private void login() throws UnsupportedEncodingException {

        String data = URLEncoder.encode("_username", "UTF-8") + "=" + URLEncoder.encode(mUsernameEdt.getText().toString(), "UTF-8");
        data += "&" + URLEncoder.encode("_password", "UTF-8") + "=" + URLEncoder.encode(mPasswordEdt.getText().toString(), "UTF-8");

        ConnectionHttp connectionHttp = new ConnectionHttp(data);
        connectionHttp.setmProgress(findViewById(R.id.progress_layout));
        connectionHttp.setmListener(this);
        connectionHttp.execute(BuildConfig.API_URL + "profile/login_check");
    }

    private void connectPusherAndSubscribe(String interest){

        PushNotifications.start(getApplicationContext(), "f1373bb0-1b5f-4939-8a25-5d730bd0037a");

        PushNotifications.subscribe(interest);

        openWebViewActivity();
    }

    private void openWebViewActivity(){
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onAnswerReceived(String answer) {
        try {

            JSONObject jsonObject = new JSONObject(answer);

            if(jsonObject.getBoolean(Utils.STATUS_KEY)){
                Toast.makeText(this, getString(R.string.you_are_logged_successful), Toast.LENGTH_SHORT).show();
                connectPusherAndSubscribe("mobile_" + jsonObject.getInt(Utils.ID_KEY));

                mEditor = mSharedPreferences.edit();
                mEditor.putString(Utils.PREFERENCES_USERNAME, mUsernameEdt.getText().toString());
                mEditor.putString(Utils.PREFERENCES_PASSWORD, mPasswordEdt.getText().toString());
                mEditor.putInt(Utils.PREFERENCES_USER_ID,  jsonObject.getInt(Utils.ID_KEY));

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
            if(!mUsernameEdt.getText().toString().isEmpty() && !mPasswordEdt.getText().toString().isEmpty()) {
                try {
                    login();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(this, getString(R.string.enter_username_and_password), Toast.LENGTH_SHORT).show();
        }
    }
}

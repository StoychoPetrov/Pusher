package eu.mobile.pusher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pusher.android.PusherAndroid;
import com.pusher.android.PusherAndroidOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

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
        if (playServicesAvailable()) {

            PusherAndroidOptions options = new PusherAndroidOptions();
            options.setCluster("eu");

            PusherAndroid pusher = new PusherAndroid("de1a93d7dd48c7a930fe", options);
            pusher.connect();

            Channel channel = pusher.subscribe(channelName);

            channel.bind(eventName, new SubscriptionEventListener() {
                @Override
                public void onEvent(String channelName, String eventName, final String data) {
                    System.out.println(data);
                }
            });
        }
    }

    private boolean playServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }

            return false;
        }
        return true;
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

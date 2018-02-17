package eu.mobile.pusher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pusher.android.PusherAndroid;
import com.pusher.android.PusherAndroidOptions;
import com.pusher.android.notifications.gcm.GCMPushNotificationReceivedListener;
import com.pusher.android.notifications.tokens.PushNotificationRegistrationListener;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements PushNotificationRegistrationListener{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GCMPushNotificationReceivedListener mListener = new GCMPushNotificationReceivedListener() {
        @Override
        public void onMessageReceived(String from, Bundle data) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("_username", "test");
            jsonObject.put("_password", "test");

            ConnectionHttp connectionHttp = new ConnectionHttp(jsonObject);
            connectionHttp.execute("https://fashionpoint.bg/profile/login_check");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        try {
            if (playServicesAvailable()) {

                PusherAndroidOptions options = new PusherAndroidOptions();
                options.setCluster("eu");

                PusherAndroid pusher = new PusherAndroid("de1a93d7dd48c7a930fe", options);
                pusher.connect();

                Channel channel = pusher.subscribe("my-channel");

                channel.bind("my-event", new SubscriptionEventListener() {
                    @Override
                    public void onEvent(String channelName, String eventName, final String data) {
                        System.out.println(data);
                    }
                });
//                PushNotificationRegistration nativePusher = pusher.nativePusher();
//                String defaultSenderId = getString(R.string.gcm_defaultSenderId); // fetched from your google-services.json
//
//                nativePusher.registerGCM(this, defaultSenderId, this);
//
//                nativePusher.subscribe("pukanki");
//
//                nativePusher.setGCMListener(new GCMPushNotificationReceivedListener() {
//                    @Override
//                    public void onMessageReceived(String from, Bundle data) {
//                        Log.d("notification", data.getString("message"));
//                    }
//                });


            } else {
                // ... log error, or handle gracefully
            }
//        } catch (ManifestValidator.InvalidManifestException e) {
//            e.printStackTrace();
//        }
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
    public void onSuccessfulRegistration() {

    }

    @Override
    public void onFailedRegistration(int statusCode, String response) {

    }
}

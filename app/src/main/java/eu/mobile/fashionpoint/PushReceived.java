package eu.mobile.fashionpoint;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Stoycho Petrov on 18.2.2018 г..
 */

public class PushReceived extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("hello","testtttt");
    }
}

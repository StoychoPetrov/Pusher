package eu.mobile.pusher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Stoycho Petrov on 18.2.2018 г..
 */

public class PushReceived extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("hello", "hello");
    }
}

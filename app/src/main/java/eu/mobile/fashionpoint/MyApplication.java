package eu.mobile.fashionpoint;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private int activityReferences = 0;

    private boolean isActivityChangingConfigurations = false;

    private boolean isAppForeground = false;

    private Intent  chatIntent;

    @Override
    public void onCreate() {

        super.onCreate();
        chatIntent  = new Intent(this, ChatHeadService.class);
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            isAppForeground = true;

            stopService(chatIntent);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();

        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            startService(chatIntent);

            isAppForeground = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}

package eu.mobile.fashionpoint;

import android.app.ActivityManager;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static final String STATUS_KEY                          = "status";
    public static final String ID_KEY                              = "id";
    public static final String PREFERENCES_USERNAME                = "username";
    public static final String PREFERENCES_PASSWORD                = "password";
    public static final String PREFERENCES_USER_ID                 = "user_id";

    // JSON TAGS
    public static final String  TAG_ID                              = "id";
    public static final String  TAG_START                           = "start";
    public static final String  TAG_END                             = "end";
    public static final String  TAG_CLIENT                          = "client";
    public static final String  TAG_SERVICE                         = "service";
    public static final String  TAG_SPECIALIST                      = "specialist";
    public static final String  TAG_ROOM                            = "room";
    public static final String  TAG_URL                             = "url";
    public static final String  TAG_IS_READ                         = "is_read";

    public static Date parseDate(String date){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return simpleDateFormat.format(date);
    }

    public static String formatHour(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return simpleDateFormat.format(date);
    }

    public static Date parseTime(String date){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.topActivity.getPackageName())
                && task.topActivity.getPackageName().equalsIgnoreCase(String.valueOf(ReservationsActivity.class)))
                return true;
        }

        return false;
    }
}

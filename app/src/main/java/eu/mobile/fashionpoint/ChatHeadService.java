package eu.mobile.fashionpoint;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import eu.mobile.fashionpoint.models.ReservationModel;

public class ChatHeadService extends Service implements ConnectionHttp.OnAnswerReceived {

    private WindowManager mWindowManager;
    private View mChatHeadView;

    private int CLICK_ACTION_THRESHHOLD = 100;

    private long lastTouchDown;

    public ChatHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the chat head layout we created
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.item_chat_head, null);

        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            flag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                flag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the chat head position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);

        startThread();

        //Set the close button.
        ImageView closeButton = (ImageView) mChatHeadView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close the service and remove the chat head from the window
                stopSelf();
            }
        });

        //Drag and move chat head using user's touch action.
        final ImageView chatHeadImage = (ImageView) mChatHeadView.findViewById(R.id.chat_head_profile_iv);
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        lastTouchDown = System.currentTimeMillis();

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.

                        float endX = event.getX();
                        float endY = event.getY();

                        if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD
                                && !Utils.isRunning(getApplicationContext())) {
                            Intent intent = new Intent(ChatHeadService.this, ReservationsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.

                            //close the service and remove the chat heads
                            stopSelf();
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });
    }

    private void setData(int notRead){

        TextView count = mChatHeadView.findViewById(R.id.countTxt);

//        int notRead = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("not_read", 0);

        if(notRead == 0)
            count.setVisibility(View.GONE);
        else {
            count.setVisibility(View.VISIBLE);
            count.setText(String.valueOf(notRead));
        }
    }

    private void getReservations(int offset)  {

        try {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            String body = URLEncoder.encode("api_key", "UTF-8") + "=" + URLEncoder.encode(BuildConfig.API_KEY, "UTF-8")
                    + "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(sharedPreferences.getString(Utils.PREFERENCES_USERNAME, ""))
                    + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(sharedPreferences.getString(Utils.PREFERENCES_PASSWORD, ""))
                    + "&" + URLEncoder.encode("offset", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(offset), "UTF-8")
                    + "&" + URLEncoder.encode("limit", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(100), "UTF-8");

            ConnectionHttp connectionHttp = new ConnectionHttp(body);
            connectionHttp.setmListener(this);
            connectionHttp.execute(BuildConfig.API_URL + "api/future-reservations");

        }catch (Exception exception){
            exception.printStackTrace();
        }
    }


    private void startThread(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getReservations(0);
                startThread();
            }
        }, 3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
    }

    @Override
    public void onAnswerReceived(String answer) {
        try {
            JSONArray jsonArray = new JSONArray(answer);

            int notRead = 0;

            for(int i = 0 ; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if(!jsonObject.getBoolean(Utils.TAG_IS_READ)){
                    notRead++;
                }
            }

            setData(notRead);

//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            SharedPreferences.Editor editor = preferences.edit();
//
//            editor.putInt("not_read", notRead);
//            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

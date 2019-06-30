package eu.mobile.fashionpoint;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import eu.mobile.fashionpoint.models.ReservationModel;

public class ReservationsActivity extends AppCompatActivity implements ConnectionHttp.OnAnswerReceived{

    private SharedPreferences   mPreferences;

    private ArrayList<ReservationModel> mReservationsArrayList  = new ArrayList<>();

    private int offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);


        mPreferences    = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            getReservations();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getReservations() throws UnsupportedEncodingException {

        String body = URLEncoder.encode("api_key", "UTF-8") + "=" + URLEncoder.encode(BuildConfig.API_KEY, "UTF-8")
                + "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(mPreferences.getString(Utils.PREFERENCES_USERNAME, ""))
                + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(mPreferences.getString(Utils.PREFERENCES_PASSWORD, ""))
                + "&" + URLEncoder.encode("offset", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(offset), "UTF-8")
                + "&" + URLEncoder.encode("limit", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(10), "UTF-8");



        ConnectionHttp connectionHttp = new ConnectionHttp(body);
        connectionHttp.setmProgress(findViewById(R.id.progress_layout));
        connectionHttp.setmListener(this);
        connectionHttp.execute(BuildConfig.API_URL + "api/future-reservations");
    }

    @Override
    public void onAnswerReceived(String answer) {

        try {
            JSONArray jsonArray = new JSONArray(answer);

            for(int i = 0 ; i < jsonArray.length(); i++){

                ReservationModel reservationModel = new ReservationModel();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                reservationModel.setmId(jsonObject.getLong(Utils.TAG_ID));
//                reservationModel.setmStartDate(jsonObject.getString(Utils.TAG_START));
//                reservationModel.setmEndDate(jsonObject.getString(Utils.TAG_END));
                reservationModel.setmClientName(jsonObject.getString(Utils.TAG_CLIENT));
                reservationModel.setmService(jsonObject.getString(Utils.TAG_SERVICE));
                reservationModel.setmSpecialist(jsonObject.getString(Utils.TAG_SPECIALIST));
                reservationModel.setmRoom(jsonObject.getString(Utils.TAG_ROOM));
                reservationModel.setmUrl(jsonObject.getString(Utils.TAG_URL));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

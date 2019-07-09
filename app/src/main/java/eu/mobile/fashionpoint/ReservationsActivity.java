package eu.mobile.fashionpoint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import eu.mobile.fashionpoint.models.ReservationModel;

public class ReservationsActivity extends AppCompatActivity implements ConnectionHttp.OnAnswerReceived, ReservationsAdapter.RecyclerListener {

    private SharedPreferences           mPreferences;

    private ArrayList<ReservationModel> mReservationsArrayList  = new ArrayList<>();

    private RecyclerView                mReservationsRecyclerView;

    private SwipeRefreshLayout          mSwipeRefreshLayout;

    private ReservationsAdapter         mAdapter;

    private LinearLayoutManager         layoutManager;

    private boolean                     mInitRequest;

    private SwipeController             mSwipeController        = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        mReservationsRecyclerView   = findViewById(R.id.reservations_recycler_view);
        mSwipeRefreshLayout         = findViewById(R.id.swipe_refresh);

        mPreferences    = PreferenceManager.getDefaultSharedPreferences(this);
        setAdapter();
//        setupRecyclerView();

        mInitRequest    =    true;
        getReservations(0);
    }

    private void setAdapter(){

        mReservationsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mReservationsRecyclerView.setLayoutManager(layoutManager);

//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mReservationsRecyclerView.getContext(), layoutManager.getOrientation());
//        mReservationsRecyclerView.addItemDecoration(dividerItemDecoration);

        mAdapter    = new ReservationsAdapter(this, mReservationsArrayList, this);
        mReservationsRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReservations(0);
            }
        });

        mReservationsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(linearLayoutManager != null) {
                    int totalItemCount  = linearLayoutManager.getItemCount();
                    int lastVisible     = linearLayoutManager.findLastVisibleItemPosition();

                    if (totalItemCount > 0 && lastVisible >= totalItemCount - 2)
                        getReservations(mReservationsArrayList.size());
                }
            }
        });
    }

    private void getReservations(int offset)  {

        try {

            if(offset > 0)
                findViewById(R.id.footer_progress_view).setVisibility(View.VISIBLE);


            String body = URLEncoder.encode("api_key", "UTF-8") + "=" + URLEncoder.encode(BuildConfig.API_KEY, "UTF-8")
                    + "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(mPreferences.getString(Utils.PREFERENCES_USERNAME, ""))
                    + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(mPreferences.getString(Utils.PREFERENCES_PASSWORD, ""))
                    + "&" + URLEncoder.encode("offset", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(offset), "UTF-8")
                    + "&" + URLEncoder.encode("limit", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(10), "UTF-8");

            ConnectionHttp connectionHttp = new ConnectionHttp(body);
            connectionHttp.setmListener(this);
            connectionHttp.execute(BuildConfig.API_URL + "api/future-reservations");

        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    private void markAsRead(long eventId)  {

        try {

            String body = URLEncoder.encode("api_key", "UTF-8") + "=" + URLEncoder.encode(BuildConfig.API_KEY, "UTF-8")
                    + "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(mPreferences.getString(Utils.PREFERENCES_USERNAME, ""))
                    + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(mPreferences.getString(Utils.PREFERENCES_PASSWORD, ""))
                    + "&" + URLEncoder.encode("event_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(eventId), "UTF-8");
            ConnectionHttp connectionHttp = new ConnectionHttp(body);
            connectionHttp.setmListener(this);
            connectionHttp.execute(BuildConfig.API_URL + "api/mark-event-as-read");

        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Override
    public void onAnswerReceived(String answer) {

        try {
            JSONArray jsonArray = new JSONArray(answer);

            if(mSwipeRefreshLayout.isRefreshing() || mInitRequest)
                mReservationsArrayList.clear();

            mInitRequest    = false;

            for(int i = 0 ; i < jsonArray.length(); i++){

                ReservationModel reservationModel = new ReservationModel();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                reservationModel.setmId(jsonObject.getLong(Utils.TAG_ID));
                reservationModel.setmStartDate(Utils.parseDate(jsonObject.getString(Utils.TAG_START)));
                reservationModel.setmEndDate(Utils.parseDate(jsonObject.getString(Utils.TAG_END)));
                reservationModel.setmClientName(jsonObject.getString(Utils.TAG_CLIENT));
                reservationModel.setmService(jsonObject.getString(Utils.TAG_SERVICE));
                reservationModel.setmSpecialist(jsonObject.getString(Utils.TAG_SPECIALIST));
                reservationModel.setmRoom(jsonObject.getString(Utils.TAG_ROOM));
                reservationModel.setmUrl(jsonObject.getString(Utils.TAG_URL));
                reservationModel.setmIsRead(jsonObject.getBoolean(Utils.TAG_IS_READ));
                reservationModel.setmColor(jsonObject.getString(Utils.TAG_COLOR));

                mReservationsArrayList.add(reservationModel);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                    findViewById(R.id.footer_progress_view).setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        mReservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mReservationsRecyclerView.setAdapter(mAdapter);

        mSwipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
//                mAdapter.players.remove(position);
//                mAdapter.notifyItemRemoved(position);
//                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(mSwipeController);
        itemTouchhelper.attachToRecyclerView(mReservationsRecyclerView);

        mReservationsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//                mSwipeController.onDraw(c);
            }
        });
    }

    private void itemClicked(int position){
        mReservationsArrayList.get(position).setmIsRead(true);
        mAdapter.notifyDataSetChanged();

//        if(!mReservationsArrayList.get(position).getmIsRead()){
        markAsRead(mReservationsArrayList.get(position).getmId());
//        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url_to_open", mReservationsArrayList.get(position).getmUrl());
        startActivity(intent);
    }

    @Override
    public void onItemClicked(int position) {
        itemClicked(position);
    }

    @Override
    public void onViewClicked(int position) {
        itemClicked(position);
    }

    @Override
    public void onOkClicked(int position) {
        markAsRead(mReservationsArrayList.get(position).getmId());
        mReservationsArrayList.get(position).setmIsRead(true);
        mAdapter.notifyDataSetChanged();
    }
}
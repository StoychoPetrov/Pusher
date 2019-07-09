package eu.mobile.fashionpoint;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import eu.mobile.fashionpoint.models.ReservationModel;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ViewHolder>{

    private Context mContext;

    public interface RecyclerListener {
        void onItemClicked(int position);
    }

    private ArrayList<ReservationModel> mReservationsArrayList;
    private RecyclerListener            mListener;

    public ReservationsAdapter(Context context, ArrayList<ReservationModel> reservationsArrayList, RecyclerListener listener){
        mReservationsArrayList  = reservationsArrayList;
        mListener               = listener;
        mContext                = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {

        ReservationModel reservationModel = mReservationsArrayList.get(i);

        viewHolder.mClientTxt.setText(reservationModel.getmClientName());
        viewHolder.mServiceTxt.setText(reservationModel.getmService());
        viewHolder.mSpecialistTxt.setText(reservationModel.getmSpecialist());

        viewHolder.mTimeTxt.setText(Utils.formatHour(reservationModel.getmStartDate()) + "-" + Utils.formatHour(reservationModel.getmEndDate()));

        viewHolder.mDateTxt.setText(Utils.formatDate(reservationModel.getmStartDate()));

        Date date1 = Utils.parseTime(Utils.formatHour(reservationModel.getmEndDate()));
        Date date2 = Utils.parseTime(Utils.formatHour(reservationModel.getmStartDate()));

        if(date1 != null && date2 != null) {
            long mills = 0;

            if(date1.getTime() > date2.getTime())
                mills = date1.getTime() - date2.getTime();
            else
                mills = date2.getTime() - date1.getTime();

            int hours = (int) (mills / (1000 * 60 * 60));
            int mins = (int) ((mills / (1000 * 60)) % 60);

            if(hours > 0 )
                viewHolder.mDurationTxt.setText(hours+ " ч. " + mins + " мин.");
            else
                viewHolder.mDurationTxt.setText(mins + " мин.");

            int totalMins = mins + hours * 60;

            int percentage  = (int) (totalMins / 12f * 10f);

            if(percentage < 100)
                viewHolder.mProgress.setProgress(percentage);
            else
                viewHolder.mProgress.setProgress(100);
        }

        if(!reservationModel.getmRoom().isEmpty())
            viewHolder.mRoomTxt.setVisibility(View.VISIBLE);
        else
            viewHolder.mRoomTxt.setVisibility(View.GONE);

        viewHolder.mRoomTxt.setText(reservationModel.getmRoom());

        viewHolder.mProgress.getProgressDrawable().setColorFilter(Color.parseColor(reservationModel.getmColor()), android.graphics.PorterDuff.Mode.MULTIPLY);

        viewHolder.mDivider.setBackgroundColor(Color.parseColor(reservationModel.getmColor()));

        if(!reservationModel.getmIsRead()){
            viewHolder.mDateTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            viewHolder.mClientTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            viewHolder.mServiceTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            viewHolder.mSpecialistTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            viewHolder.mRoomTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            viewHolder.mTimeTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            viewHolder.mDurationTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
        }
        else {
            viewHolder.mDateTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
            viewHolder.mClientTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
            viewHolder.mServiceTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
            viewHolder.mSpecialistTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
            viewHolder.mRoomTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
            viewHolder.mTimeTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
            viewHolder.mDurationTxt.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null)
                    mListener.onItemClicked(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReservationsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mClientTxt;
        public TextView mServiceTxt;
        public TextView mSpecialistTxt;
        public TextView mRoomTxt;
        public ProgressBar mProgress;

        public TextView mTimeTxt;
        public TextView mDateTxt;
        public TextView mDurationTxt;
        public View     mDivider;

        public ViewHolder(View v) {
            super(v);

            mClientTxt      = v.findViewById(R.id.client_txt);
            mServiceTxt     = v.findViewById(R.id.service_txt);
            mSpecialistTxt  = v.findViewById(R.id.specialist_txt);
            mRoomTxt        = v.findViewById(R.id.room_txt);
            mDateTxt        = v.findViewById(R.id.date_txt);
            mTimeTxt        = v.findViewById(R.id.time_txt);
            mDurationTxt    = v.findViewById(R.id.duration_txt);
            mProgress       = v.findViewById(R.id.progress);
            mDivider        = v.findViewById(R.id.divider);
        }
    }
}

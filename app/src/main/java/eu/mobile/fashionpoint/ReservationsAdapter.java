package eu.mobile.fashionpoint;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import eu.mobile.fashionpoint.models.ReservationModel;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ViewHolder>{

    public interface RecyclerListener {
        void onItemClicked(int position);
    }

    private ArrayList<ReservationModel> mReservationsArrayList = new ArrayList<>();
    private RecyclerListener            mListener;

    public ReservationsAdapter(ArrayList<ReservationModel> reservationsArrayList, RecyclerListener listener){
        mReservationsArrayList  = reservationsArrayList;
        mListener               = listener;
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

        viewHolder.mDateTxt.setText(Utils.formatHour(reservationModel.getmStartDate()) + "-" + Utils.formatHour(reservationModel.getmEndDate()));

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
        }

        if(!reservationModel.getmRoom().isEmpty())
            viewHolder.mRoomTxt.setVisibility(View.VISIBLE);
        else
            viewHolder.mRoomTxt.setVisibility(View.GONE);

        viewHolder.mRoomTxt.setText(reservationModel.getmRoom());

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

        public TextView mDateTxt;
        public TextView mDurationTxt;

        public ViewHolder(View v) {
            super(v);

            mClientTxt      = v.findViewById(R.id.client_txt);
            mServiceTxt     = v.findViewById(R.id.service_txt);
            mSpecialistTxt  = v.findViewById(R.id.specialist_txt);
            mRoomTxt        = v.findViewById(R.id.room_txt);
            mDateTxt        = v.findViewById(R.id.date_txt);
            mDurationTxt    = v.findViewById(R.id.duration_txt);
        }
    }
}
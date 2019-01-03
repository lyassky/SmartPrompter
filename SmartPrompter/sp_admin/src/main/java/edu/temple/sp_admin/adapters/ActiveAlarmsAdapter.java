package edu.temple.sp_admin.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.temple.sp_admin.R;

import edu.temple.sp_res_lib.Alarm;
import edu.temple.sp_res_lib.utils.Constants;

public class ActiveAlarmsAdapter extends RecyclerView.Adapter<ActiveAlarmsAdapter.AlarmViewHolder> {

    public interface AlarmDetailsListener {
        void onAlarmSelected(int position);
    }

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    class AlarmViewHolder extends RecyclerView.ViewHolder {

        public Context mContext;
        public TextView mTextView;

        public AlarmViewHolder(Context ctx, TextView v) {
            super(v);
            mContext = ctx;
            mTextView = v;
        }

    }

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    private List<Alarm> mAlarms;
    private AlarmDetailsListener mListener;

    public ActiveAlarmsAdapter(List<Alarm> alarms, AlarmDetailsListener listener) {
        Log.i(Constants.LOG_TAG, "Initializing Active Alarms Adapter with: "
                + alarms.size() + " records");
        mAlarms = alarms;
        mListener = listener;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_active_alarm, parent, false);
        AlarmViewHolder vh = new AlarmViewHolder(parent.getContext(), v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, final int position) {
        Log.i(Constants.LOG_TAG, "Binding new line item view for alarm at position: "
                + position);

        final Alarm currentAlarm = mAlarms.get(position);
        if (currentAlarm.isActive()) {
            Log.d(Constants.LOG_TAG, "Alarm at position: " + position
                    + " is active!  Updating line item background color.");
            holder.mTextView.setBackgroundColor(Color.GREEN);
        }

        holder.mTextView.setText(currentAlarm.toString());
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Constants.LOG_TAG, "User has clicked alarm at position: "
                        + position + ", with ID: " + currentAlarm.getID());
                mListener.onAlarmSelected(currentAlarm.getID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

}
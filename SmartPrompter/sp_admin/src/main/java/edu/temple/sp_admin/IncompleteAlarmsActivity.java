package edu.temple.sp_admin;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import edu.temple.sp_admin.adapters.SimpleAlarmAdapter;
import edu.temple.sp_admin.fragments.ActiveAlarmDetailsFragment;
import edu.temple.sp_admin.fragments.IncompleteAlarmDetailsFragment;
import edu.temple.sp_admin.fragments.IncompleteAlarmListFragment;
import edu.temple.sp_admin.utils.BaseActivity;
import edu.temple.sp_admin.utils.Constants;

public class IncompleteAlarmsActivity extends BaseActivity implements
        SimpleAlarmAdapter.AlarmSelectionListener {

    private IncompleteAlarmListFragment listFrag;
    private IncompleteAlarmDetailsFragment detailsFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.LOG_TAG, "Incomplete Alarms Activity created!");
        setContentView(R.layout.activity_active_alarms);
        super.onCreate(savedInstanceState);

        Log.i(Constants.LOG_TAG, "Populating Incomplete Alarms Activity with list fragment.");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        listFrag = IncompleteAlarmListFragment.newInstance();
        ft.replace(R.id.fragment_container, listFrag);
        ft.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(Constants.LOG_TAG, "Incomplete Alarms Activity paused!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(Constants.LOG_TAG, "Incomplete Alarms Activity destroyed!");
    }

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------
    //      AlarmSelectionListener methods
    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    @Override
    public void onAlarmSelected(int alarmID) {
        // Log.e(Constants.LOG_TAG, "INCOMPLETE ALARM SELECTED");
        // Toast.makeText(this, "Haven't programmed this path yet ...",
        //         Toast.LENGTH_SHORT).show();

        Log.i(Constants.LOG_TAG, "User wants to view details of alarm ID: " + alarmID);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        detailsFrag = IncompleteAlarmDetailsFragment.newInstance(alarmID);
        ft.replace(R.id.fragment_container, detailsFrag);
        ft.addToBackStack(null);
        ft.commit();
    }

}
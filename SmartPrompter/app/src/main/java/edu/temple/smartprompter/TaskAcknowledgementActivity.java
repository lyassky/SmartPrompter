package edu.temple.smartprompter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import edu.temple.smartprompter.fragments.TaskAcknowledgementFragment;
import edu.temple.smartprompter.utils.BaseActivity;
import edu.temple.smartprompter.utils.Constants;

import edu.temple.sp_res_lib.Reminder;
import edu.temple.sp_res_lib.SpAlarmManager;
import edu.temple.sp_res_lib.SpReminderManager;
import edu.temple.sp_res_lib.utils.Constants.ALARM_STATUS;
import edu.temple.sp_res_lib.utils.Constants.REMINDER_TYPE;

public class TaskAcknowledgementActivity extends BaseActivity implements
        TaskAcknowledgementFragment.TaskAcknowledgementListener {

    private TaskAcknowledgementFragment defaultFrag;

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.LOG_TAG, "Task Acknowledgement Activity created!");
        setContentView(R.layout.activity_task_acknowledgement);
        super.onCreate(savedInstanceState);

        // make sure we were passed the correct intent extras
        if (!verifyIntentExtras())
            return;

        // cancel any lingering notifications associated with this alarm
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.cancel(mAlarmID);

        // retrieve the current alarm
        mAlarmMgr = new SpAlarmManager(this);
        mAlarm = mAlarmMgr.get(mAlarmID);
        if (mAlarm == null) {
            Log.e(Constants.LOG_TAG, "NO MATCHING RECORD FOR PROVIDED ALARM ID.");

            DialogInterface.OnClickListener missingAlarmListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    TaskAcknowledgementActivity.this.finishAffinity();
                }};

            new AlertDialog.Builder(this)
                    .setTitle("Missing Alarm")
                    .setMessage("The alarm you are trying to access no longer exists.  "
                            + "Please check with your caretaker to verify.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, missingAlarmListener).show();
        } else if (checkPermissions()) {
            // proceed with displaying the activity view
            initNavigation();
            showDefaultFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(Constants.LOG_TAG, "Task Acknowledgement Activity paused!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(Constants.LOG_TAG, "Task Acknowledgement Activity destroyed!");
    }

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    protected void showDefaultFragment() {
        Log.i(Constants.LOG_TAG, "Populating Task Completion Activity with default fragment.");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        defaultFrag = TaskAcknowledgementFragment.newInstance(mAlarmID);
        ft.replace(R.id.fragment_container, defaultFrag, DEFAULT_FRAGMENT_TAG);
        ft.commit();
    }

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    @Override
    public void onAlarmAcknowledged(int alarmID) {
        SpReminderManager remMgr = new SpReminderManager(this);
        Reminder reminder = remMgr.get(alarmID, REMINDER_TYPE.Acknowledgement);
        remMgr.cancelReminder(reminder);
        updateAlarmStatus(ALARM_STATUS.Incomplete);

        startNextActivity(this, TaskCompletionActivity.class);
        Log.i(Constants.LOG_TAG, "Received and acknowledged alarm response for alarm ID: "
                + mAlarm.getID() + ".  \t\t and updated alarm status: "
                + mAlarm.getStatusString());
        finish();
    }

    @Override
    public void onAlarmDeferred(int alarmID) {
        Log.e(Constants.LOG_TAG, "User has elected to defer the task "
                + "acknowledgement for alarm ID: " + alarmID);

        // TODO - finish alarm acknowledgement deferral logic
        Toast.makeText(this,
                "Haven't coded the reminder deferral logic yet.",
                Toast.LENGTH_SHORT).show();
    }

}
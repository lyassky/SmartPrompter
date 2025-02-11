package edu.temple.smartprompter.receivers;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import edu.temple.smartprompter.TaskAcknowledgementActivity;
import edu.temple.smartprompter.utils.Constants;
import edu.temple.smartprompter.R;

import edu.temple.sp_res_lib.Alarm;
import edu.temple.sp_res_lib.Reminder;
import edu.temple.sp_res_lib.SpAlarmManager;
import edu.temple.sp_res_lib.SpReminderManager;
import edu.temple.sp_res_lib.utils.Constants.ALARM_STATUS;
import edu.temple.sp_res_lib.utils.Constants.REMINDER_TYPE;

import static android.app.Notification.VISIBILITY_PUBLIC;

public class AlarmReceiver extends BroadcastReceiver {

    private SpAlarmManager mAlarmMgr;
    private Alarm mAlarm;
    private int mAlarmID;
    private String mAlarmStatus;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (verifyIntentExtras(context, intent)) {
            updateAlarmStatus();
            schedulePreemptiveReminder(context);
            generateNotification(context);
        }
    }

    private boolean verifyIntentExtras(Context context, Intent intent) {
        if (!intent.hasExtra(Constants.INTENT_EXTRA_ALARM_ID)) {
            Log.e(Constants.LOG_TAG, "Alarm broadcast has been received, "
                    + "but is missing the alarm ID.");
            return false;
        }

        if (!intent.hasExtra(Constants.INTENT_EXTRA_ORIG_TIME)) {
            Log.e(Constants.LOG_TAG, "Alarm broadcast has been received, "
                    + "but is missing original alarm time.");
            return false;
        }

        mAlarmID = intent.getIntExtra(Constants.INTENT_EXTRA_ALARM_ID, Constants.DEFAULT_ALARM_ID);
        String timeString = intent.getStringExtra(Constants.INTENT_EXTRA_ORIG_TIME);

        mAlarmMgr = new SpAlarmManager(context);
        mAlarm = mAlarmMgr.get(mAlarmID);
        String statusString = mAlarm.getStatusString();

        Log.e(Constants.LOG_TAG, "Alarm broadcast has been received"
                + " for alarm ID: " + mAlarmID
                + " scheduled for original time: " + timeString
                + " with original status: " + statusString);

        return true;
    }

    private void updateAlarmStatus() {
        // update and commit the status changes for this alarm
        mAlarm.updateStatus(ALARM_STATUS.Unacknowledged);
        mAlarmMgr.update(mAlarm);

        // just for sanity's sake ...
        Alarm sanityAlarm = mAlarmMgr.get(mAlarmID);
        mAlarmStatus = sanityAlarm.getStatusString();
    }

    private void schedulePreemptiveReminder(Context context) {
        // create a new acknowledgement reminder for this alarm by default ...
        // will cancel if / when the user completes the acknowledgement phase
        SpReminderManager rm = new SpReminderManager(context);
        Reminder reminder = rm.create(mAlarmID, REMINDER_TYPE.Acknowledgement);

        // --------------------------------------------------------------------------------------

        // NOTE - THERE WILL ONLY EVER BE ONE REMINDER OF EACH TYPE IN THE DB
        // AFTER THIS POINT IN THE PROGRAM FLOW, RETRIEVE THE EXISTING ACKNOWLEDGEMENT
        // REMINDER AND KEEP RESCHEDULING IT UNTIL WE HIT THE LIMIT OR THE USER ACKNOWLEDGES

        // ... RINSE AND REPEAT FOR THE COMPLETION REMINDERS

        // --------------------------------------------------------------------------------------

        // retrieve, set the appropriate receiver settings ...
        Resources resources = context.getResources();
        reminder.updateIntentSettings(
                resources.getString(R.string.action_alarms),
                resources.getString(R.string.reminder_receiver_namespace),
                resources.getString(R.string.reminder_receiver_class)
        );

        // calculate the appropriate reminder interval and commit changes to database
        reminder.calculateNewReminder();
        rm.update(reminder);

        // schedule the reminder
        rm.scheduleReminder(reminder);
    }

    // --------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------

    private void generateNotification(Context context) {
        createNotificationChannel(context);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_alarm_on_white_18dp)
                .setContentTitle("SmartPrompter")
                .setContentText("Please complete your task!")
                .setSound(ringtoneUri)
                .setContentIntent(createNotificationIntent(context))
                .setVisibility(VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Log.i(Constants.LOG_TAG, "Building notification for alarm ID: "
                + mAlarmID + " using request code: " + mAlarm.getRequestCode());
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(mAlarm.getRequestCode(), mBuilder.build());
    }

    @SuppressLint("WrongConstant")
    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME, importance);
            channel.setDescription(Constants.CHANNEL_DESCRIPTION);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createNotificationIntent(Context context) {

        Log.i(Constants.LOG_TAG, "Creating intent to launch Task Acknowledgement activity "
                + "for alarm ID: " + mAlarmID + " \t\t with current status: " + mAlarmStatus);

        Intent intent = new Intent(context, TaskAcknowledgementActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.INTENT_EXTRA_ALARM_ID, mAlarmID);
        intent.putExtra(Constants.INTENT_EXTRA_ALARM_CURRENT_STATUS, mAlarmStatus);

        Reminder latestReminder = (new SpReminderManager(context)).getLatest(mAlarmID);
        intent.putExtra(Constants.INTENT_EXTRA_REMINDER_ID,
                (latestReminder != null
                        ? latestReminder.getID()
                        : Constants.DEFAULT_REMINDER_ID));

        Log.i(Constants.LOG_TAG, "Returning notification intent for alarm ID: "
                + mAlarmID + " using request code: " + mAlarm.getRequestCode());
        return PendingIntent.getActivity(context, mAlarm.getRequestCode(),
                intent, Constants.PENDING_INTENT_FLAGS);
    }

}
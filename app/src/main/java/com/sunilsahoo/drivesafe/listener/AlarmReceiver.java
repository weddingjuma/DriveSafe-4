package com.sunilsahoo.drivesafe.listener;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Constants;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = AlarmReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "inside onReceive() ");
		MainService driveSafeController = MainService
				.getInstance();

		if (driveSafeController == null) {
			Intent serviceStartIntent = new Intent(context, MainService.class);
			context.startService(serviceStartIntent);
			return;
		}
		Set<String> categories = intent.getCategories();
		Log.i(TAG, "categories :" + categories);

		if (categories != null) {
			if (categories.contains(
					Constants.ALARM_USERTEST_PASSED_TIMER)) {
				driveSafeController.onUsertestAccesTimePassed();
			} else if (categories.contains(
					Constants.ALARM_EMERGENCY_ACCESS_TIMER)) {
				driveSafeController.onEmergencyAccessTimePassed();
			} else if (categories.contains(
					Constants.ALARM_ENSURE_SERVICE_RUNNING)) {
				driveSafeController.isServicesRunning();
			} else if (categories.contains(
					Constants.ALARM_START_SPEED)) {
				driveSafeController.onSpeedStartRequest();
			} else if (categories.contains(
					Constants.ALARM_STOP_SPEED)) {
				driveSafeController.onSpeedStopRequest();
			} else if(categories.contains(Constants.ALARM_DELETE_REPORT)){
                long minThresholdTime = SystemClock.elapsedRealtime() - Constants.THRESHOLD_TIME_FOR_REPORT_DELETION*1000;
                DBOperation.deleteReports(context, minThresholdTime);
            }
		} else {
			driveSafeController.isServicesRunning();
		}
	}

}
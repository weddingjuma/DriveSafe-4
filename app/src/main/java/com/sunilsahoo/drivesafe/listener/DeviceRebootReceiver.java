package com.sunilsahoo.drivesafe.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.model.Report;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.ReportType;

public class DeviceRebootReceiver extends BroadcastReceiver {
	private static final String TAG = DeviceRebootReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.i(TAG, "inside onReceive()");
        if(arg1.getAction() == null){
            return;
        }
        try {
            if (arg1.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Report report = new Report();
                report.setReportType(ReportType.DEVICE_SHUTDOWN);
                DBOperation.insertReport(report, context);
            } else {
                Report report = new Report();
                report.setReportType(ReportType.DEVICE_REBOOT);
                DBOperation.insertReport(report, context);

                Intent serviceStartIntent = new Intent(context, MainService.class);
                serviceStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(serviceStartIntent);
            }
        }catch(Exception ex){
            Log.d(TAG, "Exception ex :"+ex);
        }
	}
}

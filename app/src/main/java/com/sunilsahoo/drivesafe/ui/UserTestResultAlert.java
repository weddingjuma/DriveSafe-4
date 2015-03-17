package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.model.Report;
import com.sunilsahoo.drivesafe.utility.Constants;
import com.sunilsahoo.drivesafe.utility.ReportType;
import com.sunilsahoo.drivesafe.utility.TestResultCategory;
import com.sunilsahoo.drivesafe.utility.Utility;

public class UserTestResultAlert extends Activity {

	private static final String TAG = UserTestResultAlert.class.getName();

	private int imageIdUsertestFailed;
	private int imageIdUsertestPassed;
	private byte usertestStatus;

	private void loadUsertestResultIconId() {
		imageIdUsertestFailed = R.drawable.usertest_failed;
		imageIdUsertestPassed = R.drawable.usertest_passed;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.usertest_result_alert);
		loadUsertestResultIconId();
		setIconAndLabels();

	}// End onCreate()

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "Inside onStart();");
		// we have start a Thread which will stop this activity after 4 secs
		new Thread() {
			@Override
			public void run() {
				try {

					if (usertestStatus == TestResultCategory.DISABLED) {
						Thread.sleep(Constants.ALERT_SHOW_TIME * 1000);
						Utility.lockScreen(UserTestResultAlert.this);
					} else if (usertestStatus == TestResultCategory.PASSED) {
						Report report = new Report();
						report.setReportType(ReportType.TESTPASS);
						DBOperation.insertReport(report, null);
						Thread.sleep(Constants.ALERT_SHOW_TIME);
						finish();
					} else if (usertestStatus == TestResultCategory.FAILED
							|| usertestStatus == TestResultCategory.TIMEOUT) {
						Report report = new Report();
						report.setReportType(ReportType.TESTFAIL);
						DBOperation.insertReport(report, null);
						Thread.sleep(Constants.ALERT_SHOW_TIME);
						Utility.lockScreen(UserTestResultAlert.this);
						finish();
					}

				} catch (Exception e) {
					Log.e(TAG, "Exception :"+ e);
				}

			}
		}.start();

	}

	private void setIconAndLabels() {

		Intent intent = getIntent();
		usertestStatus = intent.getByteExtra(Constants.USERTEST_STATUS,
				(byte) Constants.EOF);
		TextView textView = (TextView) findViewById(R.id.testResultTV);
		ImageView imageView = (ImageView) findViewById(R.id.resultIV);
		String usertestLabel = "";

		if (usertestStatus == TestResultCategory.DISABLED) {
			usertestLabel = getString(R.string.lbl_usertest_disabled);
			imageView.setBackgroundResource(imageIdUsertestFailed);
		} else if (usertestStatus == TestResultCategory.PASSED) {
			usertestLabel = getString(R.string.lbl_usertest_passed);
			imageView.setBackgroundResource(imageIdUsertestPassed);
		} else if (usertestStatus == TestResultCategory.FAILED) {
			usertestLabel = getString(R.string.lbl_usertest_failed);
			imageView.setBackgroundResource(imageIdUsertestFailed);
		} else if (usertestStatus == TestResultCategory.TIMEOUT) {
			usertestLabel = getString(R.string.lbl_usertest_timeout);
			imageView.setBackgroundResource(imageIdUsertestFailed);
		}

		textView.setText(usertestLabel);

	}


    /*if (thScreenLock != null) {
        thScreenLock.interrupt();
    }

    thScreenLock = new Thread() {
        @Override
        public void run() {
            try {
                // wait for 5 seconds after showing the alert
                Thread.sleep(5000);
                Utility.lockScreen(UserTestDisabledAlert.this);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Error in stopping the Activity:" + e.toString());
            }
        }
    };
    thScreenLock.start();*/

}

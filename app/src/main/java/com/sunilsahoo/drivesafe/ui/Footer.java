package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.listener.EmergencyCallDialogListener;
import com.sunilsahoo.drivesafe.utility.Utility;

public class Footer extends LinearLayout implements
		android.view.View.OnClickListener {
	private static final String TAG = Footer.class.getName();
	private Button emergencyBtn = null;
	private Context context = null;
	private static EmergencyCallDialog emergencyCallDialog = null;

	public Footer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.footer, this, true);
		emergencyBtn = (Button) findViewById(R.id.emergencyBtn);
		emergencyBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.emergencyBtn) {
			Log.i(TAG, " Emergency btn clicked");
			showEmergencyCallDialog(context);
		}
	}

	private void showEmergencyCallDialog(final Context context) {
		dismissEmergencyDialog();
		com.sunilsahoo.drivesafe.model.Profile profile = DBOperation
				.getProfile(context);
		final String[] emergencyNoArr;
		emergencyNoArr = Utility.getEmergencyNumberArr(profile
				.getEmergencyNos());
		if (emergencyNoArr != null) {
			if (emergencyNoArr.length > 0) {
				emergencyCallDialog = new EmergencyCallDialog(
						context, emergencyNoArr,
						new EmergencyCallDialogListener() {
							@Override
							public void clickedValue(int val) {
								if (!(val >= 0
								&& val < emergencyNoArr.length)) {
									return;
								}
								Utility.makeCall(emergencyNoArr[val], context);
								if (MainService.getInstance() != null) {
									MainService.getInstance()
											.initializeEmergencyState();
								}
								try {
									((Activity) context).finish();
								} catch (Exception ex) {
									Log.w(TAG, "Exception :" + ex.getMessage());
								}
							}
						});
				emergencyCallDialog.show();
			}
		}
	}

	public static void dismissEmergencyDialog() {
		if (emergencyCallDialog != null) {
			emergencyCallDialog.dismiss();
			emergencyCallDialog = null;
		}
	}

	public static boolean isDialogVisible() {
		if (emergencyCallDialog != null) {
			return emergencyCallDialog.isShowing();
		}
		return false;
	}

}

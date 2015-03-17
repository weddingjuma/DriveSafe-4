package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.listener.DeviceLockStatusReceiver;
import com.sunilsahoo.drivesafe.utility.Constants;

public class DeviceAdminScreen extends Activity {

	private static final String TAG = DeviceAdminScreen.class.getName();

	private static final int ADMIN_ACTIVE_RESULT = 1001;
	private DevicePolicyManager policyMgr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "inside onCreate()");
		super.onCreate(savedInstanceState);

		boolean ensurePermissionOnly = getIntent().getBooleanExtra(
				Constants.INTENT_LOCK_PERMISSION, false);
		policyMgr = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminName = new ComponentName(this, DeviceLockStatusReceiver.class);

		if (!policyMgr.isAdminActive(adminName)) {
			Intent adminActivationIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			adminActivationIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
			adminActivationIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.msg_admin_description);
			adminActivationIntent.putExtra(Constants.INTENT_LOCK_PERMISSION, ensurePermissionOnly);
			startActivityForResult(adminActivationIntent, ADMIN_ACTIVE_RESULT);
		} else {
			if(!ensurePermissionOnly){
				policyMgr.lockNow();
			}
			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "inside onActivityResult() ");
		try {
			boolean ensurePermissionOnly = getIntent().getBooleanExtra(Constants.INTENT_LOCK_PERMISSION, false);
			if(!ensurePermissionOnly){
				policyMgr.lockNow();
			}
		} catch (Exception ex) {
			Log.e(TAG, "Exception in onActivityResult() :"+ex.getMessage());
		}
		finish();
	}
	
	@Override
	protected void onDestroy() {
		policyMgr = null;
		super.onDestroy();
	}
	
}

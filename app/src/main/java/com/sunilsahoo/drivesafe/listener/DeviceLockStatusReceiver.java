package com.sunilsahoo.drivesafe.listener;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceLockStatusReceiver extends DeviceAdminReceiver {
	private static final String TAG = DeviceLockStatusReceiver.class.getName();

	@Override
	public void onDisabled(Context context, Intent intent) {
		Log.i(TAG, "onDisabled()");
		super.onDisabled(context, intent);
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		Log.i(TAG, "onEnabled()");
		super.onEnabled(context, intent);
	}
}

package com.sunilsahoo.drivesafe.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sunilsahoo.drivesafe.services.MainService;

public class ScreenLockStatusChangeReceiver extends BroadcastReceiver {
	private static final String TAG = ScreenLockStatusChangeReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "inside onReceive() : "+intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			if (MainService.getInstance() != null) {
				MainService.getInstance().onScreenOff(true);
			}
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			if (MainService.getInstance() != null) {
				MainService.getInstance().onScreenOff(false);
			}
		}
	}
}

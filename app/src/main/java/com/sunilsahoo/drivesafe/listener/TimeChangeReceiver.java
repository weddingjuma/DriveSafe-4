package com.sunilsahoo.drivesafe.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sunilsahoo.drivesafe.services.MainService;

public class TimeChangeReceiver extends BroadcastReceiver {
	private static final String TAG = TimeChangeReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, " inside onReceive () " + intent.getAction());
		intent.getAction();
		if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)
				|| intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
				|| intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
			MainService dsMainService = MainService
					.getInstance();
			if (dsMainService != null) {
				dsMainService.onTimeChangedCallback();

			}
		}
	}
}

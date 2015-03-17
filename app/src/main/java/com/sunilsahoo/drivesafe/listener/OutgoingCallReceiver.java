package com.sunilsahoo.drivesafe.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Utility;

public class OutgoingCallReceiver extends BroadcastReceiver {

	private static final String TAG = OutgoingCallReceiver.class.getName();
	private MainService mainService = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean isOutgoingCallMatched = false;
		String outgoingNumber = getResultData();
		Log.i(TAG, "inside onReceive() - Dialed Number :" + outgoingNumber);

		if (outgoingNumber != null) {
			mainService = MainService.getInstance();
			if (mainService == null) {
				return;
			}
			mainService.setOutgoingNo(outgoingNumber);
			// check if the initiated call is emergency call
			if (mainService.isInitiatingEmergency()) {
				if (mainService.getEmergencyNos() == null || mainService.getEmergencyNos().length <= 0) {
					return;
				}
				isOutgoingCallMatched = Utility.containsNo(outgoingNumber,
						mainService.getEmergencyNos());
				Log.i(TAG, "isOutgoingCallMatched :" + isOutgoingCallMatched);
				if (isOutgoingCallMatched) {
					new Thread() {
						@Override
						public void run() {
							mainService.onEmergency();
						}
					}.start();
				} else {
					setResultData(null);
					this.abortBroadcast();
					String msg = context.getResources().getString(
							R.string.msg_call_blocked);
					Toast.makeText(context, msg, Toast.LENGTH_SHORT)
							.show();
					new Thread() {
						@Override
						public void run() {
							mainService.restoreUsertestScreen();
						}
					}.start();
				}
			}
		}

	}

}

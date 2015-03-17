package com.sunilsahoo.drivesafe.listener;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sunilsahoo.drivesafe.services.MainService;

public class CustomPhoneStateListener extends PhoneStateListener {
	private static final String TAG = CustomPhoneStateListener.class.getName();

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		Log.d(TAG, "inside onCallStateChanged() state :" + state);
		MainService dsMainService = MainService.getInstance();
		Log.d(TAG, "dsMainService :" + dsMainService);
		if (dsMainService == null) {
			return;
		}
		try {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				dsMainService.setIncomingNo(incomingNumber);
				dsMainService.onCallReceived(incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				dsMainService.onCallDisconnected();
				dsMainService.setIncomingNo(null);
				dsMainService.setOutgoingNo(null);
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:
				dsMainService.onOutgoingCallReceived();
				break;

			default:
				break;
			}
		} catch (Exception ex) {
			Log.e(TAG, "Exception :" + ex.getMessage());
		}
		super.onCallStateChanged(state, incomingNumber);
	}
}

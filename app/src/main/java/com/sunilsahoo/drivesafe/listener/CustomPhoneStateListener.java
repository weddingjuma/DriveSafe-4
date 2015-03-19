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
		if (MainService.getInstance() == null) {
			return;
		}
		try {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
                MainService.getInstance().setIncomingNo(incomingNumber);
                MainService.getInstance().onCallReceived(incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
                MainService.getInstance().onCallDisconnected();
                MainService.getInstance().setIncomingNo(null);
                MainService.getInstance().setOutgoingNo(null);
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:
                MainService.getInstance().onOutgoingCallReceived();
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

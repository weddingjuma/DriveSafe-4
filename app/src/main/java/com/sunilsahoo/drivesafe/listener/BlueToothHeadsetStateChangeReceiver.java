package com.sunilsahoo.drivesafe.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sunilsahoo.drivesafe.utility.Constants;

public class BlueToothHeadsetStateChangeReceiver extends BroadcastReceiver {
	private static final String TAG = BlueToothHeadsetStateChangeReceiver.class
			.getName();
	private static int audioState = 129;
	private static int headsetState = 129;
	
	public static int getHeadsetAudioState() {
		return audioState;
	}

	public static int getHeadsetState() {
		return headsetState;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			if (intent.getAction().equalsIgnoreCase(
					Constants.ACTION_HEADSET_STATE_CHANGED)) {
				BlueToothHeadsetStateChangeReceiver.headsetState = intent
						.getIntExtra(Constants.EXTRA_STATE, Constants.EOF);
				Log.i(TAG, "head set :" + headsetState);
			} else if (intent.getAction().equalsIgnoreCase(
					Constants.ACTION_AUDIO_STATE_CHANGED)) {
				BlueToothHeadsetStateChangeReceiver.audioState = intent
						.getIntExtra(Constants.EXTRA_AUDIO_STATE, Constants.EOF);
				Log.i(TAG, "audio state :" + audioState);
			}

		}
	}

}

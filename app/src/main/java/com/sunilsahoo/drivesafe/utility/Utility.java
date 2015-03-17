package com.sunilsahoo.drivesafe.utility;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.sunilsahoo.drivesafe.listener.BlueToothHeadsetStateChangeReceiver;
import com.sunilsahoo.drivesafe.listener.DeviceLockStatusReceiver;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.ui.DeviceAdminScreen;
import com.sunilsahoo.drivesafe.utility.Constants.BTHeadSetState;

public class Utility {
	private static final String TAG = Utility.class.getName();
	private static char lastChar;
	private static final Random RANDOM = new Random();
	private static final char[] SYMBOLS = new char[26];
	private static TelephonyManager telephony = null;
	private static ITelephony iTelephony = null;

	static {
		for (int i = 0; i < 26; ++i) {
			SYMBOLS[i] = (char) ('a' + i);
		}
	}

	public static void makeCall(String phoneNo, Context appContext) {
		try {
			Intent callIntent = new Intent(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse("tel:" + phoneNo));
			appContext.startActivity(callIntent);
		} catch (Exception e) {
			Log.e(TAG, "Exception in makeCall() : " + e.getMessage());
		}
	}

	public static void deactivateAdmin(Context context) {
		Log.i(TAG, "inside deactivateAdmin()");
		DevicePolicyManager lockDPM;
		ComponentName mDeviceAdminSample;
		lockDPM = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(context,
				DeviceLockStatusReceiver.class);
		boolean active = lockDPM.isAdminActive(mDeviceAdminSample);

		if (active) {
			lockDPM.removeActiveAdmin(mDeviceAdminSample);
		}
	}

	/**
	 * Call this method to only activate this application as device
	 * administrator. It will show Admin Activation screen if this application
	 * already not selected as administrator in the device. If it has been
	 * already allowed to do administrative work, this method has no impact on
	 * UI.
	 * 
	 * @param context
	 *            - The application context
	 */
	public static void ensurelockPermission(Context context) {
		Log.i(TAG, "Inside the lock device.");
		Intent lockIntent = new Intent(context, DeviceAdminScreen.class);
		lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		lockIntent.putExtra(Constants.INTENT_LOCK_PERMISSION, true);
		context.startActivity(lockIntent);

	}

	public static String getAppVersion(Context context) {
		String versionName = "";
		try {
			String pkg = context.getPackageName();
			versionName = context.getPackageManager().getPackageInfo(pkg, 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "";
		}
		return versionName;
	}

	/**
	 * 
	 * @return - The current device time in yyyy-MM-dd HH:mm:ss format
	 */
	public static String getCurrentTime() {
		String date = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = sdf.format(new Date());
		} catch (Exception e) {
		}
		return date;
	}

	public static ArrayList<String> getInstalledPackages(String intentAction,
			Context context) {
		ArrayList<String> appsList = new ArrayList<String>();
		final Intent intent = new Intent(intentAction);
		final List<ResolveInfo> activityOption = context.getPackageManager()
				.queryIntentActivityOptions(null, null, intent,
						PackageManager.GET_INTENT_FILTERS);
		for (ResolveInfo resolveInfo : activityOption) {
			ActivityInfo activityInfo = resolveInfo.activityInfo;
			if (activityInfo != null)
				appsList.add(activityInfo.packageName);
		}

		return appsList;
	}

	public static int getPhoneCallState(Context context) {
		try {
			return ((TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getCallState();
		} catch (Exception ex) {
			Log.w(TAG, "Exception in getPhoneCallState() :" + ex.getMessage());
			return TelephonyManager.CALL_STATE_IDLE;
		}
	}

	/**
	 * Check if the current application has been activated as Device
	 * Administrator or not.
	 * 
	 * @param context
	 *            - The application context.
	 * @return - true if the application has been selected as device
	 *         administrator, false otherwise.
	 */
	public static boolean isAdministratorActive(Context context) {
		boolean active = false;

		if (context != null) {
			DevicePolicyManager lockDPM = (DevicePolicyManager) context
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdminSample = new ComponentName(context,
					DeviceLockStatusReceiver.class);
			active = lockDPM.isAdminActive(mDeviceAdminSample);
		}

		return active;
	}

	public static boolean isMobileNetworkAvailable(Context context) {
		boolean mobileNetworkAvailable = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] networks = cm.getAllNetworkInfo();
			for (int i = 0; i < networks.length; i++) {
				if (networks[i].getType() == ConnectivityManager.TYPE_MOBILE) {
					if (networks[i].isAvailable()) {
						mobileNetworkAvailable = true;
					}
				}
			}

			Log.i(TAG, "Checking if Mobile network is available:"
					+ mobileNetworkAvailable);
		} catch (Exception e) {
			Log.i(TAG,
					"Error in checking mobile network availability:"
							+ e.toString());
		}

		return mobileNetworkAvailable;
	}

	public static void lockScreen(Context context) {
		Log.i(TAG, "inside lockScreen()");
		DevicePolicyManager policyMgr = null;
		ComponentName cm = null;
		boolean active;

		policyMgr = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		cm = new ComponentName(context, DeviceLockStatusReceiver.class);
		active = policyMgr.isAdminActive(cm);

		if (!active) {
			policyMgr.lockNow();
			MainService dsMainService = MainService
					.getInstance();
			if (dsMainService != null) {
				dsMainService.onScreenLock();
			}
		} else {
			Intent lockIntent = new Intent(context, DeviceAdminScreen.class);
			lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(lockIntent);
		}

		if (context instanceof Activity) {
			((Activity) context).finish();
		}
	}

	public static char getNextRandomChar()
	{
		char randomChar = SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
		if (randomChar != lastChar) {
			lastChar = randomChar;
		} else {
			for (int i = 0; i < 5; i++) {
				randomChar = SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
				if (randomChar != lastChar) {
					lastChar = randomChar;
					break;
				}

			}
		}
		return randomChar;
	}

	public static int nextCharInterval(final int minTime, final int maxTime) {
		int randomDuration = 0;
		if (minTime >= maxTime) {
			randomDuration = maxTime;
		} else {
			randomDuration = RANDOM.nextInt(maxTime - minTime) + minTime;
		}

		Log.i(TAG, "Random duration is:" + randomDuration);
		return randomDuration;
	}

	/**
	 * Show a popup dialog before making the device screen locked for a short
	 * time and will be dismissed automatically
	 * 
	 * @param title
	 * @param message
	 * @param buttonText
	 * @param iconId
	 * @param context
	 */
	public static void showDeviceLockDialog(String title, String message,
			String buttonText, int iconId, final Activity context) {
		try {
			AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setTitle(title);
			alertDialog.setMessage(message);

			String buttonLabel = "OK";
			if (buttonText != null && buttonText.trim().length() > 0) {
				buttonLabel = buttonText;
			}
			alertDialog.setButton(buttonLabel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (dialog != null) {
								dialog.dismiss();

							}
						}
					});
			alertDialog.setIcon(iconId);

			alertDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					lockScreen(context);
				}
			});

			alertDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					lockScreen(context);
				}
			});

			// Show the alert dialog
			alertDialog.show();
			// Start the timer for waiting after displaying the alert.
			dismissDialogAtDelay(alertDialog);
			// alertDialog.dismiss();

		} catch (Exception e) {
			Log.e(TAG, "Error in showing Lock Button; " + e.toString());
		}

	}

	private static void dismissDialogAtDelay(final AlertDialog alertDialog) {
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(Constants.WAITING_PERIOD_BEFORE_LOCKING * 1000);
					alertDialog.dismiss();
				} catch (Exception e) {
					Log.e(TAG, "Error in startTimer:" + e.toString());
				}
			}
		}.start();
	}

	/**
	 * disconnects incoming call
	 * 
	 * @param context
	 */
	public static void disconnectCall(Context context) {
		Log.d(TAG, "inside disconnecting call");
		try {
			if (iTelephony == null) {
				initializeTelephony(context);
			}
			iTelephony.endCall();
			Log.d(TAG, "call disconnected");
		} catch (Exception e) {
			Log.e(TAG, "Exception while disconnection call :"
					+ e);
		}
	}

	public static void initializeTelephony(Context context) {
		telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class cls = Class.forName(telephony.getClass().getName());
			Method m = cls.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			iTelephony = ((ITelephony) m.invoke(telephony));
		} catch (Exception e) {
			Log.e(TAG, "FATAL ERROR: could not connect to telephony subsystem"
					+ e);
		}
	}

	public static String getDayOfMonthSuffix(final int n) {
		if (n < 1 || n > 31) {
			return "";
		}
		if (n >= 11 && n <= 13) {
			return "th";
		}
		switch (n % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public static String convertToCommaSeparatedString(List<String> list) {
		if (list == null) {
			return "";
		}
		StringBuilder commaSepValueBuilder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			commaSepValueBuilder.append(list.get(i));
			if (i != list.size() - 1) {
				commaSepValueBuilder.append(", ");
			}
		}
		return commaSepValueBuilder.toString();
	}

	public static String[] listToArray(List<String> list) {
		try {
			return list.toArray(new String[list.size()]);
		} catch (Exception ex) {
			return null;
		}
	}

	private static boolean checkForBluetoothAudio(int callState) {
		boolean btAudioState = false;
		if (BlueToothHeadsetStateChangeReceiver.getHeadsetState() == BTHeadSetState.CONNECTED) {
			if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
				try
				{
					Thread.sleep(1000);
				} catch (Exception ie) {
				}
				if (BlueToothHeadsetStateChangeReceiver.getHeadsetAudioState() == 1) {
					btAudioState = true;
				}
			}
		}
		return btAudioState;
	}

	public static String[] getEmergencyNumberArr(String emergencyNos) {
		if (emergencyNos == null) {
			return null;
		}
		try {
			return emergencyNos.split(Constants.EMERGENCY_NO_SEPARATOR);
		} catch (Exception ex) {
			return null;
		}
	}

	public static boolean isHeadsetConnected(Context context) {
		try {
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			if ((audioManager != null && audioManager.isWiredHeadsetOn())
					|| Utility.checkForBluetoothAudio(Utility
							.getPhoneCallState(context))) {
				return true;
			}
		} catch (Exception ex) {
		}
		return false;
	}

	/**
	 * format "19:16, 16th Oct 2014"
	 * 
	 * @return
	 */
	public static String getDateTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String currentTime = sdf.format(calendar.getTime());
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String dayWithSuffix = day + Utility.getDayOfMonthSuffix(day);
		sdf.applyPattern("MMM yyyy");
		String currentMonthYear = sdf.format(calendar.getTime());
		String dateNTime = currentTime + ", " + dayWithSuffix + " "
				+ currentMonthYear + ",";
		Log.i(TAG, "Date and Time:" + dateNTime);
		return dateNTime;
	}

	/**
	 * Sets the device time settings to automatic.
	 */
	public static void setTimeAutomatic(Context context) {
		try {
			android.provider.Settings.System.putInt(
					context.getContentResolver(),
					android.provider.Settings.System.AUTO_TIME, Constants.TRUE);
		} catch (Exception ex) {
			Log.w(TAG,
					"Exception in setTimeAutomatic(): " + ex.getMessage());
		}
	}

	public static boolean isTimeAutomatic(Context context) {
		try {
			return android.provider.Settings.System.getInt(
					context.getContentResolver(),
					android.provider.Settings.System.AUTO_TIME) == Constants.TRUE;
		} catch (Exception ex) {
			Log.w(TAG, "Exception in isTimeAutomatic() : " + ex.getMessage());
			return false;
		}
	}

	public static String getTopPackageName(Context context) {
		String packageName = null;
		List<ActivityManager.RunningTaskInfo> taskInfo;
		try {
			taskInfo = ((ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE))
					.getRunningTasks(1);
			packageName = taskInfo.get(0).topActivity.getPackageName();
		} catch (Exception e) {
			Log.e(TAG, "Error in checking top package name :" + e.toString());
		}

		return packageName;
	}

	public static boolean isWhitelistedAppOnTop(String[] apps, Context context) {
		Log.d(TAG, "inside isWhitelistedAppOnTop() ");
		boolean running = false;
		try {
			if (apps.length > 0) {
				ActivityManager am = (ActivityManager) context
						.getSystemService(Context.ACTIVITY_SERVICE);
				List<ActivityManager.RunningTaskInfo> rs = am
						.getRunningTasks(100);
				ComponentName rsi = rs.get(0).topActivity;
				for (String packageName : apps) {
					if (packageName != null) {
						if (rsi.getPackageName().equalsIgnoreCase(packageName)) {
							running = true;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception :" + e.getMessage());
		}
		return running;
	}

	public static boolean isKeyguardEnabled(Context context) {
		try {
			KeyguardManager kgMgr = (KeyguardManager) context
					.getSystemService(Context.KEYGUARD_SERVICE);
			return kgMgr.inKeyguardRestrictedInputMode();
		} catch (Exception e) {
		}
		return true;
	}

	public static boolean containsNo(String matchNumber, String[] numberArr) {
		if (matchNumber == null || numberArr == null) {
			return false;
		}
		for (String no : numberArr) {
			if (PhoneNumberUtils.compare(matchNumber, no)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsNo(String matchNumber, String numbersInCSA) {
		if (matchNumber == null || numbersInCSA == null) {
			return false;
		}
		String[] numberArr = numbersInCSA
				.split(Constants.EMERGENCY_NO_SEPARATOR);
		if (numberArr == null) {
			return false;
		}
		for (String no : numberArr) {
			if (PhoneNumberUtils.compare(matchNumber, no)) {
				return true;
			}
		}
		return false;
	}


    public static long timeInMilliSecond(int hour, int min, int second){
        return ((hour*60*60)+(min*60)+(second))*1000;
    }

    public static long timeInMilliSecond(String timeInStr){
        String timeUnit[] = timeInStr.split(":");
        long timeInMilli = 0L;
        for(int i =0; i<timeUnit.length; i++){
            if(i == 0){
                timeInMilli +=Integer.parseInt(timeUnit[i])*60*60;
            }else if(i == 1){
                timeInMilli +=Integer.parseInt(timeUnit[i])*60;
            }else{
                timeInMilli +=Integer.parseInt(timeUnit[i]);
            }
        }
        return timeInMilli*1000;
    }

    public static String formatTime(long timeInMillisecond){
        long time = timeInMillisecond/1000;
        int hour = (int)(time/(60*60));
        int minute = (int)((time - hour *60*60)/60);
        int seconds = (int)(time - hour *60*60 - minute *60);
        return String.format("%02d", hour)+":"+String.format("%02d", minute);
    }

    public static boolean validateTime(long timeInMillisecond){
        long time = timeInMillisecond/1000;
        int hour = (int)(time/(60*60));
        int minute = (int)((time - hour *60*60)/60);
        return ((hour <= Constants.MAX_HOUR) && (minute <= Constants.MAX_MIN));
    }


    public static int getDayIndex(String day) {
        if (day == null) {
            return Constants.EOF;
        }
        int index = Constants.EOF;

        for (int i = 0; i < Constants.WEEKDAYS.length; i++) {
            if(Constants.WEEKDAYS[i].equalsIgnoreCase(day)){
                index = i;
            }
        }
        return index;

    }

    /*if((Integer.parseInt(arr[0]) > Constants.MAX_HOUR) || (Integer.parseInt(arr[1]) > Constants.MAX_MIN)){
        arr[0] = String.valueOf(Math.min(Integer.parseInt(arr[0]), Constants.MAX_HOUR));
        arr[1] = String.valueOf(Math.min(Integer.parseInt(arr[1]), Constants.MAX_MIN));
        edittext.setText(arr[0]+":"+arr[1]);
    }else{
        edittext.setText(cashAmountBuilder.toString());
    }*/
}
package com.sunilsahoo.drivesafe.services;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.listener.AlarmReceiver;
import com.sunilsahoo.drivesafe.listener.CustomLocationManager;
import com.sunilsahoo.drivesafe.listener.CustomPhoneStateListener;
import com.sunilsahoo.drivesafe.listener.ScreenLockStatusChangeReceiver;
import com.sunilsahoo.drivesafe.model.DaySettings;
import com.sunilsahoo.drivesafe.model.Profile;
import com.sunilsahoo.drivesafe.model.Report;
import com.sunilsahoo.drivesafe.ui.GPSEnableAlert;
import com.sunilsahoo.drivesafe.ui.UserTestResultAlert;
import com.sunilsahoo.drivesafe.ui.UserTestScreen;
import com.sunilsahoo.drivesafe.utility.Constants;
import com.sunilsahoo.drivesafe.utility.MessageType;
import com.sunilsahoo.drivesafe.utility.ScreenState;
import com.sunilsahoo.drivesafe.utility.TestResultCategory;
import com.sunilsahoo.drivesafe.utility.Utility;

public class MainService extends Service {
	private static final String TAG = MainService.class.getName();
	private static MainService mMainServiceObj = null;
	private byte mTestStatus = TestResultCategory.UNKNOWN;
	private boolean mCallDuringEmergency = false;
	private boolean isStartSpeedDetect = false;
	private boolean disconnectCall = false;
	private boolean isCallInProgress = false;
	private boolean isEmergencyCallInitiated = false;
	private boolean isEmergencyCallInProgress = false;
	private boolean onUsertestPassedTime = false;
	private boolean sendCallEndMsg = false;

	private byte screenStatus = ScreenState.ON;
	private long outgoingCallDisconectTime = Constants.EOF;

	private String mOutCallNumber = null;
	private String outgoingNumber = null;
	private String incomingNumber = null;

	private BroadcastReceiver mScreenLockChangeReceiver = null;
	private ServiceHandler mServiceHandler = null;
	private Looper mServiceLooper = null;
	private NotificationManager mNotificationManager = null;
	private CustomLocationManager mLocationManager = null;
	private Profile profile = null;
	private TelephonyManager mTelephonyManager = null;
	private CustomPhoneStateListener phonStateReceiver = null;
	private Thread checkTestStatTimoutThread = null;
	private Thread checkTestTimoutThread = null;

	@Override
	public void onCreate() {
		Log.i(TAG, "Inside onCreate()");
        mMainServiceObj = this;
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
        profile = DBOperation.getProfile(this);
		startListeners();
	}

	@Override
	public void onDestroy() {
		try {
            mMainServiceObj = null;
			if (mLocationManager != null) {
				mLocationManager.stopListening();
			}
			// unregister phone change listener
			unRegisterPhoneStateChangeListener();
			// Stop Screen Lock Listening
			if (mScreenLockChangeReceiver != null) {
				unregisterReceiver(mScreenLockChangeReceiver);
			}
			if (mServiceLooper != null) {
				mServiceLooper.quit();
				mServiceLooper.getThread().interrupt();
				mServiceHandler = null;
			}

		} catch (Exception ex) {
			Log.e(TAG, "Exception in stopping the Service : " + ex.getMessage());
		}

		super.onDestroy();
	}

	private void processTestScreenMsg() {
		Log.i(TAG, "inside processTestScreenMsg() ");
            Intent intent = new Intent(this, UserTestScreen.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			Log.i(TAG, " Speed Alert Displayed :");
	}

	public synchronized void disconnectCall(boolean sendMsg,
			boolean checkHeadset) {

		if (!checkHeadset
				|| !Utility.isHeadsetConnected(getInstance(), profile.isHeadsetConnectionAllowed())) {
			disconnectCall = true;
			sendCallEndMsg = sendMsg;
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
						disconnectCall = false;
					} catch (Exception e) {

					}
				}
			}.start();
		}
	}

	/**
	 * checks whether services are running
	 */
	public void startSpeedListener() {
        boolean shouldDetectSpeed = isCapableToDetectSpeed();
		Log.i(TAG, "inside startSpeedListener() shouldDetectSpeed -:"+shouldDetectSpeed);
		if (shouldDetectSpeed) {
			if (mLocationManager == null) {
				mLocationManager = CustomLocationManager.getInstance(this);
			}
			if (!mLocationManager.isListeningSpeed()) {
				sendMsgToServiceHandler(MessageType.START_SPEED_LISTENING, null);
			}
		}

	}

	public void initializeEmergencyState() {
		Log.i(TAG, "inside initializeEmergencyState()");
		isEmergencyCallInitiated = true;
		mTestStatus = TestResultCategory.INITIATE_EMERGENCY;
		sendMsgToServiceHandler(MessageType.STOP_SPEED_LISTENING, null);
		startEmergencyTimerAtDelay(Constants.WAITING_PERIOD_BEFORE_CLEARING_EMERGENCY_CALL * 1000);
		stopTestInitTimeoutThread();
		stopTestTimeoutThread();
		disconnectCall(false, false);
	}

	/**
	 * Check if any call to/from emergency number is going on.
	 * 
	 */
	private boolean isEmergencyCall() {
		boolean isEmergency = false;
		if (outgoingNumber != null) {
			isEmergency = Utility.containsNo(outgoingNumber, getEmergencyNos());
		} else if (incomingNumber != null) {
			isEmergency = Utility.containsNo(incomingNumber, getEmergencyNos());
		}
		return isEmergency;
	}

	public boolean isCallInProgress() {
		return isCallInProgress;
	}

	public boolean isInitiatingEmergency() {
		return isEmergencyCallInitiated;
	}

	/**
	 * Return if the phone is in emergency state. This can happen after user
	 * pressed Emergency Call from UserTest screen. Also emergency state can
	 * occur
	 * for maximum two minutes.
	 * 
	 * @return - true if user selected emergency, else false.
	 */
	public boolean isOnEmergency() {
		return isEmergencyCallInProgress;
	}

	private boolean isUsertestInitTimoutRunning() {
		if (checkTestStatTimoutThread != null
				&& checkTestStatTimoutThread.isAlive()) {
			return true;
		}
		return false;
	}

	private boolean isUsertestTimoutRunning() {
		if ((checkTestTimoutThread != null) && (checkTestTimoutThread.isAlive())) {
			return true;
		}
		return false;
	}

	private void registerSpeedListener() {
		if (mLocationManager == null) {
			mLocationManager = CustomLocationManager.getInstance(this);
		}

		if (isCapableToDetectSpeed()) {
			sendMsgToServiceHandler(MessageType.START_SPEED_LISTENING, null);
		}
		performDaySettings();
	}

	/**
	 * sends message to service handler
	 * 
	 * @param requestID
	 * @param info
	 */
	public void sendMsgToServiceHandler(int requestID, String info) {
		Log.i(TAG, "inside sendMsgToServiceHandler() - requestID: " + requestID);
		if (mServiceHandler.hasMessages(requestID)) {
			mServiceHandler.removeMessages(requestID);
		}

		Message msg = mServiceHandler.obtainMessage();
		msg.what = requestID;
		if (info != null) {
			msg.obj = info;
		}
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * This method will be called whenever a call gets disconnected. This method
	 * will check the
	 * device screen status and will stop detecting the speed if the it founds
	 * that the screen is off.
	 */
	public void onCallDisconnected() {
		isCallInProgress = false;
		mCallDuringEmergency = false;
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(false);
		if (profile != null) {

			if (Utility.containsNo(outgoingNumber, profile.getEmergencyNos())) {
				registerEmergencyAccessAlarm();
			}

		} else {
			// Stop UserTest Timeout checker if it was started
			stopTestTimeoutThread();
			boolean shouldDetectSpeed = isCapableToDetectSpeed();
			if (!shouldDetectSpeed) {
				sendMsgToServiceHandler(MessageType.STOP_SPEED_LISTENING, null);
			}
		}
	}

	public void onCallReceived(String incomingNumber) {
		Log.i(TAG, "Call Coming from:" + incomingNumber);
		isCallInProgress = true;
		if (incomingNumber != null && profile != null) {
			TelephonyManager tm = (TelephonyManager) getApplicationContext()
					.getSystemService(Context.TELEPHONY_SERVICE);
			int phoneStatus = tm.getCallState();
            /*if(!profile.isTestEnable()){
                mTestStatus = TestResultCategory.DISABLED;
            }*/
			Log.i(TAG, "UserTest Status:" + mTestStatus);
			boolean isEmergencyNumber = Utility.containsNo(incomingNumber,
					profile.getEmergencyNos());

			if (mTestStatus == TestResultCategory.SCREEN
					|| mTestStatus == TestResultCategory.DISABLED
					|| mTestStatus == TestResultCategory.FAILED) {

				// Check if the wired or Bluetooth headset is connected or not
				if (!Utility.isHeadsetConnected(getInstance(), profile.isHeadsetConnectionAllowed())
						&& !isEmergencyNumber) {
					Utility.disconnectCall(this);
				} else {
					Log.i(TAG, "Head Set connected, call state:" + phoneStatus);
					// If the headset is connected then the call will be
					// remained connected, so we have to make handlingCall false
					// if the
					// call is received.
					if (phoneStatus == TelephonyManager.CALL_STATE_OFFHOOK) {
						isCallInProgress = false;
					}
				}

			} else {
				Log.i(TAG, "Usertest screen not shown.");
				isCallInProgress = false;
				// Check if this call is coming during emergency free access
				// time, if so then make a flag true which will later be used to
				// disconnect this call.
				if (isEmergencyCallInProgress && !isEmergencyNumber) {
					mCallDuringEmergency = true;
				}
                startSpeedListener();

			}
		}
	}

	/**
	 * Called when user dials an Emergency number initiated from the
	 * application.
	 */
	public void onEmergency() {

		Context appContext = getApplicationContext();

		if (!Utility.isMobileNetworkAvailable(appContext)) {
			// If the mobile network is not active, then wait for few
			// milliseconds before checking if the user really has connected to
			// a emergency number or not. if the call status is not offhook,
			// then clear all the emergency related status.
			try {
				Thread.sleep(500);
				if (Utility.getPhoneCallState(appContext) != TelephonyManager.CALL_STATE_OFFHOOK) {
					restoreUsertestScreen();
					return;
				}
			} catch (Exception e) {
				Log.d(TAG, "Exception ex :" + e);
			}
		}

		try {
			isEmergencyCallInProgress = true;
			isEmergencyCallInitiated = false;
			mTestStatus = TestResultCategory.EMERGENCY;
			Report report = new Report();
			report.setReportType(Constants.EMERGENCY_NO_CALLED);
			DBOperation.insertReport(report, null);
		} catch (Exception e) {
			Log.e(TAG, "Error onEmergency():" + e.toString());
		}

	}

	public void onEmergencyAccessTimePassed() {
		Log.i(TAG,
				"inside onEmergencyAccessTimePassed()");
		isEmergencyCallInProgress = false;
		if (mTestStatus == TestResultCategory.EMERGENCY) {
			mTestStatus = TestResultCategory.UNKNOWN;
		}
		boolean shouldDetectSpeed = isCapableToDetectSpeed();
		if (shouldDetectSpeed) {
			sendMsgToServiceHandler(MessageType.START_SPEED_LISTENING, null);
		}
	}

	private void onEmergencyTimerOver() {
		Log.d(TAG, "inside onEmergencyTimerOver() ");
		isEmergencyCallInitiated = false;
		if (mTestStatus == TestResultCategory.INITIATE_EMERGENCY) {
			mTestStatus = TestResultCategory.UNKNOWN;
		}
		Log.i(TAG, " onEmergency :" + isEmergencyCallInProgress);

		if (!isEmergencyCallInProgress) {
			verifySpeedListener();
		}
	}

	private void onGpsEnableTimeout() {
		disconnectCall(true, true);
		Utility.lockScreen(this);
	}

	/**
	 * This method will be called from {@link GPSEnableAlert} if the is not
	 * enabled within 1 minute after showing the alert to enable GPS.
	 */
	public void enableGPS() {
		if ((mLocationManager != null)
				&& (mLocationManager.isGPSProviderDisabled())) {
			sendMsgToServiceHandler(MessageType.GPS_ENABLE_TIMEOUT, null);
		}
	}

	public void onGPSStatusChange(boolean isGpsOn) {
		Log.i(TAG, "onGPSStatusChange() - isGpsOn : " + isGpsOn);
		if (!isGpsOn) {
			int callState = Utility.getPhoneCallState(getApplicationContext());
			if (callState != TelephonyManager.CALL_STATE_RINGING) {
				Log.i(TAG, "Display gps disable alert");
				sendMsgToServiceHandler(MessageType.GPS_DISABLE, null);
			}
		}
	}

	public void onOutgoingCallReceived() {
		Log.i(TAG, "onOutgoingCallReceived(); UserTest Status:"
				+ mTestStatus
				+ " Incomng Number:" + incomingNumber + " Outgoing Number:"
				+ outgoingNumber);

		boolean isEmergencyNumber = false;
		if (profile != null) {
			isEmergencyNumber = Utility.containsNo(outgoingNumber,
					profile.getEmergencyNos())
					|| Utility.containsNo(incomingNumber,
							profile.getEmergencyNos());
			if (outgoingNumber != null) {
				isEmergencyNumber = Utility.containsNo(outgoingNumber,
						profile.getEmergencyNos());
			} else if (incomingNumber != null) {
				isEmergencyNumber = Utility.containsNo(incomingNumber,
						profile.getEmergencyNos());
			}
		}

		TelephonyManager tm = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		int phoneStatus = tm.getCallState();

		String notificationNumber = null;
		if (outgoingNumber != null) {
			notificationNumber = outgoingNumber;
		} else if (incomingNumber != null) {
			notificationNumber = incomingNumber;
		}

		Log.i(TAG, "isEmergencyNumber :" + isEmergencyNumber
				+ "disconnectCall :" + disconnectCall + " handlingCall :"
				+ isCallInProgress);

		if (disconnectCall) {
			disconnectCall = false;
			if (!isEmergencyNumber) {
				// block the call
				Utility.disconnectCall(this);
				if (sendCallEndMsg) {
					if (phoneStatus == TelephonyManager.CALL_STATE_RINGING
							|| phoneStatus == TelephonyManager.CALL_STATE_OFFHOOK) {

						long curTime = System.currentTimeMillis();
						if (!(curTime - outgoingCallDisconectTime < (Constants.NOTIFICATION_THROTTLE_TIME * 1000)
								&& notificationNumber != null
								&& notificationNumber.equals(mOutCallNumber))) {
							outgoingCallDisconectTime = curTime;
							mOutCallNumber = notificationNumber;
						}
					}
				} else {
					sendCallEndMsg = true;
				}
			}

		}

		if (mTestStatus == TestResultCategory.SCREEN
				|| mTestStatus == TestResultCategory.DISABLED) {

			// Now we have to make handlingCall false, if the call is
			// answered.
			if (incomingNumber != null
					&& phoneStatus == TelephonyManager.CALL_STATE_OFFHOOK
					&& isCallInProgress) {
				isCallInProgress = false;

				displayTestScreenOnTop();
			}

		} else {

			// Check if this call is coming during emergency free access
			// time, if so then make a flag true which will later be used to
			// disconnect this call.
			if (isEmergencyCallInProgress && !isEmergencyNumber
					&& incomingNumber != null) {
				mCallDuringEmergency = true;
			}

			boolean shouldDetectSpeed = isCapableToDetectSpeed();
			if (shouldDetectSpeed) {
				startSpeedListener();
				if (mLocationManager.isGPSProviderDisabled()) {
					Log.i(TAG, "Showing GPS Enable Alert after receiving call");
					sendMsgToServiceHandler(MessageType.GPS_DISABLE, null);
				}

			}

		}

	}

	public void onScreenLock() {
		Log.i(TAG, "inside onScreenLock()");

		new Thread() {
			@Override
			public void run() {
				byte count = 0;
				byte maxcount = 15;
				try {
					Log.i(TAG, "Start Checking screen lock state");
					while (count++ < maxcount) {
						if (Utility.isKeyguardEnabled(getInstance())) {
							screenStatus = ScreenState.IDLE;
							Log.i(TAG,
									"Device screen locked state, UserTest State:"
											+ mTestStatus);
							// Clear the UserTest status
							if (mTestStatus != TestResultCategory.SCREEN) {
								mTestStatus = TestResultCategory.UNKNOWN;
							}
							break;
						}
						Thread.sleep(200);
					}

				} catch (Exception ex) {
					Log.e(TAG, "Exception : " + ex.getMessage());
				}

			}
		}.start();
	}

	/**
	 * Called when the device screen on/off.
	 */
	public void onScreenOff(boolean isOff) {
		Log.d(TAG, "inside onScreenOff() - isOff :" + isOff);
		if (isOff) {
			screenStatus = ScreenState.IDLE;
			if (!isCapableToDetectSpeed()) {
				sendMsgToServiceHandler(MessageType.STOP_SPEED_LISTENING, null);
			}
			if (mTestStatus != TestResultCategory.SCREEN) {
				mTestStatus = TestResultCategory.UNKNOWN;
			}
		} else {
			screenStatus = ScreenState.ON;
			try {
				if (isCapableToDetectSpeed()) {
					if (mLocationManager == null) {
						mLocationManager = CustomLocationManager
								.getInstance(mMainServiceObj);
					}
					if (mLocationManager.isGPSProviderDisabled()
							|| !mLocationManager.isListeningSpeed()) {
						sendMsgToServiceHandler(
								MessageType.START_SPEED_LISTENING, null);
					}
				}
				if (mTestStatus != TestResultCategory.EMERGENCY
						&& mTestStatus != TestResultCategory.PASSED
						&& mTestStatus != TestResultCategory.SCREEN) {
					mTestStatus = TestResultCategory.UNKNOWN;
				}
			} catch (Exception ex) {
				Log.w(TAG, "Exception :" + ex.getMessage());
			}
		}
	}

	/**
	 * Called when the speed listener has to be started as per time of day rule.
	 * This is called from a BroadcastReceiver registered for listening the
	 * Alarm notification for starting speed listener.
	 */
	public void onSpeedStartRequest() {
		boolean shouldDetectSpeed = isCapableToDetectSpeed();
		Log.i(TAG, "Inside onSpeedStartRequest, should detect speed:"
				+ shouldDetectSpeed);
		if (shouldDetectSpeed && profile != null) {
			// Get the stop time of speed listener
			long stopTime = 0;

			Calendar calendar = Calendar.getInstance();
			// Get the current days setting
			DaySettings curDaysSetting = profile.getDaySettings().get(calendar
					.get(Calendar.DAY_OF_WEEK) - 1); // Calendar
			if (curDaysSetting != null) {
				stopTime = curDaysSetting.getStopTime();
			} else {
				stopTime = (23 * 60 + 59) * 60 * 1000;
			}
			isStartSpeedDetect = true;
			long curTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar
					.get(Calendar.MINUTE)) * 60 * 1000;
			setSpeedAlarm(stopTime - curTime, false);
			verifySpeedListener();

		}

	}

	/**
	 * Called when the speed listener has to be stopped as per time of day rule.
	 * This is called from a BroadcastReceiver registered for listening the
	 * Alarm notification for start/stop speed listener.
	 */
	public void onSpeedStopRequest() {
		boolean shouldDetectSpeed = isCapableToDetectSpeed();
		Log.i(TAG, "Inside onSpeedStopRequest, should detect speed:"
				+ shouldDetectSpeed);
		if (!shouldDetectSpeed) {
			isStartSpeedDetect = false;
		}
		verifySpeedListener();
	}

	public void onSpeedUpdate() {

		if (profile == null) {
			return;
		}

		// Checking & converting unit of speed from Mps into (Mph or Kph)
//		currentSpeed = (float) (2.236936 * currentSpeed);
        float currentSpeed = 0.0f;
        if(CustomLocationManager.getLocation() != null){
            currentSpeed = CustomLocationManager.getLocation().getSpeed();
        }
		Log.i(TAG,
				"Updated speed is :" + currentSpeed
						+ " KMph \r\nCurrent config speed:"
						+ profile.getThresholdSpeed()
						+ " usertestStatus:"
						+ mTestStatus);

		if (currentSpeed >= profile.getThresholdSpeed()) {
			int callState = Utility.getPhoneCallState(getApplicationContext());
			if (mTestStatus == TestResultCategory.UNKNOWN) {
				// Check if the usertest is enabled or not, if not then
				// directly lock the screen without showing the speed alert.
				if (!profile.isTestEnable()) {

						if ((callState == TelephonyManager.CALL_STATE_OFFHOOK)
								&& !Utility.isHeadsetConnected(getInstance(), profile.isHeadsetConnectionAllowed())) {
							if (outgoingNumber != null || mCallDuringEmergency) {
								disconnectCall(true, true);
							}
						}

						// If the call state is not ringing, then only lock the
						// screen, so that user is able accept the incoming call
						if (callState != TelephonyManager.CALL_STATE_RINGING) {
							if (screenStatus != ScreenState.LOCK
									&& !Utility.isKeyguardEnabled(this)) {
								// Lock the screen by showing alert.
								sendMsgToServiceHandler(
										MessageType.LOCK_ON_SPEED,
										null);
								mTestStatus = TestResultCategory.DISABLED;
							}
						}

				} else {
					String updatedSpeedToShow = " "
							+ profile.getThresholdSpeed();
					if ((callState == TelephonyManager.CALL_STATE_OFFHOOK || callState == TelephonyManager.CALL_STATE_RINGING)
							&& !Utility.isHeadsetConnected(getInstance(), profile.isHeadsetConnectionAllowed())
							&& !isEmergencyCall()
							&& (outgoingNumber != null || mCallDuringEmergency)) {

						if (screenStatus != ScreenState.LOCK) {
							if (mTestStatus == TestResultCategory.PASSED) {
								sendMsgToServiceHandler(
										MessageType.START_USERTEST_IN_CALL,
										null);
							} else {
								Log.i(TAG, "Display D/P screen");
								sendMsgToServiceHandler(
										MessageType.SPEED_EXCEED,
										updatedSpeedToShow);
							}
							mTestStatus = TestResultCategory.SCREEN;
							if (!isEmergencyCall()) {
								showCallEndAlertInAdv();
								startUsertestInitTimeout();
								startUsertestTimeout();
							}
						}

					} else {
						Log.i(TAG, "Exceeds maximum speed");
						if (screenStatus != ScreenState.LOCK) {
							sendMsgToServiceHandler(MessageType.SPEED_EXCEED,
									updatedSpeedToShow);
							mTestStatus = TestResultCategory.SCREEN;
						}
					}
				}
			} else if (mTestStatus == TestResultCategory.SCREEN) {
				if (callState != TelephonyManager.CALL_STATE_RINGING) {
					String topPackage = Utility.getTopPackageName(this);
					if (!isEmergencyCall()) {
						if (topPackage != null
								&& !topPackage
										.equals(this.getPackageName())
								&& !Utility.getInstalledPackages(
										Constants.ACTION_DIALER,
										getApplicationContext()).contains(
										topPackage)
								&& screenStatus != ScreenState.LOCK) {
							displayTestScreenOnTop();
						}

						if (callState == TelephonyManager.CALL_STATE_OFFHOOK
								&& !Utility.isHeadsetConnected(getInstance(), profile.isHeadsetConnectionAllowed())
								&& !isUsertestTimoutRunning()
								&& (outgoingNumber != null || mCallDuringEmergency)
								&& mTestStatus != TestResultCategory.PASSED) {
							showCallEndAlertInAdv();
							startUsertestInitTimeout();
							startUsertestTimeout();
						}
					}
				}
			}
		} else {
			// Speed less than the max limit so stop UserTest
			stopTestInitTimeoutThread();
			stopTestTimeoutThread();
			mTestStatus = TestResultCategory.UNKNOWN;
			if (UserTestScreen.getInstance() != null) {
                UserTestScreen.getInstance().forceStop();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "inside onStartCommand()");
		mMainServiceObj = this;
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.what = MessageType.START_SERVICE;
		mServiceHandler.sendMessage(msg);
		return START_STICKY;
	}

	public void onTimeChangedCallback() {
		new Thread() {
			@Override
			public void run() {
				if (!Utility.isTimeAutomatic(getInstance())) {
					Utility.setTimeAutomatic(getInstance());
				}
				performDaySettings();

				// If speed listener is running, reschedule the stop time
				if (mLocationManager != null
						&& mLocationManager.isListeningSpeed()
						&& profile != null) {
					Calendar calendar = Calendar.getInstance();
					long stopTime = 0;
					// Get the current days setting
					DaySettings curDaysSetting = profile
							.getDaySettings().get(calendar
                                    .get(Calendar.DAY_OF_WEEK) - 1);
					if (curDaysSetting != null) {
						stopTime = curDaysSetting.getStopTime();
					} else {
						stopTime = Utility.timeInMilliSecond(Constants.MAX_HOUR, Constants.MIN_MIN, Constants.MIN_MIN);
					}

					long curTime = Utility.timeInMilliSecond(calendar
                            .get(Calendar.HOUR_OF_DAY), calendar
							.get(Calendar.MINUTE), calendar
                            .get(Calendar.SECOND));

					if (stopTime > curTime) {
						setSpeedAlarm(stopTime - curTime, false);
					}
				}
			}
		}.start();
	}

	public void onUsertestAccesTimePassed() {
		Log.i(TAG, "Inside onUsertestAccesTimePassed()");
		mTestStatus = (mTestStatus == TestResultCategory.PASSED) ? TestResultCategory.UNKNOWN
				: mTestStatus;
		onUsertestPassedTime = false;
		if (isCapableToDetectSpeed()) {
			sendMsgToServiceHandler(MessageType.START_SPEED_LISTENING, null);
		}
	}

	/*public void onUsertestDriverSelected() {
		Log.i(TAG, "Inside on Driver selected.");
		mTestStatus = TestResultCategory.DRIVER;
		if (isUsertestTimoutRunning() || isUsertestInitTimoutRunning()) {
			if (profile != null
					&& (outgoingNumber != null
							|| profile.isDisconnectCall() || mCallDuringEmergency)) {
				Log.i(TAG, "Disconnect call :"
						+ mCallDuringEmergency);
				Utility.disconnectCall(this);
			}
			testResultHandler.sendEmptyMessage(TestResultCategory.TIMEOUT);
			stopTestTimeoutThread();
			stopTestInitTimeoutThread();
		} else {
			testResultHandler.sendEmptyMessage(TestResultCategory.DRIVER);
		}
	}*/

	/**
	 * To be called if user failed to pass the UserTest.
	 */
	public void onUsertestFailed(byte reasonFailed) {
		mTestStatus = TestResultCategory.FAILED;
		if (isUsertestTimoutRunning() || isUsertestInitTimoutRunning()) {

			if (profile != null
					&& (outgoingNumber != null || mCallDuringEmergency)) {
				Log.i(TAG,
						"Disconnect call :");
				Utility.disconnectCall(this);
			}

			if (reasonFailed != TestResultCategory.PAUSED) {
				testResultHandler.sendEmptyMessage(TestResultCategory.TIMEOUT);
			}
			stopTestTimeoutThread();
			stopTestInitTimeoutThread();
		} else if (reasonFailed != TestResultCategory.PAUSED) {
			testResultHandler.sendEmptyMessage(TestResultCategory.FAILED);
		}
	}

	public void onUsertestPassAttempt() {
		stopTestInitTimeoutThread();
	}

	/**
	 * To be called if user pass the UserTest
	 */
	public void onUsertestPassed() {
		Log.i(TAG, "inside onUsertestPassed()");
		// Stop Speed Listening
		sendMsgToServiceHandler(MessageType.STOP_SPEED_LISTENING, null);
		mTestStatus = TestResultCategory.PASSED;
		screenStatus = ScreenState.ON;
		onUsertestPassedTime = true;
		registerUsertestPassedAlarm();
		if (isUsertestTimoutRunning()) {
			stopTestTimeoutThread();
		}
		stopTestInitTimeoutThread();
		testResultHandler.sendEmptyMessage(TestResultCategory.PASSED);
	}

	public void onUsertestTimout() {
		Log.d(TAG, "inside onUsertestTimout()");
		if (profile != null
				&& (outgoingNumber != null || mCallDuringEmergency)) {
			Utility.disconnectCall(this);
		}

		mTestStatus = TestResultCategory.FAILED;
		testResultHandler.sendEmptyMessage(TestResultCategory.TIMEOUT);
	}

	public void performDaySettings() {
		if (profile != null) {
			boolean shouldDetectSpeed = true; // Default settings is true

			long startTime = 0;
			long stopTime = 0;
			Log.i(TAG, "Checking if app should detect speed.");
			Calendar calendar = Calendar.getInstance();
			// Get the current days setting
			int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			DaySettings curDaysSetting = profile.getDaySettings().get(dayIndex);
			if (curDaysSetting != null) {
				if (curDaysSetting.isEnabled()) {
					// If day settings is enabled, calculate the day of time
					startTime = curDaysSetting.getStartTime();
					stopTime = curDaysSetting.getStopTime();
				} else {
					shouldDetectSpeed = false;
				}

			} else {
				startTime = 0;
				stopTime = (23 * 60 + 59) * 60 * 1000;
			}

			// Check with the current time of the day.
			int curTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar
					.get(Calendar.MINUTE)) * 60 * 1000;

			if (shouldDetectSpeed) {
				if (curTime >= startTime && curTime < stopTime) {
					isStartSpeedDetect = true;
				} else if (curTime < startTime) {
					Log.i(TAG, " start speed listener later ");
					isStartSpeedDetect = false;
					setSpeedAlarm(startTime - curTime, true);
				} else if (curTime > stopTime) {
					isStartSpeedDetect = false;
					setSpeedAlarmNext();
				}
			} else {
				isStartSpeedDetect = false;
			}
			verifySpeedListener();
		}

	}

	/**
	 * This is to notify the user that his call may
	 * disconnected due to speed as the application started detecting the speed.
	 */
	private void showCallEndAlertInAdv() {
		Log.i(TAG, "inside showCallEndAlertInAdv()");
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(Constants.ACTION_CALL_DISCONNECT_NOTIFIC);
		Notification notification = new Notification(R.drawable.icon, getString(R.string.msg_status_tickertext), System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(this, getString(R.string.msg_status_title), getString(R.string.msg_status_notification), pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;

		long[] vibratePattern = { 0, 100, 200, 300 };

		mNotificationManager.notify(Constants.NOTIFIC_STATUS_ID,
				notification);
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(vibratePattern, Constants.EOF);
		clearNotification(Constants.NOTIFIC_CLEAR_DELAY * 1000);
	}

	/**
	 * Register an alarm event after user initiates an emergency call from the
	 * Application to give free access to the user for a time defined in
	 * configuration file.
	 */
	private void registerEmergencyAccessAlarm() {
		Log.i(TAG, "inside registerEmergencyAccessAlarm() ");
		AlarmManager alarmMgr = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(getApplicationContext(),
				AlarmReceiver.class);
		alarmIntent.addCategory(Constants.ALARM_EMERGENCY_ACCESS_TIMER);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, alarmIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		long waitingTime = Constants.KEY_EMERGENCY_ACCESS_TIME * 1000;
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + waitingTime, pendingIntent);
	}

	/**
	 * Register an alarm event after user passed the UserTest. It will return a
	 * callback (call {@link #onUsertestAccesTimePassed()}) after a configured
	 * time
	 * defined under <postusertesttime> in configuration file.
	 */
	private void registerUsertestPassedAlarm() {

		Log.i(TAG, "Registering the timer for clearing UserTest access time");

		AlarmManager alarmMgr = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(getApplicationContext(),
				AlarmReceiver.class);
		alarmIntent.addCategory(Constants.ALARM_USERTEST_PASSED_TIMER);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, alarmIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		long waitingTime = Constants.POST_USERTEST_TIME; // Default value
		if (profile != null
				&& profile.getPostUsertestAccessTime() > 0) {
			waitingTime = profile.getPostUsertestAccessTime();
		}

		waitingTime = waitingTime * 1000;

		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + waitingTime, pendingIntent);
		Log.i(TAG, "Alarm set for clearing UserTest passed state.");

	}

	public void requestRestartSpeedListener() {
		Log.i(TAG, "Making speed listener restart request.");
		sendMsgToServiceHandler(MessageType.RESTART_SPEED, null);
	}

	public void restoreUsertestScreen() {
		mServiceHandler.removeCallbacks(emergecnyCallTimerRunnable);
		onEmergencyTimerOver();
	}

	public void setIncomingNo(String incomingNum) {
		incomingNumber = incomingNum;
	}

	public void setOutgoingNo(String outgoingNum) {
		this.outgoingNumber = outgoingNum;
	}

	public void setScreenStatus(final byte screenState) {
		this.screenStatus = screenState;
	}

	private void setSpeedAlarm(long delay, boolean start) {
		// Resister alarm to start speed listener for today
		AlarmManager alarmMgr = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(getApplicationContext(),
				AlarmReceiver.class);
		if (start) {
			alarmIntent.addCategory(Constants.ALARM_START_SPEED);
		} else {
			alarmIntent.addCategory(Constants.ALARM_STOP_SPEED);
		}

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, alarmIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + delay, pendingIntent);
	}

	/**
	 * Set the alarm for starting the speed listener in immediate next day in
	 * which the
	 * speed settings is enabled.
	 */
	private void setSpeedAlarmNext() {

		if (profile != null) {
			Calendar calendar = Calendar.getInstance();
			// Get the current days setting
			int curDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			DaySettings daysSetting = null;
			int daysIndexInWeek = curDayIndex + 1;

			long timeAdavanced = 0;
			for (int daysAdvanced = 0; daysAdvanced < Constants.NO_OF_DAYS_IN_WEEK; daysAdvanced++, daysIndexInWeek++) {
				daysSetting = profile.getDaySettings().get(daysIndexInWeek);
				if (daysSetting == null || daysSetting.isEnabled()) {
					long timeRemainingToday = ((24 - calendar
							.get(Calendar.HOUR_OF_DAY)) * 60 + (60 - calendar
							.get(Calendar.MINUTE))) * 60 * 1000;
					if (daysSetting == null) {
						timeAdavanced = timeRemainingToday + daysAdvanced * 24
								* 60 * 60 * 1000L;
					} else {
						timeAdavanced = timeRemainingToday + daysAdvanced * 24
								* 60 * 60 * 1000L + daysSetting.getStartTime();
					}
					setSpeedAlarm(timeAdavanced, true);
					break;
				}

				if (daysIndexInWeek == (Constants.NO_OF_DAYS_IN_WEEK - 1)) {
					daysIndexInWeek = 0;
				}

			}
		}

	}

	/**
	 * Check if the application should detect the speed now.
	 * 
	 * @return - true if application should detect speed, false otherwise.
	 */
	private boolean isCapableToDetectSpeed() {
		Log.i(TAG, "inside isCapableTODetectSpeedDetectSpeedNow()");
		if (profile != null && !isEmergencyCallInProgress && !isEmergencyCallInitiated
				&& !onUsertestPassedTime) {

			Log.i(TAG, " initiatingEmergency:" + isEmergencyCallInitiated
					+ " onEmergency:"
					+ isEmergencyCallInProgress + " onUsertestPassedTime:"
					+ onUsertestPassedTime);

			Calendar calendar = Calendar.getInstance();
			// Get the current days setting
			int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			// Check with the current time of the day.
			int curTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar
					.get(Calendar.MINUTE)) * 60 * 1000;
			DaySettings curDaysSetting = profile.getDaySettings().get(dayIndex);
			if (curDaysSetting != null) {
				if (curDaysSetting.isEnabled()) {
					// If day settings is enabled, calculate the day of time
					long startTime = curDaysSetting.getStartTime();
					long stopTime = curDaysSetting.getStopTime();
					if (curTime >= startTime && curTime < stopTime) {
						if (screenStatus == ScreenState.IDLE) {
							// Check if any call is going on
							int callState = Utility
									.getPhoneCallState(this);
							if (callState == TelephonyManager.CALL_STATE_RINGING
									|| callState == TelephonyManager.CALL_STATE_OFFHOOK) {
								return true;
							}
						} else {
							return true;
						}
					}
				} else {
					Log.i(TAG, "App should not detect speed now.");
					return false;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	private void showGpsDisabledAlert() {
		try {
			Intent gpsIntent = new Intent(this, GPSEnableAlert.class);
			gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(gpsIntent);
		} catch (Exception e) {
			Log.e(TAG, "Error in Showing the GPS enable dialog:" + e.toString());
		}
	}

	public void showNotification(String text) {
		Log.i(TAG, "Inside showNotification.");
		Message msg = mServiceHandler.obtainMessage();
		msg.what = MessageType.NOTIFICATION;
		msg.obj = text;
		mServiceHandler.sendMessage(msg);
	}

	/**
	 * Displays the speed alert screen (ie D/P screen)
	 * 
	 * @param
	 */
	private void showSpeedAlert(String speed) {
		Log.i(TAG, "Inside showSpeedAlert()");
		mTestStatus = TestResultCategory.SCREEN;
		Intent dialogIntent = new Intent(this, UserTestScreen.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		dialogIntent.putExtra("SPEED", speed);
		getApplication().startActivity(dialogIntent);
		Log.i(TAG, "Inside showSpeedAlert(), Speed Alert Shown");
	}

	/**
	 * Displays the alert screen before locking the device due to speed
	 */
	private void showSpeedLockAlert() {
		try {
			setScreenStatus(ScreenState.LOCK);
			Intent alertIntent = new Intent(getApplicationContext(),
					UserTestResultAlert.class);
            alertIntent.putExtra(Constants.USERTEST_STATUS,
                    TestResultCategory.DISABLED);
			alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(alertIntent);
		} catch (Exception ex) {
			Log.e(TAG, "Exception in showSpeedLockAlert() : " + ex.getMessage());
		}
	}

	/**
	 * Displays UserTest screen on top
	 */
	private void displayTestScreenOnTop() {
		Log.i(TAG, "inside screenOnTop()");
		testResultHandler.sendEmptyMessage(mTestStatus);
	}
	/**
	 * clears previous emergency timer and starts new timer at delay
	 * @param delay
	 */
	private void startEmergencyTimerAtDelay(long delay) {
		mServiceHandler.removeCallbacks(emergecnyCallTimerRunnable);
		mServiceHandler.postDelayed(emergecnyCallTimerRunnable,delay);
	}

	private void startListeners() {
		Log.i(TAG, "inside startListeners() ");
		Utility.setTimeAutomatic(this);
		registerPhoneStateChangeListener(this);
		registerSpeedListener();
		registerScreenLockStatusChangeReceiver();
        registerAlarmToCheckReport();
		// register alarm to check services running periodically - as android
		// may kill the service
		sendMsgToServiceHandler(MessageType.REGISTER_ALARM_TO_CHECK_SERVICE_RUNNING, null);
	}

	/**
	 * Start listening the device screen states. It will actually register a
	 * BroadcastReceiver to get SCREEN_OFF and ScreenState.ON event.
	 */
	private void registerScreenLockStatusChangeReceiver() {
		Log.i(TAG, "Starting the listener for screen lock and unlock");
		if (mScreenLockChangeReceiver != null) {
			unregisterReceiver(mScreenLockChangeReceiver);
			mScreenLockChangeReceiver = null;
		}
		mScreenLockChangeReceiver = new ScreenLockStatusChangeReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenLockChangeReceiver, filter);
		Log.i(TAG, "Listener added for screen lock and unlock");
	}

	private void startUsertestInitTimeout() {
		Log.d(TAG, "inside startUsertestInitTimeout() ");
		stopTestInitTimeoutThread();
		checkTestStatTimoutThread = new Thread() {
			@Override
			public void run() {
				try {
					long usertestInitTime = profile
							.getInitTimeInCall();
					if (usertestInitTime != Constants.EOF) {
						// Wait for the UserTest pass Time
						Thread.sleep(usertestInitTime * 1000);
						dismissAllScreens();
						stopTestTimeoutThread();
						setScreenStatus(ScreenState.LOCK);
						onUsertestTimout();
					}

				} catch (Exception ex) {
					Log.e(TAG, "Exception :" + ex.getMessage());
				}
			}
		};
		checkTestStatTimoutThread.start();
	}

	private void dismissAllScreens() {
		// finish speed alert Screen
		/*if (SpeedAlertScreen.getInstance() != null) {
			SpeedAlertScreen.getInstance().forceStop();
		}
		// finish UserTest initiator Screen
		if (UserTestInitScreen.getInstance() != null) {
			UserTestInitScreen.getInstance().forceStop();
		}*/
		// finish UserTest screen
		if (UserTestScreen.getInstance() != null) {
			UserTestScreen.getInstance().forceStop();
		}
	}

	private void startUsertestTimeout() {
		stopTestTimeoutThread();
		checkTestTimoutThread = new Thread() {
			@Override
			public void run() {
				try {
					long usertestPassTime = profile
							.gettestPassTimeInCall();
					if (usertestPassTime != Constants.EOF) {
						// Wait for the UserTest pass Time
						Thread.sleep(usertestPassTime * 1000);
						dismissAllScreens();
						setScreenStatus(ScreenState.LOCK);
						onUsertestTimout();
					}

				} catch (Exception ex) {
					Log.e(TAG, "Exception the UserTest Timeout thread :"+ex.getMessage());
				} 
			}
		};
		checkTestTimoutThread.start();
	}

	private void stopTestInitTimeoutThread() {
		if (checkTestStatTimoutThread != null
				&& checkTestStatTimoutThread.isAlive()) {
			checkTestStatTimoutThread.interrupt();
			checkTestStatTimoutThread = null;
		}
	}

	private void stopTestTimeoutThread() {
		if (checkTestTimoutThread != null && checkTestTimoutThread.isAlive()) {
			checkTestTimoutThread.interrupt();
			checkTestTimoutThread = null;
		}
	}

	/**
	 * Verify the speed listener against all the constraints and start/stop
	 * speed listener
	 */
	private void verifySpeedListener() {
		Log.d(TAG, "inside verifySpeedListener() ");
		if (profile != null && isStartSpeedDetect
				&& !isEmergencyCallInitiated
				&& !isEmergencyCallInProgress && !onUsertestPassedTime) {

			if (mLocationManager == null) {
				mLocationManager = CustomLocationManager.getInstance(this);
			}
			// start the speed manager if it is not listening the speed already.
			if (!mLocationManager.isListeningSpeed()) {
				sendMsgToServiceHandler(MessageType.START_SPEED_LISTENING, null);
			}
		} else {
			// Stop the speed listener if it is running
			if (mLocationManager != null && mLocationManager.isListeningSpeed()) {
				sendMsgToServiceHandler(MessageType.STOP_SPEED_LISTENING, null);
			}
		}
	}

	/**
	 * clears notification at an delay
	 */
	private void clearNotification(long delay) {
		Message msg = mServiceHandler.obtainMessage();
		msg.what = MessageType.CANCEL_NOTIFCATION;
		mServiceHandler.sendMessageDelayed(msg, delay);
	}

	public String[] getEmergencyNos() {
		if (profile == null) {
			return null;
		}
		return Utility.getEmergencyNumberArr(profile.getEmergencyNos());
	}

	private void registerPhoneStateChangeListener(Context context) {
		mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		phonStateReceiver = new CustomPhoneStateListener();
		mTelephonyManager.listen(phonStateReceiver,
				PhoneStateListener.LISTEN_CALL_STATE);

	}

	private void unRegisterPhoneStateChangeListener() {
		if ((mTelephonyManager != null) && (phonStateReceiver != null)) {
			mTelephonyManager.listen(phonStateReceiver,
					PhoneStateListener.LISTEN_NONE);
		}
	}

	/**
	 * Register an periodic alarm for checking application running
	 * status (for all listeners).
	 */
	private void registerAlarmToCheckServiceRunning() {
		Log.i(TAG, "Registering Time Listener for service");

		AlarmManager alarmMgr = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);

		Intent alarmIntent = new Intent(getApplicationContext(),
				AlarmReceiver.class);

		alarmIntent.addCategory(Constants.ALARM_ENSURE_SERVICE_RUNNING);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, alarmIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 60000,
				Constants.ALARM_PERIOD * 1000, pendingIntent);

	}

    private void registerAlarmToCheckReport() {
        Log.i(TAG, "Registering Time Listener for service");

        AlarmManager alarmMgr = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getApplicationContext(),
                AlarmReceiver.class);

        alarmIntent.addCategory(Constants.ALARM_DELETE_REPORT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                Constants.ALARM_REPEAT_TIME_FOR_REPORT_DELETION, pendingIntent);

    }

	public static MainService getInstance() {
		return mMainServiceObj;
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			Log.i(TAG, "inside handleMessage() - what : " + msg.what);
			switch (msg.what) {
			case MessageType.CANCEL_NOTIFCATION:
				if (mNotificationManager != null) {
					mNotificationManager
							.cancel(Constants.NOTIFIC_STATUS_ID);
				}
				break;
			case MessageType.NOTIFICATION:
				Toast.makeText(MainService.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;

			case MessageType.SPEED_EXCEED:
				if (msg.obj != null && msg.obj instanceof String) {
					// Show the Speed Alert
					showSpeedAlert((String) msg.obj);
				}
				break;

			case MessageType.GPS_DISABLE:
				showGpsDisabledAlert();
				break;

			case MessageType.RESTART_SPEED:
				if (mLocationManager != null) {
					mLocationManager.startSpeedListening();
				}
				break;

			case MessageType.GPS_ENABLE_TIMEOUT:
				onGpsEnableTimeout();
				break;

			case MessageType.START_SPEED_LISTENING:
				if (mLocationManager != null) {
					mLocationManager.startSpeedListening();
				}
				break;

			case MessageType.STOP_SPEED_LISTENING:
				if (mLocationManager != null) {
					mLocationManager.stopListening();
				}
				break;

			case MessageType.START_USERTEST_IN_CALL:
				Intent testIntent = new Intent(MainService.this,
						UserTestScreen.class);
				testIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(testIntent);
				break;

			case MessageType.LOCK_SCREEN:
				Utility.lockScreen(mMainServiceObj);
				break;

			case MessageType.LOCK_ON_SPEED:
				showSpeedLockAlert();
				break;

			case MessageType.REGISTER_ALARM_TO_CHECK_SERVICE_RUNNING:
				registerAlarmToCheckServiceRunning();
				break;

			case MessageType.START_SPEED:
				showCallEndAlertInAdv();
				break;

			case MessageType.START_SERVICE:
				if (!Utility.isAdministratorActive(getApplicationContext())) {
					Utility.ensurelockPermission(getInstance());
				}
				break;

            case MessageType.UPDATE_PROFILE:
                profile = DBOperation.getProfile(mMainServiceObj);
                break;

			default:
				Log.d(TAG, "un-handled case ");
				break;
			}
		}
	}

	private final Handler testResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Intent alertIntent = new Intent(mMainServiceObj,
					UserTestResultAlert.class);
			alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			switch (msg.what) {

			case TestResultCategory.TIMEOUT:
				alertIntent.putExtra(Constants.USERTEST_STATUS,
						TestResultCategory.TIMEOUT);
				break;

			case TestResultCategory.FAILED:
				alertIntent.putExtra(Constants.USERTEST_STATUS,
						TestResultCategory.FAILED);
				break;

			case TestResultCategory.PASSED:
				alertIntent.putExtra(Constants.USERTEST_STATUS,
						TestResultCategory.PASSED);
				break;

			case TestResultCategory.DISABLED:
                alertIntent.putExtra(Constants.USERTEST_STATUS,
                        TestResultCategory.DISABLED);
				break;

			case TestResultCategory.SCREEN:
				processTestScreenMsg();
				break;

			default:
				alertIntent = null;
				break;
			}

			if (alertIntent != null) {
				startActivity(alertIntent);
			}
		}
	};

	Runnable emergecnyCallTimerRunnable = new Runnable()
	{
		public void run()
		{
			onEmergencyTimerOver();
		}
	};

}

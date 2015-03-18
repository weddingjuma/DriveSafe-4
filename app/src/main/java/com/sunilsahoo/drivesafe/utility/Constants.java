package com.sunilsahoo.drivesafe.utility;

public class Constants {

	public interface TestInputTypeStatus {
		byte CORRECT = 1;
		byte INCORRECT = 2;
		byte NOT_FILLED = 0;
	}

	public static final int EOF = -1;

	public interface BTHeadSetState {
		int CONNECTED = 2;
		int CONNECTING = 1;
		int DISCONNECTED = 0;
		int ERROR = Constants.EOF;
	}

	public static final String[] WEEKDAYS = { "Sunday", "Monday", "Tuesday",
			"Wednesday", "Thursday", "Friday", "Saturday" };

	public static final String EXTRA_STATE = "android.bluetooth.headset.extra.STATE";
	public static final String ACTION_HEADSET_STATE_CHANGED = "android.bluetooth.headset.action.STATE_CHANGED";
	public static final String ACTION_AUDIO_STATE_CHANGED = "android.bluetooth.headset.action.AUDIO_STATE_CHANGED";
	public static final String EXTRA_AUDIO_STATE = "android.bluetooth.headset.extra.AUDIO_STATE";
	public static final String ACTION_DIALER = "android.intent.action.DIAL";
	
	public static final String USERTEST_STATUS = "USERTEST_STATUS";
	public static final String ALARM_EMERGENCY_ACCESS_TIMER = "EMERGENCY_ACCESS_TIMER";
	public static final String ALARM_ENSURE_SERVICE_RUNNING = "ENSURE_SERVICE";
    public static final String ALARM_DELETE_REPORT = "DELETE_REPORT";
	
	public static final String ALARM_START_SPEED = "START_SPEED_LISTENER";
	public static final String ALARM_STOP_SPEED = "STOP_SPEED_LISTENER";
	public static final String ALARM_USERTEST_PASSED_TIMER = "USERTEST_PASSED_TIMER";
	public static final String EMERGENCY_NO_SEPARATOR = ",";
	public static final String ACTION_CALL_DISCONNECT_NOTIFIC = "CALL_DISCONNECT_NOTIFIC";
	
	/**
	 * Default Post UserTest access time in seconds. After UserTest passed
	 * device will be
	 * free to access for this time.
	 */
	public static final long POST_USERTEST_TIME = 3 * 60;
	/**
	 * Default speed re-check interval in seconds.
	 */
	public static final int SPEED_RECHECK_INTERVAL = 30;

	public static final int EMERGENCY_NO_CALLED = 6;
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	//alarm period in second default 600 second
	public static final int ALARM_PERIOD = 10 * 60; 

//	public static boolean gpsStatus = true;
	public static final String INTENT_LOCK_PERMISSION = "ENSURE_LOCK_PERMISSION";
	public static final int NOTIFIC_STATUS_ID = "com.sunilsahoo.drivesafe.Status_Notification_Alert"
			.hashCode();
	// waiting period before locking in seconds
	public static final int WAITING_PERIOD_BEFORE_LOCKING = 3;
	// timeout period for gps enable in seconds
	public static final int TIMEOUT_PERIOD_FOR_GPS_ENABLE = 10;
	// access time for emergency number is 180 seconds ie 3 minutes
	public static final long KEY_EMERGENCY_ACCESS_TIME = 3 * 60;
	// no of characters for avt
	public static final int USERTEST_NO_CHAR = 3;
	//initial time to wait before checking GPS in seconds ie default = 10 second
	public static final long INIT_GPS_WAIT_PERIOD = 10;
	//alert dialog show time in seconds
	public static final long ALERT_SHOW_TIME = 3;
	//dalay time to clear notification in seconds
	public static final long NOTIFIC_CLEAR_DELAY = 10;

	public static final long NO_OF_DAYS_IN_WEEK = 7;
	
	public static final byte MAX_TEST_ATTEMPT = 3;
	//soft key height in pixel
	public static final int MIN_HEIGHT_OF_SOFT_KEY = 100;
	//delay to clear emergency call in sec
	public static final int WAITING_PERIOD_BEFORE_CLEARING_EMERGENCY_CALL = 2;
	//notification throttle time in sec
	public static final int NOTIFICATION_THROTTLE_TIME = 5;

    public static final int MAX_HOUR = 23;
    public static final int MAX_MIN = 59;
    public static final int MIN_MIN = 0;
    //minimum thrshold time for report deletion in days
    public static final int THRESHOLD_TIME_FOR_REPORT_DELETION = 30;
    //alarm repeat time for report deletion
    public static final int ALARM_REPEAT_TIME_FOR_REPORT_DELETION = 23*60*60*1000;

    public static final int BLUE_TOOTH_CHECK_WAIT_PERIOD = 1;
	
}

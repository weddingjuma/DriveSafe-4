package com.sunilsahoo.drivesafe.database;

import android.net.Uri;

public class DBProviderMetaData {

	public static final String AUTHORITY = "com.sunilsahoo.drivesafe";
	public static final String DATABASE_NAME = "drivesafe";
	public static final int DATABSE_VERSION = 1;

	public static final class DaySettings implements DaySettingsColumns {
		public static final String PATH = "daysettings";
		public static final String TABLE_NAME = "daysettings";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
				+ AUTHORITY + "." + PATH;
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
				+ AUTHORITY + "." + PATH;
		public static final String DEFAULT_SORT_ORDER = DaySettings._ID
				+ " ASC";
	}

	public interface DaySettingsColumns {
		String _ID = "_id";
		String DAY = "day";
		String IS_ENABLE = "isEnable";
		String START_TIME = "start_time";
		String STOP_TIME = "stop_time";
	}
	
	public static final class Profile implements ProfileColumns {
		public static final String _ID = "_id";
		public static final String PATH = "profile";
		public static final String TABLE_NAME = "profile";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
				+ AUTHORITY + "." + PATH;
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
				+ AUTHORITY + "." + PATH;
	}

	public interface ProfileColumns {
		String _ID = "_id";
		String SPEED_RECHECK_INTERVAL = "spped_recheck_interval";
		String MAX_SPEED = "max_speed";
		String EMERGENCY_NOS = "emergency_nos";
        String CAPTCHA_ENABLE = "captcha_enable";
        String ALLOW_HEADSET = "allow_headset";
		/*String POST_USERTEST_TIME = "post_test_time";
		String USERTEST_CHAR_PRESENT_TIME = "char_pres_time";
		String USERTEST_CHAR_RESPONSE_TIME = "char_resp_time";
		String USERTEST_CHAR_INTERVAL_MIN = "char_interval_time";
		String USERTEST_CHAR_INTERVAL_MAX = "char_interval_max";
		String USERTEST_PASS_TIME_IN_CALL = "test_pass_time_call";
		String USERTEST_INIT_TIME_IN_CALL = "test_init_time_call";
		String DISCONNECT_CALL = "disconn_call";

		String WHITE_LIST_APP = "white_list_app";*/
	}
	
	public static final class Report implements ReportColumns {
		public static final String TABLE_NAME = "report";
		public static final String PATH = "report";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
				+ AUTHORITY + "." + PATH;
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
				+ AUTHORITY + "." + PATH;
		public static final String DEFAULT_SORT_ORDER = ReportColumns.TIME
				+ " DESC";
	}

	public interface ReportColumns {
		String _ID = "_id";
		String TYPE = "type";
		String VALUE = "value";
		String TIME = "time";
	}

}
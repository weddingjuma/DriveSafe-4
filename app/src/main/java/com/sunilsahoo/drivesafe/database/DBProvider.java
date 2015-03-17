package com.sunilsahoo.drivesafe.database;

import java.io.File;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.sunilsahoo.drivesafe.database.DBProviderMetaData.DaySettings;
import com.sunilsahoo.drivesafe.database.DBProviderMetaData.Profile;
import com.sunilsahoo.drivesafe.database.DBProviderMetaData.ProfileColumns;
import com.sunilsahoo.drivesafe.database.DBProviderMetaData.Report;
import com.sunilsahoo.drivesafe.utility.Utility;

public class DBProvider extends ContentProvider {
	private static final UriMatcher sUriMatcher;
	private static final int PROFILE_TYPE_LIST = 1;
	private static final int PROFILE_TYPE_ONE = 2;
	private static final int DAY_SETTINGS_TYPE_LIST = 3;
	private static final int DAY_SETTINGS_TYPE_ONE = 4;
	private static final int REPORT_TYPE_LIST = 5;
	private static final int REPORT_TYPE_ONE = 6;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(DBProviderMetaData.AUTHORITY, "profile",
				PROFILE_TYPE_LIST);
		sUriMatcher.addURI(DBProviderMetaData.AUTHORITY, "profile/#",
				PROFILE_TYPE_ONE);
		sUriMatcher.addURI(DBProviderMetaData.AUTHORITY, "daysettings",
				DAY_SETTINGS_TYPE_LIST);
		sUriMatcher.addURI(DBProviderMetaData.AUTHORITY, "daysettings/#",
				DAY_SETTINGS_TYPE_ONE);
		sUriMatcher.addURI(DBProviderMetaData.AUTHORITY, "report",
				REPORT_TYPE_LIST);
		sUriMatcher.addURI(DBProviderMetaData.AUTHORITY, "report/#",
				REPORT_TYPE_ONE);
	}

	private static final HashMap<String, String> sDaySettingsProjectionMap;
	static {

		sDaySettingsProjectionMap = new HashMap<String, String>();
		sDaySettingsProjectionMap.put(DaySettings._ID, DaySettings._ID);
		sDaySettingsProjectionMap.put(DaySettings.DAY,
				DaySettings.DAY);
		sDaySettingsProjectionMap.put(DaySettings.IS_ENABLE,
				DaySettings.IS_ENABLE);
		sDaySettingsProjectionMap.put(DaySettings.START_TIME,
				DaySettings.START_TIME);
		sDaySettingsProjectionMap.put(DaySettings.STOP_TIME,
				DaySettings.STOP_TIME);
	}

	private static final HashMap<String, String> sReportProjectionMap;
	static {

		sReportProjectionMap = new HashMap<String, String>();
		sReportProjectionMap.put(Report.TABLE_NAME + "." + Report._ID,
				Report.TABLE_NAME + "." + Report._ID);
		sReportProjectionMap.put(Report.TYPE,
				Report.TYPE);
		sReportProjectionMap.put(Report.VALUE,
				Report.VALUE);
		sReportProjectionMap.put(Report.TIME,
				Report.TIME);
	}

	private static final HashMap<String, String> sItemsProjectionMap;
	static {
		sItemsProjectionMap = new HashMap<String, String>();
		sItemsProjectionMap.put(Profile.TABLE_NAME + "." + ProfileColumns._ID,
				Profile.TABLE_NAME + "." + ProfileColumns._ID);
		sItemsProjectionMap.put(ProfileColumns.SPEED_RECHECK_INTERVAL,
				ProfileColumns.SPEED_RECHECK_INTERVAL);
		sItemsProjectionMap.put(ProfileColumns.MAX_SPEED,
				ProfileColumns.MAX_SPEED);
		sItemsProjectionMap.put(ProfileColumns.EMERGENCY_NOS,
				ProfileColumns.EMERGENCY_NOS);
		/*sItemsProjectionMap.put(ProfileColumns.POST_USERTEST_TIME,
				ProfileColumns.POST_USERTEST_TIME);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_CHAR_PRESENT_TIME,
				ProfileColumns.USERTEST_CHAR_PRESENT_TIME);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_CHAR_RESPONSE_TIME,
				ProfileColumns.USERTEST_CHAR_RESPONSE_TIME);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_CHAR_INTERVAL_MIN,
				ProfileColumns.USERTEST_CHAR_INTERVAL_MIN);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_CHAR_INTERVAL_MAX,
				ProfileColumns.USERTEST_CHAR_INTERVAL_MAX);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_PASS_TIME_IN_CALL,
				ProfileColumns.USERTEST_PASS_TIME_IN_CALL);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_INIT_TIME_IN_CALL,
				ProfileColumns.USERTEST_INIT_TIME_IN_CALL);
		sItemsProjectionMap.put(ProfileColumns.DISCONNECT_CALL,
				ProfileColumns.DISCONNECT_CALL);
		sItemsProjectionMap.put(ProfileColumns.USERTEST_ENABLE,
				ProfileColumns.USERTEST_ENABLE);
		sItemsProjectionMap.put(ProfileColumns.WHITE_LIST_APP,
				ProfileColumns.WHITE_LIST_APP);*/

	}

	private static class DBHelper extends SQLiteOpenHelper {

        private static final String TAG = DBHelper.class.getName();

        public DBHelper(Context c) {
			super(c, DBProviderMetaData.DATABASE_NAME, null,
					DBProviderMetaData.DATABSE_VERSION);
		}

		private static final String SQL_QUERY_CREATE_ITEMS = "CREATE TABLE " +

				Profile.TABLE_NAME + " ( " + Profile._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Profile.SPEED_RECHECK_INTERVAL
				+ " TEXT, "
				+ Profile.MAX_SPEED
				+ " TEXT, "
				+ Profile.EMERGENCY_NOS
				/*+ " TEXT, "
				+ Profile.POST_USERTEST_TIME
				+ " TEXT, "
				+ Profile.USERTEST_CHAR_PRESENT_TIME
				+ " TEXT, "
				+ Profile.USERTEST_CHAR_RESPONSE_TIME
				+ " TEXT, "
				+ Profile.USERTEST_CHAR_INTERVAL_MIN
				+ " INTEGER ,"
				+ Profile.USERTEST_CHAR_INTERVAL_MAX
				+ " INTEGER ,"
				+ Profile.USERTEST_PASS_TIME_IN_CALL
				+ " INTEGER ,"
				+ Profile.USERTEST_INIT_TIME_IN_CALL
				+ " INTEGER ,"
				+ Profile.DISCONNECT_CALL
				+ " INTEGER ,"
				+ Profile.USERTEST_ENABLE
				+ " INTEGER ,"
				+ Profile.WHITE_LIST_APP*/
				+ " TEXT );";

		private static final String SQL_QUERY_CREATE_DAYSETTINGS = "CREATE TABLE "
				+ DaySettings.TABLE_NAME
				+ " ( "
				+ DaySettings._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DaySettings.DAY
				+ " TEXT NOT NULL ,"
				+ DaySettings.IS_ENABLE
				+ " INTEGER ,"
				+ DaySettings.START_TIME
				+ " TEXT ,"
				+ DaySettings.STOP_TIME
				+ " TEXT ) ";

		private static final String SQL_QUERY_CREATE_CATEGORY = "CREATE TABLE "
				+ Report.TABLE_NAME
				+ " ( "
				+ Report._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Report.TYPE
				+ " TEXT NOT NULL ,"
				+ Report.VALUE
				+ " TEXT NOT NULL ,"
				+ Report.TIME
				+ " INTEGER )";



        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_0 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "SUN"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                + Utility.timeInMilliSecond(17,0,0) + ","
                + 0 + ")";

        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_1 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "MON"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                 + Utility.timeInMilliSecond(17,0,0) + ","
                + 1 + ")";

        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_2 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "TUE"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                + Utility.timeInMilliSecond(17,0,0) + ","

                        + 1 + ")";

        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_3 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "WED"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                + Utility.timeInMilliSecond(17,0,0) + ","

                + 1 + ")";

        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_4 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "THUR"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                + Utility.timeInMilliSecond(17,0,0) + ","

                + 1 + ")";

        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_5 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "FRI"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                + Utility.timeInMilliSecond(17,0,0) + ","

                + 1 + ")";

        private static final String SQL_QUERY_INSERT_DAY_SETTINGS_6 =
                "INSERT INTO " + DBProviderMetaData.DaySettings.TABLE_NAME + " ( "
                        + DBProviderMetaData.DaySettings.DAY + ","+
        DBProviderMetaData.DaySettings.START_TIME + ","
                + DBProviderMetaData.DaySettings.STOP_TIME + ","
                + DBProviderMetaData.DaySettings.IS_ENABLE + ") VALUES ("
                + "'" + "SAT"   + "'" + ","
                + Utility.timeInMilliSecond(9,0,0) + ","
                + Utility.timeInMilliSecond(17,0,0) + ","

                + 0 + ")";



		@Override
		public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "inside oncreate() of db");
			db.execSQL(SQL_QUERY_CREATE_DAYSETTINGS);
			db.execSQL(SQL_QUERY_CREATE_ITEMS);
			db.execSQL(SQL_QUERY_CREATE_CATEGORY);
            //insert default values of day settings in db
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_0);
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_1);
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_2);
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_3);
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_4);
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_5);
            db.execSQL(SQL_QUERY_INSERT_DAY_SETTINGS_6);

        }

		private static final String SQL_QUERY_DROP_ITEMS = "DROP TABLE IF EXISTS "
				+ Profile.TABLE_NAME + ";";
		private static final String SQL_QUERY_DROP_CATEGORY = "DROP TABLE IF EXISTS "
				+ Report.TABLE_NAME + ";";
		private static final String SQL_QUERY_DROP_DISCOUNTS = "DROP TABLE IF EXISTS "
				+ DaySettings.TABLE_NAME + ";";

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
			db.execSQL(SQL_QUERY_DROP_ITEMS);
			db.execSQL(SQL_QUERY_DROP_CATEGORY);
			db.execSQL(SQL_QUERY_DROP_DISCOUNTS);
			onCreate(db);
		}

	}

	private DBHelper mDbHelper;

	@Override
	public boolean onCreate() {
		mDbHelper = new DBHelper(getContext());
		return false;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = getWritableDatabase();
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case PROFILE_TYPE_LIST:
			count = db.delete(Profile.TABLE_NAME, where, whereArgs);
			break;

		case PROFILE_TYPE_ONE:
			String rowId = uri.getPathSegments().get(1);
			count = db.delete(
					Profile.TABLE_NAME,
					ProfileColumns._ID
							+ " = "
							+ rowId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ")" : ""), whereArgs);
			break;

		case DAY_SETTINGS_TYPE_LIST:
			count = db.delete(DaySettings.TABLE_NAME, where, whereArgs);
			break;

		case DAY_SETTINGS_TYPE_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.delete(
					DaySettings.TABLE_NAME,
					DaySettings._ID
							+ " = "
							+ rowId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ")" : ""), whereArgs);
			break;

		case REPORT_TYPE_LIST:
			count = db.delete(Report.TABLE_NAME, where, whereArgs);
			break;

		case REPORT_TYPE_ONE:
			rowId = uri.getPathSegments().get(1);
			count = db.delete(
					Report.TABLE_NAME,
					Report._ID
							+ " = "
							+ rowId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ")" : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {

		switch (sUriMatcher.match(uri)) {
		case PROFILE_TYPE_LIST:
			return Profile.CONTENT_TYPE;
		case PROFILE_TYPE_ONE:
			return Profile.CONTENT_ITEM_TYPE;
		case DAY_SETTINGS_TYPE_LIST:
			return DaySettings.CONTENT_TYPE;
		case DAY_SETTINGS_TYPE_ONE:
			return DaySettings.CONTENT_ITEM_TYPE;
		case REPORT_TYPE_LIST:
			return Report.CONTENT_ITEM_TYPE;
		case REPORT_TYPE_ONE:
			return Report.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = getWritableDatabase();
		// Log.i(TAG, "MAtch URI ::::" +
		// sUriMatcher.match(uri)+"~~~path~~~"+db.getPath());
		switch (sUriMatcher.match(uri)) {
		case DAY_SETTINGS_TYPE_LIST:
			long rowIdtask = db.insert(DaySettings.TABLE_NAME, null, values);
			if (rowIdtask > 0) {
				Uri articleUri = ContentUris.withAppendedId(
						DaySettings.CONTENT_URI, rowIdtask);
				getContext().getContentResolver()
						.notifyChange(articleUri, null);
				return articleUri;
			}
			break;
		case PROFILE_TYPE_LIST:

			long rowId = db.insert(Profile.TABLE_NAME, null, values);
			if (rowId > 0) {
				Uri articleUri = ContentUris.withAppendedId(
						Profile.CONTENT_URI,
						rowId);

				getContext().getContentResolver()
						.notifyChange(articleUri, null);
				return articleUri;
			}
			break;
		case REPORT_TYPE_LIST:

			long rowCategoryId = db.insert(Report.TABLE_NAME, null, values);
			if (rowCategoryId > 0) {
				Uri articleUri = ContentUris.withAppendedId(Report.CONTENT_URI,
						rowCategoryId);

				getContext().getContentResolver()
						.notifyChange(articleUri, null);
				return articleUri;
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);

		}
		// close(db);
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case DAY_SETTINGS_TYPE_LIST:
			builder.setTables(DaySettings.TABLE_NAME);
			builder.setProjectionMap(sDaySettingsProjectionMap);
			break;

		case DAY_SETTINGS_TYPE_ONE:
			builder.setTables(DaySettings.TABLE_NAME);
			builder.setProjectionMap(sDaySettingsProjectionMap);
			builder.appendWhere(selection);
			break;

		case PROFILE_TYPE_LIST:
			builder.setProjectionMap(sItemsProjectionMap);
			builder.setTables(Profile.TABLE_NAME);
			break;

		case PROFILE_TYPE_ONE:
			builder.setProjectionMap(sItemsProjectionMap);
			builder.setTables(Profile.TABLE_NAME);
			builder.appendWhere(selection);
			break;

		case REPORT_TYPE_LIST:
			builder.setTables(Report.TABLE_NAME);
			builder.setProjectionMap(sReportProjectionMap);
			break;

		case REPORT_TYPE_ONE:
			builder.setTables(Report.TABLE_NAME);
			builder.setProjectionMap(sReportProjectionMap);
			builder.appendWhere(selection);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = getReadableDatabase();
		Cursor queryCursor = builder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
		// close(db);
		return queryCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count = 0;

		switch (sUriMatcher.match(uri)) {
		case DAY_SETTINGS_TYPE_LIST:
			count = db.update(DaySettings.TABLE_NAME, values, where, whereArgs);
			break;

		case DAY_SETTINGS_TYPE_ONE:
			String rowId = uri.getPathSegments().get(1);
			count = db
					.update(DaySettings.TABLE_NAME, values,
							"_id"
									+ " = "
									+ rowId
									+ (!TextUtils.isEmpty(where) ? " AND ("
											+ ")" : ""), whereArgs);
			break;

		case PROFILE_TYPE_LIST:
			count = db.update(Profile.TABLE_NAME, values, where, whereArgs);
			break;

		case PROFILE_TYPE_ONE:
			String rowIdtask = uri.getPathSegments().get(1);
			count = db.update(Profile.TABLE_NAME, values, "_id" + " = "
					+ rowIdtask
					+ (!TextUtils.isEmpty(where) ? " AND (" + ")" : ""),
					whereArgs);
			break;
		case REPORT_TYPE_LIST:
			count = db.update(Report.TABLE_NAME, values, where, whereArgs);
			break;

		case REPORT_TYPE_ONE:
			String rowIdCategory = uri.getPathSegments().get(1);
			count = db.update(Report.TABLE_NAME, values, "_id" + " = "
					+ rowIdCategory
					+ (!TextUtils.isEmpty(where) ? " AND (" + ")" : ""),
					whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		// close(db);
		return count;
	}

	private SQLiteDatabase getWritableDatabase() {
		File sqliteDBFile = null;
		SQLiteDatabase sqliteDB = null;
		try {
			sqliteDB = mDbHelper.getWritableDatabase();
			sqliteDBFile = new File(sqliteDB.getPath());
			if (!sqliteDBFile.exists()) {
				onCreate();
				sqliteDB = mDbHelper.getWritableDatabase();
			}
		} finally {
			sqliteDBFile = null;
		}
		return sqliteDB;
	}

	private SQLiteDatabase getReadableDatabase() {
		File sqliteDBFile = null;
		SQLiteDatabase sqliteDB = null;
		try {
			sqliteDB = mDbHelper.getReadableDatabase();
			sqliteDBFile = new File(sqliteDB.getPath());
			if (!sqliteDBFile.exists()) {
				onCreate();
				sqliteDB = mDbHelper.getWritableDatabase();
			}
		} finally {
			sqliteDBFile = null;
		}
		return sqliteDB;
	}

}

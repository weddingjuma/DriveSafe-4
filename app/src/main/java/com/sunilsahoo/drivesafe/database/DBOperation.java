package com.sunilsahoo.drivesafe.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.model.DaySettings;
import com.sunilsahoo.drivesafe.model.Profile;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Constants;
import com.sunilsahoo.drivesafe.utility.Utility;

import java.util.ArrayList;

public class DBOperation {

    private static final String TAG = DBOperation.class.getName();

    public static void insertProfile(com.sunilsahoo.drivesafe.model.Profile profile, Context context) {
		context = context == null ? MainService.getInstance() : context;
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBProviderMetaData.Profile.SPEED_RECHECK_INTERVAL, profile.getSpeedRechckInterval());
		initialValues.put(DBProviderMetaData.Profile.MAX_SPEED, profile.getThresholdSpeed());
		initialValues.put(DBProviderMetaData.Profile.EMERGENCY_NOS, profile.getEmergencyNos());
		initialValues.put(DBProviderMetaData.Profile.POST_USERTEST_TIME, profile.getPostUsertestAccessTime());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_PRESENT_TIME, profile.getCharPresentTime());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_RESPONSE_TIME, profile.getCharResponseTime());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_INTERVAL_MIN, profile.getCharIntervalMin());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_INTERVAL_MAX, profile.getCharIntervalMax());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_ENABLE, profile.isTestEnable());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_PASS_TIME_IN_CALL, profile.gettestPassTimeInCall());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_INIT_TIME_IN_CALL, profile.getInitTimeInCall());
		initialValues.put(DBProviderMetaData.Profile.DISCONNECT_CALL, profile.isDisconnectCall());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_ENABLE, profile.isTestEnable());
		initialValues.put(DBProviderMetaData.Profile.WHITE_LIST_APP, Utility.convertToCommaSeparatedString(profile.getNavigationAppList()));

		context.getContentResolver().insert(
				DBProviderMetaData.Profile.CONTENT_URI, initialValues);
	}

	public static void updateProfile(com.sunilsahoo.drivesafe.model.Profile profile, Context context) throws Exception{
		context = context == null ? MainService.getInstance() : context;
        for(DaySettings daySettings : profile.getDaySettings()){
            DBOperation.updateDaysettings(daySettings, context);
        }


		ContentValues initialValues = new ContentValues();
		initialValues.put(DBProviderMetaData.Profile.SPEED_RECHECK_INTERVAL, profile.getSpeedRechckInterval());
		initialValues.put(DBProviderMetaData.Profile.MAX_SPEED, profile.getThresholdSpeed());
		initialValues.put(DBProviderMetaData.Profile.EMERGENCY_NOS, profile.getEmergencyNos());
		initialValues.put(DBProviderMetaData.Profile.POST_USERTEST_TIME, profile.getPostUsertestAccessTime());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_PRESENT_TIME, profile.getCharPresentTime());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_RESPONSE_TIME, profile.getCharResponseTime());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_INTERVAL_MIN, profile.getCharIntervalMin());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_CHAR_INTERVAL_MAX, profile.getCharIntervalMax());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_ENABLE, profile.isTestEnable());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_PASS_TIME_IN_CALL, profile.gettestPassTimeInCall());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_INIT_TIME_IN_CALL, profile.getInitTimeInCall());
		initialValues.put(DBProviderMetaData.Profile.DISCONNECT_CALL, profile.isDisconnectCall());
		initialValues.put(DBProviderMetaData.Profile.USERTEST_ENABLE, profile.isTestEnable());
		initialValues.put(DBProviderMetaData.Profile.WHITE_LIST_APP, Utility.convertToCommaSeparatedString(profile.getNavigationAppList()));
		
		Uri uri = ContentUris.withAppendedId(
					DBProviderMetaData.Profile.CONTENT_URI, profile.getId());
			context.getContentResolver().update(uri, initialValues, null, null);
		
	}

	public static int  deleteItem(com.sunilsahoo.drivesafe.model.Profile profile, Context context) {
		context = context == null ? MainService.getInstance() : context;
		if (profile.getId() != Constants.EOF) {
			Uri uri = ContentUris.withAppendedId(
					DBProviderMetaData.Profile.CONTENT_URI, profile.getId());
			return context.getContentResolver().delete(uri, null, null);
		} else{
			return 0;
		}

	}
	
	
	
	public static com.sunilsahoo.drivesafe.model.Profile getProfile(Context context) {
        Profile profile = new Profile();
        profile.setDaySettings(getDaySettings(context));

        Cursor profileDetailCursor = null;

        profileDetailCursor = context.getContentResolver()
                .query(DBProviderMetaData.Profile.CONTENT_URI, null, null, null, null);
        if (profileDetailCursor == null) {
            return null;
        }

        if (profileDetailCursor.moveToNext()) {
            profile.setEmergencyNos(profileDetailCursor.getString(profileDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.Profile.EMERGENCY_NOS)));
            profile.setThresholdSpeed(profileDetailCursor.getFloat(profileDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.Profile.MAX_SPEED)));
            profile.setSpeedRechckInterval(profileDetailCursor.getInt(profileDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.Profile.SPEED_RECHECK_INTERVAL)));
            profile.setPostUsertestAccessTime(profileDetailCursor.getInt(profileDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.Profile.POST_USERTEST_TIME)));
            profile.setId(profileDetailCursor.getInt(profileDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.Profile._ID)));
        }

		return profile;
	}


	private static void insertDaySettings(com.sunilsahoo.drivesafe.model.DaySettings daySettings, Context context) throws Exception{
		context = context == null ? MainService.getInstance() : context;
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBProviderMetaData.DaySettings.DAY,
				daySettings.getDay());
		initialValues.put(DBProviderMetaData.DaySettings.START_TIME,
				daySettings.getStartTime());
		initialValues.put(DBProviderMetaData.DaySettings.STOP_TIME,
				daySettings.getStopTime());
		initialValues.put(DBProviderMetaData.DaySettings.IS_ENABLE,
				daySettings.isEnabled());

		context.getContentResolver().insert(
				DBProviderMetaData.DaySettings.CONTENT_URI, initialValues);
	}

	private static void updateDaysettings(com.sunilsahoo.drivesafe.model.DaySettings daySettings, Context context) throws Exception{
		context = context == null ? MainService.getInstance() : context;
		ContentValues initialValues = new ContentValues();
		/*initialValues.put(DBProviderMetaData.DaySettings.DAY,
				daySettings.getDay());*/
		initialValues.put(DBProviderMetaData.DaySettings.START_TIME,
				daySettings.getStartTime());
		initialValues.put(DBProviderMetaData.DaySettings.STOP_TIME,
				daySettings.getStopTime());
		initialValues.put(DBProviderMetaData.DaySettings.IS_ENABLE,
				daySettings.isEnabled());
		if (daySettings.getId() != Constants.EOF) {
			Uri uri = ContentUris.withAppendedId(
					DBProviderMetaData.DaySettings.CONTENT_URI,
					daySettings.getId());
			context.getContentResolver().update(uri, initialValues, null, null);
		} 
	}
	

	public static void deleteDaySettings(com.sunilsahoo.drivesafe.model.DaySettings daySettings, Context context, boolean deleteFromMappingTable) throws Exception{
		if (daySettings.getId() != Constants.EOF) {
			Uri uri = ContentUris.withAppendedId(
					DBProviderMetaData.DaySettings.CONTENT_URI,
					daySettings.getId());
			context.getContentResolver().delete(uri, null, null);
		} 
	}
	
	public static void insertReport(com.sunilsahoo.drivesafe.model.Report report, Context context) {
		context = context == null ? MainService.getInstance() : context;
		if (report != null) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(DBProviderMetaData.Report.TYPE,
					report.getReportType());
			initialValues.put(DBProviderMetaData.Report.VALUE,
					report.getReportValue());
			initialValues.put(DBProviderMetaData.Report.TIME,
					report.getTime());

			context.getContentResolver().insert(
					DBProviderMetaData.Report.CONTENT_URI, initialValues);
		}
	}


    public static ArrayList<DaySettings> getDaySettings(Context context) {
        Log.d(TAG, "inside getDaySettings()");
        Cursor daySettingsDetailCursor = null;

        daySettingsDetailCursor = context.getContentResolver()
                .query(DBProviderMetaData.DaySettings.CONTENT_URI, null, null, null, null);
        if (daySettingsDetailCursor == null) {
            return null;
        }
        ArrayList<DaySettings> daySettingList = new ArrayList<DaySettings>();
        DaySettings daySettings = null;
        while (daySettingsDetailCursor.moveToNext()) {
            daySettings = new DaySettings();
            daySettings.setDay(daySettingsDetailCursor.getString(daySettingsDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.DaySettings.DAY)));
            daySettings.setId(daySettingsDetailCursor.getLong(daySettingsDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.DaySettings._ID)));
            daySettings.setStartTime(daySettingsDetailCursor.getLong(daySettingsDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.DaySettings.START_TIME)));
            daySettings.setStopTime(daySettingsDetailCursor.getLong(daySettingsDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.DaySettings.STOP_TIME)));
            daySettings.setEnabled(daySettingsDetailCursor.getInt(daySettingsDetailCursor
                    .getColumnIndexOrThrow(DBProviderMetaData.DaySettings.IS_ENABLE)) == Constants.TRUE);
            daySettingList.add(daySettings);
        }

        if (daySettingsDetailCursor != null) {
            daySettingsDetailCursor.close();
            daySettingsDetailCursor = null;
        }
        return daySettingList;
    }

	public static void updateCategory(com.sunilsahoo.drivesafe.model.Report report, Context context) {
		context = context == null ? MainService.getInstance() : context;
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBProviderMetaData.Report.TYPE,
				report.getReportType());
		initialValues.put(DBProviderMetaData.Report.VALUE,
				report.getReportValue());
		initialValues.put(DBProviderMetaData.Report.VALUE,
				report.getTime());
		if (report.getId() != Constants.EOF) {
			Uri uri = ContentUris.withAppendedId(
					DBProviderMetaData.Report.CONTENT_URI,
					report.getId());
			context.getContentResolver().update(uri, initialValues, null, null);
		} 
	}
	
	public static int deleteReport(
			com.sunilsahoo.drivesafe.model.Report report, Context context) {
		context = context == null ? MainService.getInstance() : context;
		if (report.getId() != Constants.EOF) {
			Uri uri = ContentUris.withAppendedId(
					DBProviderMetaData.Report.CONTENT_URI,
					report.getId());
			return context.getContentResolver().delete(uri, null, null);
		}else{
			return 0;
		}

	}


    public static void deleteReports(Context context, long time) {
        context = context == null ? MainService.getInstance() : context;
        String selection = DBProviderMetaData.Report.TIME+" < "+time;
        context.getContentResolver().delete(DBProviderMetaData.Report.CONTENT_URI, selection, null);
    }
	
}

package com.sunilsahoo.drivesafe.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.os.Environment;

public class Log {
	public static final int NO_DEBUG_LOGS = -1;
	public static final String TAG = Log.class.getName();

	public static boolean mIsDebugEnabled = true;
	public static boolean mIsWriteToFileEnabled = true;
	private static String logFileName = null;
	public static int v(String tag, String msg) {
		writeToLogFile(tag, msg);
		return mIsDebugEnabled ? android.util.Log.v(tag, msg) : NO_DEBUG_LOGS;
	}

	public static int d(String tag, String msg) {
		writeToLogFile(tag, msg);
		return mIsDebugEnabled ? android.util.Log.d(tag, msg) : NO_DEBUG_LOGS;
	}

	public static int i(String tag, String msg) {
		writeToLogFile(tag, msg);
		return mIsDebugEnabled ? android.util.Log.i(tag, msg) : NO_DEBUG_LOGS;
	}

	public static int w(String tag, String msg) {
		writeToLogFile(tag, msg);
		return mIsDebugEnabled ? android.util.Log.w(tag, msg) : NO_DEBUG_LOGS;
	}

	public static int e(String tag, String msg) {
		writeToLogFile(tag, msg);
		return mIsDebugEnabled ? android.util.Log.e(tag, msg) : NO_DEBUG_LOGS;
	}

	private static boolean writeToLogFile(String tag, String msg) {
		if (mIsWriteToFileEnabled) {
			initializeLogParams();
			return writeLog(logFileName, tag + " :" + msg);
		} else {
			return false;
		}
	}

	private static boolean writeLog(String fname, String content) {
		try {
			String fpath = Environment.getExternalStorageDirectory()
					+ File.separator + "BorqsgamePadLogs"+File.separator+fname;
			// android.util.Log.d(TAG,"file name :"+fpath);
			File file = new File(fpath);
			// If file does not exists, then create it
			// android.util.Log.d(TAG,"file name :"+fpath+
			// "exists before :"+file.exists());
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("\n" + content);
			bw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			android.util.Log.w(TAG, "Exception :" + e.getMessage());
			return false;
		}
	}
	
	private static void initializeLogParams() {
		if ((logFileName == null)||logFileName.trim().length()<=0) {
			boolean directoryExist = createLogDirectory();
			if(directoryExist){
			logFileName = "Log_" + System.currentTimeMillis()
					+ ".txt";
			}
		}
	}
	
	private static boolean createLogDirectory() {
		String dirpath = null;
		File file = null;
		try {
			dirpath = Environment.getExternalStorageDirectory()
					+ File.separator + "BorqsgamePadLogs";
			file = new File(dirpath);
			if (!file.exists()) {
				return file.mkdir();
			}
			return true;
		} catch (Exception ex) {
			return false;
		} finally {
			file = null;
			dirpath = null;
		}
	}
}

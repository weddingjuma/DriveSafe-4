package com.sunilsahoo.drivesafe.listener;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.model.Report;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Constants;
import com.sunilsahoo.drivesafe.utility.ReportType;

public class CustomLocationManager {
	private static final String TAG = CustomLocationManager.class.getName();

	private CustomLocationManager() {
	}

	private static boolean isProviderEnabled = false;
	private static CustomLocationManager currentSpeedManager;
	private Context appContext = null;
	private MainService dsMainService;
	private int currGPSStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
	private long lastUpdateTime = System.currentTimeMillis();
	private boolean listeningSpeed;

	private ProviderStatusCheckerThread providerStatusListener = null;
	private int updateInterval = Constants.SPEED_RECHECK_INTERVAL;
	private LocationManager locationManager = null;
	private SpeedListener speedListener = null;

	class SpeedListener implements LocationListener {

		@Override
		public void onProviderEnabled(String provider) {
			Log.i(TAG, "onProviderEnabled() :" + provider);
			isProviderEnabled = true;
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.i(TAG, "inside onProviderDisabled() Provider:" + provider);
			if (isProviderEnabled) {
				Report report = new Report();
				report.setReportType(ReportType.GPSDISABLED);
				DBOperation.insertReport(report, null);
				isProviderEnabled = false;
			}
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				// Show alert to user to enable the GPS
				dsMainService.onGPSStatusChange(false);
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.i(TAG, "updateLocation() Latitude:" + " Speed :"
					+ location.getSpeed());
			lastUpdateTime = System.currentTimeMillis();
			currGPSStatus = LocationProvider.AVAILABLE;
			dsMainService.onSpeedUpdate(location.getSpeed());
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, "onStatusChanged() status :" + status);
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				currGPSStatus = status;
			}
		}
	}

	public static synchronized CustomLocationManager getInstance(
			final MainService appController) {
		if ((currentSpeedManager == null) && (appController != null)) {
			currentSpeedManager = new CustomLocationManager(appController);
		}
		return currentSpeedManager;
	}

	private CustomLocationManager(final MainService appController) {
		this.dsMainService = appController;
		this.appContext = appController.getApplicationContext();
	}

	public boolean isListeningSpeed() {
		return listeningSpeed;
	}

	public boolean isGPSProviderDisabled() {
		LocationManager locMgr = locationManager;
		try {
			if (locMgr == null) {
				locMgr = (LocationManager) appContext
						.getSystemService(Context.LOCATION_SERVICE);
			}
			return !locMgr
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

		} catch (Exception e) {
		}
		return false;
	}

	public void setSpeedUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	private void startProviderStatusCheck() {
		stopProviderStatusCheck();
		providerStatusListener = new ProviderStatusCheckerThread();
		providerStatusListener.start();
	}

	public void startSpeedListening() {
		Log.i(TAG, "inside startSpeedListening()");
		stopListening();
		try {
			locationManager = (LocationManager) appContext
					.getSystemService(Context.LOCATION_SERVICE);
			if (!locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				dsMainService.onGPSStatusChange(false);
			} 

			speedListener = new SpeedListener();
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					updateInterval * 1000L, 0, speedListener);
			// Start checking the Location Provider status,
			startProviderStatusCheck();
			listeningSpeed = true;

		} catch (Exception e) {
			Log.e(TAG, "Exception :"+ e.getMessage());
		}

	}

	private void stopProviderStatusCheck() {
		try {
			if (providerStatusListener != null
					&& providerStatusListener.isAlive()) {
				providerStatusListener.stopChecking();
				providerStatusListener.interrupt();
			}
			providerStatusListener = null;
		} catch (Exception e) {

		}
	}

	/**
	 * Stop the GPS Listener to stop listening the speed
	 */
	public void stopListening() {
		Log.i(TAG, "inside stopListening()");
		try {
			if ((locationManager != null) && (speedListener != null)) {
				locationManager.removeUpdates(speedListener);
			}
			locationManager = null;
			speedListener = null;
			stopProviderStatusCheck();
			listeningSpeed = false;
		} catch (Exception ex) {
			Log.i(TAG, "Exception in stopListening() :" + ex.getMessage());
		}
	}

	class ProviderStatusCheckerThread extends Thread {
		boolean running = false;

		@Override
		public void run() {
			running = true;
			lastUpdateTime = System.currentTimeMillis();
			try {
				while (running) {
					if (currGPSStatus == LocationProvider.TEMPORARILY_UNAVAILABLE
							&& !isGPSProviderDisabled()) {
						long curTime = System.currentTimeMillis();
						if ((curTime - lastUpdateTime) > ((updateInterval + 10) * 1000L)) {
							dsMainService.requestRestartSpeedListener();
							break;
						}
					}
					Thread.sleep(Constants.TIMEOUT_PERIOD_FOR_GPS_ENABLE * 1000);
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception " + e.toString());
			}
		}

		public void stopChecking() {
			running = false;
		}
	}
}

package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Constants;

public class GPSEnableAlert extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_status);
        ((TextView)findViewById(R.id.textView1)).setText(getResources().getString(R.string.lbl_gps_disabled_header));
        ((TextView)findViewById(R.id.textView2)).setText(getResources().getString(R.string.lbl_gps_disabled));
		Button btnEnable = (Button) findViewById(R.id.btn1);
        btnEnable.setText(getResources().getString(R.string.lbl_enable_gps));
		btnEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent gpsOptionsIntent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(gpsOptionsIntent);
				finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		startGPSCheckAtDelay();
	}

	@Override
	public void onBackPressed() {
        //TODO don't do anything in order to force user not to use back key
	}

	private void startGPSCheckAtDelay() {
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
                 final Handler handler = new Handler();
                 handler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
   					if (MainService.getInstance() != null) {
   						MainService.getInstance().enableGPS();
   					}
   					finish();
                   }
                 }, Constants.INIT_GPS_WAIT_PERIOD * 1000);

            }
        });
	}

}

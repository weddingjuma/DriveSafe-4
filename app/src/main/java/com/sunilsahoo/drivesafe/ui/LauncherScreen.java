package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Utility;

public class LauncherScreen extends Activity implements OnClickListener {
	private static final String TAG = LauncherScreen.class.getName();
	private Button startServiceBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_screen);
		startServiceBtn = (Button) findViewById(R.id.startServiceBtn);
		startServiceBtn.setOnClickListener(this);
		showOrHideAppStatus();

	}

	@Override
	protected void onResume() {
		super.onResume();
		showOrHideAppStatus();
	}

	private void startApplicationService() {
		Log.i(TAG, "inside startApplicationService()");
		Intent serviceStartIntent = new Intent(getApplicationContext(),
				MainService.class);
		serviceStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(serviceStartIntent);
	}

	@Override
	public void onClick(View v) {
		if (v == startServiceBtn) {
			startApplicationService();
			showOrHideAppStatus();
		}
	}

	private void showOrHideAppStatus() {
		MainService dsMainService = MainService.getInstance();
		if (dsMainService == null) {
			startServiceBtn.setVisibility(View.VISIBLE);
		} else {
			startServiceBtn.setVisibility(View.GONE);
		}
	}

}

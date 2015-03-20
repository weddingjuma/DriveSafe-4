package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.services.MainService;

public class LauncherScreen extends Activity implements OnClickListener {
    private static final String TAG = LauncherScreen.class.getName();
    private Button startServiceBtn;
    private String headerStr = "";
    private String msgStr = "";
    private String btnStr = "";
    private ImageView actionbar_moreIV = null;
    private static LauncherScreen launcherScreen= null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        launcherScreen = this;
		setContentView(R.layout.app_status);
		startServiceBtn = (Button) findViewById(R.id.btn1);
		startServiceBtn.setOnClickListener(this);
        actionbar_moreIV = (ImageView) findViewById(R.id.actionbar_moreBtn);
        actionbar_moreIV.setOnClickListener(this);
        invalidateUI(MainService.getInstance() != null);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
        if(v == startServiceBtn) {
            if (MainService.getInstance() == null) {
                Intent serviceStartIntent = new Intent(this,
                        MainService.class);
                serviceStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(serviceStartIntent);
                Toast.makeText(this, getResources().getString(R.string.toast_service_start), Toast.LENGTH_SHORT).show();
                invalidateUI(true);
            } else {
                MainService.getInstance().stopSelf();
                Toast.makeText(this, getResources().getString(R.string.toast_service_stop), Toast.LENGTH_SHORT).show();
                invalidateUI(false);
            }
        }else if(v == actionbar_moreIV){
            showMenuDrawer(v);
        }
	}

    private void showMenuDrawer(View v){
        int topMargin = ((((View) v.getParent()).getTop() + ((View) v
                .getParent()).getHeight())) - (v.getTop() + v.getHeight());
        CustomMenuDrawer.showMenuDrawer(this, v, 0, topMargin);
    }

    public void invalidateUI(boolean isServiceRunning){
        if(!isServiceRunning) {
            headerStr = getResources().getString(R.string.lbl_serivce_stop_header);
            msgStr = getResources().getString(R.string.lbl_serivce_stop_msg);
            btnStr = getResources().getString(R.string.lbl_service_start);
        }else{
            headerStr = getResources().getString(R.string.lbl_serivce_start_header);
            msgStr = getResources().getString(R.string.lbl_serivce_start_msg);
            btnStr = getResources().getString(R.string.lbl_service_stop);
        }
        ((TextView)findViewById(R.id.textView1)).setText(headerStr);
        ((TextView)findViewById(R.id.textView2)).setText(msgStr);
        startServiceBtn.setText(btnStr);
    }

    public static LauncherScreen getInstance(){
        return launcherScreen;
    }
    public void updateSpeed(String speed){
        try {
            String title = String.format(getString(R.string.last_detected_speed), speed);
            ((TextView) findViewById(R.id.textView3)).setText(title);
        }catch(Exception ex){
            Log.w(TAG, "Exception :"+ex);
        }
    }


}

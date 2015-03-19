package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.listener.TimeFilterWatcher;
import com.sunilsahoo.drivesafe.model.Profile;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.MessageType;
import com.sunilsahoo.drivesafe.utility.Utility;


/**
 * Created by sunil on 18/2/15.
 */
public class DaySettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener{
    private static final String TAG = DaySettingsActivity.class.getName();
    private ListView daySettingsLV = null;
    private EditText startTime0 = null;
    private EditText startTime1 = null;
    private EditText startTime2 = null;
    private EditText startTime3 = null;
    private EditText startTime4 = null;
    private EditText startTime5 = null;
    private EditText startTime6 = null;

    private EditText stopTime0 = null;
    private EditText stopTime1 = null;
    private EditText stopTime2 = null;
    private EditText stopTime3 = null;
    private EditText stopTime4 = null;
    private EditText stopTime5 = null;
    private EditText stopTime6 = null;
    private EditText emergencyNumberET = null;
    private EditText speedET = null;

    private TextView dayTV0 = null;
    private TextView dayTV1 = null;
    private TextView dayTV2 = null;
    private TextView dayTV3 = null;
    private TextView dayTV4 = null;
    private TextView dayTV5 = null;
    private TextView dayTV6 = null;

    private CheckBox enableCB0 = null;
    private CheckBox enableCB1 = null;
    private CheckBox enableCB2 = null;
    private CheckBox enableCB3 = null;
    private CheckBox enableCB4 = null;
    private CheckBox enableCB5 = null;
    private CheckBox enableCB6 = null;
    private CheckBox captchaEnableCB = null;
    private CheckBox headsetModeEnableCB = null;

    private ImageView saveBtn = null;
    private ImageView cancelBtn = null;
    private Profile profile = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daysettings);
        boolean isEnabled = false;
        profile = DBOperation.getProfile(this);
        startTime0 = (EditText) findViewById(R.id.startTimeET0);
        startTime0.addTextChangedListener(new TimeFilterWatcher(startTime0));
        startTime0.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(0).getStartTime())));
        startTime1 = (EditText) findViewById(R.id.startTimeET1);
        startTime1.addTextChangedListener(new TimeFilterWatcher(startTime1));
        startTime1.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(1).getStartTime())));
        startTime2 = (EditText) findViewById(R.id.startTimeET2);
        startTime2.addTextChangedListener(new TimeFilterWatcher(startTime2));
        startTime2.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(2).getStartTime())));
        startTime3 = (EditText) findViewById(R.id.startTimeET3);
        startTime3.addTextChangedListener(new TimeFilterWatcher(startTime3));
        startTime3.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(3).getStartTime())));
        startTime4 = (EditText) findViewById(R.id.startTimeET4);
        startTime4.addTextChangedListener(new TimeFilterWatcher(startTime4));
        startTime4.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(4).getStartTime())));
        startTime5 = (EditText) findViewById(R.id.startTimeET5);
        startTime5.addTextChangedListener(new TimeFilterWatcher(startTime5));
        startTime5.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(5).getStartTime())));
        startTime6 = (EditText) findViewById(R.id.startTimeET6);
        startTime6.addTextChangedListener(new TimeFilterWatcher(startTime6));
        startTime6.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(6).getStartTime())));

        stopTime0 = (EditText) findViewById(R.id.endTimeET0);
        stopTime0.addTextChangedListener(new TimeFilterWatcher(stopTime0));
        stopTime0.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(0).getStopTime())));
        stopTime1 = (EditText) findViewById(R.id.endTimeET1);
        stopTime1.addTextChangedListener(new TimeFilterWatcher(stopTime1));
        stopTime1.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(1).getStopTime())));
        stopTime2 = (EditText) findViewById(R.id.endTimeET2);
        stopTime2.addTextChangedListener(new TimeFilterWatcher(stopTime2));
        stopTime2.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(2).getStopTime())));
        stopTime3 = (EditText) findViewById(R.id.endTimeET3);
        stopTime3.addTextChangedListener(new TimeFilterWatcher(stopTime3));
        stopTime3.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(3).getStopTime())));
        stopTime4 = (EditText) findViewById(R.id.endTimeET4);
        stopTime4.addTextChangedListener(new TimeFilterWatcher(stopTime4));
        stopTime4.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(4).getStopTime())));
        stopTime5 = (EditText) findViewById(R.id.endTimeET5);
        stopTime5.addTextChangedListener(new TimeFilterWatcher(stopTime5));
        stopTime5.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(5).getStopTime())));
        stopTime6 = (EditText) findViewById(R.id.endTimeET6);
        stopTime6.addTextChangedListener(new TimeFilterWatcher(stopTime6));
        stopTime6.setText(String.valueOf(Utility.formatTime(profile.getDaySettings().get(6).getStopTime())));

        emergencyNumberET = (EditText) findViewById(R.id.emergencyNumberET);
        emergencyNumberET.setText(profile.getEmergencyNos());
        speedET = (EditText) findViewById(R.id.speedET);
        speedET.setText(String.valueOf(profile.getThresholdSpeed()));


        enableCB0 = (CheckBox) findViewById(R.id.enableCB0);
        enableCB0.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(0).isEnabled();
        enableCB0.setChecked(isEnabled);
        startTime0.setEnabled(isEnabled);
        stopTime0.setEnabled(isEnabled);

        captchaEnableCB = (CheckBox) findViewById(R.id.captchaEnable);
        captchaEnableCB.setOnCheckedChangeListener(this);
        captchaEnableCB.setChecked(profile.isTestEnable());

        headsetModeEnableCB = (CheckBox) findViewById(R.id.headsetModeEnable);
        headsetModeEnableCB.setOnCheckedChangeListener(this);
        headsetModeEnableCB.setChecked(profile.isHeadsetConnectionAllowed());

        enableCB1 = (CheckBox) findViewById(R.id.enableCB1);
        enableCB1.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(1).isEnabled();
        enableCB1.setChecked(isEnabled);
        startTime1.setEnabled(isEnabled);
        stopTime1.setEnabled(isEnabled);
        enableCB2 = (CheckBox) findViewById(R.id.enableCB2);
        enableCB2.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(2).isEnabled();
        enableCB2.setChecked(isEnabled);
        startTime2.setEnabled(isEnabled);
        stopTime2.setEnabled(isEnabled);
        enableCB3 = (CheckBox) findViewById(R.id.enableCB3);
        enableCB3.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(3).isEnabled();
        enableCB3.setChecked(isEnabled);
        startTime3.setEnabled(isEnabled);
        stopTime3.setEnabled(isEnabled);
        enableCB4 = (CheckBox) findViewById(R.id.enableCB4);
        enableCB4.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(4).isEnabled();
        enableCB4.setChecked(isEnabled);
        startTime4.setEnabled(isEnabled);
        stopTime4.setEnabled(isEnabled);
        enableCB5 = (CheckBox) findViewById(R.id.enableCB5);
        enableCB5.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(5).isEnabled();
        enableCB5.setChecked(isEnabled);
        startTime5.setEnabled(isEnabled);
        stopTime5.setEnabled(isEnabled);
        enableCB6 = (CheckBox) findViewById(R.id.enableCB6);
        enableCB6.setOnCheckedChangeListener(this);
        isEnabled = profile.getDaySettings().get(6).isEnabled();
        enableCB6.setChecked(isEnabled);
        startTime6.setEnabled(isEnabled);
        stopTime6.setEnabled(isEnabled);

        dayTV0 = (TextView) findViewById(R.id.dayTV0);
        dayTV0.setText(profile.getDaySettings().get(0).getDay());
        dayTV1 = (TextView) findViewById(R.id.dayTV1);
        dayTV1.setText(profile.getDaySettings().get(1).getDay());
        dayTV2 = (TextView) findViewById(R.id.dayTV2);
        dayTV2.setText(profile.getDaySettings().get(2).getDay());
        dayTV3 = (TextView) findViewById(R.id.dayTV3);
        dayTV3.setText(profile.getDaySettings().get(3).getDay());
        dayTV4 = (TextView) findViewById(R.id.dayTV4);
        dayTV4.setText(profile.getDaySettings().get(4).getDay());
        dayTV5 = (TextView) findViewById(R.id.dayTV5);
        dayTV5.setText(profile.getDaySettings().get(5).getDay());
        dayTV6 = (TextView) findViewById(R.id.dayTV6);
        dayTV6.setText(profile.getDaySettings().get(6).getDay());

        saveBtn = (ImageView) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);
        cancelBtn = (ImageView) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton == enableCB0) {
            startTime0.setEnabled(b);
            stopTime0.setEnabled(b);
        }else if(compoundButton == enableCB1){
            startTime1.setEnabled(b);
            stopTime1.setEnabled(b);
        }else if(compoundButton == enableCB2){
            startTime2.setEnabled(b);
            stopTime2.setEnabled(b);
        }else if(compoundButton == enableCB3){
            startTime3.setEnabled(b);
            stopTime3.setEnabled(b);
        }else if(compoundButton == enableCB4){
            startTime4.setEnabled(b);
            stopTime4.setEnabled(b);
        }else if(compoundButton == enableCB5){
            startTime5.setEnabled(b);
            stopTime5.setEnabled(b);
        }else if(compoundButton == enableCB6){
            startTime6.setEnabled(b);
            stopTime6.setEnabled(b);
        }


    }

    @Override
    public void onClick(View view) {
        if(view == cancelBtn){
            finish();
        }else if(view == saveBtn){
            try {
                profile.setTestEnable(captchaEnableCB.isChecked());
                profile.setHeadsetConnectionAllowed(headsetModeEnableCB.isChecked());

                profile.getDaySettings().get(0).setStartTime(Utility.timeInMilliSecond(startTime0.getText().toString()));
                profile.getDaySettings().get(0).setStopTime(Utility.timeInMilliSecond(stopTime0.getText().toString()));
                profile.getDaySettings().get(0).setEnabled(enableCB0.isChecked());

                profile.getDaySettings().get(1).setStartTime(Utility.timeInMilliSecond(startTime1.getText().toString()));
                profile.getDaySettings().get(1).setStopTime(Utility.timeInMilliSecond(stopTime1.getText().toString()));
                profile.getDaySettings().get(1).setEnabled(enableCB1.isChecked());

                profile.getDaySettings().get(2).setStartTime(Utility.timeInMilliSecond(startTime2.getText().toString()));
                profile.getDaySettings().get(2).setStopTime(Utility.timeInMilliSecond(stopTime2.getText().toString()));
                profile.getDaySettings().get(2).setEnabled(enableCB2.isChecked());

                profile.getDaySettings().get(3).setStartTime(Utility.timeInMilliSecond(startTime3.getText().toString()));
                profile.getDaySettings().get(3).setStopTime(Utility.timeInMilliSecond(stopTime3.getText().toString()));
                profile.getDaySettings().get(3).setEnabled(enableCB3.isChecked());

                profile.getDaySettings().get(4).setStartTime(Utility.timeInMilliSecond(startTime4.getText().toString()));
                profile.getDaySettings().get(4).setStopTime(Utility.timeInMilliSecond(stopTime4.getText().toString()));
                profile.getDaySettings().get(4).setEnabled(enableCB4.isChecked());

                profile.getDaySettings().get(5).setStartTime(Utility.timeInMilliSecond(startTime5.getText().toString()));
                profile.getDaySettings().get(5).setStopTime(Utility.timeInMilliSecond(stopTime5.getText().toString()));
                profile.getDaySettings().get(5).setEnabled(enableCB5.isChecked());

                profile.getDaySettings().get(6).setStartTime(Utility.timeInMilliSecond(startTime6.getText().toString()));
                profile.getDaySettings().get(6).setStopTime(Utility.timeInMilliSecond(stopTime6.getText().toString()));
                profile.getDaySettings().get(6).setEnabled(enableCB6.isChecked());
                boolean valid = true;


                if(!Utility.validateTime(profile.getDaySettings().get(0).getStartTime())){
                    startTime0.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(0).getStopTime())){
                    stopTime0.setError(getString(R.string.invalid_time));
                    valid = false;
                }


                if(!Utility.validateTime(profile.getDaySettings().get(1).getStartTime())){
                    startTime1.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(1).getStopTime())){
                    stopTime1.setError(getString(R.string.invalid_time));
                    valid = false;
                }


                if(!Utility.validateTime(profile.getDaySettings().get(2).getStartTime())){
                    startTime2.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(2).getStopTime())){
                    stopTime2.setError(getString(R.string.invalid_time));
                    valid = false;
                }

                if(!Utility.validateTime(profile.getDaySettings().get(3).getStartTime())){
                    startTime3.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(3).getStopTime())){
                    stopTime3.setError(getString(R.string.invalid_time));
                    valid = false;
                }

                if(!Utility.validateTime(profile.getDaySettings().get(4).getStartTime())){
                    startTime4.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(4).getStopTime())){
                    stopTime4.setError(getString(R.string.invalid_time));
                    valid = false;
                }

                if(!Utility.validateTime(profile.getDaySettings().get(5).getStartTime())){
                    startTime5.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(5).getStopTime())){
                    stopTime5.setError(getString(R.string.invalid_time));
                    valid = false;
                }

                if(!Utility.validateTime(profile.getDaySettings().get(6).getStartTime())){
                    startTime6.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!Utility.validateTime(profile.getDaySettings().get(6).getStopTime())){
                    stopTime6.setError(getString(R.string.invalid_time));
                    valid = false;
                }
                if(!valid){
                    return;
                }

                try {
                    profile.setThresholdSpeed(Float.parseFloat(speedET.getText().toString()));
                }catch(Exception ex){
                    profile.setThresholdSpeed(0.0f);
                }

                profile.setEmergencyNos(emergencyNumberET.getText().toString());
                DBOperation.updateProfile(profile, this);
                if(MainService.getInstance() != null) {
                    MainService.getInstance().sendMsgToServiceHandler(MessageType.UPDATE_PROFILE, null);
                }
                Toast.makeText(this,getResources().getString(R.string.day_settings_update_success), Toast.LENGTH_SHORT).show();
                finish();
            }catch(Exception ex){
                Log.d(TAG, "Exception ex :"+ex);
                Toast.makeText(this,getResources().getString(R.string.day_settings_update_failure), Toast.LENGTH_LONG).show();
            }
        }
    }
}





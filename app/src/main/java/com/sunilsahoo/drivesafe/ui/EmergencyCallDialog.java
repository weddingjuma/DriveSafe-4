package com.sunilsahoo.drivesafe.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.listener.EmergencyCallDialogListener;

public class EmergencyCallDialog extends Dialog implements
		android.view.View.OnClickListener {

	private EmergencyCallDialogListener dialogClickEvent;
	private String[] emergencyNoArr = null;

	private Button[] selBtn = new Button[3];

	
	public EmergencyCallDialog(Context context, String[] phonenumbers,
			EmergencyCallDialogListener dialogClickEvent) {
		super(context, R.style.EmergencyDialog);
		this.emergencyNoArr = phonenumbers;
		this.dialogClickEvent = dialogClickEvent;
		this.setTitle(getContext().getString(R.string.lbl_select_emergency_no));

	}

	@Override
	public void onBackPressed() {
		dismiss();
	}

	@Override
	public void onClick(View v) {
		if (v == selBtn[0]) {
			dialogClickEvent.clickedValue(0);
			dismiss();
		} else if (v == selBtn[1]) {
			dialogClickEvent.clickedValue(1);
			dismiss();
		} else if (v == selBtn[2]) {
			dialogClickEvent.clickedValue(2);
			dismiss();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencycalldialog);

		selBtn[0] = (Button) findViewById(R.id.btnEmergency1);
		selBtn[1] = (Button) findViewById(R.id.btnEmergency2);
		selBtn[2] = (Button) findViewById(R.id.btnEmergency3);

		for (Button btn : selBtn) {
			btn.setOnClickListener(this);
			btn.setVisibility(View.GONE);
		}

		if (emergencyNoArr != null && emergencyNoArr.length > 0) {
			if (emergencyNoArr.length >= selBtn.length) {
				for (int i = 0; i < selBtn.length; i++) {
					selBtn[i].setText(emergencyNoArr[i]);
					selBtn[i].setVisibility(View.VISIBLE);
				}

			} else {
				for (int i = 0; i < emergencyNoArr.length; i++) {
					selBtn[i].setText(emergencyNoArr[i]);
					selBtn[i].setVisibility(View.VISIBLE);
				}
			}
		}

	}

}

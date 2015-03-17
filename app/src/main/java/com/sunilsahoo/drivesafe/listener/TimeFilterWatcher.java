package com.sunilsahoo.drivesafe.listener;

/**
 * Created by sunil on 18/2/15.
 */

import android.text.Editable;
        import android.text.Selection;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.widget.EditText;

import com.sunilsahoo.drivesafe.utility.Constants;

public class TimeFilterWatcher implements TextWatcher {
    private static final String TAG = TimeFilterWatcher.class.getName();
    EditText edittext;

    public TimeFilterWatcher(EditText edittext) {
        this.edittext = edittext;
    }

    public void afterTextChanged(Editable s) {}
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        Log.d(TAG, "onTextChanged");
        if(!s.toString().matches("^(\\d+)(\\:\\d{2})$"))
        {
            edittext.removeTextChangedListener(this);
            String userInput= ""+s.toString().replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);
            Log.v(TAG, cashAmountBuilder.toString());

            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                cashAmountBuilder.deleteCharAt(0);
            }
            while (cashAmountBuilder.length() < 5) {
                cashAmountBuilder.insert(0, "0");
            }
            if (cashAmountBuilder.length() > 5) {
                Log.v(TAG, ""+cashAmountBuilder.charAt(cashAmountBuilder.length() - 1));
                cashAmountBuilder.deleteCharAt(0);
            }

            cashAmountBuilder.insert(cashAmountBuilder.length()-2, ':');
//            cashAmountBuilder.insert(0, '₱');
            String[] arr = cashAmountBuilder.toString().split(":");
            edittext.setText(cashAmountBuilder.toString());

            // keeps the cursor always to the right
            Selection.setSelection(edittext.getText(), edittext.getText().length());
            edittext.addTextChangedListener(this);
        }
    }
}

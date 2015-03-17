package com.sunilsahoo.drivesafe.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.utility.Constants.TestInputTypeStatus;

public class ImageAdapter extends BaseAdapter {
	private Context mContext = null;
	private byte inputStatusArray[] = null;
	private int itemCount = 0;
	private Integer[] imageIds = {
			R.drawable.indicator_white,
			R.drawable.indicator_green,
			R.drawable.indicator_red
	};

	public ImageAdapter(Context ctx, int noOfItem) {
		mContext = ctx;
		this.itemCount = (noOfItem < 0) ? 0 : noOfItem;
		inputStatusArray = new byte[noOfItem];
	}

	@Override
	public int getCount() {
		if (inputStatusArray != null) {
			return inputStatusArray.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = new ImageView(mContext);

		if (inputStatusArray != null && position >= 0
				&& position < inputStatusArray.length) {

			byte inputStatus = inputStatusArray[position];
			switch (inputStatus) {
			case TestInputTypeStatus.NOT_FILLED:
				imageView.setImageResource(imageIds[0]); // White
				break;
			case TestInputTypeStatus.CORRECT:
				imageView.setImageResource(imageIds[1]); // Green
				break;
			case TestInputTypeStatus.INCORRECT:
				imageView.setImageResource(imageIds[2]); // RED
				break;
			default:
				break;
			}
		}

		imageView.setFocusable(false);
		imageView.setFocusableInTouchMode(false);
		return imageView;
	}

	public void initializeIndicator() {
		inputStatusArray = new byte[itemCount];
	}

	public void setInputStatus(int position, byte status) {
		if (inputStatusArray != null && position < inputStatusArray.length
				&& position >= 0) {
			inputStatusArray[position] = status;
		}
	}
}

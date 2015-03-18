package com.sunilsahoo.drivesafe.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.sunilsahoo.drivesafe.R;


public class CustomMenuDrawer implements OnItemClickListener, OnDismissListener {
	private static String TAG = CustomMenuDrawer.class.getName();
	private static int EXTRA_MARGIN_MENU_OPTION = 50;
	Context context = null;
	PopupWindow pw = null;
	private static boolean isPopupOpen = false;

	private CustomMenuDrawer(Context context) {
		this.context = context;
	}

	public class MenuDrawerAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;

		class ViewHolder {
			public TextView text;
		}

		private MenuDrawerAdapter(Context context, String[] values) {
			super(context, R.layout.menu_drawer_options, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.menu_drawer_options,
						null, false);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.text = (TextView) rowView
						.findViewById(R.id.menu_iem);
				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();
			holder.text.setText(values[position]);

			return rowView;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Log.i(TAG, "Item clicked " + position);
		closeMenuDrawer();
		switch (position) {
		/*case 0:
			// game pad controls
			break;*/
		case 0:
			// settings
			Intent intent = new Intent(context, DaySettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			context.startActivity(intent);
			break;
		case 1:		
			//Report screen
            //TODO launch report screen
			break;
        case 2:
            Activity activity = (Activity) context;
            activity.finish();
            break;
		default:
			break;
		}
	}

	private void closeMenuDrawer() {
		if (pw != null) {
			pw.dismiss();
		}
		isPopupOpen = false;
	}

	/**
	 * Description: Returns width of Widest View of list view
	 * 
	 * @param context
	 * @param adapter
	 * @return
	 */
	private int getWidestView(Context context, Adapter adapter) {
		int maxWidth = 0;
		View view = null;
		FrameLayout fakeParent = new FrameLayout(context);
		for (int i = 0, count = adapter.getCount(); i < count; i++) {
			view = adapter.getView(i, view, fakeParent);
			view.measure(View.MeasureSpec.UNSPECIFIED,
					View.MeasureSpec.UNSPECIFIED);
			int width = view.getMeasuredWidth();
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		return maxWidth;
	}

	/**
	 * OPens Menu Drawer
	 * 
	 * @param rootView
	 */
	private void openMenuDrawer(View rootView, int leftMargin, int topMargin) {
		try {
			isPopupOpen = true;
			MenuDrawerAdapter menuListAdapter = new MenuDrawerAdapter(context,
					context.getResources().getStringArray(
							R.array.menu_drawer_options));

			LinearLayout listViewContainer = new LinearLayout(context);
			LinearLayout.LayoutParams listViewContainerLP = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			listViewContainer.setLayoutParams(listViewContainerLP);
			listViewContainer.setBackgroundResource(R.drawable.text_bg_normal_tile);
			ListView menuList = new ListView(context);
			menuList.setFadingEdgeLength(0);
			menuList.setVerticalFadingEdgeEnabled(false);
			menuList.setVerticalScrollBarEnabled(false);
			menuList.setHorizontalScrollBarEnabled(false);
			menuList.setOnItemClickListener(this);
			listViewContainer.addView(menuList);
			// set width of list view
			menuList.getLayoutParams().width = getWidestView(context,
					menuListAdapter) + EXTRA_MARGIN_MENU_OPTION;

			menuList.setAdapter(menuListAdapter);

			pw = new PopupWindow(context);
			pw.setTouchable(true);
			pw.setFocusable(true);
			pw.setOutsideTouchable(true);
			pw.setOnDismissListener(this);

			pw.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			pw.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
			pw.setOutsideTouchable(false);
			pw.setContentView(listViewContainer);
			pw.showAsDropDown(rootView, leftMargin, topMargin);
			
		} catch (Exception ex) {
			isPopupOpen = false;
			Log.e(TAG, "Error in Opening Menu Drawer :" + ex.getMessage());
		}
	}

	/**
	 * Description: Display Menu drawer returns true if success and false if not
	 * success
	 * 
	 * @param context
	 * @param rootView
	 * @param leftMargin
	 * @param topMargin
	 * @return
	 */

	public static boolean showMenuDrawer(Context context, View rootView,
			int leftMargin, int topMargin) {
		Log.i(TAG, "SHOW MENU DRAWER :" + leftMargin + " :" + topMargin);
		if (!isPopupOpen) {
			new CustomMenuDrawer(context).openMenuDrawer(rootView, leftMargin,
					topMargin);
		}
		return isPopupOpen;
	}

	@Override
	public void onDismiss() {
		closeMenuDrawer();
	}

}

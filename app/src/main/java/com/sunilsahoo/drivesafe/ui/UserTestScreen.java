package com.sunilsahoo.drivesafe.ui;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sunilsahoo.drivesafe.R;
import com.sunilsahoo.drivesafe.database.DBOperation;
import com.sunilsahoo.drivesafe.model.Profile;
import com.sunilsahoo.drivesafe.services.MainService;
import com.sunilsahoo.drivesafe.utility.Constants;
import com.sunilsahoo.drivesafe.utility.Constants.TestInputTypeStatus;
import com.sunilsahoo.drivesafe.utility.MessageType;
import com.sunilsahoo.drivesafe.utility.ScreenState;
import com.sunilsahoo.drivesafe.utility.TestResultCategory;
import com.sunilsahoo.drivesafe.utility.Utility;

public class UserTestScreen extends Activity implements OnKeyListener,
		OnTouchListener {
	private static final String TAG = UserTestScreen.class.getName();
	private static int charCount = Constants.USERTEST_NO_CHAR;
	private static UserTestScreen usertestScreen = null;

	private View activityRootView = null;
	private int charIndex = 0;
	private long charInputTime = Constants.EOF;
	Profile profile = new Profile();
	private char charShown = ' ';
	private String currentTaskId = null;
	private EditText editUsertest = null;

	private boolean forceStop = false;
	private boolean enableInput = false;
	private boolean isCharPresent = false;
	private boolean isKeypadShown = false;
	private int failCount = 0;
	private int prevCharIndex = Constants.EOF;
	private int hardKeyboradHidden = Configuration.HARDKEYBOARDHIDDEN_YES;
	private int testStatus = TestResultCategory.START_TEST;
	private volatile boolean isRunning = true;

	private final String TASK_USERTEST_INIT = "USERTEST_INIT";

	private TextView textViewCharContainer = null;
	private TextView textViewTapUsertest = null;
	private Thread captchaRemoverThread = null;

	private UsertestTask mTestTask = null;
	private Timer mTestTimer = null;
	private IndicatorGallery mIndicatorGallery = null;
	private ImageAdapter mImageAdapter = null;
	private InputMethodManager imManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "inside onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usertest_screen);
        //added extra start
        if (MainService.getInstance() != null) {
            MainService.getInstance().onUsertestPassAttempt();
        }
        //removes extra


		profile = DBOperation.getProfile(this);
		imManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mIndicatorGallery = (IndicatorGallery) findViewById(R.id.gallery_UsertestIndicator);
		mImageAdapter = new ImageAdapter(this, charCount);
		mIndicatorGallery.setAdapter(mImageAdapter);
		mIndicatorGallery.setAnimationCacheEnabled(false);
		mIndicatorGallery.setUnselectedAlpha(255);
		mIndicatorGallery.setHorizontalFadingEdgeEnabled(false);

		textViewTapUsertest = (TextView) findViewById(R.id.textView_TapUsertest);
		textViewCharContainer = (TextView) findViewById(R.id.textView_UsertestChar);
		editUsertest = (EditText) findViewById(R.id.editText_UsertestCharEntered);
		editUsertest.setRawInputType(InputType.TYPE_CLASS_TEXT);
		editUsertest.setOnTouchListener(this);
		editUsertest.clearFocus();
		editUsertest.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable editView) {
				if (isCharPresent && enableInput) {
					disableInput();
					processInput();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		usertestScreen = this;
		isKeypadShown = false;
		hardKeyboradHidden = getResources().getConfiguration().hardKeyboardHidden;
		addSoftkeyListener();
	}

	@Override
	protected void onDestroy() {
		stopUsertestTimer();
		stopCharRemover();
		currentTaskId = "" + System.currentTimeMillis();
		mImageAdapter = null;
		editUsertest = null;
		textViewCharContainer = null;
		usertestScreen = null;
		Footer.dismissEmergencyDialog();
		super.onDestroy();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (v == editUsertest && keyCode == KeyEvent.KEYCODE_DEL) {
			Log.i(TAG, "backspace pressed");
			return true;
		}
		return false;
	}

	public static UserTestScreen getInstance() {
		return usertestScreen;
	}

	public static int getCharCount() {
		return charCount;
	}

	private void addSoftkeyListener() {
		activityRootView = findViewById(R.id.usertest_screen);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect activityRect = new Rect();
						activityRootView
								.getWindowVisibleDisplayFrame(activityRect);
						int activityHeight = activityRootView
								.getRootView()
								.getHeight();
						int heightDiff = activityHeight
								- (activityRect.bottom - activityRect.top);

						if (heightDiff > Constants.MIN_HEIGHT_OF_SOFT_KEY) {
							onSoftKeyboardShown();
						} else if (activityHeight == activityRect.bottom) {
							if (Footer.isDialogVisible()) {
								isKeypadShown = false;
							}
						}
					}
				});
	}

	private void disableInput() {
		enableInput = false;
	}

	private void enableInput() {
		enableInput = true;
		editUsertest.setEnabled(true);

	}

	public void forceStop() {
		Log.i(TAG, "Inside force stop.");
		forceStop = true;
		finish();
	}

	/**
	 * To be called after user entered an invalid character
	 */
	private void onInvalidCharEnter(boolean isCharEntered) {
		failCount++;
		stopUsertestTimer();
		if (mImageAdapter != null) {
			mImageAdapter.setInputStatus(charIndex,
					TestInputTypeStatus.INCORRECT);
			sendMessage(MessageType.UI_UPDATE_INDICATOR, null);
		}

		if (isCharEntered) {
			charIndex++;
		}
		if (failCount == Constants.MAX_TEST_ATTEMPT) {
			processTestResult(TestResultCategory.FAILED);
		} else {
			showToastNotification(getString(R.string.msg_usertest_failed));
			sendMessage(MessageType.UI_RESTART_TEST, null);
		}
	}

	private void sendMessage(int id, Object obj) {
		Message msg = handler.obtainMessage();
		msg.what = id;
		if (obj != null) {
			msg.obj = obj;
		}
		handler.sendMessage(msg);
	}

	@Override
	public void onBackPressed() {
		processTestResult(TestResultCategory.FAILED);
		super.onBackPressed();
	}

	public void onSoftKeyboardShown() {
		if (textViewTapUsertest != null
				&& textViewTapUsertest.getText().length() > 0) {
			textViewTapUsertest.setText("");
		}
		if (!isKeypadShown) {
			isKeypadShown = true;
			if (MainService.getInstance() != null) {
				MainService.getInstance().onUsertestPassAttempt();
			}
			startUsertestTimer();
		}
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "inside onStop()");
		super.onStop();

		int callState = Utility.getPhoneCallState(this);
		if (testStatus == TestResultCategory.START_TEST
				&& callState != TelephonyManager.CALL_STATE_RINGING
				&& !forceStop) {

			if (MainService.getInstance() != null
					&& !MainService.getInstance().isHandleCall()) {
				isRunning = false;
				MainService.getInstance().onUsertestFailed(
						TestResultCategory.PAUSED);
				MainService.getInstance().showNotification(
						getString(R.string.lbl_usertest_failed));
				Utility.lockScreen(this);
				finish();
			}

		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		Log.i(TAG, "inside onTouch() Mask Action:" + event.getActionMasked()
				+ ", Action:" + event.getAction());
		if (view == editUsertest) {
			if (hardKeyboradHidden == Configuration.HARDKEYBOARDHIDDEN_NO
					&& event.getAction() == MotionEvent.ACTION_UP) {
				// Remove the hints after soft keyboard is shown
				if (textViewTapUsertest != null
						&& textViewTapUsertest.getText().length() > 0) {
					textViewTapUsertest.setText("");
				}
				if (!isKeypadShown) {
					isKeypadShown = true;
					MainService dsMainService = MainService
							.getInstance();
					if (dsMainService != null) {
						dsMainService.onUsertestPassAttempt();
					}
					startUsertestTimer();
				}
			}
		}
		return false;
	}

	private void processTestResult(int testResult) {
		testStatus = testResult;
		stopUsertestTimer();
		stopCharRemover();

		if (MainService.getInstance() != null) {
			if (testResult == TestResultCategory.PASSED) {
				MainService.getInstance().onUsertestPassed();
			} else {
				MainService.getInstance()
						.setScreenStatus(ScreenState.LOCK);
				MainService.getInstance()
						.onUsertestFailed(TestResultCategory.FAILED);
			}
		}
		finish();

	}

	/**
	 * Call this method to bring UserTest in Paused State. This method has to be
	 * called from
	 * main thread.
	 */
	private void pauseUsertestProcess() {

		stopUsertestTimer();
		stopCharRemover();

		prevCharIndex = 0;
		charIndex = 0;
		currentTaskId = TASK_USERTEST_INIT;
		editUsertest.setText("");
		editUsertest.setEnabled(true);
		editUsertest.clearFocus();

		textViewCharContainer.setText("");

		String lblTapText = getString(R.string.lbl_touch_usertest);

		textViewTapUsertest.setText(lblTapText);

		// Make all the image indicator as white.
		if (mImageAdapter != null) {
			mImageAdapter.initializeIndicator();
			mImageAdapter.notifyDataSetChanged();
		}

	}

	/**
	 * Handles the character input for UserTest text box.
	 */
	private void processInput() {

		Log.i(TAG, "Inside process Input. Enter time:" + new Date());
		String textEntered = editUsertest.getText().toString();
		isCharPresent = false;
		charInputTime = SystemClock.elapsedRealtime();
		if (textEntered.length() > 0) {
			char charEntered = Character.toLowerCase(textEntered.charAt(0));
			Log.i(TAG, "Entered char is : " + charEntered);
			if (charEntered == charShown) {
				if (mImageAdapter != null) {
					Log.i(TAG, "Entered Correct Character");
					mImageAdapter.setInputStatus(charIndex,
							TestInputTypeStatus.CORRECT);
					sendMessage(MessageType.UI_UPDATE_INDICATOR, null);
				}

				charIndex++;
				Log.i(TAG, "current char Index:" + charIndex + " max no of char :"
						+ charCount);
				if (charIndex == charCount) {
					processTestResult(TestResultCategory.PASSED);
				} else {
					showNextChar(false);
				}
			} else {
				onInvalidCharEnter(true);
			}
		}
	}

	private void showNextChar(boolean firstTime) {
		stopUsertestTimer();
		String taskId = "" + System.currentTimeMillis();
		mTestTask = new UsertestTask(taskId);
		if (firstTime) {
			mTestTask.startTest(true);
		}
		mTestTimer = new Timer();
		mTestTimer.schedule(mTestTask,
				Utility.nextCharInterval(profile.getCharIntervalMin(),
						profile.getCharIntervalMax()));
	}

	private void showToastNotification(String message) {
		sendMessage(MessageType.UI_NOTICATION_TOAST, message);
	}

	private void startCharRemover() {
		stopCharRemover();
		captchaRemoverThread = new Thread() {
			@Override
			public void run() {
				try {
					long currentTime = Constants.EOF;
					while (true) {
						if (enableInput == false
								&& editUsertest.getText().length() > 0) {
							currentTime = SystemClock.elapsedRealtime();
							if (currentTime - charInputTime > 400) {
								sendMessage(MessageType.UI_CLEAR_EDITFIELD,
										null);

							}
						}
						Thread.sleep(100);
					}

				} catch (Exception e) {
					Log.e(TAG, " Exception :" + e.getMessage());
				}
			}
		};
		captchaRemoverThread.start();
	}

	/**
	 * It will start the timer for displaying a character randomly with fixed
	 * intervals as well as for checking if
	 * the user has entered any character within the time.
	 */
	private void startUsertestTimer() {
		Log.i(TAG, "Starting UserTest.");
		prevCharIndex = 0;
		charIndex = 0;
		currentTaskId = TASK_USERTEST_INIT;
		showNextChar(true);
		startCharRemover();
	}

	private void stopCharRemover() {
		if (captchaRemoverThread != null && captchaRemoverThread.isAlive()) {
			captchaRemoverThread.interrupt();
			captchaRemoverThread = null;
		}
	}

	private void stopUsertestTimer() {
		if (mTestTimer != null) {
			mTestTimer.cancel();
			mTestTimer = null;
		}
		if (mTestTask != null) {
			mTestTask.cancel();
			mTestTask = null;
		}
	}

	private class UsertestTask extends TimerTask {
		private String taskId = null;
		private boolean startTest = false;

		public UsertestTask(String taskId) {
			super();
			this.taskId = taskId;
		}

		@Override
		public void run() {
			try {
				currentTaskId = taskId;
				if (startTest) {
					if (mImageAdapter != null) {
						mImageAdapter.initializeIndicator();
						sendMessage(MessageType.UI_UPDATE_INDICATOR, null);
					}
				}
				// first show the character
				sendMessage(MessageType.UI_INSERT_CHAR,
						Character.toString(Utility.getNextRandomChar()));

				// Now wait for the presentation time
				if (profile.getCharPresentTime() > profile
						.getCharResponseTime()) {
					Thread.sleep(profile.getCharResponseTime());
				} else {
					Thread.sleep(profile.getCharPresentTime());
				}

				if (taskId.equals(currentTaskId) || currentTaskId != null
						&& currentTaskId.equals(TASK_USERTEST_INIT)) {

					sendMessage(MessageType.UI_INSERT_CHAR, "");
				}

				// Now wait for response time. time to wait = response_time -
				// present_time; because we have already wait for present time.
				if (profile.getCharResponseTime() > profile
						.getCharPresentTime()) {
					Thread.sleep(profile.getCharResponseTime()
							- profile.getCharPresentTime());
				}

				if (currentTaskId != null && taskId != null
						&& currentTaskId.equals(taskId)) {

					if (charIndex == prevCharIndex) {
						sendMessage(MessageType.UI_DISABLE_INPUT, null);
						isCharPresent = false;
						if (failCount < (Constants.MAX_TEST_ATTEMPT -1 )) {
							showToastNotification(getString(R.string.msg_usertest_noinput));
						}
						if (isRunning) {
							onInvalidCharEnter(false);
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Error in run for checking char input.", e);
			}
		}

		private void startTest(boolean usertestStarter) {
			this.startTest = usertestStarter;
		}

	}

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.UI_INSERT_CHAR:
				// Show the char
				if (textViewCharContainer != null && msg.obj != null
						&& msg.obj instanceof String) {
					String charString = (String) msg.obj;
					textViewCharContainer.setText(charString);

					if (charString.length() > 0) {
						charShown = charString.charAt(0);
						Log.i(TAG, "Char shown:" + charShown);
						editUsertest.setText("");
						editUsertest.setEnabled(true);
						enableInput();
						editUsertest.requestFocus();
						isCharPresent = true;

						if (imManager != null) {
							imManager.showSoftInput(editUsertest, 0);
						}
						prevCharIndex = charIndex;
					}
				}
				break;

			case MessageType.UI_RESTART_TEST:
				startUsertestTimer();
				break;

			case MessageType.UI_NOTICATION_TOAST:
				if (msg.obj != null && msg.obj instanceof String) {
					Toast.makeText(getApplicationContext(), (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case MessageType.UI_LOCK_SCREEN:
				Utility.lockScreen(UserTestScreen.this);
				break;

			case MessageType.UI_UPDATE_INDICATOR:
				if (mImageAdapter != null) {
					mImageAdapter.notifyDataSetChanged();
				}
				break;

			case MessageType.UI_DISABLE_INPUT:
				if (editUsertest != null) {
					editUsertest.setEnabled(false);
				}
				break;

			case MessageType.UI_PAUSE_UserTest:
				pauseUsertestProcess();
				break;

			case MessageType.UI_CLEAR_EDITFIELD:
				editUsertest.setText("");
				break;

			default:
				break;
			}

		}
	};

}

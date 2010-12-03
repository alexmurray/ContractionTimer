package com.alexdroid.contractiontimer;

import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.Button;
import android.view.View;
import android.util.Log;

public class ContractionTimer extends Activity
{
	private static String TAG = "ContractionTimer";

	private boolean mRunning = false;
	private Chronometer mTimer;
	private Button mButton;
	private TextView mLastTime;

	/* updates UI elements depending on state of mRunning variable */
	private void updateUI()
	{
		Log.v(TAG, "Updating UI: mRunning=" + mRunning);
		if (mRunning) {
			mTimer.start();
			mButton.setText(R.string.stop_text);
		} else {
			mTimer.stop();
			mLastTime.setText(mTimer.getText());
			/* clear mTimer display back to zero */
			mTimer.setBase(android.os.SystemClock.elapsedRealtime());
			mButton.setText(R.string.start_text);
		}
	}

	/* restores state from preferences */
	private void restoreState()
	{
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		mRunning = settings.getBoolean("running", false);
		mLastTime.setText(settings.getString("lastTime", getString(R.string.last_time_value_text)));
		mTimer.setBase(settings.getLong("timerBase", android.os.SystemClock.elapsedRealtime()));
		Log.v(TAG, "Restored state from preferences: mRunning: " + mRunning + " lastTime: " + mLastTime.getText() + " timerBase: " + mTimer.getBase());
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mLastTime = (TextView)findViewById(R.id.last_time_value);
		mTimer = (Chronometer)findViewById(R.id.timer);
		mButton = (Button)findViewById(R.id.button);

		/* restore state */
		restoreState();

		/* register listener for click events */
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mRunning = !mRunning;
				if (mRunning) {
					/* set mTimer to count from now */
					mTimer.setBase(android.os.SystemClock.elapsedRealtime());
				}
				updateUI();
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		/* update our UI */
		updateUI();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		Log.v(TAG, "Saving state to preferences: mRunning: " + mRunning + " lastTime: " + mLastTime.getText() + " timerBase: " + mTimer.getBase());
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("running", mRunning);
		editor.putString("lastTime", mLastTime.getText().toString());
		editor.putLong("timerBase", mTimer.getBase());

		editor.commit();
	}
}

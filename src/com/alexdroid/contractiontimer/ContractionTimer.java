package com.alexdroid.contractiontimer;

import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.Button;
import android.view.View;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class ContractionTimer extends Activity
{
	private static String TAG = "ContractionTimer";

	private boolean mRunning = false;
	private Chronometer mTimer;
	private Button mButton;
	private TextView mLastTime, mAverageTime;
	private DataHelper mDataHelper;
	private long mLastTimeMillis, mAverageTimeMillis;
	private long mNumContractions = 0;

	/* updates UI elements depending on state of mRunning variable */
	private void updateUI()
	{
		Log.v(TAG, "Updating UI: mRunning=" + mRunning);
		if (mRunning) {
			mTimer.start();
			mButton.setText(R.string.stop_text);
		} else {
			mTimer.stop();
			mAverageTime.setText(android.text.format.DateUtils.formatElapsedTime(mAverageTimeMillis / 1000));
			mLastTime.setText(android.text.format.DateUtils.formatElapsedTime(mLastTimeMillis / 1000));
			/* clear mTimer display back to zero */
			mTimer.setBase(android.os.SystemClock.elapsedRealtime());
			mButton.setText(R.string.start_text);
		}
	}

	/* restores state from preferences and database */
	private void restoreState()
	{
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		mRunning = settings.getBoolean("running", false);
		mTimer.setBase(settings.getLong("timerBase", android.os.SystemClock.elapsedRealtime()));
		Log.v(TAG, "Restored state from preferences: mRunning: " + mRunning + " timerBase: " + mTimer.getBase());

		mAverageTimeMillis = 0;
		long newestStart = 0;
		Iterator<Contraction> iter = mDataHelper.getAllContractions().iterator();
		while (iter.hasNext())
		{
			Contraction c = iter.next();
			mAverageTimeMillis += c.getDuration();
			mNumContractions++;
			if (c.getStart() > newestStart)
			{
				newestStart = c.getStart();
				mLastTimeMillis = c.getDuration();
			}
		}
		if (mNumContractions > 0) {
			mAverageTimeMillis /= mNumContractions;
		}
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mAverageTime = (TextView)findViewById(R.id.average_time_value);
		mLastTime = (TextView)findViewById(R.id.last_time_value);
		mTimer = (Chronometer)findViewById(R.id.timer);
		mButton = (Button)findViewById(R.id.button);
		mDataHelper = new DataHelper(this);

		mDataHelper.deleteAll();
		/* restore state */
		restoreState();

		/* register listener for click events */
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mRunning = !mRunning;
				if (mRunning) {
					/* set mTimer to count from now */
					mTimer.setBase(android.os.SystemClock.elapsedRealtime());
				} else {
					long duration = android.os.SystemClock.elapsedRealtime() - mTimer.getBase();
					long start = java.lang.System.currentTimeMillis();

					if (mDataHelper.insertContraction(start, duration) != -1) {
						mLastTimeMillis = duration;
						/* update average */
						mAverageTimeMillis *= mNumContractions;
						mAverageTimeMillis += mLastTimeMillis;
						mAverageTimeMillis /= ++mNumContractions;
					}
					Log.v(TAG, "Contractions: " + mDataHelper.getAllContractions().toString());
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

		Log.v(TAG, "Saving state to preferences: mRunning: " + mRunning + " timerBase: " + mTimer.getBase());
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("running", mRunning);
		editor.putLong("timerBase", mTimer.getBase());

		editor.commit();
	}
}

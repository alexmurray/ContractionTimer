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

	private long mCurrentID = -1, mLastID = -1;
	private Chronometer mTimer;
	private Button mButton;
	private TextView mLastTime, mAverageTime;
	private ContractionStore mStore;

	/* updates UI elements depending on state of mCurrentID variable */
	private void updateUI()
	{
		Log.v(TAG, "Updating UI: mCurrentID=" + mCurrentID);

		long lastTimeMillis = 0;
		long averageTimeMillis = 0;
		long numContractions = 0;

		/* 1 hour is 60 * 60 * 1000 = 3600000 milliseconds */
		long recent = 3600000;
		ArrayList<Contraction> contractions = mStore.getRecentContractions(java.lang.System.currentTimeMillis() - recent);
		Log.v(TAG, "Calculating average of contractions which occurred in the last " + recent / 1000 + " seconds: " + contractions.toString());
		Iterator<Contraction> iter = contractions.iterator();
		while (iter.hasNext())
		{
			Contraction c = iter.next();
			if (c.getID() != mCurrentID)
			{
				averageTimeMillis += c.getDuration();
				numContractions++;
			}
		}
		if (numContractions > 0) {
			averageTimeMillis /= numContractions;
		}
		Log.v(TAG, "Calculated averageTimeMillis: " + averageTimeMillis);
		mAverageTime.setText(averageTimeMillis > 0 ?
				android.text.format.DateUtils.formatElapsedTime(averageTimeMillis / 1000) :
				this.getString(R.string.none_text));

		if (mLastID != -1) {
			lastTimeMillis = mStore.getContraction(mLastID).getDuration();
		}
		mLastTime.setText(averageTimeMillis > 0 ?
				android.text.format.DateUtils.formatElapsedTime(averageTimeMillis / 1000) :
				this.getString(R.string.none_text));
		if (mCurrentID != -1) {
			/* set time based on start time of current contraction
			*/
			Contraction contraction = mStore.getContraction(mCurrentID);
			mTimer.setBase(android.os.SystemClock.elapsedRealtime() -
					(java.lang.System.currentTimeMillis() -
					 contraction.getStart()));
			mTimer.start();
			mButton.setText(R.string.stop_text);
		} else {
			mTimer.stop();
			/* clear mTimer display back to zero */
			mTimer.setBase(android.os.SystemClock.elapsedRealtime());
			mButton.setText(R.string.start_text);
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

		/* register listener for click events */
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mCurrentID == -1) {
					/* start a contraction at the current
					 * time */
					mCurrentID = mStore.startContraction(java.lang.System.currentTimeMillis());
					/* set mTimer to count from now */
					mTimer.setBase(android.os.SystemClock.elapsedRealtime());
				} else {
					long duration = android.os.SystemClock.elapsedRealtime() - mTimer.getBase();

					mStore.setDuration(mCurrentID, duration);
					/* no current contraction now */
					mLastID = mCurrentID;
					mCurrentID = -1;
				}
				updateUI();
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		Log.v(TAG, "onResume");
		mStore = new ContractionStore(this);

		/* restore state */
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		mCurrentID = settings.getLong("currentID", -1);
		mLastID = settings.getLong("lastID", -1);
		Log.v(TAG, "Restored state from preferences: mCurrentID: " + mCurrentID + " mLastID: " + mLastID);

		/* update our UI */
		updateUI();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		Log.v(TAG, "onPause");
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("currentID", mCurrentID);
		editor.putLong("lastID", mLastID);

		editor.commit();
		Log.v(TAG, "Saved state to preferences: mCurrentID: " + mCurrentID + " mLastID: " + mLastID);
		mStore.close();
	}
}

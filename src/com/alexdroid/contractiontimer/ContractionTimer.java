package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.Button;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class ContractionTimer extends Activity
{
	private static final String TAG = "ContractionTimer";

	private Chronometer mTimer;
	private Button mButton;
	private TextView mPreviousLength, mAverageLength, mPreviousPeriod, mAveragePeriod;
	private ContractionStore mStore;

	private void updateUI()
	{
		long previousLengthMillis = 0;
		long previousPeriodMillis = 0;
		long averageLengthMillis = 0;
		long averagePeriodMillis = 0;
		long numContractions = 0;
		long numPeriods = 0;
		Contraction current = null;
		Contraction previous = null;

		/* get the two most recent contractions */
		ArrayList<Contraction> contractions = mStore.getRecentContractions(0, 2);
		if (contractions.size() > 0) {
			Contraction contraction = contractions.get(0);
			if (contraction.getLengthMillis() == 0) {
				/* we have a current contraction */
				current = contraction;
				if (contractions.size() > 1) {
					previous = contractions.get(1);
				}
			} else {
				previous = contraction;
			}
			if (contractions.size() > 1) {
				previousPeriodMillis = contractions.get(0).getStartMillis() - contractions.get(1).getStartMillis();
			}
		}
		Log.v(TAG, "Updating UI: current = " + current + " previous = " + previous);
		mPreviousLength.setText(previous != null ?
				android.text.format.DateUtils.formatElapsedTime(previous.getLengthMillis() / 1000) :
				null);
		Log.v(TAG, "previousPeriodMillis = " + previousPeriodMillis);
		mPreviousPeriod.setText(previousPeriodMillis > 0 ?
				android.text.format.DateUtils.formatElapsedTime(previousPeriodMillis / 1000) :
				null);

		/* 1 hour is 60 * 60 * 1000 = 3600000 milliseconds */
		long recent = 3600000;
		contractions = mStore.getRecentContractions(java.lang.System.currentTimeMillis() - recent, -1);
		Log.v(TAG, "Calculating averages of contractions which occurred in the last " + recent / 1000 + " seconds: " + contractions.toString());
		Contraction prev = null;
		for (Contraction c : contractions) {
			if (c != current) {
				averageLengthMillis += c.getLengthMillis();
				numContractions++;
			}
			if (prev != null) {
				/* contractions are sorted newest to oldest so prev actually
				 * occurred before c */
				averagePeriodMillis += prev.getStartMillis() - c.getStartMillis();
				numPeriods++;
			}
			prev = c;
		}
		if (numContractions > 0) {
			averageLengthMillis /= numContractions;
		}
		if (numPeriods > 0) {
			averagePeriodMillis /= numPeriods;
		}
		Log.v(TAG, "Calculated averageLengthMillis = " + averageLengthMillis + " averagePeriodMillis = " + averagePeriodMillis);
		mAverageLength.setText(averageLengthMillis > 0 ?
				android.text.format.DateUtils.formatElapsedTime(averageLengthMillis / 1000) :
				null);
		mAveragePeriod.setText(averagePeriodMillis > 0 ?
				android.text.format.DateUtils.formatElapsedTime(averagePeriodMillis / 1000) :
				null);

		if (current != null) {
			/* set time based on start time of current contraction
			*/
			mTimer.setBase(android.os.SystemClock.elapsedRealtime() -
					(java.lang.System.currentTimeMillis() -
					 current.getStartMillis()));
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
		setContentView(R.layout.contraction_timer);

		mAverageLength = (TextView)findViewById(R.id.average_length_value);
		mPreviousLength = (TextView)findViewById(R.id.previous_length_value);
		mAveragePeriod = (TextView)findViewById(R.id.average_period_value);
		mPreviousPeriod = (TextView)findViewById(R.id.previous_period_value);
		mTimer = (Chronometer)findViewById(R.id.timer);
		mButton = (Button)findViewById(R.id.button);

		/* register listener for click events */
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/* see if we are in the middle of a contraction */
				ArrayList<Contraction> contractions = mStore.getRecentContractions(0, 1);
				Contraction contraction = contractions.size() > 0 ? contractions.get(0) : null;
				if (contraction == null ||
					contraction.getLengthMillis() > 0) {
					/* start a contraction at the current
					 * time */
					long id = mStore.startContraction(java.lang.System.currentTimeMillis());
					Log.v(TAG, "Created new contraction " + mStore.getContraction(id));
					/* set mTimer to count from now */
					mTimer.setBase(android.os.SystemClock.elapsedRealtime());
				} else {
					long length = android.os.SystemClock.elapsedRealtime() - mTimer.getBase();

					mStore.setlength(contraction.getID(), length);
					Log.v(TAG, "Set length of contraction " + mStore.getContraction(contraction.getID()));
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

		/* update our UI */
		updateUI();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		Log.v(TAG, "onPause");
		mStore.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contraction_timer, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		/* set state of menu options depending on if have any available
		 * */
		boolean available = ((mStore.getRecentContractions(0, 1)).size() > 0);
		menu.findItem(R.id.list_contractions_menu_item).setEnabled(available);
		menu.findItem(R.id.graph_contractions_menu_item).setEnabled(available);
		menu.findItem(R.id.reset_menu_item).setEnabled(available);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.list_contractions_menu_item:
				startActivity(new Intent(this, ContractionList.class));
				return true;
			case R.id.graph_contractions_menu_item:
				startActivity(new Intent(this, ContractionGraph.class));
				return true;
			case R.id.reset_menu_item:
				mStore.deleteAll();
				updateUI();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}

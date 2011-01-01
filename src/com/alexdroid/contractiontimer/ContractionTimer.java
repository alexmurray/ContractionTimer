package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.ToggleButton;
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
	private static final int N_RECENT = 5;
	private static final long MAX_ACTIVE_PHASE_PERIOD = 4000 * 60;
	private static final long MAX_TRANSITIONAL_PHASE_PERIOD = 2000 * 60;

	private CountDownTimer mCountDownTimer;
	private Chronometer mTimer;
	private ToggleButton mButton;
	private TextView mPhase, mPreviousLength, mPredictedLength, mPreviousPeriod, mPredictedPeriod, mTimerFunction;
	private ContractionStore mStore;

	private void updateUI()
	{
		long previousLengthMillis = 0;
		long previousPeriodMillis = 0;
		long predictedLengthMillis = 0;
		long predictedPeriodMillis = 0;
		Contraction current = null;
		Contraction previous = null;

		/* get the two most recent contractions to find out if have a current
		 * and previous contraction - get in descending order */
		ArrayList<Contraction> contractions = mStore.getRecentContractions(2, false);
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
				DateUtils.formatElapsedTime(previous.getLengthMillis() / 1000) :
				null);
		Log.v(TAG, "previousPeriodMillis = " + previousPeriodMillis);
		mPreviousPeriod.setText(previousPeriodMillis > 0 ?
				DateUtils.formatElapsedTime(previousPeriodMillis / 1000) :
				null);

		/* calculate predicted next contraction length and period from the
		 * N_RECENT most recent contractions - get ascending from oldest to
		 * newest */
		contractions = mStore.getRecentContractions(N_RECENT, true);
		Log.v(TAG, "Calculating predicted next from " + N_RECENT + " most recent contractions " + contractions.toString());
		Contraction prev = null;
		LeastSquaresEstimator lengthEstimator = new LeastSquaresEstimator();
		LeastSquaresEstimator periodEstimator = new LeastSquaresEstimator();
		for (Contraction c : contractions) {
			if (current == null || c.getID() != current.getID()) {
				lengthEstimator.addValue(c.getLengthMillis());
			}
			if (prev != null) {
				/* contractions are sorted oldest to newest */
				periodEstimator.addValue(c.getStartMillis() - prev.getStartMillis());
			}
			prev = c;
		}
		predictedLengthMillis = lengthEstimator.getNext();
		predictedPeriodMillis = periodEstimator.getNext();
		Log.v(TAG, "Calculated predictedLengthMillis = " + predictedLengthMillis + " predictedPeriodMillis = " + predictedPeriodMillis);
		mPredictedLength.setText(predictedLengthMillis > 0 ?
				DateUtils.formatElapsedTime(predictedLengthMillis / 1000) :
				null);
		mPredictedPeriod.setText(predictedPeriodMillis > 0 ?
				DateUtils.formatElapsedTime(predictedPeriodMillis / 1000) :
				null);
		int phaseTextID = (predictedPeriodMillis > MAX_ACTIVE_PHASE_PERIOD ?
				R.string.latent_phase_text :
				predictedPeriodMillis > MAX_TRANSITIONAL_PHASE_PERIOD ?
				R.string.active_phase_text :
				predictedPeriodMillis > 0 ? R.string.transitional_phase_text :
				-1);
		if (phaseTextID > 0) {
			mPhase.setText(phaseTextID);
		} else {
			mPhase.setText(null);
		}

		/* stop any countdown timer */
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
			mCountDownTimer = null;
		}

		if (current != null) {
			/* set time based on start time of current contraction */
			mTimerFunction.setText(R.string.countup_label_text);
			mButton.setChecked(true);
			mTimer.setBase(android.os.SystemClock.elapsedRealtime() -
					(System.currentTimeMillis() -
					 current.getStartMillis()));
			mTimer.start();
		} else {
			/* not having a contraction so set button to unchecked and stop any
			 * timer */
			mButton.setChecked(false);
			mTimer.stop();
			/* use mTimer to display the amount of time till next contraction */
			if (predictedPeriodMillis > 0) {
				long nextContractionMillis = predictedPeriodMillis - 
					(System.currentTimeMillis() - previous.getStartMillis());
				mTimerFunction.setText(R.string.countdown_label_text);
				mTimer.setText(DateUtils.formatElapsedTime(nextContractionMillis / 1000));
				/* create a countdown timer to update the value of the timer
				 * for us to show how long till next contraction */
				mCountDownTimer = new CountDownTimer(nextContractionMillis, 1000) {
					public void onTick(long millisUntilFinished) {
						mTimer.setText(DateUtils.formatElapsedTime(millisUntilFinished / 1000));
					}
					public void onFinish() {
						/* use default hint when nothing to display */
						mTimerFunction.setText(null);
						mTimer.setText(null);
					}
				}.start();
			} else {
				/* use default hint when nothing to display */
				mTimerFunction.setText(null);
				mTimer.setText(null);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contraction_timer);

		mPhase = (TextView)findViewById(R.id.phase_label);
		mPredictedLength = (TextView)findViewById(R.id.predicted_length_value);
		mPreviousLength = (TextView)findViewById(R.id.previous_length_value);
		mPredictedPeriod = (TextView)findViewById(R.id.predicted_period_value);
		mPreviousPeriod = (TextView)findViewById(R.id.previous_period_value);
		mTimerFunction = (TextView)findViewById(R.id.timer_function_label);
		mTimer = (Chronometer)findViewById(R.id.timer);
		mButton = (ToggleButton)findViewById(R.id.button);

		/* register listener for click events */
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/* see if we are in the middle of a contraction */
				ArrayList<Contraction> contractions = mStore.getRecentContractions(1, false);
				Contraction contraction = contractions.size() > 0 ? contractions.get(0) : null;
				Log.v(TAG, "Button click: contraction: " + contraction);
				if (contraction == null ||
					contraction.getLengthMillis() > 0) {
					/* start a contraction at the current
					 * time */
					long id = mStore.startContraction(System.currentTimeMillis());
					Log.v(TAG, "Created new contraction " + mStore.getContraction(id));
					/* set mTimer to count from now */
					mTimer.setBase(android.os.SystemClock.elapsedRealtime());
				} else {
					long length = android.os.SystemClock.elapsedRealtime() - mTimer.getBase();

					mStore.setLength(contraction.getID(), length);
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
		boolean available = ((mStore.getRecentContractions(1, false)).size() > 0);
		menu.findItem(R.id.list_contractions_menu_item).setEnabled(available);
		menu.findItem(R.id.graph_contractions_menu_item).setEnabled(available);
		menu.findItem(R.id.reset_menu_item).setEnabled(available);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return this.onCreateDialog(id, null);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog = null;
		switch (id) {
			case 0:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.confirm_reset_text)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mStore.deleteAll();
							updateUI();
						}
					})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				dialog = builder.create();
				break;

			default:
				Log.e(TAG, "Unknown dialog id " + id);
		}
		Log.v(TAG, "Created dialog for id " + id);
		return dialog;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.list_contractions_menu_item:
				startActivity(new Intent(this, ContractionList.class));
				return true;
			case R.id.graph_contractions_menu_item:
				startActivity(new Intent(this, ContractionGrapher.class));
				return true;
			case R.id.reset_menu_item:
				showDialog(0);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}

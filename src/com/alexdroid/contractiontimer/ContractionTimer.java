package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
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

	private CountdownWidget mCountdownTimer;
	private Chronometer mCountupTimer;
	private ToggleButton mButton;
	private TextView mPhase, mPreviousLength, mPredictedLength, mPreviousInterval, mPredictedInterval;
	private ContractionStore mStore;

	private void updateUI() {
		long previousLengthMillis = 0;
		long previousIntervalMillis = 0;
		long predictedLengthMillis = 0;
		long predictedIntervalMillis = 0;
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
				previousIntervalMillis = contractions.get(0).getStartMillis() - contractions.get(1).getStartMillis();
			}
		}

		Log.v(TAG, "Updating UI: current = " + current + " previous = " + previous);
		mPreviousLength.setText(previous != null ?
					DateUtils.formatElapsedTime(previous.getLengthMillis() / 1000) :
					null);
		Log.v(TAG, "previousIntervalMillis = " + previousIntervalMillis);
		mPreviousInterval.setText(previousIntervalMillis > 0 ?
					DateUtils.formatElapsedTime(previousIntervalMillis / 1000) :
					null);

		/* calculate predicted next contraction length and interval from the
		 * N_RECENT most recent contractions - get ascending from oldest to
		 * newest */
		contractions = mStore.getRecentContractions(N_RECENT, true);
		Log.v(TAG, "Calculating predicted next from " + N_RECENT + " most recent contractions " + contractions.toString());
		Contraction prev = null;
		LeastSquaresEstimator lengthEstimator = new LeastSquaresEstimator();
		LeastSquaresEstimator intervalEstimator = new LeastSquaresEstimator();
		for (Contraction c : contractions) {
			if (current == null || c.getID() != current.getID()) {
				lengthEstimator.addValue(c.getLengthMillis());
			}
			if (prev != null) {
				/* contractions are sorted oldest to newest */
				intervalEstimator.addValue(c.getStartMillis() - prev.getStartMillis());
			}
			prev = c;
		}
		predictedLengthMillis = lengthEstimator.getNext();
		predictedIntervalMillis = intervalEstimator.getNext();
		Log.v(TAG, "Calculated predictedLengthMillis = " + predictedLengthMillis + " predictedIntervalMillis = " + predictedIntervalMillis);
		mPredictedLength.setText(predictedLengthMillis > 0 ?
					 DateUtils.formatElapsedTime(predictedLengthMillis / 1000) :
					 null);
		mPredictedInterval.setText(predictedIntervalMillis > 0 ?
					 DateUtils.formatElapsedTime(predictedIntervalMillis / 1000) :
					 null);
		int phaseTextID = (predictedIntervalMillis > MAX_ACTIVE_PHASE_PERIOD ?
				   R.string.latent_phase_text :
				   predictedIntervalMillis > MAX_TRANSITIONAL_PHASE_PERIOD ?
				   R.string.active_phase_text :
				   predictedIntervalMillis > 0 ? R.string.transitional_phase_text :
				   -1);
		if (phaseTextID > 0) {
			mPhase.setText(phaseTextID);
		} else {
			mPhase.setText(null);
		}

		if (current != null) {
			/* set time based on start time of current contraction */
			mButton.setChecked(true);
			mCountdownTimer.stop();
			mCountdownTimer.setTime(SystemClock.elapsedRealtime());
			mCountdownTimer.setText(null);
			mCountupTimer.setBase(SystemClock.elapsedRealtime() -
				       (System.currentTimeMillis() -
					current.getStartMillis()));
			mCountupTimer.start();
		} else {
			/* not having a contraction so set button to unchecked and stop any
			 * timer */
			mButton.setChecked(false);
			mCountupTimer.stop();
			mCountupTimer.setBase(SystemClock.elapsedRealtime());
			mCountupTimer.setText(null);
			if (predictedIntervalMillis > 0) {
				long nextContractionMillis = predictedIntervalMillis -
					(System.currentTimeMillis() - previous.getStartMillis());
				mCountdownTimer.setTime(SystemClock.elapsedRealtime() +
							nextContractionMillis);
				mCountdownTimer.start();
			} else {
				mCountdownTimer.stop();
				mCountdownTimer.setTime(SystemClock.elapsedRealtime());
				mCountdownTimer.setText(null);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contraction_timer);

		mPhase = (TextView)findViewById(R.id.phase_label);
		mPredictedLength = (TextView)findViewById(R.id.predicted_length_value);
		mPreviousLength = (TextView)findViewById(R.id.previous_length_value);
		mPredictedInterval = (TextView)findViewById(R.id.predicted_interval_value);
		mPreviousInterval = (TextView)findViewById(R.id.previous_interval_value);
		mCountdownTimer = (CountdownWidget)findViewById(R.id.countdown_timer);
		mCountupTimer = (Chronometer)findViewById(R.id.countup_timer);
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
						/* set mCountupTimer to count from now */
						mCountupTimer.setBase(SystemClock.elapsedRealtime());
					} else {
						long length = (SystemClock.elapsedRealtime()-
							       mCountupTimer.getBase());

						mStore.setLength(contraction.getID(), length);
						Log.v(TAG, "Set length of contraction " + mStore.getContraction(contraction.getID()));
					}
					updateUI();
				}
			}
			);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.v(TAG, "onResume");
		mStore = new ContractionStore(this);

		/* update our UI */
		updateUI();
	}

	@Override
	protected void onPause() {
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
		menu.findItem(R.id.undo_menu_item).setEnabled(available);
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
		case R.id.undo_menu_item:
			/* either delete most recent contraction if has no
			   duration or set duration 0 to undo last action */
			ArrayList<Contraction> contractions = mStore.getRecentContractions(1,
											   false);
			if (contractions.size() > 0) {
				Contraction contraction = contractions.get(0);
				if (contraction.getLengthMillis() > 0) {
					mStore.setLength(contraction.getID(), 0);
				} else {
					mStore.delete(contraction.getID());
				}
				updateUI();
			}
			return true;
		case R.id.reset_menu_item:
			showDialog(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

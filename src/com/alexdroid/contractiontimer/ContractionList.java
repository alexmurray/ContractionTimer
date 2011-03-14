package com.alexdroid.contractiontimer;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class ContractionList extends ListActivity {
	private static final String TAG = "ContractionList";

	@Override
	protected void onResume() {
		super.onResume();

		Log.v(TAG, "onResume()");
		ContractionStore store = new ContractionStore(this);

		setListAdapter(new ContractionArrayAdapter(this, R.layout.contraction_list_item,
							   store.getAllContractions()));
		/* close store since are done with it now */
		store.close();
	}

	/* provide a custom ArrayAdapter to view Contraction data more easily */
	private class ContractionArrayAdapter extends ArrayAdapter<Contraction> {
		private ArrayList<Contraction> contractions;

		public ContractionArrayAdapter(Context context, int textViewResourceId, ArrayList<Contraction> contractions) {
			super(context, textViewResourceId, contractions);
			this.contractions = contractions;
		}


		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater inf = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inf.inflate(R.layout.contraction_list_item, null);
			}
			Contraction contraction = contractions.get(position);
			if (contraction != null) {
				TextView start = (TextView)v.findViewById(R.id.contraction_start_view);
				TextView length = (TextView)v.findViewById(R.id.contraction_length_view);
				TextView interval = (TextView)v.findViewById(R.id.contraction_interval_view);
				if (start != null) {
					start.setText(getString(R.string.start_label_text) +
						      "\n" +
						      DateUtils.formatDateTime(this.getContext(),
									       contraction.getStartMillis(),
									       DateUtils.FORMAT_SHOW_TIME |
									       DateUtils.FORMAT_SHOW_DATE |
									       DateUtils.FORMAT_NUMERIC_DATE));
				}
				if (length != null) {
					length.setText(getString(R.string.length_label_text) +
						       "\n" +
						       DateUtils.formatElapsedTime(contraction.getLengthMillis() / 1000));
				}
				if (interval != null) {
					Contraction previous = null;
					long intervalSecs = 0;
					if (position > 0) {
						previous = contractions.get(position - 1);
					}
					if (previous != null) {
						intervalSecs = (contraction.getStartMillis() -
							    previous.getStartMillis()) / 1000;
					}
					interval.setText(getString(R.string.interval_label_text) +
							 "\n" +
							 DateUtils.formatElapsedTime(intervalSecs));
				}
			}
			return v;
		}
	}


}

package com.alexdroid.contractiontimer;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.util.Log;
import java.util.ArrayList;

public class ContractionList extends ListActivity {
	private static final String TAG = "ContractionList";
	private ContractionStore mStore;

	@Override
	protected void onResume() {
		super.onResume();

		Log.v(TAG, "onResume()");
		mStore = new ContractionStore(this);

		ArrayList<Contraction> contractions = mStore.getAllContractions();
		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.contraction_list_item,
				contractions);
		setListAdapter(adapter);
	}


	@Override
	protected void onPause() {
		super.onPause();

		Log.v(TAG, "onPause()");
		mStore.close();
	}
}
	

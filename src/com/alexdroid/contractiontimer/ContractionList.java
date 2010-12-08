package com.alexdroid.contractiontimer;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.util.Log;
import java.util.ArrayList;

public class ContractionList extends ListActivity {
	private static final String TAG = "ContractionList";

	@Override
	protected void onResume() {
		super.onResume();

		Log.v(TAG, "onResume()");
		ContractionStore store = new ContractionStore(this);

		/* create an array adapter to adapt from the contractions list to the
		 * list view */
		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.contraction_list_item,
				store.getAllContractions());
		/* close store since are done with it now */
		store.close();
		setListAdapter(adapter);
	}
}
	

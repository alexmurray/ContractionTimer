package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.os.Bundle;

public class ContractionGraph extends Activity {
	private ContractionGraphView mGraphView;
	private ContractionStore mStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contractiongraph);

		mGraphView = (ContractionGraphView)findViewById(R.id.contraction_graph_view);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStore = new ContractionStore(this);
		mGraphView.setContractions(mStore.getAllContractions());
	}

	@Override
	protected void onPause() {
		super.onPause();
		mStore.close();
	}
}

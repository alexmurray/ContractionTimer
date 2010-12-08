package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.os.Bundle;

public class ContractionGraph extends Activity {
	private ContractionGraphView mGraphView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contraction_graph);

		mGraphView = (ContractionGraphView)findViewById(R.id.contraction_graph_view);
		ContractionStore store = new ContractionStore(this);
		mGraphView.setContractions(store.getAllContractions());
		store.close();
	}
}

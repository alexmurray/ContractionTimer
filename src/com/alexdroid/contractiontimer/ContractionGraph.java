package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.os.Bundle;

public class ContractionGraph extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.contraction_graph);

		ContractionStore store = new ContractionStore(this);
		ContractionGraphView graphView = (ContractionGraphView)findViewById(R.id.contraction_graph_view);
		graphView.setContractions(store.getAllContractions());
		store.close();
	}
}

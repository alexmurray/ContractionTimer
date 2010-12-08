package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ZoomControls;

public class ContractionGraph extends Activity {
	private ContractionGraphView mGraphView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.contraction_graph);

		ContractionStore store = new ContractionStore(this);
		mGraphView = (ContractionGraphView)findViewById(R.id.contraction_graph_view);
		mGraphView.setContractions(store.getAllContractions());
		store.close();

		ZoomControls controls = (ZoomControls)findViewById(R.id.zoom_controls);
		controls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mGraphView.setResolution(mGraphView.getResolution() * (float)2.0);
			}
		});
		controls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mGraphView.setResolution(mGraphView.getResolution() / (float)2.0);
			}
		});
	}
}

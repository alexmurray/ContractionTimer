package com.alexdroid.contractiontimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomControls;

public class ContractionGraph extends Activity {
	private ContractionGraphView mGraphView;
	private ZoomControls mControls;
	private CountDownTimer mTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.contraction_graph);

		mGraphView = (ContractionGraphView)findViewById(R.id.contraction_graph_view);
		mControls = (ZoomControls)findViewById(R.id.zoom_controls);

		ContractionStore store = new ContractionStore(this);
		mGraphView.setContractions(store.getAllContractions());
		store.close();

		/* display zoom controls when graph view is touched */
		mGraphView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				/* show controls and hide in 5 seconds */
				boolean enableZoomIn = mGraphView.getCanZoomIn();
				boolean enableZoomOut = mGraphView.getCanZoomOut();
				if (!(enableZoomIn || enableZoomOut)) {
					/* neither can zoom so don't bother showing controls */
					return false;
				}
				mControls.setIsZoomInEnabled(enableZoomIn);
				mControls.setIsZoomOutEnabled(enableZoomOut);
				mControls.show();
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				/* hide zoom controls after 5 seconds */
				mTimer = new CountDownTimer(5000, 5000) {
					public void onTick(long millisUntilFinished) {
						/* nothing to do */
					}
					public void onFinish() {
						mControls.hide();
					}
				}.start();
				/* let other handlers see this event */
				return false;
			}
		});

		mControls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mGraphView.zoom(2.0);
				mControls.setIsZoomInEnabled(mGraphView.getCanZoomIn());
				mControls.setIsZoomOutEnabled(mGraphView.getCanZoomOut());
			}
		});
		mControls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mGraphView.zoom(0.5);
				mControls.setIsZoomInEnabled(mGraphView.getCanZoomIn());
				mControls.setIsZoomOutEnabled(mGraphView.getCanZoomOut());
			}
		});
		mControls.hide();
	}
}

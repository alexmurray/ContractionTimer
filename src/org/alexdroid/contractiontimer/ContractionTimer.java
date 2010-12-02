package org.alexdroid.contractiontimer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.Button;
import android.view.View;

public class ContractionTimer extends Activity
{
	private boolean running = false;
	private Chronometer timer;
	private Button button;
	private TextView lastTime;

	/** Called when the activity is first created. */
	@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);

			lastTime = (TextView)findViewById(R.id.last_time_value);
			timer = (Chronometer)findViewById(R.id.timer);
			button = (Button)findViewById(R.id.button);

			// init button with correct text for current state
			button.setText(running ? R.string.stop_text : R.string.start_text);

			// register listener for click events
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					running = !running;
					if (running) {
						timer.setBase(android.os.SystemClock.elapsedRealtime());
						timer.start();
						button.setText(R.string.stop_text);
					} else {
						timer.stop();
						lastTime.setText(timer.getText());
						timer.setBase(android.os.SystemClock.elapsedRealtime());
						button.setText(R.string.start_text);
					}
				}
			});
		}
}

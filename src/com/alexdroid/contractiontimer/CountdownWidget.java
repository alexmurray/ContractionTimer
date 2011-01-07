package com.alexdroid.contractiontimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Class (based off of Android Chronometer) which implements a count-down timer
 *(as opposed to a count-up timer)
 */

 public class CountdownWidget extends TextView {
	 private static final String TAG = "CountdownWidget";

	 private long mTime;
	 private boolean mVisible;
	 private boolean mStarted;
	 private boolean mRunning;
	 private StringBuilder mRecycle = new StringBuilder(8);
	 private static final int TICK_WHAT = 2;

	 public CountdownWidget(Context context, AttributeSet attrs) {
		 super(context, attrs);
		 init();
	 }

	 private void init() {
		 mTime = SystemClock.elapsedRealtime();
		 updateText(mTime);
	 }

	 public void setTime(long time) {
		 mTime = time;
		 updateText(SystemClock.elapsedRealtime());
	 }

	 public long getTime() {
		 return mTime;
	 }

	 public void start() {
		 mStarted = true;
		 updateRunning();
	 }

	 public void stop() {
		 mStarted = false;
		 updateRunning();
	 }

	 @Override
	 protected void onWindowVisibilityChanged(int visibility) {
		 super.onWindowVisibilityChanged(visibility);
		 mVisible = (visibility == VISIBLE);
		 updateRunning();
	 }

	 private synchronized void updateText(long now) {
		 long seconds = Math.max(0, (mTime - now) / 1000);
		 setText(DateUtils.formatElapsedTime(mRecycle, seconds));
	 }

	 private void updateRunning() {
		 boolean running = (mVisible && mStarted);
		 if (running != mRunning) {
			 if (running) {
				 updateText(SystemClock.elapsedRealtime());
				 mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT),
							    1000);
			 } else {
				 mHandler.removeMessages(TICK_WHAT);
			 }
			 mRunning = running;
		 }
	 }

	 private Handler mHandler = new Handler() {
			 public void handleMessage(Message m) {
				 if (mRunning) {
					 updateText(SystemClock.elapsedRealtime());
					 sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
				 }
			 }
		 };
 }

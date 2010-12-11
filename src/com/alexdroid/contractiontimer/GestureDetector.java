package com.alexdroid.contractiontimer;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public abstract class GestureDetector {
	private static final String TAG = "GestureDetector";

	OnGestureListener mListener;

	public static GestureDetector newInstance(Context context,
			OnGestureListener listener) {
		final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		GestureDetector detector = null;
		if (sdkVersion >= Build.VERSION_CODES.FROYO) {
			detector = new FroyoDetector(context);
			detector.mListener = listener;
		}

		return detector;
	}

	public abstract boolean onTouchEvent(MotionEvent ev);

	public interface OnGestureListener {
		public void onScale(float scaleFactor);
	}

	private static class FroyoDetector extends GestureDetector {
		private ScaleGestureDetector mDetector;

		public FroyoDetector(Context context) {
			mDetector = new ScaleGestureDetector(context,
					new ScaleGestureDetector.SimpleOnScaleGestureListener() {
						@Override
					   	public boolean onScale(ScaleGestureDetector detector) {
							mListener.onScale(detector.getScaleFactor());
							return true;
						}
					});
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			mDetector.onTouchEvent(ev);
			return true;
		}
	}
}

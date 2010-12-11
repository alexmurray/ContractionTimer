package com.alexdroid.contractiontimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.Path;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class ContractionGraphView extends View {
	private static final String TAG = "ContractionGraphView";
	private static final int MIN_WIDTH = 240;
	private static final double MAX_RESOLUTION = 0.05;

	private GestureDetector mDetector;
	private ShapeDrawable mDrawable;
	private ArrayList<Contraction> mContractions;
	private long mMaxLengthMillis;
	private long mMinMillis;
	private long mMaxMillis;
	/* dp per millisecond */
	private double mResolution = MAX_RESOLUTION / 5.0;
	private boolean mCanZoomIn = mResolution < MAX_RESOLUTION;
	private boolean mCanZoomOut = false;

	public ContractionGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDrawable = new ShapeDrawable();

		mDetector = GestureDetector.newInstance(context, new GestureCallback());

		/* get and set our custom parameters */
		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ContractionGraphView);
		mDrawable.getPaint().setColor(array.getColor(R.styleable.ContractionGraphView_graphColor, 0xff74AC23));
		array.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/* use super to handle this but then override with our required width */
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		/* if super class didn't set a width, then set our own - but if did
		 * then respect it by recalculating mResolution */
		if (getMeasuredWidth() == 0) {
			/* set our width depending on size - ensure we aren't smaller than
			 * MIN_WIDTH */
			int w = Math.max(MIN_WIDTH, (int)((mMaxMillis - mMinMillis) * mResolution));
			mCanZoomOut = (w > MIN_WIDTH);
			mCanZoomIn = (mResolution < MAX_RESOLUTION);
			/* use super's value of measured height */
			int h = getMeasuredHeight();
			setMeasuredDimension(w, h);
		} else {
			mResolution = (float)getMeasuredWidth() / (mMaxMillis - mMinMillis);
			mCanZoomIn = false;
			mCanZoomOut = false;
		}
		Log.v(TAG, "onMeasure: measuredWidth = " + getWidth() + " measuredHeight = " + getMeasuredHeight() + " mResolution = " + mResolution);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mContractions == null) {
			return;
		}
		int w = getWidth();
		int h = getHeight();

		float w_scale = (float)w / (float)(mMaxMillis - mMinMillis);
		float h_scale = (float)h / (float)mMaxLengthMillis;

		/* draw contractions into path */
		Path path = new Path();
		for (Contraction contraction : mContractions) {
			float start = (contraction.getStartMillis() - mMinMillis) * w_scale;
			float length = (contraction.getLengthMillis() * w_scale);
			float height = contraction.getLengthMillis() * h_scale;

			path.moveTo(start, (float)h);
			path.cubicTo(start + (length / 3),  height / 2,
					start + (2 * length / 3), height / 2,
					start + length, (float)h);
			path.close();
		}

		mDrawable.setBounds(0, 0, w, h);
		mDrawable.setShape(new PathShape(path, w, h));
		mDrawable.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mDetector != null) {
			mDetector.onTouchEvent(ev);
		}
		return true;
	}

	public void setContractions(ArrayList<Contraction> contractions) {
		mContractions = contractions;

		mMinMillis = Long.MAX_VALUE;
		mMaxMillis = 0;
		mMaxLengthMillis = 0;
		for (Contraction contraction : mContractions) {
			long lengthMillis = contraction.getLengthMillis();
			long minMillis = contraction.getStartMillis();
			long maxMillis = minMillis + lengthMillis;
			if (lengthMillis > mMaxLengthMillis) {
				mMaxLengthMillis = lengthMillis;
			}
			if (minMillis < mMinMillis) {
				mMinMillis = minMillis;
			}
			if (maxMillis > mMaxMillis) {
				mMaxMillis = maxMillis;
			}
		}
		/* update resolution to ensure we aren't smaller than MIN_WIDTH */
		mResolution = Math.max(mResolution, (double)MIN_WIDTH / (double)(mMaxMillis - mMinMillis));
		Log.v(TAG, "setContractions: mMinMillis = " + mMinMillis + " mMaxMillis = " + mMaxMillis);
		/* schedule relayout and hence redraw */
		requestLayout();
	}

	public void zoom(double scale) {
		if (mCanZoomIn && scale > 1.0 || mCanZoomOut && scale < 1.0) {
			double minResolution = (double)MIN_WIDTH / (double)(mMaxMillis - mMinMillis);
			mResolution = Math.max(mResolution * scale, minResolution);
			mCanZoomOut = (mResolution > minResolution);
			mCanZoomIn = (mResolution < MAX_RESOLUTION);
			requestLayout();
		}
	}

	public boolean getCanZoomIn() {
		return mCanZoomIn;
	}

	public boolean getCanZoomOut() {
		return mCanZoomOut;
	}

	private class GestureCallback implements GestureDetector.OnGestureListener {
		public void onScale(float scaleFactor) {
			zoom(scaleFactor);
		}
	}
}

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
import android.widget.HorizontalScrollView;
import java.util.ArrayList;

public class ContractionGraphView extends HorizontalScrollView {
	private static final String TAG = "ContractionGraphView";
	private GestureDetector mDetector;
	private ContractionGraph mGraph;

	public ContractionGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDetector = GestureDetector.newInstance(context, new GestureCallback());
		mGraph = new ContractionGraph(context);

		/* get and set custom parameters */
		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ContractionGraphView);
		mGraph.setColor(array.getColor(R.styleable.ContractionGraphView_graphColor, 0xff74AC23));
		int textSize = array.getDimensionPixelOffset(R.styleable.ContractionGraphView_textSize, 0);
		if (textSize > 0) {
			mGraph.setTextSize(textSize);
		}
		array.recycle();
		this.addView(mGraph);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean ret = super.onTouchEvent(ev);
		if (mDetector != null) {
			ret |= mDetector.onTouchEvent(ev);
		}
		return ret;
	}

	public void setContractions(ArrayList<Contraction> contractions) {
		mGraph.setContractions(contractions);
	}

	public boolean getCanZoomIn() {
		return mGraph.getCanZoomIn();
	}

	public boolean getCanZoomOut() {
		return mGraph.getCanZoomOut();
	}

	public void zoom(double scale) {
		mGraph.zoom(scale);
	}

	private class GestureCallback implements GestureDetector.OnGestureListener {
		public void onScale(float scaleFactor) {
			mGraph.zoom(scaleFactor);
		}
	}

	private class ContractionGraph extends View {
		private static final String TAG = "ContractionGraph";

		private static final int MIN_WIDTH = 240;
		/* maximum resolution of 0.05 pixels per millisecond or conversely 1 pixel
		 * per 20 milliseconds */
		private static final double MAX_RESOLUTION = 0.05;

		private ShapeDrawable mDrawable;
		private ArrayList<Contraction> mContractions;
		private long mMaxLengthMillis;
		private long mMinMillis;
		private long mMaxMillis;
		/* dp per millisecond */
		private double mResolution = MAX_RESOLUTION / 5.0;
		private boolean mCanZoomIn = mResolution < MAX_RESOLUTION;
		private boolean mCanZoomOut = false;

		public ContractionGraph(Context context) {
			super(context);
			mDrawable = new ShapeDrawable();
		}

		public void setColor(int color) {
			mDrawable.getPaint().setColor(color);
		}

		public void setTextSize(int textSize) {
			mDrawable.getPaint().setTextSize(textSize);
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

		private void drawTimeAxisLabels(Canvas canvas, int w, int h) {
			/* define some static ranges of resolutions to know what scale to use
			 * */
			final int SECONDS = 0;
			final int MINUTES = 1;
			final int HOURS = 2;
			final int DAYS = 3;
			final double RANGES[] = {
				(double)w / 1000.0f,
				(double)w / (60 * 1000.0f),
				(double)w / (60 * 60 * 1000.0f),
				(double)w / (24 * 60 * 60 * 1000.0f),
			};

			for (int i = 0; i <= DAYS; i++) {
				if (RANGES[i] > 1.0 || i == DAYS) {
					canvas.drawText("Resolution: " + mResolution + " < " + 
							RANGES[i] + " Range: " + i , 0, h, mDrawable.getPaint());
					break;
				}
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mContractions == null) {
				return;
			}
			int w = getWidth();
			int h = getHeight();
			int fontHeight = mDrawable.getPaint().getFontMetricsInt(null);

			/* remove font spacing from height */
			h -= fontHeight;

			drawTimeAxisLabels(canvas, w, h);

			/* now shift up again */
			h -= fontHeight;

			float w_scale = (float)w / (float)(mMaxMillis - mMinMillis);
			float h_scale = (float)h / (float)mMaxLengthMillis;

			Path path = new Path();

			/* draw contractions into path */
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

	}
}

package com.alexdroid.contractiontimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.text.format.DateFormat;
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
		/* maximum resolution of 0.05 pixels per millisecond or conversely 1
		 * pixel per 20 milliseconds */
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
			mDrawable.getPaint().setTextAlign(Paint.Align.CENTER);
		}

		public void setColor(int color) {
			mDrawable.getPaint().setColor(color);
		}

		public void setTextSize(int textSize) {
			mDrawable.getPaint().setTextSize(textSize);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			/* use super to handle this but then override with our required
			 * width */
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			/* if super class didn't set a width, then set our own - but if did
			 * then respect it by recalculating mResolution */
			if (getMeasuredWidth() == 0) {
				/* set our width depending on size - ensure we aren't smaller
				 * than MIN_WIDTH */
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

		private int drawTimeAxisLabels(Canvas canvas, int fontHeight,
				int w, int h) {
			/* define some time constants to know what scale to use */
			final int TIME_SCALE_SECONDS = 0;
			final int TIME_SCALE_MINUTES = 1;
			final int TIME_SCALE_HOURS = 2;
			final int TIME_SCALE_DAYS = 3;
			final int NUM_TIME_SCALES = 4;
			/* length of each time unit in milliseconds */
			final int LENGTHS[] = {
				1000,
				60 * 1000,
				60 * 60 * 1000,
				24 * 60 * 60 * 1000,
			};
			final String FORMATS[] = {
				"kk:mm:ss",
				"kk:mm",
				"kk:00",
				"E",
			};
			int dh = 0;

			Log.v(TAG, "mResolution: " + mResolution);
			for (int i = 0; i < NUM_TIME_SCALES; i++) {
				/* draw ticks for each time scale but only if resolution is
				 * okay - i.e. if ticks will be more than 5 pixels apart then
				 * draw */
				double dp = mResolution * LENGTHS[i];

				if (dp > 5.0) {
					/* round up to next unit */
					long ms = (mMinMillis + LENGTHS[i]);
					float tickLength = (float)fontHeight / (NUM_TIME_SCALES - i);
					dh = fontHeight + (int)Math.ceil(tickLength);
					ms -= (ms % LENGTHS[i]);

					/* convert ms to dp */
					double x = (ms - mMinMillis) * mResolution;
					while (x < w) {
						canvas.drawLine((float)x, (float)(h - dh),
								(float)x, (float)(h - dh) + tickLength,
								mDrawable.getPaint());
						/* draw labels if can fit in between each tick */
						if (dp > mDrawable.getPaint().measureText(FORMATS[i])) {
							canvas.drawText((String)DateFormat.format(FORMATS[i], ms),
								   	(float)x, (float)h, mDrawable.getPaint());
						}
						ms += LENGTHS[i];
						x = (ms - mMinMillis) * mResolution;
					}
					break;
				}
			}
			/* return the height we drew into */
			return dh;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mContractions == null) {
				return;
			}
			int w = getWidth();
			int h = getHeight();
			int fontHeight = mDrawable.getPaint().getFontMetricsInt(null);

			int dh = drawTimeAxisLabels(canvas, fontHeight, w, h);

			/* now shift up again */
			h -= dh;

			float w_scale = (float)w / (float)(mMaxMillis - mMinMillis);

			Path path = new Path();

			/* draw contractions into path */
			for (Contraction contraction : mContractions) {
				float start = (contraction.getStartMillis() - mMinMillis) * w_scale;
				float length = (contraction.getLengthMillis() * w_scale);
				float height = ((float)contraction.getLengthMillis() / (float)mMaxLengthMillis) * h;
				Log.v(TAG, "start: " + start + " height: " + height);

				path.moveTo(start, (float)h);
				path.cubicTo(start + (length / 3),  h - height,
						start + (2 * length / 3), h - height,
						start + length, (float)h);
				path.close();
			}

			mDrawable.setBounds(0, 0, w, h);
			mDrawable.setShape(new PathShape(path, w, h));
			mDrawable.draw(canvas);

			/* draw x axis */
			canvas.drawLine(0.f, (float)h, (float)w, (float)h, mDrawable.getPaint());
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

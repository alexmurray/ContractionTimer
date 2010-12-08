package com.alexdroid.contractiontimer;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.Path;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;

public class ContractionGraphView extends View {
	private static final String TAG = "ContractionGraphView";

	private ShapeDrawable mDrawable;
	private ArrayList<Contraction> mContractions;
	private long mMaxLengthMillis;
	private long mMinMillis;
	private long mMaxMillis;
	/* dp per millisecond */
	private float mResolution = 0.01f;

	public ContractionGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDrawable = new ShapeDrawable();
		mDrawable.getPaint().setColor(0xff74AC23);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		/* set our width depending on size */
		int w = (int)((mMaxMillis - mMinMillis) * mResolution);
		int h = getMeasuredHeight();
		Log.v(TAG, "onMeasure: w = " + w + " h = " + h);
		setMeasuredDimension(w, h);
		mDrawable.setBounds(0, 0, w, h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mContractions == null) {
			return;
		}
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		Log.v(TAG, "onDraw: w = " + w + " h = " + h);

		float w_scale = (float)w / (float)(mMaxMillis - mMinMillis);
		float h_scale = (float)h / (float)mMaxLengthMillis;

		/* draw contractions into path */
		Path path = new Path();
		for (Contraction contraction : mContractions) {
			float start = (contraction.getStartMillis() - mMinMillis) * w_scale;
			float length = (contraction.getLengthMillis() * w_scale);
			float height = contraction.getLengthMillis() * h_scale;

			Log.v(TAG, "contraction " + contraction + " start: " + start + " length: " + length + " height: " + height);
			path.moveTo(start, (float)h);
			path.cubicTo(start + (length / 3),  height / 2,
					start + (2 * length / 3), height / 2,
					start + length, (float)h);
			path.close();
		}

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
		Log.v(TAG, "setContractions: mMinMillis = " + mMinMillis + " mMaxMillis = " + mMaxMillis);
		/* schedule relayout and hence redraw */
		requestLayout();
	}

	public void setResolution(float resolution) {
		mResolution = resolution;
		requestLayout();
	}

	public float getResolution() {
		return mResolution;
	}
}

package com.alexdroid.contractiontimer;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.Path;
import android.graphics.Canvas;
import android.view.View;
import java.util.ArrayList;

public class ContractionGraphView extends View {
	private static final String TAG = "ContractionGraphView";

	private Path mPath;
	private PathShape mShape;
	private ShapeDrawable mDrawable;

	public ContractionGraphView(Context context) {
		super(context);

		mPath = new Path();
		mDrawable = new ShapeDrawable(new PathShape(mPath, getWidth(), getHeight()));
	}

	protected void onDraw(Canvas canvas) {
		mDrawable.draw(canvas);
	}

	public void setContractions(ArrayList<Contraction> contractions) {
		/* do something with contractions - update path */
	}
}

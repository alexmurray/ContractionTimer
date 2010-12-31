package com.alexdroid.contractiontimer;

import java.util.ArrayList;

public class LeastSquaresEstimator {
	private long next;
	private long n;
	private long sumX, sumY, sumXX, sumXY;
	private boolean dirty;

	public void addValue(long value) {
		sumX += n;
		sumXX += n * n;
		sumY += value;
		sumXY += n * value;
		n++;
		dirty = true;
	}

	public long getNext() {
		if (dirty && n > 1) {
			long m = ((n * sumXY) - (sumX * sumY)) /
				((n * sumXX) - (sumX * sumX));
			long c = (sumY - (m * sumX)) / n;
			next = (m * n) + c;
			dirty = false;
		}
		return next;
	}
}


package com.alexdroid.contractiontimer;

import android.text.format.DateUtils;
import java.text.DateFormat;

public class Contraction {
	private long mId;
	private long mStartMillis;
	private long mLengthMillis;

	public Contraction(long id, long startMillis, long lengthMillis)
	{
		mId = id;
		mStartMillis = startMillis;
		mLengthMillis = lengthMillis;
	}

	public long getID()
	{
		return mId;
	}

	public long getStartMillis()
	{
		return mStartMillis;
	}

	public long getLengthMillis()
	{
		return mLengthMillis;
	}

	public String toString()
	{
		return "[" + DateFormat.getDateTimeInstance().format(mStartMillis) + "]: " + 

			DateUtils.formatElapsedTime(mLengthMillis / 1000);
	}
}


package com.alexdroid.contractiontimer;

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
		return android.text.format.DateUtils.getRelativeTimeSpanString(mStartMillis,
				java.lang.System.currentTimeMillis(), 0) + ": " +

			android.text.format.DateUtils.formatElapsedTime(mLengthMillis / 1000);
	}
}


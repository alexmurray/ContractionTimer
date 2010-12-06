package com.alexdroid.contractiontimer;

public class Contraction {
	private long id;
	private long startMillis;
	private long lengthMillis;

	public Contraction(long id, long startMillis, long lengthMillis)
	{
		this.id = id;
		this.startMillis = startMillis;
		this.lengthMillis = lengthMillis;
	}

	public long getID()
	{
		return id;
	}

	public long getStartMillis()
	{
		return startMillis;
	}

	public long getLengthMillis()
	{
		return lengthMillis;
	}

	public String toString()
	{
		return android.text.format.DateFormat.format("MMM dd, yyyy h:mmaa", startMillis) + ": " +
			android.text.format.DateUtils.formatElapsedTime(lengthMillis / 1000);
	}
}


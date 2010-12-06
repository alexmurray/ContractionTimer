package com.alexdroid.contractiontimer;

public class Contraction {
	private long id;
	private long start;
	private long duration;

	public Contraction(long id, long start, long duration)
	{
		this.id = id;
		this.start = start;
		this.duration = duration;
	}

	public long getID()
	{
		return id;
	}

	public long getStart()
	{
		return start;
	}

	public long getDuration()
	{
		return duration;
	}

	public String toString()
	{
		return android.text.format.DateFormat.format("MMM dd, yyyy h:mmaa", start) + ": " +
			android.text.format.DateUtils.formatElapsedTime(duration / 1000);
	}
}


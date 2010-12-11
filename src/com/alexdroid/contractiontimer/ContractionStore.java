package com.alexdroid.contractiontimer;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ContractionStore {
	public static final String ID = "id";
	public static final String START_MILLIS = "start_millis";
	public static final String LENGTH_MILLIS = "length_millis";

	private SQLiteDatabase db;

	private static final String TAG = "CTContractionStore";
	private static final String DATABASE_NAME = "contractiontimer.db";
	private static final int DATABASE_VERSION = 1;
	private static final String CONTRACTIONS_TABLE_NAME = "contractions";
	private static final String CONTRACTIONS_TABLE_CREATE =
		"CREATE TABLE " + CONTRACTIONS_TABLE_NAME + " (" +
		ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
		START_MILLIS + " INTEGER, " +
		LENGTH_MILLIS + " INTEGER);";

	public ContractionStore(Context context) {
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		db = helper.getWritableDatabase();
		/* we only access from a single thread */
		db.setLockingEnabled(false);
	}

	public void close() {
		db.close();
	}

	public long startContraction(long start) {
		ContentValues values = new ContentValues();

		values.put(START_MILLIS, start);
		return db.insert(CONTRACTIONS_TABLE_NAME, null, values);
	}

	public boolean setlength(long id, long length) {
		ContentValues values = new ContentValues();

		values.put(LENGTH_MILLIS, length);
		return (db.update(CONTRACTIONS_TABLE_NAME, values,
					ID + " = " + id, null) == 1);
	}

	private ArrayList<Contraction> getContractions(String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		ArrayList<Contraction> contractions = new ArrayList<Contraction>();

		try {
			Cursor cursor = db.query(CONTRACTIONS_TABLE_NAME,
					new String[]{ID, START_MILLIS, LENGTH_MILLIS},
					selection, selectionArgs, groupBy, having, orderBy, limit);
			/* move to first row */
			cursor.moveToFirst();	

			if (!cursor.isAfterLast())
			{
				do {
					contractions.add(new Contraction(cursor.getLong(cursor.getColumnIndex(ID)),
								cursor.getLong(cursor.getColumnIndex(START_MILLIS)),
								cursor.getLong(cursor.getColumnIndex(LENGTH_MILLIS))));
				} while (cursor.moveToNext());
			}
			/* finished with cursor */
			cursor.close();
		} catch (SQLException e) {
			Log.e(TAG, "DB Error: " + e.toString());
			e.printStackTrace();
		}
		return contractions;
	}

	public Contraction getContraction(long id) {
		return getContractions(ID + " = " + id, null, null, null, null, null).get(0);
	}

	/**
	 * Returns an ArrayList of the n most recent contractions sorted
	 * from newest to oldest - if n is less than or equal to zero returns all
	 */
	public ArrayList<Contraction> getRecentContractions(int n) {
		return getContractions(null, null, null, null, START_MILLIS + " DESC", n > 0 ? Integer.toString(n) : null);
	}

	/**
	 * Returns an ArrayList of all contractions, sorted from oldest to newest
	 */
	public ArrayList<Contraction> getAllContractions() {
		return getContractions(null, null, null, null, START_MILLIS + " ASC", null);
	}

	public int deleteAll()
	{
		return db.delete(CONTRACTIONS_TABLE_NAME, null, null);
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		CustomSQLiteOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CONTRACTIONS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/* if we were upgrading database versions would have to handle
			 * here */
		}
	}
}


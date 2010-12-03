package com.alexdroid.contractiontimer;

import java.util.ArrayList;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper {
	public static final String ID = "id";
	public static final String START_TIME = "start_time";
	public static final String DURATION_TIME = "duration_time";

	private SQLiteDatabase db;

	private static final String TAG = "CTDataHelper";
	private static final String DATABASE_NAME = "contractiontimer.db";
	private static final int DATABASE_VERSION = 1;
	private static final String CONTRACTIONS_TABLE_NAME = "contractions";
	private static final String CONTRACTIONS_TABLE_CREATE =
		"CREATE TABLE " + CONTRACTIONS_TABLE_NAME + " (" +
		ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
		START_TIME + " INTEGER, " +
		DURATION_TIME + " INTEGER);";

	public DataHelper(Context context) {
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}

	public long insertContraction(long start, long duration) {
		ContentValues values = new ContentValues();

		values.put(START_TIME, start);
		values.put(DURATION_TIME, duration);
		return db.insert(CONTRACTIONS_TABLE_NAME, null, values);
	}

	public Contraction getContraction(long id) {
		Contraction contraction = null;
		try {
			Cursor cursor = db.query(CONTRACTIONS_TABLE_NAME,
					new String[]{ID, START_TIME, DURATION_TIME},
					ID + " = " + id, null, null, null, null);
			/* move to first row */
			cursor.moveToFirst();	

			if (!cursor.isAfterLast())
			{
				contraction = new Contraction(cursor.getLong(0),
						cursor.getLong(1),
						cursor.getLong(2));
			}
			/* finished with cursor */
			cursor.close();
		} catch (SQLException e) {
			Log.e(TAG, "DB Error: " + e.toString());
			e.printStackTrace();
		}
		return contraction;
	}

	public ArrayList<Contraction> getAllContractions() {
		ArrayList<Contraction> contractions = new ArrayList<Contraction>();

		try {
			Cursor cursor = db.query(CONTRACTIONS_TABLE_NAME,
					new String[]{ID, START_TIME, DURATION_TIME},
					null, null, null, null, null);
			/* move to first row */
			cursor.moveToFirst();	

			if (!cursor.isAfterLast())
			{
				do {
					contractions.add(new Contraction(cursor.getLong(0),
								cursor.getLong(1),
								cursor.getLong(2)));
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

	public int deleteContraction(Contraction contraction)
	{
			return db.delete(CONTRACTIONS_TABLE_NAME,
				       	ID + "=" + contraction.getID() + " AND " +
				       	START_TIME + "=" + contraction.getStart() + " AND " +
				       	DURATION_TIME + "=" + contraction.getDuration(),
				       	null);
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


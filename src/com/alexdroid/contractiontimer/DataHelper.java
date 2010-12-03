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
	private SQLiteDatabase db;

	private static final String TAG = "CTDataHelper";
	private static final String DATABASE_NAME = "contractiontimer.db";
	private static final int DATABASE_VERSION = 1;
	private static final String CONTRACTIONS_TABLE_NAME = "contractions";
	private static final String KEY_ID = "id";
	private static final String KEY_START_TIME = "start_time";
	private static final String KEY_DURATION_TIME = "duration_time";
	private static final String CONTRACTIONS_TABLE_CREATE =
		"CREATE TABLE " + CONTRACTIONS_TABLE_NAME + " (" +
		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
		KEY_START_TIME + " INTEGER, " +
		KEY_DURATION_TIME + " INTEGER);";

	public DataHelper(Context context) {
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}

	public long insertContraction(long start, long duration) {
		ContentValues values = new ContentValues();

		values.put(KEY_START_TIME, start);
		values.put(KEY_DURATION_TIME, duration);
		return db.insert(CONTRACTIONS_TABLE_NAME, null, values);
	}

	public Contraction getContraction(long id) {
		Contraction contraction = null;
		try {
			Cursor cursor = db.query(CONTRACTIONS_TABLE_NAME,
					new String[]{KEY_ID, KEY_START_TIME, KEY_DURATION_TIME},
					KEY_ID + " = " + id, null, null, null, null);
			/* move to first row */
			cursor.moveToFirst();	

			if (!cursor.isAfterLast())
			{
				contraction = new Contraction(cursor.getLong(0),
						cursor.getLong(1),
						cursor.getLong(2));
			}
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
					new String[]{KEY_ID, KEY_START_TIME, KEY_DURATION_TIME},
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
		} catch (SQLException e) {
			Log.e(TAG, "DB Error: " + e.toString());
			e.printStackTrace();
		}
		return contractions;
	}

	public int deleteContraction(Contraction contraction)
	{
			return db.delete(CONTRACTIONS_TABLE_NAME,
				       	KEY_ID + "=" + contraction.getID() + " AND " +
				       	KEY_START_TIME + "=" + contraction.getStart() + " AND " +
				       	KEY_DURATION_TIME + "=" + contraction.getDuration(),
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


package com.nullpointer.location;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationDatabase extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
public static final String TABLE_NAME = "locationsTable";
    
    public static final String KEY_ID = "_id";
    public static final String LATITUDE= "Latitude";
    public static final String LONGITUDE= "Longitude";
    public static final String TIME= "Time";
    public static final String TYPE="Type";
    
    private static final String DICTIONARY_TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME + 
    		" (" +KEY_ID + " integer primary key autoincrement, " +
    		LATITUDE+ " TEXT, "+ 
    		LONGITUDE +" TEXT, "+
    		TIME  +" TEXT, " +
    		TYPE+ " TEXT"+
    		");";
    //CREATE TABLE locations(_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude TEXT, longitude TEXT, time TEXT);
	public static final String DATABASE_NAME="locations";
	 
	LocationDatabase(Context context) {    
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	    }
	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}

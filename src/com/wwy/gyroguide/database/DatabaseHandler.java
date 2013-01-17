package com.wwy.gyroguide.database;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

public class DatabaseHandler extends SQLiteOpenHelper {
	private static final String TABLE_LOCATIONS = "gpslocation";
	private static final String KEY_ID = "id";
	private static final String KEY_LONGTITUDE = "longtitude";
	private static final String KEY_LATITUDE = "latitude";

	private static final String TABLE_HERKENNINGSPUNTEN_ONDERZOEK = "herkenninspuntenonderzoek";
	private static final String COLUMN_LONGITUDE = "LONGITUDE";
	private static final String COLUMN_LATITUDE = "LATITUDE";

	private SparseArray<GeoPoint> geoPoints = new SparseArray<GeoPoint>();

	public DatabaseHandler(Context context, String databaseName, int databaseVersion) {
		super(context, databaseName, null, databaseVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_LONGTITUDE + " INTEGER,"
				+ KEY_LATITUDE + " INTEGER)";
		database.execSQL(CREATE_CONTACTS_TABLE);
		String CREATE_ONDERZOEK_TABEL = "CREATE TABLE " + TABLE_HERKENNINGSPUNTEN_ONDERZOEK + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + COLUMN_LONGITUDE + " INTEGER," 
				+ COLUMN_LATITUDE + " INTEGER)";
		database.execSQL(CREATE_ONDERZOEK_TABEL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS); 
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_HERKENNINGSPUNTEN_ONDERZOEK); 
		onCreate(database);
	}
	public int getGeoPointRowCount() {
		int rowCount = 0;
		Cursor cursor = null;
		String selectQuery = "SELECT COUNT(*) FROM " + TABLE_LOCATIONS;
		SQLiteDatabase db = this.getWritableDatabase();
		cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		rowCount = cursor.getInt(0);
		cursor.close();
		db.close();
		return rowCount;
	}

	public GeoPoint getGeoPoint(int index) {
		if(geoPoints.size() > 0 && index < geoPoints.size() &&  geoPoints.get(index) != null) {
			return geoPoints.get(index);
		} else {
			geoPoints.clear();
			String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS + " WHERE " + KEY_ID + " >= " + index + " AND " + KEY_ID + " <= " + (index + 1000);
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);
			if(cursor.moveToFirst()) {
				do {
					GeoPoint punt = new GeoPoint(cursor.getInt(2), cursor.getInt(1));
					geoPoints.put(cursor.getInt(0), punt);
				} while(cursor.moveToNext());
			}
			cursor.close();
			db.close();
		}
		return geoPoints.get(index);
	}

	public void addGeoPoint(GeoPoint geoPoint) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_LONGTITUDE, geoPoint.getLongitudeE6()); 
		values.put(KEY_LATITUDE, geoPoint.getLatitudeE6()); 
		database.insert(TABLE_LOCATIONS, null, values);
		database.close();
	}
	public List<GeoPoint> getHerkenningPunten() {
		List<GeoPoint> herkenningPunten = new ArrayList<GeoPoint>();
		String selectQuery = "SELECT  * FROM " + TABLE_HERKENNINGSPUNTEN_ONDERZOEK;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.moveToFirst()) {
			do {
				GeoPoint punt = new GeoPoint(cursor.getInt(2), cursor.getInt(1));
				herkenningPunten.add(punt);
			} while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return herkenningPunten;
	}

	public void deleteCrumbs() {
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL("DELETE FROM " + TABLE_LOCATIONS);
		database.close();
	}
	public void addHerkenningPunt(GeoPoint herkenningPunt) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_LONGITUDE, herkenningPunt.getLongitudeE6()); 
		values.put(COLUMN_LATITUDE, herkenningPunt.getLatitudeE6()); 
		database.insert(TABLE_HERKENNINGSPUNTEN_ONDERZOEK, null, values);
		database.close();
	}
	
	public void deleteHerkenningsPunten() {
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL("DELETE FROM " + TABLE_HERKENNINGSPUNTEN_ONDERZOEK);
		database.close();
	}

}
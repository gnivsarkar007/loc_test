package com.nullpointer.location;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.w3c.dom.Comment;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {

	private static final int TYPE_NETWORK = 0, TYPE_GPS = 1;
	private LocationManager mLocationManager;
	private PowerManager.WakeLock mPartialWakeLock;
	private LocationDatabase mLocationDatabase;
	private CountDownTimer mSearchTimeout, mWaitTimer, mNetworkLocTimeout;
	private boolean locationRequested = false;

	private String TAG = "loc_test_debug";

	@Override
	public void onCreate() {

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Location");
		mLocationDatabase = new LocationDatabase(this);
		Log.d(TAG, "service created");

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		startLocSearch();
		Log.d(TAG, "service started");

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		mLocationManager.removeUpdates(mGPSListener);
		mLocationManager.removeUpdates(mNetworkListener);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void startLocSearch()
	/*
	 * acquire wakelock->start listening for gps location->start timer->timer
	 * ends without gps fix->stop gps
	 */
	{
		mPartialWakeLock.acquire();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 100, mGPSListener);
		locationRequested = true;

		Log.d(TAG, "location requested, 10 min timer start");

		mSearchTimeout = new CountDownTimer(10 * 60 * 1000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {

				Log.d(TAG, "10 mins over, no gps loc found, using cell tower loc");

				// writeLocToDB(mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER),
				// TYPE_NETWORK);

				useNetworkLocation();
				stopLocSearch();
			}
		};
		mSearchTimeout.start();
	}

	private void stopLocSearch()
	/*
	 * stop listening for gps location->start timer->release wakelock->timer
	 * ends->start listening for gps location
	 */
	{
		mLocationManager.removeUpdates(mGPSListener);

		Log.d(TAG, "locationlistener removed,1 min timer start");
		locationRequested = false;

		CountDownTimer mWaitTimer = new CountDownTimer(1 * 60 * 1000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				Log.d(TAG, "1 mins over");

				startLocSearch();

			}
		};
		mWaitTimer.start();

		mPartialWakeLock.release();
	}

	private void useNetworkLocation() {
		// stop listening for updates when no cellular network is available
		// (no network location found in 10 secs)

		mLocationManager.requestLocationUpdates(mLocationManager.NETWORK_PROVIDER, 10, 10, mNetworkListener);
		mNetworkLocTimeout = new CountDownTimer(10 * 1000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {
				mLocationManager.removeUpdates(mNetworkListener);
			}
		}.start();
	}

	private void writeLocToDB(Location currLoc, int _type) {

		Date mDate = new Date(currLoc.getTime());


		  Log.d(TAG, "location: type: "+_type +" lat: " + currLoc.getLatitude() +
		  " long: " + currLoc.getLongitude() + " time: " + mDate);
		 

		SQLiteDatabase mSQLDb = mLocationDatabase.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(LocationDatabase.LATITUDE, currLoc.getLatitude());
		values.put(LocationDatabase.LONGITUDE, currLoc.getLongitude());
		values.put(LocationDatabase.TIME, mDate.toString());

		if (_type == TYPE_GPS) {
			values.put(LocationDatabase.TYPE, "GPS");
		}

		else if (_type == TYPE_NETWORK) {
			values.put(LocationDatabase.TYPE, "Network");
		}

		mSQLDb.insert(LocationDatabase.TABLE_NAME, null, values);

		Log.d(TAG, "writeLoctodb called-------------------------------");
	}

	private LocationListener mGPSListener = new LocationListener() {

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		public void onLocationChanged(Location location) {

			writeLocToDB(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), TYPE_GPS);
			Log.d(TAG, "GPS location found");
			mSearchTimeout.cancel();
			stopLocSearch();
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

	};

	private LocationListener mNetworkListener = new LocationListener() {

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}

		public void onLocationChanged(Location location) {
			writeLocToDB(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER), TYPE_NETWORK);
			mNetworkLocTimeout.cancel();
			mLocationManager.removeUpdates(mNetworkListener);

		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}
	};

}

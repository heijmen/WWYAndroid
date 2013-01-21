package com.wwy.wwyandroid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.LocationManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.wwy.gyroguide.database.DatabaseHandler;
import com.wwy.gyroguide.route.DrawView;
import com.wwy.gyroguide.route.RoadHandler;
import com.wwy.gyroguide.route.RoadHandlerListener;

import eu.uniek.compas.Kompas;
import eu.uniek.compas.KompasListener;
import eu.uniek.gps.GPSLocationListener;

public class WWYAndroid extends Activity {

	private LocationManager mLocationManager;
	private GPSLocationListener mLocationListener;
	private Kompas mKompas;

	private PendingIntent pendingIntent;
	private IntentFilter writeTagFilters[];
	private Tag locationTag;
	private NfcAdapter nfcAdapter;

	private RoadHandler mRoadHandler;

	private GeoPoint fakeDestination = new GeoPoint(52.100811,5.111536);

	private int led = 0;
	private Handler mLedHandler = new Handler();
	private Handler mRoadUpdateHander = new Handler();
	private Vibrator mVibrator;
	private boolean first = true;
	private TextView led1;
	private TextView led2;
	private TextView led3;
	private TextView led4;
	private TextView led5;
	private TextView led6;
	private TextView led7;

	private DrawView pijlView;
	private int timesKitt = 0;
	private DatabaseHandler mDatabaseHandler;

	private Button herkenningspuntButton;
	private int timesPulse = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wwy_android_layout);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		led1 = (TextView) findViewById(R.id.led1);
		led2 = (TextView) findViewById(R.id.led2);
		led3 = (TextView) findViewById(R.id.led3);
		led4 = (TextView) findViewById(R.id.led4);
		led5 = (TextView) findViewById(R.id.led5);
		led6 = (TextView) findViewById(R.id.led6);
		led7 = (TextView) findViewById(R.id.led7);

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		pijlView = (DrawView) findViewById(R.id.pijlView);
		startComponents();
		//		kitt(4);
		herkenningspuntButton = (Button) findViewById(R.id.buttonHerkenningspunt);
		herkenningspuntButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//	startRoute(fakeDestination);
					mVibrator.vibrate(500);
					mDatabaseHandler.addHerkenningPunt(mLocationListener.getCurrentLocation());
				//	pulseRedLed(5);
			}
		});
		mDatabaseHandler = new DatabaseHandler(this, "WwyAndroidDaba", 1);

	}

	private void rechtsLed(int hoever) {
		redLeds();
		if(hoever < 33) {
			led5.setBackgroundColor(getResources().getColor(R.color.green));
		} else if (hoever < 66) {
			led5.setBackgroundColor(getResources().getColor(R.color.green));
			led6.setBackgroundColor(getResources().getColor(R.color.green));
		} else {
			led5.setBackgroundColor(getResources().getColor(R.color.green));
			led6.setBackgroundColor(getResources().getColor(R.color.green));
			led7.setBackgroundColor(getResources().getColor(R.color.green));
		}
	}

	private void pulseRedLed(final int hoeveel) {
		redLeds();
		timesPulse = 0;
		first = false;
		final Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				timesPulse++;
				if(first) {
					first = false;
					greenLeds();
				} else {
					first = true;
					redLeds();
					handler.removeCallbacks(this);
				}
				if(timesPulse > hoeveel * 2) {
					handler.removeCallbacks(this);
					return;
				}
				handler.postDelayed(this, 250);
			}
		};
		r.run();
	}

	private void greenLeds() {
		led1.setBackgroundColor(getResources().getColor(R.color.green));
		led2.setBackgroundColor(getResources().getColor(R.color.green));
		led3.setBackgroundColor(getResources().getColor(R.color.green));
		led4.setBackgroundColor(getResources().getColor(R.color.green));
		led5.setBackgroundColor(getResources().getColor(R.color.green));
		led6.setBackgroundColor(getResources().getColor(R.color.green));
		led7.setBackgroundColor(getResources().getColor(R.color.green));
	}

	private void linksLed(int hoever) {
		redLeds();
		if(hoever < 33) {
			led3.setBackgroundColor(getResources().getColor(R.color.green));
		} else if (hoever < 66) {
			led3.setBackgroundColor(getResources().getColor(R.color.green));
			led2.setBackgroundColor(getResources().getColor(R.color.green));
		} else {
			led3.setBackgroundColor(getResources().getColor(R.color.green));
			led2.setBackgroundColor(getResources().getColor(R.color.green));
			led1.setBackgroundColor(getResources().getColor(R.color.green));
		}
	}
	private void rechtdoorLed() {
		redLeds();
		led3.setBackgroundColor(getResources().getColor(R.color.green));
		led4.setBackgroundColor(getResources().getColor(R.color.green));
		led5.setBackgroundColor(getResources().getColor(R.color.green));
	}

	private void startUpdatingRoad() {
		updateRoadRunnable.run();
	}

	Runnable updateRoadRunnable = new Runnable() {
		public void run() {
			if(mRoadHandler != null && mRoadHandler.getCurrentDestination() != null && mLocationListener.getCurrentLocation() != null) {
				int heijmething = (int) getAngleBetweenGeoPoints(mLocationListener.getCurrentLocation(), mRoadHandler.getCurrentDestination(), mKompas.getAzimuth());
				drawImage(heijmething);
				drawRichtingLeds();
			}
			mRoadUpdateHander.postDelayed(this, 50);
		}
	};

	private void kitt(final int aantal) {
		timesKitt = 0;
		redLeds();
		Runnable r = new Runnable() {
			public void run() {
				redLeds();
				if(timesKitt > aantal * 7) {
					mLedHandler.removeCallbacks(this);
					return;
				}
				if(led == 8) {
					led = 0;
				}
				led++;
				switch (led) {
				case 1 : led1.setBackgroundColor(getResources().getColor(R.color.green));
				case 2 : led2.setBackgroundColor(getResources().getColor(R.color.green));
				case 3 : led3.setBackgroundColor(getResources().getColor(R.color.green));
				case 4 : led4.setBackgroundColor(getResources().getColor(R.color.green));
				case 5 : led5.setBackgroundColor(getResources().getColor(R.color.green));
				case 6 : led6.setBackgroundColor(getResources().getColor(R.color.green));
				case 7 : led7.setBackgroundColor(getResources().getColor(R.color.green));
				}
				mLedHandler.postDelayed(this, 100);
				timesKitt++;
			}
		};
		r.run();
	}

	private void redLeds() {
		led1.setBackgroundColor(getResources().getColor(R.color.red));
		led2.setBackgroundColor(getResources().getColor(R.color.red));
		led3.setBackgroundColor(getResources().getColor(R.color.red));
		led4.setBackgroundColor(getResources().getColor(R.color.red));
		led5.setBackgroundColor(getResources().getColor(R.color.red));
		led6.setBackgroundColor(getResources().getColor(R.color.red));
		led7.setBackgroundColor(getResources().getColor(R.color.red));
	}

	private void startComponents() {
		startGps();
		startKompas();
		startNfc();
	}
	private void startNfc() {
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };
	}

	private void startKompas() {
		mKompas = new Kompas(this, new KompasListener() {
			public void onSensorChanged(float azimuth) {
				//				if(mRoadHandler != null && mRoadHandler.getCurrentDestination() != null) { 
				//					pijlView.updateArrow((int) azimuth);
				//				}
			}
		});
		mKompas.startListening();
	}

	public void drawImage(int heijmething) {
		//	DrawView drawView = (DrawView) findViewById(R.id.drawView1);
		//	drawView.setBackgroundColor(Color.WHITE);
		//	int heijmething = (int) getAngleBetweenGeoPoints(startPoint, endPoint, azimuth);
		//	drawView.drawTheThing(heijmething);  
		pijlView.updateArrow(heijmething);
	}

	private void drawRichtingLeds() {
		sendGyroDirection(mRoadHandler.getCurrentDestination());
	}

	private void sendGyroDirection(GeoPoint waypoint) {
		int direction = (int) getAngleBetweenGeoPoints(mLocationListener.getCurrentLocation(), waypoint, mKompas.getAzimuth());
		if(direction > 110 && direction < 250) {
			redLeds();
			return;
		}
		if(direction == 0) {
			direction = 360;
		}
		if(direction < 22.5) {
			rechtdoorLed();
		} else if(direction < 45)  {
			rechtsLed(2);
		} else if(direction < 68) {
			rechtsLed(50);
		} else if(direction < 110) {
			rechtsLed(70);
		} else if(direction < 292.5) {
			linksLed(90);
		} else if(direction < 315) {
			linksLed(60);
		} else if(direction < 337.5) {
			linksLed(20);
		} else {
			rechtdoorLed();
		}
	}

	private void startRoute(GeoPoint destination) {
		Log.e("TA","com ik hier");
		mRoadHandler = new RoadHandler(mLocationListener, destination, mDatabaseHandler, new RoadHandlerListener() {

			public void onWayPointReached(GeoPoint newWayPoint) {
				mVibrator.vibrate(1000);
			}

			public void onRouteStart(GeoPoint firstWayPoint) {
				mVibrator.vibrate(1000);
			}

			public void onDestinationReached() {
				mVibrator.vibrate(5000);
				kitt(5);
				stopUpdatingRoad();
			}

		});
		startUpdatingRoad();
	}
	private void stopUpdatingRoad() {
		mRoadUpdateHander.removeCallbacks(updateRoadRunnable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.wwy_android_layout, menu);
		return true;
	}

	private void startGps() {
		mLocationListener = new GPSLocationListener();
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = mLocationManager.getBestProvider(c, true);
		mLocationManager.requestLocationUpdates(provider, 500, 1, mLocationListener);
	}

	public void writeModeOn(){
		if (nfcAdapter != null) {
			nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
		}
	}

	public void writeModeOff() { 
		if (nfcAdapter != null) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onNewIntent(Intent intent){
		pulseRedLed(5);
		Log.i("NFC", "KOM IK HIER");
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
			startRoute(fakeDestination);
			Log.i("NFC", "KOM IK HIER2");

			locationTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);    
			final String s = readTag(locationTag);
			if (s == null) {
				Log.i("NFC", "KOM IK HIER3");

				//TODO XXX FIXME tag is not formatted
			} else {
				Log.i("NFC", "KOM IK HIER4");

				if (s.contains(":D")) {
					Log.i("NFC", "KOM IK HIER5");
					
				} 
//				} else {
//					Log.i("NFC", "KOM IK HIER6");
//
//					GeoPoint location = mLocationListener.getCurrentLocation();
//					writeTag(locationTag, location.getLatitudeE6() / 1E6 + "," + location.getLongitudeE6() / 1E6);
//				}
			}
		}
	}

	private String readTag(Tag tag) {
		locationTag = tag;
		String s = "";
		try {
			if(locationTag!=null) {
				NdefMessage m = read(locationTag);
				if (m != null) {
					s = new String(m.toByteArray());
				} else {
					s = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		Log.e("Ta", s);
		return s;
	}

	private NdefMessage read(Tag tag) throws IOException, FormatException {
		Ndef ndef = Ndef.get(tag);
		if (ndef == null) {
			return null;
		}
		ndef.connect();
		NdefMessage message2 = ndef.getNdefMessage();
		ndef.close();

		return message2;
	}

	public void writeTag(Tag tag, String location) {
		locationTag = tag;
		try {
			if(locationTag!=null) {
				write(":D" + location, locationTag);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
	}

	private void write(String text, Tag tag) throws IOException, FormatException {
		NdefRecord[] records = { createRecord(text) };
		NdefMessage message = new NdefMessage(records);
		Ndef ndef = Ndef.get(tag);
		ndef.connect();
		ndef.writeNdefMessage(message);
		ndef.close();
	}

	private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
		String lang = "en";
		byte[] textBytes = text.getBytes();
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		int textLength = textBytes.length;
		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
		return recordNFC;
	}
	public double getAngleBetweenGeoPoints(GeoPoint currentLocation, GeoPoint destination, float azimuth) {
		double angle = currentLocation.bearingTo(destination) - azimuth;
		if (angle < 0) {
			angle += 360; 
		}
		return angle;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
		switch (item.getItemId()) {
		case R.id.item1:
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Verwijder gegevens")
			.setMessage("Deze optie is bedoeld om de herkenningspunten te resetten.")
			.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int arg1) {
					mDatabaseHandler.deleteCrumbs();
					mDatabaseHandler.deleteHerkenningsPunten();
				}
			})
			.setNegativeButton("Annuleren", null)
			.show();
			return true;
		case R.id.item2 :
			startRoute(fakeDestination);
			return true;
		case R.id.item3 :
			pulseRedLed(5);
			return true;
		case R.id.item4 :
			kitt(5);
			return true;
		case R.id.item5 :
			stopUpdatingRoad();
			if(mRoadHandler != null) {
				mRoadHandler.stopRoute();
			}
			return true;
		default:
			return false;
		}
	}
}

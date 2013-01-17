package eu.uniek.compas;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Kompas implements SensorEventListener {
	
	private float azimuth;
	private SensorManager mSensorManager;
	private Sensor mCompass;
	private KompasListener mKompasListener;
	
	public Kompas (Context context, KompasListener kompasListener) {
		this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		this.mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		this.mKompasListener = kompasListener;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		azimuth = event.values[0];
		mKompasListener.onSensorChanged(azimuth);
	}
	public float getAzimuth() {
		return azimuth;
	}
	
	public void startListening() {
		mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
	}
	public void stopListening() {
		mSensorManager.unregisterListener(this);
	}
	

}

package eu.uniek.compas;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Kompas implements SensorEventListener {
	private float mAzimuth;
	private SensorManager mSensorManager;
	private Sensor mCompass;
	private KompasListener mKompasListener;
	
	public Kompas (Context context, KompasListener kompasListener) {
		this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		this.mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		this.mKompasListener = kompasListener;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	public void onSensorChanged(SensorEvent event) {
		mAzimuth = event.values[0];
		mKompasListener.onSensorChanged(mAzimuth);
	}
	
	public float getAzimuth() {
		return mAzimuth;
	}
	
	public void startListening() {
		mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopListening() {
		mSensorManager.unregisterListener(this);
	}
}

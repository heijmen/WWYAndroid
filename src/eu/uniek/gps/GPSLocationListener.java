package eu.uniek.gps;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSLocationListener implements LocationListener {  
	private GeoPoint currentLocation;

	public void onLocationChanged(Location location) {  
		setCurrentLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
	}
	
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public GeoPoint getCurrentLocation() {
		if(currentLocation == null) {
			currentLocation = new GeoPoint(52.102149,5.107124);
		}
		return currentLocation;
	}

	public void setCurrentLocation(GeoPoint currentLocation) {
		this.currentLocation = currentLocation;
	}

	
}

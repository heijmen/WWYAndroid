package eu.uniek.gps;

import java.util.List;

import org.osmdroid.util.GeoPoint;


import android.location.Location;

public class GPSHandler {

	public static float distanceBetween(GeoPoint firstLocation, GeoPoint secondLocation) {
		float[] results = new float[3];
		Location.distanceBetween(
				firstLocation.getLatitudeE6() / 1e6, 
				firstLocation.getLongitudeE6() / 1e6,
				secondLocation.getLatitudeE6() / 1e6,
				secondLocation.getLongitudeE6() / 1e6,
				results);
		return results[0];
	}

	public static boolean locationExistsInRange(int range, GeoPoint newLocation, 
			List<GeoPoint> allLocations) {
		for (GeoPoint oldLocation : allLocations) {
			if (distanceBetween(newLocation, oldLocation) <= range) {
				return true;
			}
		}
		return false;
	}


}

package com.wwy.gyroguide.route;

import org.osmdroid.util.GeoPoint;

public interface RoadHandlerListener {
	public void onWayPointReached(GeoPoint newWayPoint);
	public void onDestinationReached();
	public void onRouteStart(GeoPoint firstWayPoint);
}
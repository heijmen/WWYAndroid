package com.wwy.gyroguide.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import android.os.AsyncTask;
import android.os.Handler;

import com.wwy.gyroguide.database.DatabaseHandler;

import eu.uniek.gps.GPSHandler;
import eu.uniek.gps.GPSLocationListener;

public class RoadHandler extends AsyncTask<Void,Void,Void> {
	private static final float ALLOWED_DISTANCE = 50;
	private GPSLocationListener mLocationListener;
	private GeoPoint mDestination;
	private DatabaseHandler mDatabaseHandler;
	private Handler mUpdateRoadHandler = new Handler();
	private Road mRoad;
	private int mCurrentDestinationIndex = 0;
	private List<GeoPoint> mBreadcrumbs;
	private List<GeoPoint> mHerkenningspunten;
	private RoadHandlerListener mAnotherRoadHanderListener;

	private Runnable mUpdateRoadRunnable = new Runnable() {
		public void run() {
			checkNearWayPoint();
			mUpdateRoadHandler.postDelayed(mUpdateRoadRunnable, 7000);
		}
	};

	public RoadHandler(GPSLocationListener locationListener, GeoPoint destination, DatabaseHandler databaseHandler, RoadHandlerListener anotherRoadHanderListener) {
		mLocationListener = locationListener;
		mDestination = destination;
		mDatabaseHandler = databaseHandler;
		this.mAnotherRoadHanderListener = anotherRoadHanderListener;
		setHerkenningsPuntenAndBreadcrumbs();
		execute(new Void[0]);
	}

	private void checkNearWayPoint() {
		if(getCurrentDestination() == null) {
			mAnotherRoadHanderListener.onDestinationReached();
			stopRoute();
			return;
		}
		if(GPSHandler.distanceBetween(mLocationListener.getCurrentLocation(), getCurrentDestination()) < ALLOWED_DISTANCE) {
			mAnotherRoadHanderListener.onWayPointReached(getCurrentDestination());
			mCurrentDestinationIndex++;
		}
	}

	private Void createRoute() {
		RoadManager roadManager = new GoogleRoadManager();
		roadManager.addRequestOption("mode=walking");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		waypoints.add(mLocationListener.getCurrentLocation()); 
		waypoints.add(mDestination);
		if(!mBreadcrumbs.isEmpty() || !mHerkenningspunten.isEmpty()) {
			int waypointCount = 1;
			String waypointsRequestOption = "";
			waypointsRequestOption += "waypoints=optimize:true";
			Random r = new Random();
			List<Integer> tempIndexes = new ArrayList<Integer>();
			int iterations = 16;
			if(!mHerkenningspunten.isEmpty()) {
				for(int i = 0; i < iterations; i++) {
					int index = r.nextInt(mHerkenningspunten.size());
					if(!tempIndexes.contains(index)) {
						if(waypointCount >= 8) {
							break;
						}
						waypointsRequestOption += "%7C";
						waypointsRequestOption += geoPointAsString(mHerkenningspunten.get(index));
						waypointCount++;
						tempIndexes.add(index);
					}
				}
			}
			if(!mBreadcrumbs.isEmpty()) {
				List<Integer> temp_breacrumbindexes = new ArrayList<Integer>();
				if(waypointCount < 8) {
					for(int i = 0; i < iterations; i++) {
						int index = r.nextInt(mBreadcrumbs.size());
						if(!temp_breacrumbindexes.contains(index)) {
							if(waypointCount >= 8) {
								break;
							}
							waypointsRequestOption += "%7C";
							waypointsRequestOption += geoPointAsString(mBreadcrumbs.get(index));
							waypointCount++;
							temp_breacrumbindexes.add(index);
						}
						temp_breacrumbindexes.add(index);
					}
				}
			}
			roadManager.addRequestOption(waypointsRequestOption);
		}
		mRoad = roadManager.getRoad(waypoints);
		mCurrentDestinationIndex = 0;
		if(getCurrentDestination() != null) {
			mAnotherRoadHanderListener.onRouteStart(getCurrentDestination());			
		} else {
			mAnotherRoadHanderListener.onDestinationReached();
			stopRoute();
		}
		return null;
	}

	protected String geoPointAsString(GeoPoint geoPoint){
		StringBuffer result = new StringBuffer();
		double geoPointFloating = geoPoint.getLatitudeE6()*1E-6;
		result.append(Double.toString(geoPointFloating));
		geoPointFloating = geoPoint.getLongitudeE6()*1E-6;
		result.append("," + Double.toString(geoPointFloating));
		return result.toString();
	}

	public void startRoute() {
		mUpdateRoadRunnable.run();
	}

	public void stopRoute() {
		mUpdateRoadHandler.removeCallbacks(mUpdateRoadRunnable);
	}

	private void setHerkenningsPuntenAndBreadcrumbs() {
		int averageLatitude = (mLocationListener.getCurrentLocation().getLatitudeE6() + mDestination.getLatitudeE6()) / 2;
		int avarageLongitude = (mLocationListener.getCurrentLocation().getLongitudeE6() +  mDestination.getLongitudeE6()) / 2; 
		GeoPoint middle = new GeoPoint(averageLatitude, avarageLongitude);
		float radius = GPSHandler.distanceBetween(mLocationListener.getCurrentLocation(),  mDestination) / 2 + 20;
		this.mBreadcrumbs = getBreadcrumbsInRange(middle, radius);
		this.mHerkenningspunten = getHerkenninspuntenInRange(middle, radius);
	}

	private List<GeoPoint> getBreadcrumbsInRange(GeoPoint startingPoint, float radius) {
		List<GeoPoint> breadcrumbs = new ArrayList<GeoPoint>();
		for(int breadcrumbsIndex = 1; breadcrumbsIndex < mDatabaseHandler.getGeoPointRowCount(); breadcrumbsIndex++) {
			GeoPoint breadcrumb = mDatabaseHandler.getGeoPoint(breadcrumbsIndex);
			if(GPSHandler.distanceBetween(startingPoint, breadcrumb) < radius) {
				breadcrumbs.add(breadcrumb);
			}
		}
		return breadcrumbs;
	}

	private List<GeoPoint> getHerkenninspuntenInRange(GeoPoint startingPoint, float radius) {
		List<GeoPoint> herkenningspunten = new ArrayList<GeoPoint>();
		for(GeoPoint herkenningspunt : mDatabaseHandler.getHerkenningPunten()) {
			if(GPSHandler.distanceBetween(startingPoint, herkenningspunt) < radius) {
				herkenningspunten.add(herkenningspunt);
			}
		}
		return herkenningspunten;
	}
	public GeoPoint getCurrentDestination() {
		if(mRoad != null && mCurrentDestinationIndex < mRoad.mNodes.size()) {
			return mRoad.mNodes.get(mCurrentDestinationIndex).mLocation;
		}
		return null;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		return createRoute();
	}
}

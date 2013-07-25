package com.titutorial.mapdemo;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.titutorial.mapdemo.MainActivity.LoadPlaces;

import android.R.string;
import android.content.Intent;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MapActivity extends FragmentActivity {
	GoogleMap map;
	GPSTracker gps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_list);

		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, String>> placesListItems = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("placeList");
		
		Log.d("--", "placesListItems init = " + placesListItems.toString());
		gps = new GPSTracker(MapActivity.this);
		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		map.setMyLocationEnabled(true);
		
		if (gps.canGetLocation()) {
			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();
			LatLng currentLocation = new LatLng(latitude, longitude);
			BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
			
			map.addMarker(new MarkerOptions().position(currentLocation).title("You are here!").icon(bitmapMarker));			
		}else{
			Log.d("error", "unable to fetch current location :(");
		}

		addMarkersToMap(placesListItems);
	}

	private void addMarkersToMap(
			ArrayList<HashMap<String, String>> pList) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (int i = 0; i < pList.size(); i++) {
			Log.d("--", "item = " + pList.get(i));

			HashMap<String, String> itm1 = pList.get(i);
			HashMap<String, String> itm2 = pList.get(i);
			HashMap<String, String> itm3 = pList.get(i);
			HashMap<String, String> itm4 = pList.get(i);
			try {
				String pName = itm1.get(MainActivity.KEY_NAME);
				Double pLatitude = Double.parseDouble(itm2
						.get(MainActivity.KEY_LATITUDE));
				Double pLongitude = Double.parseDouble(itm3
						.get(MainActivity.KEY_LONGITUDE));
				String pAddress = itm4.get(MainActivity.KEY_ADDRESS);
				
				LatLng ll = new LatLng(pLatitude, pLongitude);
				BitmapDescriptor bitmapMarker;
				bitmapMarker = BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
				
				Marker m =  map.addMarker(new MarkerOptions().position(ll).title(pName)
						.snippet(pAddress).icon(bitmapMarker));
				builder.include(m.getPosition());
				
			} catch (Exception e) {
				Log.d("--", "error = " + e.toString());
			}
		}

		//add current location to map
		if (gps.canGetLocation()) {
			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();
			LatLng currentLocation = new LatLng(latitude, longitude);
			BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);		
			Marker m = map.addMarker(new MarkerOptions().position(currentLocation).title("You are here!").icon(bitmapMarker));
			builder.include(m.getPosition());
		}else{
			Log.d("error", "unable to fetch current location :(");
		}
		
		LatLngBounds bounds = builder.build();
		int padding = 50; // offset from edges of the map in pixels
		Log.d("bound", "bounds = "+bounds);
		Display mDisplay= getWindowManager().getDefaultDisplay();
		int width= mDisplay.getWidth();
		int Height= mDisplay.getHeight();
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, Height, padding);
		//map.moveCamera(cu);
		map.animateCamera(cu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.list_view:
			// do something when this button is pressed
			Log.d("menu", "list item clicked");
			finish();

		default:
			return true;
		}
	}
}

package com.titutorial.mapdemo;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.titutorial.mapdemo.MainActivity.LoadPlaces;

import android.R.string;
import android.content.Intent;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MapActivity extends FragmentActivity {
	static final LatLng currentLocation = new LatLng(11.21951, 78.167799);
	static final LatLng KPALAYAM = new LatLng(11.2818, 78.1648);
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
			// Move the camera instantly to currentLocation with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));

			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
			
		}else{
			Log.d("error", "unable to fetch current location :(");
		}

		addMarkersToMap(placesListItems);
	}

	private void addMarkersToMap(
			ArrayList<HashMap<String, String>> pList) {

		for (int i = 0; i < pList.size(); i++) {
			Log.d("--", "item = " + pList.get(i));

			HashMap<String, String> itm1 = pList.get(i);
			HashMap<String, String> itm2 = pList.get(i);
			HashMap<String, String> itm3 = pList.get(i);
			HashMap<String, String> itm4 = pList.get(i);
			try {
				String pName = itm1.get(MainActivity.KEY_NAME);
				Log.d("--", "pName = " + pName);
				Double pLatitude = Double.parseDouble(itm2
						.get(MainActivity.KEY_LATITUDE));
				Log.d("--", "pLatitude = " + pLatitude);
				Double pLongitude = Double.parseDouble(itm3
						.get(MainActivity.KEY_LONGITUDE));
				Log.d("--", "pLongitude = " + pLongitude);
				String pAddress = itm4.get(MainActivity.KEY_ADDRESS);
				Log.d("--", "pName = " + pName);
				
				LatLng ll = new LatLng(pLatitude, pLongitude);
				BitmapDescriptor bitmapMarker;
				bitmapMarker = BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

				map.addMarker(new MarkerOptions().position(ll).title(pName)
						.snippet(pAddress).icon(bitmapMarker));

			} catch (Exception e) {
				Log.d("--", "error = " + e.toString());
			}
		}
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

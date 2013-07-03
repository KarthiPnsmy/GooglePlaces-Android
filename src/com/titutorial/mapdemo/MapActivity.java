package com.titutorial.mapdemo;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MapActivity extends FragmentActivity {
	static final LatLng NAMAKKAL = new LatLng(11.21951, 78.167799);
	static final LatLng KPALAYAM = new LatLng(11.2818, 78.1648);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_list);

		GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		map.setMyLocationEnabled(true);
		map.addMarker(new MarkerOptions().position(NAMAKKAL).title("Namakkal")
				.snippet("Transport city"));
		map.addMarker(new MarkerOptions()
				.position(KPALAYAM)
				.title("K.Palayam")
				.snippet("Coolest place")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_launcher)));

		// Move the camera instantly to NAMAKKAL with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(NAMAKKAL, 15));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		
		Intent intent = getIntent();
		String stock_list = intent.getStringExtra("stock_list");
		Log.d("list", "list = "+stock_list);
		
		//List<String> stringList = new ArrayList<String> (Arrays.asList(stock_list)); //n
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

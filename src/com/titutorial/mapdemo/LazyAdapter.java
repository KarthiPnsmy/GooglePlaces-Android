package com.titutorial.mapdemo;

import java.util.ArrayList;
import java.util.HashMap;

import com.titutorial.mapdemo.MainActivity.LoadPlaces;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class LazyAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	private ListView clv;
	public Context context;
	
	public LazyAdapter(Context context, ListView lv, Activity a,
			ArrayList<HashMap<String, String>> d) {
		this.context = context;
		clv = lv;
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public void editAddress(String userId) {

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_item, null);

		TextView placeReference = (TextView) vi.findViewById(R.id.reference); // reference
		TextView placeName = (TextView) vi.findViewById(R.id.name); // name
		RatingBar  placeRatingBar = (RatingBar) vi.findViewById(R.id.place_ratingbar); // rating bar
		TextView placeAddress = (TextView) vi.findViewById(R.id.address); // address
		TextView distance = (TextView) vi.findViewById(R.id.distance); // distance
		TextView placeLatitude = (TextView) vi.findViewById(R.id.itemLat); // lat
		TextView placeLongitude = (TextView) vi.findViewById(R.id.itemLong); // long
		ImageView directionBtn = (ImageView) vi.findViewById(R.id.direction); // direction
		HashMap<String, String> place = new HashMap<String, String>();
		place = data.get(position);
		//Log.d("place", "place item = "+place.toString());
		// Setting all values in listview
		placeReference.setText(place.get(MainActivity.KEY_REFERENCE));
		placeName.setText(place.get(MainActivity.KEY_NAME));
		if(place.get(MainActivity.KEY_RATING) != null){
			placeRatingBar.setRating(Float.parseFloat(place.get(MainActivity.KEY_RATING)));
		}else{
			Log.d("rating", "rating null for "+place.get(MainActivity.KEY_NAME));
			placeRatingBar.setVisibility(View.GONE);
		}
		
		
		placeAddress.setText(place.get(MainActivity.KEY_ADDRESS));
		placeLatitude.setText(place.get(MainActivity.KEY_LATITUDE));
		placeLongitude.setText(place.get(MainActivity.KEY_LONGITUDE));
		distance.setText(place.get(MainActivity.KEY_DISTANCE)+" KM");
		
		
		directionBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				final int position = clv.getPositionForView((View) v
						.getParent());
				HashMap<String, String> currentPlace = new HashMap<String, String>();
				currentPlace = data.get(position);
				
				Log.d("currentPlace", "currentPlace = "+currentPlace.toString());
				
				GPSTracker gps = new GPSTracker(context);
				if (gps.canGetLocation()) {

					double sLat = gps.getLatitude();
					double sLong = gps.getLongitude();
					String dLat = currentPlace.get(MainActivity.KEY_LATITUDE);
					String dLong = currentPlace.get(MainActivity.KEY_LONGITUDE);
					Log.d("currentPlace", "dLat = "+dLat+", dLong = "+dLong);
					String iUrl = "http://maps.google.com/maps?saddr="+sLat+","+sLong+"&daddr="+dLat+","+dLong;
					Log.d("currentPlace", "iUrl = "+iUrl);
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
						    Uri.parse(iUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				} else {
					// can't get location
					// GPS or Network is not enabled
					// Ask user to enable GPS/network in settings
					Log.d("currentPlace", "can't get location");
					gps.showSettingsAlert();
				}
			}
		});
		
		return vi;
	}
}
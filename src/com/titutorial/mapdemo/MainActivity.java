package com.titutorial.mapdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.titutorial.mapdemo.helper.AlertDialogManager;
import com.titutorial.mapdemo.helper.ConnectionDetector;


public class MainActivity extends FragmentActivity implements OnClickListener,OnSeekBarChangeListener {
	private static final int earthRadius = 6371;
	Button typesValue;
	Button currentLocation;
	GPSTracker gps;
	EditText searchBar;
	SeekBar slider;
	TextView distanceLabel;
	public int radiusValue = 5;
	StringBuilder stringBuilder;
	ListView lv;
	Boolean useCurrentLocation = false;
	String searchBarValue = "";
	String pagetoken;
	
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();
	
	// Progress dialog
	ProgressDialog pDialog;

	ArrayList<Place> placeList;
	ArrayList<Place> findPlaces;
	
	// ListItems data
	ArrayList<HashMap<String, String>> placesListItems;
	
	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place
	public static String KEY_NAME = "name"; // name of the place
	public static String KEY_RATING = "rating"; // rating of the place
	public static String KEY_ADDRESS = "formatted_address"; // Place area name
	public static String KEY_DISTANCE = "distance"; // distance
	public static String KEY_LATITUDE = "latitude"; // latitude
	public static String KEY_LONGITUDE = "longitude"; // longitude
	
	protected CharSequence[] placeTypes = { "ATM", "Bank", "Bus Station",
			"Department Store", "Hospital", "Movie Theater", "Pharmacy",
			"Restaurant" };
	
	protected ArrayList<CharSequence> selectedTypes = new ArrayList<CharSequence>();

	// Flag for current page
	int current_page = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		currentLocation = (Button) findViewById(R.id.currentLocation);
		searchBar = (EditText) findViewById(R.id.searchBar);
		typesValue = (Button) findViewById(R.id.typesValue);
		distanceLabel = (TextView) findViewById(R.id.distanceLabel);
		slider = (SeekBar) findViewById(R.id.distanceSlider);
		lv = (ListView) findViewById(R.id.list);
		
		gps = new GPSTracker(MainActivity.this);
		currentLocation.setOnClickListener(this);
		typesValue.setOnClickListener(this);
		slider.setOnSeekBarChangeListener(this);

		// LoadMore button
		Button btnLoadMore = new Button(this);
		btnLoadMore.setText("Load More");

		// Adding Load More button to lisview at bottom
		lv.addFooterView(btnLoadMore);
		
		/**
		 * Listening to Load More button click event
		 * */
		btnLoadMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Starting a new async task
				new LoadPlaces().execute();
			}
		});
		
		searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							
							cd = new ConnectionDetector(getApplicationContext());

							// Check if Internet present
							isInternetPresent = cd.isConnectingToInternet();
							if (!isInternetPresent) {
								// Internet Connection is not present
								alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
										"Please connect to working Internet connection", false);
							}
							
							Log.d("search",	"search btn clikec - "+ searchBar.getText());
							useCurrentLocation = false;
							searchBarValue = searchBar.getText().toString();
							new LoadPlaces().execute();
							
							//hide keyboard
							hideKeyboard();
							
						}
						return false;
					}
		});
	}

	@Override
	public void onDestroy() {
	   super.onDestroy();
       SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
       Editor editor = pref.edit();
       editor.clear();
       editor.commit();
	}
	 
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.currentLocation:
			gps = new GPSTracker(MainActivity.this);
			// check if GPS enabled
			if (gps.canGetLocation()) {

				double latitude = gps.getLatitude();
				double longitude = gps.getLongitude();
				useCurrentLocation = true;
				//call search function
				new LoadPlaces().execute();
				hideKeyboard();
				// \n is for new line
				Toast.makeText(
						getApplicationContext(),
						"Your Location is - \nLat: " + latitude + "\nLong: "
								+ longitude, Toast.LENGTH_LONG).show();
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}
			break;

		case R.id.typesValue:
			Log.d("select", "typesValue clicked");
			showSelectColoursDialog();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.map_view:
			//do something when this button is pressed
			Log.d("menu", "map_view clicked");
			if(placesListItems == null){
				Toast.makeText(this, "Place list empty!", Toast.LENGTH_SHORT).show();
				return false;
			}
			Log.d("menu", "placesListItems = "+placesListItems);
			Intent i = new Intent(this, MapActivity.class);
			i.putExtra("placeList", placesListItems);
			startActivity(i);
			return true;

		default: 
			return true;
		}	
	}
	
	protected void onChangeSelectedTypes() {
		stringBuilder = new StringBuilder();

		for (CharSequence colour : selectedTypes) {
			if (stringBuilder.length() == 0) {
				stringBuilder.append(colour);
			} else {
				stringBuilder.append(", " + colour);
			}

		}
		
		if(stringBuilder.toString() == "" || stringBuilder.toString() == null){
			typesValue.setText("None");
		}else{
			typesValue.setText(stringBuilder.toString());
		}
		
	}

	protected void showSelectColoursDialog() {
		boolean[] checkedColours = new boolean[placeTypes.length];
		int count = placeTypes.length;

		for (int i = 0; i < count; i++)
			checkedColours[i] = selectedTypes.contains(placeTypes[i]);

		DialogInterface.OnMultiChoiceClickListener coloursDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				if (isChecked)
					selectedTypes.add(placeTypes[which]);
				else
					selectedTypes.remove(placeTypes[which]);

				onChangeSelectedTypes();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Types");
		builder.setMultiChoiceItems(placeTypes, checkedColours,
				coloursDialogListener);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		radiusValue = progress;
		Log.d("seek",  "onProgressChanged Seekbar Value : " + radiusValue);
		distanceLabel.setText("Search Radius: "+radiusValue+"KM");
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		Log.d("seek",  "onStartTrackingTouch Seekbar");
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		Log.d("seek",  "onStopTrackingTouch Seekbar");
	}

	public void hideKeyboard() {
	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
	}

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2)
    {
    	
        float dLat = (float) Math.toRadians(lat2 - lat1);
        float dLon = (float) Math.toRadians(lon2 - lon1);
        float a =
                (float) (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float d = earthRadius * c;
        
        double finalValue = Math.round( d * 100.0 ) / 100.0;
        //Log.d("distance", "finalValue = "+finalValue+" KM");
        
        return finalValue;
    }
    
	/**
	 * Background Async Task to Load Google places
	 * */
	class LoadPlaces extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Fetching places...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			
			PlacesService service = new PlacesService("AIzaSyDXwZXOcTmSY67VLgL9A3ycolOjVm-5COY");
			
			try {
				

				// Separeate your place types by PIPE symbol "|"
				// If you want all types places make it as null
				// Check list of types supported by google
				// 
				//String types = "cafe|restaurant"; // Listing places only cafes, restaurants
				String types;
				if(stringBuilder == null){
					types = "";
				}else{
					types = stringBuilder.toString();
				}

				 Log.d("current_page", "current_page = "+current_page);
				 if(current_page>0){
				       SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
				       pagetoken = pref.getString("next_page_token", null); // getting String
				       Log.d("pagetoken", "pagetoken = "+pagetoken);
				 }

				// Radius in meters - increase this value if you don't find any places
				//double radius = 1000; // 1000 meters
				double radius = radiusValue*1000; // rdius in meters 
				types = types.toLowerCase(Locale.ENGLISH);
				types = types.replace(", ", "|");
				types = types.replace(" ", "_");

				 findPlaces = service.findPlaces(gps.getLatitude(), 
						  gps.getLongitude(), types, radius, useCurrentLocation, searchBarValue, pagetoken, getApplicationContext());
				 
				// increment current page
				current_page += 1;		 
						 			

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * and show the data in UI
		 * Always use runOnUiThread(new Runnable()) to update UI from background
		 * thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			
			runOnUiThread(new Runnable() {
				public void run() {
					

					if (findPlaces != null) {
						// loop through each place
						 placesListItems = new ArrayList<HashMap<String,String>>();
						   for (int i = 0; i < findPlaces.size(); i++) {
							    HashMap<String, String> map = new HashMap<String, String>(); 
							    Place placeDetail = findPlaces.get(i);
							    //Log.e("placesL", "places : " + placeDetail.getName());
								// Place reference is used to get "place full details"
								map.put(KEY_REFERENCE, placeDetail.getId());
								
								// Place name
								map.put(KEY_NAME,placeDetail.getName());

								// Place rating
								map.put(KEY_RATING,placeDetail.getRating());
								
								double distance = 0;
								if(useCurrentLocation == true){
									Location locationA = new Location("LocA");
									locationA.setLatitude(gps.getLatitude());
									locationA.setLongitude(gps.getLongitude());

									Location locationB = new Location("LocB");
									locationB.setLatitude(placeDetail.getLatitude());
									locationB.setLongitude(placeDetail.getLongitude());

									distance = locationA.distanceTo(locationB);
								}else{
									//Log.d("Distance", "no distance calculation ");
								}
								
								// Place address
								map.put(KEY_ADDRESS,placeDetail.getAddress());
								// Place latitude
								map.put(KEY_LATITUDE, placeDetail.getLatitude().toString());
								// Place longitude
								map.put(KEY_LONGITUDE, placeDetail.getLongitude().toString());
								// Distance from current location
								map.put(KEY_DISTANCE, calculateDistance(gps.getLatitude(),gps.getLongitude(),placeDetail.getLatitude(),placeDetail.getLongitude() )+" KM");
								// adding HashMap to ArrayList
								placesListItems.add(map);
							}	
						   
							//Log.d("Result", "placesListItems = "+placesListItems);
							
							// list adapter
							LazyAdapter adapter = new LazyAdapter(getApplicationContext(), lv,
									MainActivity.this, placesListItems);
							
							// Adding data into listview
							lv.setAdapter(adapter);					   
					}else{
						Log.d("error", "findPlaces is null");
					}
						   
				}
				
			});

		}

	}

}

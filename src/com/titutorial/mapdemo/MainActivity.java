package com.titutorial.mapdemo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.titutorial.mapdemo.helper.AlertDialogManager;
import com.titutorial.mapdemo.helper.ConnectionDetector;
import com.titutorial.mapdemo.GooglePlaces;
import com.titutorial.mapdemo.PlacesList;
import com.titutorial.mapdemo.MainActivity.LoadPlaces;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnClickListener,OnSeekBarChangeListener {
	static final LatLng NAMAKKAL = new LatLng(11.21951, 78.167799);
	static final LatLng KPALAYAM = new LatLng(11.2818, 78.1648);
	Button typesValue;
	Button currentLocation;
	GPSTracker gps;
	EditText searchBar;
	SeekBar slider;
	TextView distanceLabel;
	public int radiusValue = 5;
	StringBuilder stringBuilder;
	
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;
	
	// Progress dialog
	ProgressDialog pDialog;
	
	// Places List
	PlacesList nearPlaces;
	
	// ListItems data
	ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();
	
	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place
	public static String KEY_NAME = "name"; // name of the place
	public static String KEY_VICINITY = "vicinity"; // Place area name
	
	protected Button selectColoursButton;
	protected CharSequence[] placeTypes = { "ATM", "Bank", "Bus Station",
			"Department Store", "Hospital", "Movie Theater", "Pharmacy",
			"Restaurant" };
	protected ArrayList<CharSequence> selectedTypes = new ArrayList<CharSequence>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		currentLocation = (Button) findViewById(R.id.currentLocation);
		searchBar = (EditText) findViewById(R.id.searchBar);
		typesValue = (Button) findViewById(R.id.typesValue);
		distanceLabel = (TextView) findViewById(R.id.distanceLabel);
		slider = (SeekBar) findViewById(R.id.distanceSlider);
		
		currentLocation.setOnClickListener(this);
		typesValue.setOnClickListener(this);
		slider.setOnSeekBarChangeListener(this);
		
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
							
							new LoadPlaces().execute();
							
							//return true;
						}
						return false;
					}
		});





		/*
		 * 
		 * GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
		 * .findFragmentById(R.id.map)).getMap();
		 * 
		 * map.setMyLocationEnabled(true); map.addMarker(new
		 * MarkerOptions().position(NAMAKKAL) .title("Namakkal")
		 * .snippet("Transport city")); map.addMarker(new MarkerOptions()
		 * .position(KPALAYAM) .title("K.Palayam") .snippet("Coolest place")
		 * .icon(BitmapDescriptorFactory
		 * .fromResource(R.drawable.ic_launcher)));
		 * 
		 * // Move the camera instantly to NAMAKKAL with a zoom of 15.
		 * map.moveCamera(CameraUpdateFactory.newLatLngZoom(NAMAKKAL, 15));
		 * 
		 * // Zoom in, animating the camera.
		 * map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		 */
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

	protected void onChangeSelectedTypes() {
		stringBuilder = new StringBuilder();

		for (CharSequence colour : selectedTypes) {
			if (stringBuilder.length() == 0) {
				stringBuilder.append(colour);
			} else {
				stringBuilder.append(", " + colour);
			}

		}

		typesValue.setText(stringBuilder.toString());
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
			pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();
			
			try {
				// Separeate your place types by PIPE symbol "|"
				// If you want all types places make it as null
				// Check list of types supported by google
				// 
				String types = "cafe|restaurant"; // Listing places only cafes, restaurants
				
				// Radius in meters - increase this value if you don't find any places
				double radius = radiusValue*1000; // 1000 meters 
				Log.d("search", "radius = "+radius);
				// get nearest places
				nearPlaces = googlePlaces.search(gps.getLatitude(),
						gps.getLongitude(), radius, types);
				

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
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					// Get json response status
					String status = nearPlaces.status;
					
					// Check for all possible status
					if(status.equals("OK")){
						// Successfully got places details
						if (nearPlaces.results != null) {
							// loop through each place
							for (Place p : nearPlaces.results) {
								HashMap<String, String> map = new HashMap<String, String>();
								
								// Place reference won't display in listview - it will be hidden
								// Place reference is used to get "place full details"
								map.put(KEY_REFERENCE, p.reference);
								
								// Place name
								map.put(KEY_NAME, p.name);
								
								
								// adding HashMap to ArrayList
								placesListItems.add(map);
							}
							
							Log.d("Result", "placesListItems = "+placesListItems);
							
							/*
							// list adapter
							ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
					                R.layout.list_item,
					                new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
					                        R.id.reference, R.id.name });
							
							// Adding data into listview
							lv.setAdapter(adapter);
							*/
						}
					}
					else if(status.equals("ZERO_RESULTS")){
						// Zero results found
						alert.showAlertDialog(MainActivity.this, "Near Places",
								"Sorry no places found. Try to change the types of places",
								false);
					}
					else if(status.equals("UNKNOWN_ERROR"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry unknown error occured.",
								false);
					}
					else if(status.equals("OVER_QUERY_LIMIT"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry query limit to google places is reached",
								false);
					}
					else if(status.equals("REQUEST_DENIED"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured. Request is denied",
								false);
					}
					else if(status.equals("INVALID_REQUEST"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured. Invalid Request",
								false);
					}
					else
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured.",
								false);
					}
				}
			});

		}

	}

}

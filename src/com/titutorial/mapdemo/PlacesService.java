package com.titutorial.mapdemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class PlacesService {
	 
	 private String API_KEY;
	 
	 public PlacesService(String apikey) {
	  this.API_KEY = apikey;
	 }
	 
	 public void setApiKey(String apikey) {
	  this.API_KEY = apikey;
	 }
	 
	 public ArrayList<Place> findPlaces(double latitude, double longitude,
	   String placeSpacification, Double radius, Boolean useCurrentLocation, String searchBarValue, String pagetoken, Context context) {
	 
	  Log.d("pagetoken", "findPlaces pagetoken = "+pagetoken);
	  String urlString = makeUrl(latitude, longitude, placeSpacification, radius, useCurrentLocation, searchBarValue, pagetoken);
	 
	  try {
	   String json = getJSON(urlString);
	 
	   System.out.println(json);
	   JSONObject object = new JSONObject(json);
	   
       try{
           String next_page_token = object.getString("next_page_token");
           Log.d("next_page_token", "next_page_token = "+next_page_token);
           
           SharedPreferences pref = context.getSharedPreferences("MyPref", 0); // 0 - for private mode
           Editor editor = pref.edit();
           editor.putString("next_page_token", next_page_token); // Storing string
           editor.commit(); // commit changes
           
           SharedPreferences pref1 = context.getSharedPreferences("MyPref", 0); // 0 - for private mode
           String next_page_token1 = pref1.getString("next_page_token", null); // getting String
           Log.d("next_page_token1", "next_page_token1 = "+next_page_token1);
      }catch(JSONException e){
    	  
          SharedPreferences pref = context.getSharedPreferences("MyPref", 0); // 0 - for private mode
          Editor editor = pref.edit();
          editor.putString("next_page_token", "empty"); // Storing string
          editor.commit(); // commit changes
      	   Log.d("next_page_token", "next_page_token tag not found");
      }

       
	   JSONArray array = object.getJSONArray("results");
	 
	   ArrayList<Place> arrayList = new ArrayList<Place>();
	   for (int i = 0; i < array.length(); i++) {
	    try {
	     Place place = Place
	       .jsonToPontoReferencia((JSONObject) array.get(i));
	    // Log.v("Places Services ", "Place Obj = " + place);
	     arrayList.add(place);
	    } catch (Exception e) {
	    }
	   }
	   return arrayList;
	  } catch (JSONException ex) {
	   Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE,
	     null, ex);
	  }
	  return null;
	 }
	 
	 // https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey
	 private String makeUrl(double latitude, double longitude, String place, Double radius, Boolean useCurrentLocation, String searchBarValue, String pagetoken) {
	  StringBuilder urlString;
	  
	  Log.d("url", "useCurrentLocation = "+useCurrentLocation);
	 
		if (useCurrentLocation == false) {
			urlString = new StringBuilder(
					"https://maps.googleapis.com/maps/api/place/textsearch/json?");
			urlString.append("query=");
			try {
				urlString.append(java.net.URLEncoder.encode(searchBarValue,
						"utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			urlString.append("&radius=");
			urlString.append(Double.toString(radius));
			urlString.append("&types=" + place);
			urlString.append("&sensor=false&key=" + API_KEY);
		} else {
			urlString = new StringBuilder(
					"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
			urlString.append("location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			urlString.append("&radius=");
			urlString.append(Double.toString(radius));
			// urlString.append("&radius=1000");
			urlString.append("&types=" + place);
			//urlString.append("&rankby=distance");
			urlString.append("&sensor=false&key=" + API_KEY);
		}
		
		if(pagetoken != null && pagetoken!= ""){
			urlString.append("&pagetoken=" + pagetoken);
		}
	  Log.d("urlString ", "urlString = "+urlString);
	  return urlString.toString();
	 }
	 
	 protected String getJSON(String url) {
	  return getUrlContents(url);
	 }
	 
	 private String getUrlContents(String theUrl) {
	  StringBuilder content = new StringBuilder();
	  try {
	   URL url = new URL(theUrl);
	   URLConnection urlConnection = url.openConnection();
	   BufferedReader bufferedReader = new BufferedReader(
	     new InputStreamReader(urlConnection.getInputStream()), 8);
	   String line;
	   while ((line = bufferedReader.readLine()) != null) {
	    content.append(line + "\n");
	   }
	   bufferedReader.close();
	  }catch (Exception e) {
	   e.printStackTrace();
	  }
	  return content.toString();
	 }
	}

package com.titutorial.mapdemo;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Place {
    private String id;
    private String icon;
    private String name;
    private String rating;
    private String address;
    private Double latitude;
    private Double longitude;
 
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }

	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	
    public void setAddress(String address) {
        this.address = address;
    }
 
    static Place jsonToPontoReferencia(JSONObject pontoReferencia) {
        try {
            Place result = new Place();
            JSONObject geometry = (JSONObject) pontoReferencia.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            result.setIcon(pontoReferencia.getString("icon"));
            result.setName(pontoReferencia.getString("name"));
            
            try{
            	 result.setRating(pontoReferencia.getString("rating"));
            }catch(JSONException e){
            	//Log.d("rating", "rating tag not found for "+pontoReferencia.getString("name"));
            }
           

			try {
				//Log.v("tPlace1 ", "before address = " + pontoReferencia.getString("formatted_address"));
				result.setAddress(pontoReferencia.getString("formatted_address"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//Log.v("tPlace2 ", "before address = " + pontoReferencia.getString("vicinity"));
				result.setAddress(pontoReferencia.getString("vicinity"));
				//e.printStackTrace();
			}
            
            result.setId(pontoReferencia.getString("id"));
            //Log.v("Place ", "result = " + result.toString());
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
	@Override
	public String toString() {
		return "Place [id=" + id + ", icon=" + icon + ", name=" + name + ", rating=" + rating
				+ ", address=" + address + ", latitude=" + latitude
				+ ", longitude=" + longitude + "]";
	}
 
}
package crs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/** Simple Listing Object */
public class Listing {
	
	/** Raw data values */
	public final String title;
	public final String manufacturer;
	public final String currency;
	public final String price;
	
	/** Construct the listing from the json object. */
	public Listing(JSONObject object) throws JSONException {
		
		// get the raw data
		this.title = object.getString("title");						// required
		this.manufacturer = object.getString("manufacturer");		// required
		this.currency = object.getString("currency");				// required 
		this.price = object.getString("price");						// required
	}
	
	/** Simple listings file reader, throws IOException for all file and json format errors. No error recovery. */
	public static List<Listing> loadListings(String filename) throws IOException {
		List<Listing> result = new ArrayList<Listing>();
		
		// simple line by line file read
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		for(String line = reader.readLine(); line != null; line = reader.readLine()) {
			try {
				result.add(new Listing(new JSONObject(line)));
			} catch (JSONException exception) {
				reader.close();
				throw new IOException("Invalid JSON format: " + exception.getMessage(), exception);
			}
		}
		reader.close();
		
		return result;
	}
	
}

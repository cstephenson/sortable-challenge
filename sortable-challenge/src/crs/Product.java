package crs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Product {
	
	/** Raw data values */
	public final String product_name;
	public final String manufacturer;
	public final String family;
	public final String model;
	public final String announced_date;
	
	/** Construct the product from the json object. */
	public Product(JSONObject object) throws JSONException {
		
		// get the raw data
		this.product_name = object.getString("product_name");		// required
		this.manufacturer = object.getString("manufacturer");		// required
		this.family = object.optString("family");					// optional 
		this.model = object.getString("model");						// required
		this.announced_date = object.getString("announced-date");	// required
	}
	
	/** Simple product file reader, throws IOException for all file and json format errors. No error recovery. */
	public static List<Product> loadProducts(String filename) throws IOException {
		List<Product> result = new ArrayList<Product>();
		
		// simple line by line file read
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		for(String line = reader.readLine(); line != null; line = reader.readLine()) {
			try {
				result.add(new Product(new JSONObject(line)));
			} catch (JSONException exception) {
				reader.close();
				throw new IOException("Invalid JSON format: " + exception.getMessage(), exception);
			}
		}
		reader.close();
		
		return result;
	}
	
}

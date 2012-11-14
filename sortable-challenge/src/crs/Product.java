package crs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Simple Product Object */
public class Product {
	
	/** Raw data values */
	public final String product_name;
	public final String manufacturer;
	public final String family;
	public final String model;
	public final String announced_date;
	
	/** Matched listings, thread safe collection */
	public final List<Listing> listings = Collections.synchronizedList(new ArrayList<Listing>());
	
	/** Construct the product from the json object. */
	public Product(JSONObject object) throws JSONException {
		
		// get the raw data
		this.product_name = object.getString("product_name");		// required
		this.manufacturer = object.getString("manufacturer");		// required
		this.family = object.optString("family");					// optional 
		this.model = object.getString("model");						// required
		this.announced_date = object.getString("announced-date");	// required
	}
	
	/** Simple products file reader, throws IOException for all file and json format errors. No error recovery. */
	public static List<Product> loadProducts(String filename) throws IOException {
		List<Product> result = new ArrayList<Product>();
		
		System.out.println("Reading products file: " + filename);
		
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
	
	/** Save the product listings, throws IOException for all file and json format errors. No error recovery. */
	public static void saveProductListings(String filename,  Map<String, Product> productMap)  throws IOException {
		
		System.out.println("Saving product listings file: " + filename);
		
		// simple line by line file writer
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for(Map.Entry<String, Product> entry : productMap.entrySet()) {
			
			Product product = entry.getValue();
			JSONObject result = new JSONObject();
			try {
				result.put("product_name", product.product_name);
				result.put("listings", product.getListings());
				result.write(writer);
			} catch (JSONException exception) {
				writer.close();
				throw new IOException("Invalid JSON format: " + exception.getMessage(), exception);
			}
			writer.write("\n");
			writer.flush();
		}
		
		// done
		writer.close();
	}
	
	/** Returns the listings as json objects. */
	public JSONArray getListings() {
		JSONArray result = new JSONArray();
		for(Listing listing : listings) {
			result.put(listing.jsonObject);	
		}
		return result;
	}
	
}

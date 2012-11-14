package crs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Storage and logic for manufacturer based lookup and identification. */
public class ManufacturerLookup {

	/** list products for each manufacturer. */
	private final Map<String, List<Product>> manufacturerToProducts = new TreeMap<String, List<Product>>();
	
	/** model lookup for each manufacturer. */
	private final Map<String, ModelLookup> manufacturerToModelLookup = new TreeMap<String, ModelLookup>();
	
	/** manufacturer lookup. */
	private final KeywordLookup lookup; 
	
	/** Construct the lookup and all needed pre-computation. */
	public ManufacturerLookup(List<Product> products) {
		
		// get the base map of cleaned manufacturer names to product names
		for(Product product : products) {
			String manufacturer = Challenge.clean(product.manufacturer);
			put(this.manufacturerToProducts, manufacturer, product);
		}
		
		// initialize the keyword lookup
		this.lookup = new KeywordLookup(this.manufacturerToProducts.keySet(), Challenge.MANUFACTURER_MATCH_DELTA);
		
		// create the model lookups for each manufacturer
		for(Map.Entry<String, List<Product>> entry : this.manufacturerToProducts.entrySet()) {
			String manufacturer = entry.getKey();
			List<Product> manufacturerProducts = entry.getValue();
			ModelLookup manufacturerModelLookup = new ModelLookup(manufacturerProducts);
			this.manufacturerToModelLookup.put(manufacturer, manufacturerModelLookup);
		}
	}
	
	/** refactored map with list putting. */
	private static void put(Map<String, List<Product>> map, String key, Product value) {
		List<Product> list = map.get(key);
		if(list == null) {
			list = new ArrayList<Product>();
			map.put(key, list);
		}
		list.add(value);
	}
	
	/** 
	 * Do a multi word lookup of the listing's manufacturer returning the best matching manufacturer. 
	 * returns null for no good match. 
	 */
	public String lookupManufacturer(Listing listing) {
		String manufacturer = Challenge.clean(listing.manufacturer);
		String title = Challenge.clean(listing.title);
		return lookupManufacturer(manufacturer, title);
	}
	
	/** 
	 * Do a multi word lookup of the listing's manufacturer returning the best matching manufacturer. 
	 * returns null for no good match. 
	 */
	public String lookupManufacturer(String manufacturer, String title) {
		
		// lookup based on only manufacturer first
		String result = this.lookup.lookup(manufacturer, null);
		
		// lookup on manufacturer + title if the first did not return anything
		if(result == null) {
			result = this.lookup.lookup(manufacturer + " " + title, null);
		}
		
		return result;
	}
	
	/** 
	 * Do a multi word lookup of the listing's manufacturer and model (and family) returning the best matching product name. 
	 * returns null for no good match. 
	 */
	public String lookupProductName(Listing listing) {
		String manufacturer = Challenge.clean(listing.manufacturer);
		String title = Challenge.clean(listing.title);
		return lookupProductName(manufacturer, title);
	}
	
	/** 
	 * Do a multi word lookup of the listing's manufacturer and model (and family) returning the best matching product name. 
	 * returns null for no good match. 
	 */
	public String lookupProductName(String manufacturer, String title) {
		
		// find the manufacturer first
		String manufacturerResult = lookupManufacturer(manufacturer, title);
		
		// lookup within the manufacturer's products
		if(manufacturerResult != null) {
			ModelLookup manufacturerLookup = manufacturerToModelLookup.get(manufacturerResult);
			if (manufacturerLookup != null) {
				String result = manufacturerLookup.lookupProductName(manufacturer, title);
				if(result != null) {
					// found it
					return result;
				}
			}			
		}
		
		// not found
		return null;
	}
	
}

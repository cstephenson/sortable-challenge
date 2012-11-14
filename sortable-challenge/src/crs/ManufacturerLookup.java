package crs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Storage and Logic for manufacturer identification. */
public class ManufacturerLookup {

	/** list product names for each manufacturer. */
	private final Map<String, List<String>> manufacturerToProductNames = new TreeMap<String, List<String>>();
	
	/** list of one to one manufacturer name aliases. */
	private final KeywordLookup lookup; 
	
	/** Construct the lookup and all needed pre-computation. */
	public ManufacturerLookup(List<Product> products) {
		
		// get the base map of cleaned manufacturer names to product names
		for(Product product : products) {
			String manufacturer = Challenge.clean(product.manufacturer);
			List<String> list = this.manufacturerToProductNames.get(manufacturer);
			if(list == null) {
				list = new ArrayList<String>();
				this.manufacturerToProductNames.put(manufacturer, list);
			}
			list.add(product.product_name);
		}
		
		// initialize the keyword lookup
		this.lookup = new KeywordLookup(this.manufacturerToProductNames.keySet());
	}
	
	/** Do a multi word lookup of the listings manufacturer and return the best result. (null for no match) */
	public String lookup(Listing listing) {
		
		// lookup based on only manufacturer first
		String manufacturer = Challenge.clean(listing.manufacturer);
		String result = this.lookup.lookup(manufacturer);
		
		// lookup on manufacturer + title if the first did not return anything
		if(result == null) {
			String title = Challenge.clean(listing.title);
			result = this.lookup.lookup(manufacturer + " " + title);
		}
		
		return result;
	}
	
}

package crs;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Challenge {
	
	/** Challenge Entry Point */
	public static void main(String[] args) throws IOException {
		
		// chriss - testing args
		args = new String[] {
			"C:\\Users\\Chris Stephenson\\git\\repository\\sortable-challenge\\data\\products.txt",
			"C:\\Users\\Chris Stephenson\\git\\repository\\sortable-challenge\\data\\listings.txt",
			"f:\fail"
		};
		
		// simple usage message
		if(args.length != 3)
		{
			System.out.println("Usage is: java [-options] " + Challenge.class.getName() + " [product file] [listing file] [output file]");
			return;
		}
		
		// load products and setup lookups
		List<Product> products = Product.loadProducts(args[0]);
		ManufacturerLookup manufacturerLookup = new ManufacturerLookup(products);
		
		// load and process listings
		List<Listing> listings = Listing.loadListings(args[1]);
		
		for(Listing listing : listings) {
			manufacturerLookup.lookup(listing);
		}
		
		// chriss - debug
//		Map<String, Integer> product_manufacturer = new TreeMap<String, Integer>();
//		for(Product product : products) {
//			if(!product_manufacturer.containsKey(product.manufacturer)) {
//				product_manufacturer.put(product.manufacturer, 0);
//			}
//			product_manufacturer.put(product.manufacturer, 1 + product_manufacturer.get(product.manufacturer));
//		}
//		
//		Map<String, Integer> product_family = new TreeMap<String, Integer>();
//		for(Product product : products) {
//			if(!product_family.containsKey(product.family)) {
//				product_family.put(product.family, 0);
//			}
//			product_family.put(product.family, 1 + product_family.get(product.family));
//		}
//		
//		Map<String, Integer> listings_manufacturer = new TreeMap<String, Integer>();
//		for(Listing listing : listings) {
//			if(!listings_manufacturer.containsKey(listing.manufacturer)) {
//				listings_manufacturer.put(listing.manufacturer, 0);
//			}
//			listings_manufacturer.put(listing.manufacturer, 1 + listings_manufacturer.get(listing.manufacturer));
//		}
//		
//		System.out.println();
//		System.out.println("product_manufacturer:");
//		for(Map.Entry<String, Integer> entry : product_manufacturer.entrySet()) {
//			System.out.println(entry.getKey() + ": " + entry.getValue());
//		}
//		
//		System.out.println();
//		System.out.println("product_family:");
//		for(Map.Entry<String, Integer> entry : product_family.entrySet()) {
//			System.out.println(entry.getKey() + ": " + entry.getValue());
//		}
//		
//		System.out.println();
//		System.out.println("listings_manufacturer:");
//		for(Map.Entry<String, Integer> entry : listings_manufacturer.entrySet()) {
//			System.out.println(entry.getKey() + ": " + entry.getValue());
//		}
		
	}
	
	/** Cleans out any punctuation letters, converts to lower case and reduces whitespace. */
	public static String clean(String string) {
		
		// convert to lower case
		string = string.toLowerCase();
		
		// convert punctuation into nothing
		string = string.replaceAll("\\p{Punct}+", "");
		
		// convert whitespace into single space
		string = string.replaceAll("\\p{Space}+", " ");
		
		return string;
	}
}

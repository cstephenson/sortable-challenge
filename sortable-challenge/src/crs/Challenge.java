package crs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Challenge {
	
	/** Default delta amount to get good matches. */
	public static final float MANUFACTURER_MATCH_DELTA = 0.45f;
	public static final float MODEL_MATCH_DELTA = 0.25f;
	
	/** Small words in the model will be handled with extra processing.*/
	public static final int SMALL_WORD_SIZE = 3;
	
	/** Number of threads to use for matching. */
	public static final int THREADS = 4;
	
	/** Problem words (occur too often, or too common), these can be a problem, and thus should be ignored. */
	public static final Set<String> IGNORABLE_WORDS = new HashSet<String>();
	static {
		IGNORABLE_WORDS.add("zoom");		// Really only 'zoom' is the problem.
		IGNORABLE_WORDS.add("camera");		// just add these others because they are common and do not identify one camera from the other  
		IGNORABLE_WORDS.add("digital");
		IGNORABLE_WORDS.add("optical");
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
	
	/** Separate a keyword into indedivdual words. */
	public static List<String> split(String keyword) {
		List<String> words = new ArrayList<String>();
		for (String word : keyword.split(" ")) {
			if (word.length() == 0)
				continue;
			words.add(word);
		}
		return words;
	}
	
	/** Challenge Entry Point */
	public static void main(String[] args) throws IOException {

		// simple usage message
		if(args.length != 3)
		{
			System.out.println("Usage is: java -jar sortable-challenge.jar [product file] [listing file] [output file]");
			return;
		}
		
		long startTime = System.currentTimeMillis();
		
		// load products and setup lookups
		List<Product> products = Product.loadProducts(args[0]);
		ManufacturerLookup manufacturerLookup = new ManufacturerLookup(products);
		
		// map to store matching results and link them to products
		Map<String, Product> productMap = new LinkedHashMap<String, Product>();
		for(Product product : products) {
			productMap.put(product.product_name, product);
		}
		
		// load and process listings
		List<Listing> listings = Listing.loadListings(args[1]);
		Listing.matchListings(listings, productMap, manufacturerLookup);
		
		// save output
		Product.saveProductListings(args[2], productMap);
		
		// done
		long endTime = System.currentTimeMillis();
		System.out.println("Done in " + (endTime-startTime) + "ms");
	}
	
	
}

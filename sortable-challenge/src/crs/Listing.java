package crs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

/** Simple Listing Object */
public class Listing {
	
	/** Raw json object is echoed in the final output*/
	public final JSONObject jsonObject;
	
	/** Raw data values */
	public final String title;
	public final String manufacturer;
	public final String currency;
	public final String price;
	
	/** Construct the listing from the json object. */
	public Listing(JSONObject object) throws JSONException {
		this.jsonObject = object;
		
		// get the raw data
		this.title = object.getString("title");						// required
		this.manufacturer = object.getString("manufacturer");		// required
		this.currency = object.getString("currency");				// required 
		this.price = object.getString("price");						// required
	}
	
	/** Simple listings file reader, throws IOException for all file and json format errors. No error recovery. */
	public static List<Listing> loadListings(String filename) throws IOException {
		List<Listing> result = new ArrayList<Listing>();
		
		System.out.println("Reading listings file: " + filename);
		
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
	
	/** Does the listing matching loop. */
	public static void matchListings(List<Listing> listings, final Map<String, Product> productMap, final ManufacturerLookup manufacturerLookup) {
		
		System.out.println("Matching listings...");
		
//		// single threaded implementation
//		for(Listing listing : listings) {
//			
//			// match product name
//			String productName = manufacturerLookup.lookupProductName(listing);
//			if(productName == null) {
//				continue;
//			}
//			
//			// add to the product's listings
//			Product product = productMap.get(productName);
//			if(product == null) {
//				continue;
//			}
//			
//			product.listings.add(listing);
//		}
		
		// mutli-threaded mostly because it was easy to implement for this algorithm 
		// and does speed up the matching a lot for multi-core machines.
		
		// make a thread safe copy to use as the source (linked list since we are removing from the front)
		final List<Listing> source = Collections.synchronizedList(new LinkedList<Listing>(listings));
		
		// objects used to signal that all threads are done
		final Object signal = new Object();
		final AtomicInteger remainingThreads = new AtomicInteger();
		
		for(int i = 0; i < Challenge.THREADS; ++i) {
			
			// another thread added
			remainingThreads.incrementAndGet();
			
			// very simple threads implementation of the above code
			Runnable runner = new Runnable() {
				public void run() {
					while(true) {
						
						// remove head items until collection is empty
						Listing listing;
						try {
							listing = source.remove(0);
						} catch (IndexOutOfBoundsException e) {
							// lists throw IndexOutOfBoundsException when remove is called on an empty list
							// we will use that as a signal for done
							break;
						}
					
						// match product name
						String productName = manufacturerLookup.lookupProductName(listing);
						if(productName == null) {
							continue;
						}
											
						Product product = productMap.get(productName);
						if(product == null) {
							continue;
						}
						
						// add to the product's listings
						// product.listings collection is thread safe
						product.listings.add(listing);
					}
					
					// signal when all threads are done
					int remaining = remainingThreads.decrementAndGet();
					if(remaining <= 0) {
						synchronized (signal) {
							signal.notify();
						}
					}
				}
			};
			
			// run the matcher
			Thread thread = new Thread(runner);
			thread.start();
		}
		
		// wait for all threads to finish
		while(remainingThreads.get() > 0) {
			try {
				synchronized (signal) {
					signal.wait();
				}
			} catch (InterruptedException e) {
				// dont care, try again
			}
		}
	}

	
}

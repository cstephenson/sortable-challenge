package crs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Storage and logic for model (and family) based lookup and identification. */
public class ModelLookup {

	/** list product names for each model (and family). */
	private final Map<String, List<String>> modelToProductNames = new TreeMap<String, List<String>>();
	
	/** model lookup. */
	private final KeywordLookup lookup;
	
	/** Construct the lookup and all needed pre-computation. */
	public ModelLookup(List<Product> products) {
		
		// get the base map of cleaned model (and family) names to product names
		for(Product product : products) {
			String model = Challenge.clean(product.model);
			String family = Challenge.clean(product.family);
			
			for(String modelCombination : combinations(model)) {
				put(this.modelToProductNames, modelCombination, product.product_name);
				
				if(family.length() > 0) {
					put(this.modelToProductNames, modelCombination + " " + family, product.product_name);
				}	
			}	
		}
		
		// initialize the keyword lookup
		this.lookup = new KeywordLookup(this.modelToProductNames.keySet(), Challenge.MODEL_MATCH_DELTA);
	}
	
	/** refactored map with list putting. */
	private static void put(Map<String, List<String>> map, String key, String value) {
		List<String> list = map.get(key);
		if(list == null) {
			list = new ArrayList<String>();
			map.put(key, list);
		}
		list.add(value);
	}
	
	/** Model number could (and often does) have the spaces removed, this will give a better list of models with and without spaces */
	private static Set<String> combinations(String model) {
		
		// start with the model as is
		Set<String> result = new LinkedHashSet<String>();
		result.add(model);
		
		// separate the words
		List<String> words = Challenge.split(model);
		
		// might only be 1 word, then we do not need to do this
		if(words.size() < 2) {
			return result;
		}
		
		// check for small words
		for(int i = 0; i < words.size(); ++i) {
			
			String word = words.get(i);
			if(word.length() > Challenge.SMALL_WORD_SIZE) {
				// not small
				continue;
			}
			
			// remove space to the left
			if(i > 0) {
				StringBuilder left = new StringBuilder();
				for(int j = 0; j < words.size(); ++j) {
					if(j != 0 && j != i)
						left.append(" ");
					left.append(words.get(j));
				}
				result.add(left.toString());
			}
			
			
			// remove space to the right
			if(i < words.size() - 1) {
				StringBuilder right = new StringBuilder();
				for(int j = 0; j < words.size(); ++j) {
					if(j != 0 && j != i + 1)
						right.append(" ");
					right.append(words.get(j));
				}
				result.add(right.toString());
			}
		}
		
		return result;
	}
	
	/** 
	 * Do a multi word lookup of the listing's model (and family) returning the best matching product name. 
	 * returns null for no good match. 
	 */
	public String lookupProductName(Listing listing) {
		String manufacturer = Challenge.clean(listing.manufacturer);
		String title = Challenge.clean(listing.title);
		return lookupProductName(manufacturer, title);
	}
	
	/** 
	 * Do a multi word lookup of the listing's model (and family) returning the best matching product name. 
	 * returns null for no good match. 
	 */
	public String lookupProductName(String manufacturer, String title) {
		
		// lookup based on title
		String result = this.lookup.lookup(title, this.modelToProductNames);
		return result;
	}
}

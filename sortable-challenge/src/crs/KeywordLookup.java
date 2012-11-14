package crs;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Keyword lookup with aliasing of keywords. */
public class KeywordLookup {
	
	/** Default delta amount to get good matches. */
	public static final float DEFAULT_DELTA = 0.45f;

	/** A list of the keywords we can look for. */
	private final Set<String> keywords = new TreeSet<String>();
	
	/** This is the amount better of a match before it is a match, if the amount is less than this, then not good enough. */
	private final float delta;
	
	/** map of one to one keyword aliases. */
	private final Map<String, String> singleAliases = new TreeMap<String, String>();
	
	/** map of one to many keyword aliases. */
	private final Map<String, Set<String>> sharedAliases = new TreeMap<String, Set<String>>();
	
	/** Construct the lookup with list of keywords. */
	public KeywordLookup(Collection<String> keywords) {
		this(keywords, DEFAULT_DELTA);
	}
	
	/** Construct the lookup with list of keywords. */
	public KeywordLookup(Collection<String> keywords, float delta) {
		this.keywords.addAll(keywords);
		this.delta = delta;
		
		// alias all the words of the keywords
		for(String keyword : this.keywords) {
			for(String word : keyword.split(" ")) {
				if(word.length() == 0) continue;
				this.addAlias(word, keyword);
			}
		}
	}
	
	/** Add another alias to the lookup. */
	public void addAlias(String alias, String keyword) {
		
		if (sharedAliases.containsKey(alias)) {
			// already present in the one2many list, so just add to it
			sharedAliases.get(alias).add(keyword);
			
		} else if (singleAliases.containsKey(alias)) {
			// already present in the one2one list, check for duplicate or new
			String otherKeyword = singleAliases.get(alias);
			
			if(alias.equals(otherKeyword)) {
				// same item, leave everything alone
			} else {
				// new item, move to the one2many list
				singleAliases.remove(alias);
				Set<String> set = new TreeSet<String>();
				set.add(otherKeyword);
				set.add(keyword);
				sharedAliases.put(alias, set);
			}
			
		} else {
			// not present anywhere, add to one2one
			singleAliases.put(alias, keyword);
		}
	}
	
	/** Do a multi word lookup and return the best result. */
	public String lookup(String keyword) {
		
		// attempt a direct lookup
		// chriss - should I?
		
		// attempt to locate each word using the aliases
		Map<String, Float> resultMap = new TreeMap<String, Float>();
		for(String word : keyword.split(" ")) {
			if(word.length() == 0) continue;
			
			if (sharedAliases.containsKey(word)) {
				// found in the many2one map
				add(resultMap, sharedAliases.get(word), 1);
				
			} else if (singleAliases.containsKey(word)) {
				// found in the one2one map
				add(resultMap, singleAliases.get(word), 1);
			}
		}
		
		// find best and 2nd best matches
		float best1_value = 0;
		float best2_value = 0;
		String best1_keyword = null;
		String best2_keyword = null;
		
		for(Map.Entry<String, Float> entry : resultMap.entrySet()) {
			
			float entry_value = entry.getValue();
			String entry_keyword = entry.getKey();
			
			if (entry_value > best1_value) {
				// found a best (also move old 1st to 2nd) 
				best2_value = best1_value;
				best1_value = entry_value;
				best2_keyword = best1_keyword;
				best1_keyword = entry_keyword;
				
			} else if (entry_value > best2_value) {
				// found a 2nd best
				best2_value = entry_value;
				best2_keyword = entry_keyword;
			}
		}
		
		// only return the best result if it is significantly better then the 2nd best
		if(best1_value > best2_value + this.delta)
			return best1_keyword;
		
		// no good match
		return null;		
	}
	
	/** refactored result adding. */
	private static void add(Map<String, Float> map, Set<String> keys, float amount) {
		amount = amount / keys.size(); // divid by zero should not be possible
		for(String key : keys) {
			add(map, key, amount);
		}
	}
	
	/** refactored result adding. */
	private static void add(Map<String, Float> map, String key, float amount) {
		if(map.containsKey(key)) {
			map.put(key, amount + map.get(key));
		} else {
			map.put(key, amount);
		}
	}
}

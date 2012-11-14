package crs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Keyword lookup with aliasing of keywords. */
public class KeywordLookup {
	
	/** Default delta amount to get good matches. */
	public static final float DEFAULT_DELTA = 0.45f;

	/** A list of the keywords we can look for. */
	private final Map<String, List<String>> keywords = new TreeMap<String, List<String>>();
	
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
		this.delta = delta;
		
		// alias all the words of the keywords
		for(String keyword : keywords) {
			List<String> words = Challenge.split(keyword);
			this.keywords.put(keyword, words);
			for(String word : words) {
				this.addAlias(word, keyword);
			}
		}
	}
	
	/** Add another alias to the lookup. */
	public void addAlias(String alias, String keyword) {
		
		// some words appear way too often and should be ignored
		if(Challenge.IGNORABLE_WORDS.contains(alias)) {
			return;
		}
		
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
	
	/** 
	 * Do a multi word lookup and return the best result.
	 * Reduce is used to reduce the result map further 
	 */
	public String lookup(String keyword, Map<String, List<String>> reduce) {
		
		// separate the words
		List<String> words = Challenge.split(keyword);
		
		// attempt to locate each word using the aliases
		// note: add positive value for found keywords
		Map<String, Float> resultMap = new TreeMap<String, Float>();
		for(String word : words) {
			if (sharedAliases.containsKey(word)) {
				// found in the many2one map
				add(resultMap, sharedAliases.get(word), 1);
				
			} else if (singleAliases.containsKey(word)) {
				// found in the one2one map
				add(resultMap, singleAliases.get(word), 1);
			}
		}

		// check for missing words (ignore small words) (only check for longer keywords)
		// note: reduce value by ratio of missing words from keyword
		checkMissingWords(resultMap, words);
		
		// reduce the map
		if(reduce != null) {
			resultMap = reduce(resultMap, reduce);
		}
		
		// return the best result
		return selectBestResult(resultMap);
	}
	
	/** Checks match values for missing keywords and reduces the value by the ratio of found/total. */
	private void checkMissingWords(Map<String, Float> resultMap, List<String> words) {
		Set<String> sortedWords = new TreeSet<String>(words);
		
		// check each match
		for(String possibleMatch : resultMap.keySet()) {
			
			// get the full keyword list
			List<String> matchWords = this.keywords.get(possibleMatch);
			
			// count the missing words
			int missingCount = 0;
			int totalCount = 0;
			for(String matchWord : matchWords) {
				totalCount += 1;
				
				if(sortedWords.contains(matchWord)) {
					continue;
				}
				
				missingCount += 1;				
			}
			
			// modify the results to have reduced value if missing parts of the key word
			if(totalCount > 0 && missingCount > 0) {
				// x *= 1 - (missing / total)
				multi(resultMap, possibleMatch, 1f - missingCount * 1f / totalCount);
			}
		}
	}
	
	/** Find the first index such that 'items.contains(list.get(index))', or -1 if not found */
	private static int findFirstIndex(List<String> list, List<String> items)
	{
		for(int index = 0; index < list.size(); ++index) {
			String item = list.get(index);
			if(items.contains(item)) {
				return index;
			}
		}
		return -1;
	}
	
	/** Find the last index such that 'items.contains(list.get(index))', or -1 if not found */
	private static int findLastIndex(List<String> list, List<String> items)
	{
		for(int index = list.size() - 1; index >= 0; --index) {
			String item = list.get(index);
			if(items.contains(item)) {
				return index;
			}
		}
		return -1;
	}
	
	/** Search the result map for the best result, and only return it if it is significantly better then the 2nd best result. */
	private String selectBestResult(Map<String, Float> resultMap) {
		
		// find best and 2nd best matches
		float bestValue1 = 0;
		float bestValue2 = 0;
		String bestKeyword1 = null;
		String bestKeyword2 = null;
		
		for(Map.Entry<String, Float> entry : resultMap.entrySet()) {
			
			float entry_value = entry.getValue();
			String entry_keyword = entry.getKey();
			
			if (entry_value > bestValue1) {
				// found a best (also move old 1st to 2nd) 
				bestValue2 = bestValue1;
				bestValue1 = entry_value;
				bestKeyword2 = bestKeyword1;
				bestKeyword1 = entry_keyword;
				
			} else if (entry_value > bestValue2) {
				// found a 2nd best
				bestValue2 = entry_value;
				bestKeyword2 = entry_keyword;
			}
		}
		
		// return the best result if it is significantly better then the 2nd best
		if(bestValue1 > bestValue2 + this.delta) {
			return bestKeyword1;
		}
		
		// no good match
		return null;	
	}
	
	/** Add a set of keys, dividing the value equally */
	private static void add(Map<String, Float> map, Collection<String> keys, float amount) {
		// no divide by zero's please
		if(keys.size() == 0)
			return;
		
		amount = amount / keys.size(); 
		for(String key : keys) {
			add(map, key, amount);
		}
	}
	
	/** Add a single key with a set amount */
	private static void add(Map<String, Float> map, String key, float amount) {
		if(map.containsKey(key)) {
			map.put(key, amount + map.get(key));
		} else {
			map.put(key, amount);
		}
	}
	
	/** Mutliply a single key with a set amount */
	private static void multi(Map<String, Float> map, String key, float amount) {
		if(map.containsKey(key)) {
			map.put(key, amount * map.get(key));
		} else {
			// nothing to multiply
		}
	}
	
	/** Reduce the map using a 2nd mapping. */
	private static Map<String, Float> reduce(Map<String, Float> map, Map<String, List<String>> reduce) {
		Map<String, Float> result = new TreeMap<String, Float>();
		
		for(Map.Entry<String, Float> entry : map.entrySet()) {
			String key = entry.getKey();
			float value = entry.getValue();
			add(result, reduce.get(key), value);
		}
		
		return result;
	}
}

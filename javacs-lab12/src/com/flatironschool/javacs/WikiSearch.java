package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * just returns the relavance score of a url
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * returns a WikiSearch map
	 * 
	 * get all urls that have search term that and what is being test, add to HashMap and return
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // returns a WikiSearch object (map) with the union of urls of 2 search terms to their relavance
		Map<String, Integer> union = new HashMap<String, Integer>(map);
		// compute score of new urls
		int score;
		// go through each url and get relavance score with the map url
		for (String url: that.map.keySet()) {
			int score1 = this.getRelevance(url);
			int score2 = that.getRelevance(url);
			score = totalRelevance(score1, score2);
			// add score and url to hashmap
			union.put(url, score);
		}
		return new WikiSearch(union);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> intersection = new HashMap<String, Integer>();
		int score, score1, score2;
		for (String url: that.map.keySet()) {
			if (this.map.containsKey(url)) {
				score1 = this.map.get(url);
				score2 = that.map.get(url);
				score = totalRelevance(score1, score2);
				intersection.put(url, score);
			}
		}
		return new WikiSearch(intersection);
	}
	
	/**
	 * Computes the difference of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		Map<String, Integer> difference = new HashMap<String, Integer>(map);
		for (String url: that.map.keySet()) {
			difference.remove(url);
		}
		return new WikiSearch(difference);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * results are returned in increasing order of relevance
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
		// makes linked list and initializes the entries to have the entries found in map
		List<Entry<String, Integer>> sorted = new LinkedList<Entry<String, Integer>>(this.map.entrySet());
				
		Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> x, Entry<String, Integer> y) {
				// returns int based on whether x's value is greater than, less than, equal to y's value
				return x.getValue().compareTo(y.getValue());
			}
		};
		// sorts specified list according to order induced by the comparator
		Collections.sort(sorted, comparator);
		return sorted;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}

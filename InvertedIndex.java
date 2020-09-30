import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.List;

/**
 * Class that defines the structure of my inverted index
 * 
 * Structure: Each WORD maps to several LOCATIONS, each LOCATION maps to several
 * POSITIONS Thus Map<String, Map<String, List<Integer>>>
 */
public class InvertedIndex {
  // TODO Make the data private
  // TODO Should not have a query parser member
  // TODO Minus a new addAll method this should be exactly the same as from project 2

	/**
	 * Declaring type of inverted index
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * Declaring wordCounts map
	 */
	private final TreeMap<String, Integer> wordCounts;
	
	/**
	 * Declaring queryParser variable
	 */
	private final QueryParser queryParser;
	
	/**
	 * Instantiating my inverted index
	 */
	public InvertedIndex() {
		invertedIndex = new TreeMap<>();
		wordCounts = new TreeMap<>();
		queryParser = new QueryParser(this);
	}

	/**
	 * Allows to pass in entries to the Inverted index data structure
	 * 
	 * @param word     to be added to inverted index
	 * @param location of word in a file
	 * @param position (index) in which the file was found
	 */
	public void addEntry(String word, String location, int position) {
		invertedIndex.putIfAbsent(word, new TreeMap<String, TreeSet<Integer>>());
		invertedIndex.get(word).putIfAbsent(location, new TreeSet<Integer>());
		boolean result = invertedIndex.get(word).get(location).add(position);
		if (result) {
			incrementWordCount(location);
		}
	}

	/**
	 * Increments word count
	 *
	 * @param location of of which is counted
	 */
	protected void incrementWordCount(String location) {
		wordCounts.put(location, wordCounts.getOrDefault(location, 0) + 1);
	}

	/**
	 * Checks if word is contained in the inverted index
	 *
	 * @param word to be checked
	 * @return boolean
	 */
	public boolean contains(String word) {
		return invertedIndex.containsKey(word);
	}

	/**
	 * Checks if word is contained in the inverted index in a file
	 *
	 * @param word     to be checked
	 * @param location of word in a file
	 * @return boolean if true or false
	 */
	public boolean contains(String word, String location) {
		return contains(word) && invertedIndex.get(word).containsKey(location);
	}

	/**
	 * Checks if word is contained in the inverted index in a file and a position
	 *
	 * @param word     to be checked
	 * @param location a file that to be searched
	 * @param position position (index) of the word
	 * @return boolean works as a switcher
	 */
	public boolean contains(String word, String location, int position) {
		return contains(word, location) && invertedIndex.get(word).get(location).contains(position);
	}

	/**
	 * Getter of Pathname ALL LOCATIONS in the inverted index
	 * 
	 * @return unmodifiable collection to ensure data integrity
	 */
	public Set<String> getLocations() {
		return Collections.unmodifiableSet(wordCounts.keySet());
	}
	
	/**
	 * Writes the inverted index as pretty JSON to the specified file path
	 *
	 * @param path Path to be written
	 * @throws IOException throws exception
	 */
	public void writeIndex(Path path) throws IOException {
		JsonWriter.asVeryNestedObject(Collections.unmodifiableMap(invertedIndex), path);
	}

	/**
	 * Writes word-frequency count by location as pretty JSON to the specified path
	 *
	 * @param path Path to be written
	 * @throws IOException throws exception
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.asObject(Collections.unmodifiableMap(wordCounts), path);
	}

	/**
	 * Returns a String object of the inverted index as pretty JSON
	 *
	 * @return String representation (JSON) of the inverted index
	 */
	@Override
	public String toString() {
		return JsonWriter.asVeryNestedObject(Collections.unmodifiableMap(invertedIndex));
	}
	
	/**
	 * Makes the whole thing run
	 * 
	 * @param queryPath		query location
	 * @param exact			flag to check if exact or partial search will be performed 
	 * @throws IOException	throws exception
	 */
	public void performSearch(Path queryPath, boolean exact) throws IOException {
		queryParser.performSearch(queryPath, exact);
	}
	
	/**
	 * partial/ exact "switcher" function
	 * 
	 * @param queries 	query to be processed
	 * @param exact 	determines the switch of which function to run
	 * @return List of SearchMetrics objects
	 */
	public List<SearchMetrics> search(Set<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}
	
	/**
	 * exact search
	 * 
	 * @param query (set of words in a query line)
	 * @return list of search results, containing a result for each page (location) where a query word was found
	 * 	results sorted by SearchMetrics score
	 */
	public List<SearchMetrics> exactSearch(Set<String> query) {
		List<SearchMetrics> results = new ArrayList<>(); // (we need this to sort and store mutable objects)
		Map<String, SearchMetrics> lookup = new HashMap<>(); //lookup search results by location
		
		for (String word : query) {
			if (contains(word)) {
				searchHelper(lookup, results, word);
			}
		}
		Collections.sort(results);
		return results;
	}
	
	
	/**
	 * Partial search of query and returns a list of relevant SearchMetrics
	 * 
	 * @param query (set of words in a query line)
	 * @return list of search results, containing a result for each page (location) where a query word was found
	 * 	results sorted by SearchMetrics score
	 * Inner class in compliance with Professor Engle's code review
	 * 
	 */
	/**
	 * Partial search of query and returns a list of relevant SearchMetrics
	 * 
	 * @param query (set of words in a query line)
	 * @return list of search results, containing a result for each page (location) where a query word was found
	 * 	results sorted by SearchMetrics score
	 */
	public List<SearchMetrics> partialSearch(Set<String> query) {
		List<SearchMetrics> results = new ArrayList<>(); // (we need this to sort and store mutable objects)
		Map<String, SearchMetrics> lookup = new HashMap<>(); //lookup search results by location
		
		for (String word : query) {
			SortedMap<String, TreeMap<String, TreeSet<Integer>>> partialMatchingInvertedIndex = invertedIndex.tailMap(word);
			for (String partialMatchingWord : partialMatchingInvertedIndex.keySet()) {
				if (!partialMatchingWord.startsWith(word)) break;
				searchHelper(lookup, results, partialMatchingWord);
			}
		}	
		Collections.sort(results);
		return results;
	}
	
	/**
	 * Helper method to streamline the adding and updating of SearchMetrics
	 * 
	 * @param lookup 	Object that holds query and SearchMetric object
	 * @param results	list of SearchMetrics objects with data member metrics
	 * @param word used in the outer map as declared by the inverted index
	 */
	protected void searchHelper(Map<String, SearchMetrics> lookup, List<SearchMetrics> results,
			String word) {
		for (String location : invertedIndex.get(word).keySet()) {
			if (!lookup.containsKey(location)) {
				SearchMetrics metrics = new SearchMetrics(location);
				lookup.put(location, metrics);
				results.add(metrics);
			}
			lookup.get(location).update(word);
		}
	}
	
	/**
	 * @param local is an InvertedIndex representing only one "location" (file)
	 */
	public void addAll(InvertedIndex local) {
		for (String word : local.invertedIndex.keySet()) {
			if (!this.invertedIndex.containsKey(word)) {
				this.invertedIndex.put(word, local.invertedIndex.get(word));
			}
			else {
			  /*
			   * TODO 
			   * For each location in the other index
			   * If the word exists but the location doesn't, can put the entire inner set
			   * Else if the word and location exist, you have 2 different position sets you can combine using set.addAll
			   */
				//A, location doesn't exist in global index? put inner set
				//B, location does exist in global index? [global-position-set].addAll([local-position-set])
			  
				for (String location : local.invertedIndex.get(word).keySet()) {
					if(!this.invertedIndex.get(word).containsKey(location)) this.invertedIndex.get(word).put(location, local.invertedIndex.get(word).get(location)); //Case A
					else this.invertedIndex.get(word).get(location).addAll(local.invertedIndex.get(word).get(location));
				}
			}
		}
		for(String location : local.wordCounts.keySet()) {
			this.wordCounts.putIfAbsent(location,0);
			this.wordCounts.put(location,this.wordCounts.get(location)+local.wordCounts.get(location));
		}
	}
	
	

	/**
	 * SearchMetrics inner class according to code review suggestions
	 * 
	 * @author pcarbajal
	 */
	public class SearchMetrics implements Comparable<SearchMetrics> {
		
		/**
		 * count variable definition
		 */
		protected int count;
		
		/**
		 * score variable definition
		 */
		protected double score;
		
		/**
		 * where variable definition
		 */
		protected final String where;
		
		/**
		 * Initializes SearchMetrics object
		 * 
		 * @param where location that the SearchMetrics object describes
		 * */
		public SearchMetrics(String where) {
			super();
			this.where = where;
			this.count = 0;
		}

		/**
		 * Auto generated getter
		 * 
		 * @return (word) count data member
		 */
		public int getCount() {
			return count;
		}

		/**
		 * getter for 'score' data member of SearchMetric object
		 * Adds count 
		 * 
		 * @param count	which is used later to compute SearchMetrics object data
		 */
		public void addCount(int count) {
			this.count += count;
		}

		/**
		 * Auto generated getter for score
		 * 
		 * @return score data member
		 */
		public double getScore() {
			return score;
		}


		/**
		 * getter for 'where' data member of SearchMetric object
		 * 
		 * @return where (location) data member
		 */
		public String getWhere() {
			return where;
		}
		
		/**
		 * Initializes SearchMetrics object
		 * 
		 * @param where location that the SearchMetrics object describes
		 */
		
		/**
		 * Looks up a query word in the inverted index and computes relevant data 
		 * for the search result based on frequency of the query word in the 
		 * SearchMetrics's file (see @location)
		 * @param word String coming from a query
		 */
		protected void update(String word) {
			this.count += invertedIndex.get(word).get(where).size();
			this.score = (double)this.count / wordCounts.get(where);
		}
		 
		/**
		 * Custom comparable to set SearchMetric object correctly
		 * 
		 * @param other object to be compared
		 */
		@Override
		public int compareTo(SearchMetrics other) {
			if (this.score != other.score) return -1 * Double.compare(this.score, other.score);
			if (this.count != other.count) return -1 * Integer.compare(this.count, other.count);
			return this.where.compareToIgnoreCase(other.where);
		}
		
		/**
		 * Linter suggested to do this for better readability
		 * 
		 * @param other object to check equality
		 * @return boolean indicating equality
		 */
		public boolean equals(SearchMetrics other) {
			return compareTo(other) == 0;
		}
	}
}
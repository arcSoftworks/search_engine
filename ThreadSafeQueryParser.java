import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.util.List;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Put it as a class of its own as suggested by Professor
 * 
 * @author pcarbajal
 */
public class ThreadSafeQueryParser {
	/**
	 * declaring map for query results
	 */
	private static Map<String, List<InvertedIndex.SearchMetrics>> allQueryResults;

	/**
	 * declaring invertedIndex
	 */
	private static InvertedIndex index = new InvertedIndex();
	
	/**
	 * declaring WorkQueue
	 */
	private static WorkQueue workQueue = new WorkQueue();
	
	/**
	 * Setting up class and data structure
	 * 
	 * @param invertedIndex	invertedIndex access
	 * @param workQueue passing in a workQueue
	 */
	public ThreadSafeQueryParser(InvertedIndex invertedIndex, WorkQueue workQueue) {
		ThreadSafeQueryParser.index = invertedIndex;
		ThreadSafeQueryParser.workQueue = workQueue;
		allQueryResults = new TreeMap<>();
	}
		
	/**
	 * opens the file line-by-line and calls perform search
	 * 
	 * @param path	path to be read in
	 * @param exact	flag to check exact or partial search
	 * @throws IOException	throws exception
	 */
	public void performSearch(Path path, boolean exact) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while((line = br.readLine()) != null) {
				workQueue.execute(new SearchTask(line, exact));
			}
			workQueue.finish();
		}
	}
	
	/**
	 * Breaks and cleans the line into queries
	 * Conducts search and stores them 
	 * 
	 * @param line	text line to be cleaned and parsed
	 * @param exact	flag to check if exact or partial
	 */
	public static void performSearch(String line, boolean exact) {
		Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
		Set<String> query = TextFileStemmer.uniqueStems(line, stemmer);
		if (query.isEmpty()) {
			return;
		}
		String joined = String.join(" ", query);
		 
		if (allQueryResults.containsKey(joined)) {
			return;
		}
		List<InvertedIndex.SearchMetrics> results = index.search(query,exact);
		synchronized(allQueryResults) {
			allQueryResults.put(joined, results);
		}
	}
	
	/**
	 * Writing search results to Json format
	 * 
	 * @param resultsPath	path of which to write to Json
	 * @throws IOException	throws exception
	 */
	public void writeSearchResults(Path resultsPath) throws IOException {
		JsonWriter.searchResultsToJson(allQueryResults,resultsPath);
	}

	/**
	 * @author pablo
	 *
	 */
	private static class SearchTask implements Runnable {
		/**
		 * 
		 */
		private boolean exact;
		/**
		 * 
		 */
		private String line;
		
		/**
		 * @param line line to searched
		 * @param exact flag
		 */
		public SearchTask(String line, boolean exact) {
			this.line=line;
			this.exact=exact;
		}
		@Override
		public void run() {
			performSearch(line, exact);
		}
	}
}


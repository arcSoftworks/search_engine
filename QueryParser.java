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
public class QueryParser {
	/**
	 * declaring map for query results
	 */
	private final Map<String, List<InvertedIndex.SearchMetrics>> allQueryResults;

	/**
	 * Declaring inverted index
	 */
	private final InvertedIndex index;
	
	/**
	 * Setting up class and data structure
	 * 
	 * @param invertedIndex	invertedIndex access
	 */
	public QueryParser(InvertedIndex invertedIndex) {
		this.index = invertedIndex;
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
				performSearch(line, exact);
			}
		}
	}
	
	/**
	 * Breaks and cleans the line into queries
	 * Conducts search and stores them 
	 * 
	 * @param line	text line to be cleaned and parsed
	 * @param exact	flag to check if exact or partial
	 */
	public void performSearch(String line, boolean exact) {
		Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
		Set<String> query = TextFileStemmer.uniqueStems(line, stemmer);
		if (query.isEmpty()) {
			return;
		}
		String joined = String.join(" ", query);
		 
		if (allQueryResults.containsKey(joined)) {
			return;
		}
		
		allQueryResults.put(joined, index.search(query, exact));
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
}
import java.io.IOException;
import java.nio.file.Path;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author CS 212 Software Development is an author
 * @author University of San Francisco
 * @version Spring 2020
 */
public class Driver {

	/**
	 * If the path argument is not provided, use index.json as the default output
	 * path
	 */
	public static final String DEFAULT_INDEX_PATH = "index.json";

	/**
	 * If the filepath argument is not provided, use counts.json as the default
	 * output filename
	 */
	public static final String DEFAULT_COUNTS_PATH = "counts.json";
	
	/**
	 * If resultspath is not provided, use results.json as the default output filename
	 */
	public static final String DEFAULT_RESULTS_PATH = "results.json";
	
	/**
	 * Default number of threads
	 */
	public static final int DEFAULT_NUM_THREADS = 5;
	
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		
		/* instantiating inverted index */
		InvertedIndex invertedIndex = null;
		
		/* instantiating an argumentParser and passing in arguments from main */
		ArgumentParser argumentParser = new ArgumentParser(args);

				
		/* declaring and setting value to workQueue */
		WorkQueue workQueue = null;

		if(argumentParser.hasFlag("-threads")) { // do we run the single threaded version, or just 1 thread upon invalid entry
			invertedIndex = new ThreadSafeInvertedIndex();
			String strNumThreads = argumentParser.getString("-threads");
			int numThreads;
			try {
				numThreads = Integer.parseInt(strNumThreads);
			} catch (NumberFormatException e) {
				numThreads = DEFAULT_NUM_THREADS;
			}
			if (numThreads <= 0) {
				workQueue = new WorkQueue(DEFAULT_NUM_THREADS);
			} else {
				workQueue = new WorkQueue(numThreads);
			}
		} else {
			invertedIndex = new InvertedIndex();
		}

		
		if (argumentParser.hasFlag("-path")) {
			Path path = argumentParser.getPath("-path");
			if (path == null) {
				System.out.println("The -path argument is required");
			} else {
				try {
					if (workQueue != null) { //multithreading
						ThreadSafeInvertedIndexBuilder.buildInvertedIndexFromPath((ThreadSafeInvertedIndex)invertedIndex, path, workQueue);
					} else { //singlethreaded
						InvertedIndexBuilder.buildInvertedIndex(invertedIndex, path);
					}
				} catch (IOException e) {
					System.out.println("Unable to build the inverted index from path" + path);
				}
			}
		}
		
		if (argumentParser.hasFlag("-index")) {
			Path indexPath = argumentParser.getPath("-index", Path.of(DEFAULT_INDEX_PATH));
			try {
				invertedIndex.writeIndex(indexPath);
			} catch (IOException e) {
				System.out.println("Unable to write the inverted index to path: " + indexPath);
			}
		}

		if (argumentParser.hasFlag("-counts")) {
			Path countsPath = argumentParser.getPath("-counts", Path.of(DEFAULT_COUNTS_PATH));
			try {
				invertedIndex.writeCounts(countsPath);
			} catch (IOException e) {
				System.out.println("Unable to write word counts to path: " + countsPath);
			}
		}

		// TODO Not yet multithreading the search
		QueryParser queryParser = new QueryParser(invertedIndex);
		ThreadSafeQueryParser threadSafeQueryParser = new ThreadSafeQueryParser(invertedIndex, workQueue);
		if (argumentParser.hasValue("-query")) {
			Path queryPath = argumentParser.getPath("-query");
			try {
				
				if (workQueue != null) { // multithreading
					threadSafeQueryParser.performSearch(queryPath, argumentParser.hasFlag("-exact"));
				} else { // singlethreaded
					queryParser.performSearch(queryPath, argumentParser.hasFlag("-exact"));
				}
			} catch (IOException e) {
				System.out.println("Unable to search for query: " + queryPath);
			}
		}
		if (argumentParser.hasFlag("-results")) {
			Path resultsPath = argumentParser.getPath("-results", Path.of(DEFAULT_RESULTS_PATH));
			try {
				if(workQueue != null) {
					threadSafeQueryParser.writeSearchResults(resultsPath); //what if this is null?
				} else {
					queryParser.writeSearchResults(resultsPath); //what if this is null?
				}
			} catch (IOException e) {
				System.out.println("Unable to write search results for : " + resultsPath);
			}
		}
		if(workQueue != null) {
			workQueue.shutdown();
		}
	}
}
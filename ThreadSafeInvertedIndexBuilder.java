import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 
 * Class responsible for building and reading form inverted index. Maintains
 * proper encapsulation
 * 
 * @author pcarbajal
 *
 */
public class ThreadSafeInvertedIndexBuilder {
	
	/**
	 * Inverted Index declaration
	 */
	//private final ThreadSafeInvertedIndex invertedIndex;
	
	/**
	 * Assigning value to invertedIndex as suggested by Professor
	 * 
	 * @param invertedIndex	assigns value 
	 */
	public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex invertedIndex) {
		//this.invertedIndex = invertedIndex;
	}

	/**
	 * declaring new logger
	 */
	private static Logger log = LogManager.getLogger();

	/**
	 * Checks if the path given is a text file
	 */
	public static final Predicate<Path> IS_TEXT_FILE = (Path p) -> {
		try {
			BasicFileAttributes basicAttr = Files.readAttributes(p, BasicFileAttributes.class);
			String pathStr = p.toString().toLowerCase();
			return ((pathStr.endsWith(".txt") || pathStr.endsWith(".text")) && basicAttr.isRegularFile());
		} catch (IOException e) {
			return false;
		}
	};

	/**
	 * Traverses a path for a file or directory
	 * 
	 * @param start argument to be traversed
	 * @return Stream<Path> open stream to process file
	 * @throws IOException	throws exception
	 */
	public static List<Path> find(Path start) throws IOException {
		try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
				.filter(IS_TEXT_FILE)) {
			return stream.collect(Collectors.toList());
		}
	}
	
	/**
	 * Builds inverted index as suggested by Professor
	 *
	 * @param invertedIndex InvertedIndex data structure
	 * @param inputPath     input path to be read and traversed recursively
	 * @param workQueue 	array of workers for multi-threading
	 * @throws IOException	throws exception
	 */
	public static void buildInvertedIndexFromPath(ThreadSafeInvertedIndex invertedIndex, Path inputPath, WorkQueue workQueue) throws IOException {
		List<Path> files = find(inputPath); //main thread
		for (Path path : files) {
			workQueue.execute(new InvertedIndexBuilderTask(path, invertedIndex));

		}
		workQueue.finish();
	}
	
	/**
	 * Inner class for multithreading
	 * 
	 * @author pcarbajal
	 *
	 */
	private static class InvertedIndexBuilderTask implements Runnable {
		
		/**
		 * declaring path variable
		 */
		private Path path;
		
		/**
		 * Declaring InvertedIndex variable
		 */
		private ThreadSafeInvertedIndex invertedIndex;
		
		/**
		 * Assigns values to Path and inverted Index to be used to assign tasks (per file)later on
		 * 
		 * @param path	builds tasks to
		 * @param invertedIndex	throws exception
		 */
		public InvertedIndexBuilderTask(Path path, ThreadSafeInvertedIndex invertedIndex) {
			this.path = path;
			this.invertedIndex = invertedIndex;
		}
		
		/**
		 * Why did Eclipse not highlight this?
		 * function from lecture code
		 */
		@Override
		public void run() {
			log.info("(ii builder)indexing " + path + " started");
			try {
				ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex();
				InvertedIndexBuilder.addFile(path, local); /*ThreadSafe*/
				invertedIndex.addAll(local);
			} catch (IOException e) {
			  // TODO Better exception output
				System.out.println("unable to index " + path);
			} finally {
				log.info("(ii builder)indexing " + path + " ended");
			}
		}
	}
}
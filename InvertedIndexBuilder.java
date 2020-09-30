import java.io.BufferedReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * 
 * Class responsible for building and reading form inverted index. Maintains
 * proper encapsulation
 * 
 * @author pcarbajal
 *
 */
public class InvertedIndexBuilder {

	/**
	 * Stemmer DEFAULT setup
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

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
	 * @param path     input path to be read and traversed recursively
	 * @throws IOException	throws exception
	 */
	public static void buildInvertedIndex(InvertedIndex invertedIndex, Path path) throws IOException {
		List<Path> files = find(path);
		for (Path file : files) {
			addFile(file, invertedIndex);
		}
	}

	/**
	 * Adds stemmed word into the inverted index with a file directory and its word
	 * position
	 * 
	 * @param file  Path of which to add
	 * @param index Data Structure Object
	 * @throws IOException	throws exception
	 */
	public static void addFile(Path file, InvertedIndex index) throws IOException {
		int wordPosition = 0;
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		String fileStr = file.toString();
		try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					index.addEntry(toStem(word, stemmer), fileStr, ++wordPosition);
				}
			}
		}
	}

	/**
	 * Stems individual words
	 * 
	 * @param word    (String) to be stemmed
	 * @param stemmer imported by opennlp-tools
	 * @return String representation of word
	 */
	public static String toStem(String word, Stemmer stemmer) {
		CharSequence sequence = stemmer.stem(word);
		return sequence.toString();
	}
}
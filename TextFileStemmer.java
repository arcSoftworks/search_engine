import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into sets of
 * stemmed words.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 *
 * @see TextParser
 */
/**
 * @author pcarbajal
 *
 */
public class TextFileStemmer {

	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed from
	 * the provided line. (cool)
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static Set<?> uniqueStems(String line) {
		return uniqueStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed from
	 * the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */

	public static Set<String> uniqueStems(String line, Stemmer stemmer) {
		Set<String> set = new TreeSet<>();
		String[] split = TextParser.parse(line);
		for (String word : split) {
			String stemmedString = toStem((word), stemmer);
			if (!stemmedString.equals("")) {
				set.add(stemmedString);
			}
		}
		return set;
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static List<String> uniqueStems(Path inputFile) throws IOException {
		List<String> list = new ArrayList<>();
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		String newFile = inputFile.toString();
		try (BufferedReader br = new BufferedReader(new FileReader(newFile, StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] arrayOfWords = TextParser.split(TextParser.clean(line));
				for (String word : arrayOfWords) {
					String stemmed = toStem(word, stemmer);
					list.add(stemmed);
				}
			}
		}
		return list;
	}
	
	/**
	 * Used for getting lines of query files
	 * 
	 * @param inputFile file to be read and stemmed
	 * @return queries of words
	 */
	public static List<Set<String>> queriesFromFile(String inputFile) {
		List<Set<String>> queries = new ArrayList<>();
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8))) {
			String line;
			while((line = br.readLine()) != null) {
				Set<String> wordsOfQuery = uniqueStems(TextParser.clean(line), stemmer);
				if (!wordsOfQuery.isEmpty()) queries.add(wordsOfQuery);
			}
		} catch (IOException e) {
			System.out.println("Not a file or directory " + e.getMessage());
		}
		return queries;
	}

	/**
	 * Might be leftover code from other things, but I think I remember reading in
	 * the instrucions that I needed to use CharSequence for some reason?
	 * 
	 * @param word	to be stemmed
	 * @param stemmer stemmer to stem (pretty bad explanation)
	 * @return String	returns string representation
	 */
	private static String toStem(String word, Stemmer stemmer) {
		CharSequence sequence = stemmer.stem(word);
		return sequence.toString();
	}
}
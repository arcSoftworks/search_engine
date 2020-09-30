import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class JsonWriter {
	
	
	/**
	 * Overloads function to write search results to Json
	 * 
	 * @param allQueryResults	map of query results
	 * @param path	paths of files
	 * @throws IOException	throws exception
	 */
	public static void searchResultsToJson(Map<String, ? extends Collection<InvertedIndex.SearchMetrics>> allQueryResults, Path path)
		throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			searchResultsToJson(allQueryResults, writer);
		}
	}
	
	/**
	 * Writes object to Json format 
	 * 
	 * @param allQueryResults	iterates SearchMetric Object
	 * @param writer			file writer
	 * @throws IOException	throws exception
	 */
	public static void searchResultsToJson(Map<String, ? extends Collection<InvertedIndex.SearchMetrics>> allQueryResults, Writer writer) 
			throws IOException {
			writer.write("{\n");
			Iterator<String> it = allQueryResults.keySet().iterator();

			if(it.hasNext()) {
				String queryText = it.next();
				indent(writer, 1);
				quote(queryText, writer);
				writer.write(": ");
				asSearchResultsArray(allQueryResults.get(queryText), writer, 1); 			
			}
			while (it.hasNext()) {
				writer.write(",");
				writer.write("\n");
				String queryText = it.next();
				indent(writer, 1);
				quote(queryText, writer);
				writer.write(": ");
				asSearchResultsArray(allQueryResults.get(queryText), writer, 1);
			}
			writer.write("\n");
			indent(writer, 1);
			writer.write("}");
		}
	
	/**
	 * writes generic to Json format
	 * 
	 * @param searchResults		array version
	 * @param writer			file writer
	 * @param level				indentation
	 * @throws IOException	throws exception
	 */
	public static void asSearchResultsArray(Collection<InvertedIndex.SearchMetrics> searchResults, Writer writer, int level) throws IOException {
		writer.write("[\n");
		Iterator<InvertedIndex.SearchMetrics> it = searchResults.iterator();
		while (it.hasNext()) {
			asSearchResultObject(it.next(), writer, level+1);
			if (it.hasNext()) writer.write(",");
			writer.write("\n");
		}
		indent(writer,level);
		writer.write("]");
	}

	/**
	 * Writes search result object to Json format
	 * 
	 * @param searchResult	Object to be written
	 * @param writer		file writer
	 * @param level			indentation
	 * @throws IOException	throws exception
	 */
	public static void asSearchResultObject(InvertedIndex.SearchMetrics searchResult, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.write("{\n");
		//WHERE
		indent(writer, level+1);
		quote("where", writer);
		writer.write(": ");
		quote(searchResult.getWhere(), writer);
		writer.write(",\n");
		//COUNT
		indent(writer, level+1);
		quote("count", writer);
		writer.write(": ");
		writer.write(Integer.toString(searchResult.getCount()));
		writer.write(",\n");
		//SCORE
		indent(writer, level+1);
		quote("score", writer);
		writer.write(": ");
		writer.write(String.format("%.8f", searchResult.getScore()));
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}
	
	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException	throws exception
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		writer.write("[\n");
		Iterator<Integer> it = elements.iterator();
		if (it.hasNext()) {
			indent(writer, level + 1);
			writer.write(it.next().toString());
		}
		while (it.hasNext()) {
			writer.write(",");
			writer.write("\n");
			indent(writer, level + 1);
			writer.write(it.next().toString());
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException	throws exception
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException	throws exception
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		writer.write("{\n");
		Iterator<String> it = elements.keySet().iterator();
		if (it.hasNext()) {
			String elem = it.next();
			indent(writer, level + 1);
			quote(elem, writer);
			writer.write(": ");
			writer.write(elements.get(elem).toString());
		}
		while (it.hasNext()) {
			writer.write(",");
			writer.write("\n");
			String elem = it.next();
			indent(writer, level + 1);
			quote(elem, writer);
			writer.write(": ");
			writer.write(elements.get(elem).toString());
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException	throws exception
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a nested pretty JSON object. The generic notation used
	 * allows this method to be used for any type of map with any type of nested
	 * collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException	throws exception
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		writer.write("{\n");
		Iterator<String> it = elements.keySet().iterator();
		if (it.hasNext()) {
			String elem = it.next();
			indent(writer, level + 1);
			quote(elem, writer);
			writer.write(": ");
			asArray(elements.get(elem), writer, level + 1); 
		}
		while (it.hasNext()) {
			writer.write(",");
			writer.write("\n");
			String elem = it.next();
			indent(writer, level + 1);
			quote(elem, writer);
			writer.write(": ");
			asArray(elements.get(elem), writer, level + 1); 
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException	throws exception
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static String asNestedObject(Map<String, ? extends Collection<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the structure (an inverted index) as a nested pretty JSON object to
	 * file.
	 * @param elements element of the Inverted Index
	 * @param writer        util to write
	 * @param level         for proper formatting
	 * @throws IOException	throws exception
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static void asVeryNestedObject(Map<String, Map<String, ? extends Collection<Integer>>> elements, Writer writer, int level) throws IOException {
		writer.write("{\n");
		Iterator<String> it = elements.keySet().iterator();
		if (it.hasNext()) {
			String word = it.next();
			indent(writer, level + 1);
			quote(word, writer);
			writer.write(": ");
			asNestedObject(elements.get(word), writer, level + 1);
		}
		while (it.hasNext()) {
			writer.write(",");
			writer.write("\n");
			String word = it.next();
			indent(writer, level + 1);
			quote(word, writer);
			writer.write(": ");
			asNestedObject(elements.get(word), writer, level + 1);
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}

	/**
	 * Writes an inverted-index structure as pretty JSON object.
	 * 
	 * @param elements to be written 
	 * @param path          path to be processed
	 * @throws IOException	throws exception
	 */
	public static void asVeryNestedObject(Map<String, Map<String, ? extends Collection<Integer>>> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asVeryNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the structure (inverted index) as a nested pretty JSON object.
	 * 
	 * @param elements to be iterated over
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static String asVeryNestedObject(Map<String, Map<String, ? extends Collection<Integer>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asVeryNestedObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the {@code \t} tab symbol by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException	throws exception
	 */
	public static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException	throws exception
	 *
	 * @see #indent(String, Writer, int)
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException	throws exception
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @throws IOException	throws exception
	 */
	public static void quote(String element, Writer writer) throws IOException {
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Indents and then writes the element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException	throws exception
	 *
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		quote(element, writer);
	}
}
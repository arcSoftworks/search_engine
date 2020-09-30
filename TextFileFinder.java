import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class used to recursively traverse a filepath 
 * 
 * @author pcarbajal
 *
 */
public class TextFileFinder {

	/**
	 * Setup for lambda function thing
	 */
	public static final Predicate<Path> IS_TEXT_FILE_OR_DIRECTORY = (Path p) -> {
		try {
			BasicFileAttributes basicAttr = Files.readAttributes(p, BasicFileAttributes.class);
			return (((p.toString().toLowerCase().endsWith(".txt")) || (p.toString().toLowerCase().endsWith(".text")))
					&& basicAttr.isRegularFile()) || (basicAttr.isDirectory());
		} catch (IOException e) {
			return false;
		}
	};
	
	/**
	 * Traverses though file path by walking 
	 *
	 * @param start path where to begin walking
	 * @return Stream<Path>	sequence of Path objects
	 * @throws IOException	throws exception
	 */
	public static List<String> find(Path start) throws IOException {
	  return Files.walk(start, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
	      .filter(IS_TEXT_FILE_OR_DIRECTORY)
	      .map(x -> x.toString())
	      .collect(Collectors.toList());
	}
}

// should we lock at the addAll? or at addEntry?

/**
 * Class that defines the structure of my inverted index
 * 
 * Structure: Each WORD maps to several LOCATIONS, each LOCATION maps to several
 * POSITIONS Thus Map<String, Map<String, List<Integer>>>
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	
	/**
	 * lock object declaration
	 */
	private final ReadWriteLock lock;


	/**
	 * Instantiating my inverted index
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new ReadWriteLock();
	}

//	/**
//	 * Allows to pass in entries to the Inverted index data structure
//	 * 
//	 * @param word     to be added to inverted index
//	 * @param location of word in a file
//	 * @param position (index) in which the file was found
//	 *
//	 */
	/*
	@Override
	public void addEntry(String word, String location, int position) {
		lock.writeLock().lock();
		try {
			super.addEntry(word, location, position);
		} finally {
			lock.writeLock().unlock();
		}
	}
	*/
	/*
	@Override
	public void incrementWordCount(String location) {
		lock.writeLock().lock();
		try {
			super.incrementWordCount(location);
		} finally {
			lock.writeLock().unlock();
		}
	}
	*/
	
	
	@Override
	public void addAll(InvertedIndex local) {
		lock.writeLock().lock();
		try {
			super.addAll(local);
		} finally {
			lock.writeLock().unlock();
		}
	}
	

	/**
	 * Increments word count
	 *
	 * @param location of of which is counted
	 */
//	@Override
//	protected void incrementWordCount(String location) {
//		lock.writeLock().lock();
//		try {
//			super.incrementWordCount(location);
//		} finally {
//			lock.writeLock().unlock();
//		}
//	}

	/**
	 * Checks if word is contained in the inverted index
	 *
	 * @param word to be checked
	 * @return boolean
	 */
	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if word is contained in the inverted index in a file
	 *
	 * @param word     to be checked
	 * @param location of word in a file
	 * @return boolean if true or false
	 */
	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.contains(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if word is contained in the inverted index in a file and a position
	 *
	 * @param word     to be checked
	 * @param location a file that to be searched
	 * @param position position (index) of the word
	 * @return boolean works as a switcher
	 */
	@Override
	public boolean contains(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.contains(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/*
	 * TODO Need to override and lock every public method from InvertedIndex
	 * that directly accesses the private data that could be shared between threads
	 */
	
}
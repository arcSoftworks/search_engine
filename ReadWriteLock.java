import java.util.ConcurrentModificationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive, but
 * also tracks which thread holds the lock. If unlock is called by any other
 * thread, a {@link ConcurrentModificationException} is thrown.
 *
 * @see SimpleLock
 */
public class ReadWriteLock {

	/** The lock used for reading. */
	private final SimpleLock readerLock;

	/** The lock used for writing. */
	private final SimpleLock writerLock;
	
	/** number of reader threads */
	private int numReaderThreads;
	
	/** number of writer threads */
	private int numWriterThreads;
	
	/** current writer threads */
	private Thread currentWriterThread;

	/** synchronization construct that allows threads to have both mutual exclusion (using locks) 
	 * and cooperation i.e. the ability to make threads wait for certain condition to be true 
	 * (using wait-set).
	 * */
	private final Object monitor;
	
	/**
	 * instantiating log4j2
	 */
	private final Logger log = LogManager.getLogger();


	/**
	 * Initializes a new simple read/write lock.
	 */
	public ReadWriteLock() {
		readerLock = new ReadLock();
		writerLock = new WriteLock();
		numReaderThreads = 0;
		numWriterThreads = 0;
		currentWriterThread = null;
		monitor = new Object();
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		// NOTE: DO NOT MODIFY THIS METHOD
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		// NOTE: DO NOT MODIFY THIS METHOD
		return writerLock;
	}

	/**
	 * Determines whether the thread running this code and the other thread are
	 * in fact the same thread.
	 *
	 * @param other the other thread to compare
	 * @return true if the thread running this code and the other thread are not
	 * null and have the same ID
	 *
	 * @see Thread#getId()
	 * @see Thread#currentThread()
	 */
	public static boolean sameThread(Thread other) {
		// NOTE: DO NOT MODIFY THIS METHOD
		return other != null && other.getId() == Thread.currentThread().getId();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class ReadLock implements SimpleLock {
		
		/**
		 * Will wait until there are no active writers in the system, and then will
		 * increase the number of active readers.
		 */
		@Override
		public synchronized void lock() {
			//synchronized (monitor) {
				while(numWriterThreads > 0) {
					try {
						this.wait();
					} catch(InterruptedException e) {
						// log and re-interrupt
						log.error(e.getMessage() + " interrupted while trying to acquire read lock");
					}
				}
				numReaderThreads++;
			//}
		}

		/**
		 * Will decrease the number of active readers, and notify any waiting threads if
		 * necessary.
		 */
		@Override
		public synchronized void unlock() {
			//synchronized (monitor) {
				numReaderThreads--;
			//}
			//if (currentWriterThread != Thread.currentThread()) {
				notifyAll();
			//}
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class WriteLock implements SimpleLock {

		/**
		 * Will wait until there are no active readers or writers in the system, and
		 * then will increase the number of active writers and update which thread
		 * holds the write lock.
		 */
		@Override
		public synchronized void lock() {
			while (numWriterThreads > 0 || numReaderThreads > 0) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					//re interrupt
					log.error(e.getMessage() + " exceptio while trying to acquire write lock");
					Thread.currentThread().interrupt();
				}
			}
			currentWriterThread = Thread.currentThread();
			numWriterThreads++;
			log.debug("Locking {}, numWriterThreads {}", Thread.currentThread().getName(), numWriterThreads);
		}

		/**
		 * Will decrease the number of active writers, and notify any waiting threads if
		 * necessary. If unlock is called by a thread that does not hold the lock, then
		 * a {@link ConcurrentModificationException} is thrown.
		 *
		 * @see #sameThread(Thread)
		 *
		 * @throws ConcurrentModificationException if unlock is called without previously
		 * calling lock or if unlock is called by a thread that does not hold the write lock
		 */
		@Override
		public synchronized void unlock() throws ConcurrentModificationException {
			if (numWriterThreads < 1 || !sameThread(currentWriterThread)) {
				log.fatal("wrong thread is calling unlock or lock has not yet been called {}", Thread.currentThread().getName());
				throw new ConcurrentModificationException();
			}
			currentWriterThread = null;
			numWriterThreads--;
			log.debug("Unlocking {}, num writer threads %{}", Thread.currentThread().getName(), numWriterThreads);
			notifyAll();		
		}
	}
}

/**
 * A simple lock used for conditional synchronization as an alternative to using
 * a {@code synchronized} block.
 *
 */
interface SimpleLock { // TODO Make a public interface?

	// NOTE: DO NOT MODIFY THIS CLASS

	/**
	 * Acquires the lock. If the lock is not available then the current thread
	 * becomes disabled for thread scheduling purposes and lies dormant until the
	 * lock has been acquired.
	 */
	public void lock();

	/**
	 * Releases the lock.
	 */
	public void unlock();

}
package simphony.util;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import simphony.util.messages.MessageCenter;

/**
 * Thread related utility methods.
 *
 * @author Nick Collier
 * @version $Revision: 1.1.2.3 $ $Date: 2007/03/14 20:28:06 $
 */
public class ThreadUtilities {

  private static Lock rieLock = new ReentrantLock();
  private static Lock riewLock = new ReentrantLock();

  private static class Runner {

    private Runnable runnable;
    private Lock lock;

    private Runner(Lock lock, Runnable runnable) {
      this.lock = lock;
      this.runnable = runnable;
    }

    public void run() {
      lock.unlock();
      runnable.run();
    }
  }

  /**
	 * Runs the specified runnable in the swing event thread. If the
	 * current thread is the event thread then this is run immediately,
	 * otherwise it is run via EventQueue.invokeLater
	 *
	 * @param runnable the runnable to run
	 */
	public static void runInEventThread(Runnable runnable) {
    rieLock.lock();
    if (EventQueue.isDispatchThread()) {
			new Runner(rieLock, runnable).run();
    } else {
			EventQueue.invokeLater(runnable);
      rieLock.unlock();
    }
	}

	/**
	 * Posts the specified runnable to the event queue and returns. The
	 * runnable will run in the event queue when its turn is reached.
	 *
	 * @param runnable the runnable to run in the event queue
	 */
	public synchronized static void runLaterInEventThread(Runnable runnable) {
		EventQueue.invokeLater(runnable);
	}


	/**
	 * Runs the specified runnable in the swing event thread and wait
	 * for it to finish executing. If the
	 * current thread is the event thread then this is run immediately,
	 * otherwise it is run via EventQueue.invokeAndWaity
	 *
	 * @param runnable the runnable to run
	 */
	public static void runInEventThreadAndWait(Runnable runnable) {
		try {
      riewLock.lock();
      if (EventQueue.isDispatchThread()) {
				new Runner(riewLock, runnable).run();
			} else {
				EventQueue.invokeAndWait(runnable);
        riewLock.unlock();
      }
		} catch (InvocationTargetException e) {
      riewLock.unlock();
      MessageCenter.getMessageCenter(ThreadUtilities.class).error("Error in invokeAndWait", e);
		} catch (InterruptedException e) {
      riewLock.unlock();
      MessageCenter.getMessageCenter(ThreadUtilities.class).error("Error in invokeAndWait", e);
		}
	}
}


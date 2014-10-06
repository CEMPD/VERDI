package anl.verdi.area;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

/**
 * 
 * File Name:LongTask.java
 * Description:
 * A task that could take a long time.
 * 
 * @version May 27, 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 * LongTask.java is used by:
 *   ProgressBarDemo.java
 *   ProgressBarDemo2.java
 *   ProgressMonitorDemo
 */
public class LongTask {
	static final Logger Logger = LogManager.getLogger(LongTask.class.getName());
	protected int lengthOfTask;
	protected int current = 0;
	protected volatile boolean done = false;
	protected volatile boolean canceled = false;
	protected String statMessage;

	public LongTask() {
		//Compute length of task...
		//In a real program, this would figure out
		//the number of bytes to read or whatever.
		lengthOfTask = 1000;
	}
	public void doWork() {
		new ActualTask();
	}
	/**
	 * Called from ProgressBarDemo to start the task.
	 */
	public void go() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				current = 0;
				done = false;
				canceled = false;
				statMessage = null;
				doWork();
				return null;
			}
		};
		worker.start();
	}

	/**
	 * Called from ProgressBarDemo to find out how much work needs
	 * to be done.
	 */
	public int getLengthOfTask() {
		return lengthOfTask;
	}

	/**
	 * Called from ProgressBarDemo to find out how much has been done.
	 */
	public int getCurrent() {
		return current;
	}

	public void stop() {
		canceled = true;
		statMessage = null;
	}

	/**
	 * Called from ProgressBarDemo to find out if the task has completed.
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the most recent status message, or null
	 * if there is no current status message.
	 */
	public String getMessage() {
		return statMessage;
	}

	/**
	 * The actual long running task.  This runs in a SwingWorker thread.
	 */
	class ActualTask {
		ActualTask() {
			//Fake a long task,
			//making a random amount of progress every second.
			while (!canceled && !done) {
				try {
					Thread.sleep(1000); //sleep for a second
					current += Math.random() * 100; //make some progress
					if (current >= lengthOfTask) {
						done = true;
						current = lengthOfTask;
					}
					statMessage = "Completed " + current + " out of " + lengthOfTask + ".";
				} catch (InterruptedException e) {
					Logger.error("ActualTask interrupted");
				}
			}
		}
	}
	/**
	 * See if the user interrupted the task
	 * @return if the task has been cancelled
	 */
	public boolean isCanceled() {
		return canceled;
	}

}

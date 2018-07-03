package mpi.search.gui;

import javax.swing.SwingUtilities;


/**
 * This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated
 * thread.  For instructions on using this class, see:
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html Note that
 * the API changed slightly in the 3rd version: You must now invoke start() on
 * the SwingWorker after creating it.
 */
public abstract class SwingWorker {
    private Object value; // see getValue(), setValue()
    private Thread thread;
    private int priority = Thread.NORM_PRIORITY;
    private boolean isrunning = false;
    private ThreadVar threadVar;

    /**
     * Start a thread that will call the <code>construct</code> method and then
     * exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
                @Override
				public void run() {
                    isrunning = false;
                    finished();
                }
            };

        Runnable doConstruct = new Runnable() {
                @Override
				public void run() {
                    try {
                        setValue(construct());
                    } finally {
                        threadVar.clear();
                    }

                    SwingUtilities.invokeLater(doFinished);
                }
            };

        Thread t = new Thread(doConstruct);
        threadVar = new ThreadVar(t);
    }

    /**
     * Get the value produced by the worker thread, or null if it hasn't been
     * constructed yet.
     *
     * @return The value.
     */
    protected synchronized Object getValue() {
        return value;
    }

    /**
     * Set the value produced by worker thread
     *
     * @param x DOCUMENT ME!
     */
    private synchronized void setValue(Object x) {
        value = x;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     *
     * @return The object that should be accessible via <CODE>get()</CODE>.
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * A new method that interrupts the worker thread.  Call this method to
     * force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();

        if (t != null) {
            t.interrupt();
            isrunning = false;
        }

        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.   Returns
     * null if either the constructing thread or the current thread was
     * interrupted before a value was produced.
     *
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {
            Thread t = threadVar.get();

            if (t == null) {
                return getValue();
            }

            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate

                return null;
            }
        }
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();

        if (t != null) {
            t.setPriority(priority);
            isrunning = true;
            t.start();
        }
    }

    /**
     * Sets the priority of the thread.
     *
     * @param priority The priority.
     *
     * @see #getPriority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the priority of the thread.
     *
     * @return The priority.
     *
     * @see #setPriority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns true when the thread is running.
     *
     * @return True when the thread is running, false otherwise.
     */
    public boolean isRunning() {
        return isrunning;
    }

    /**
     * Class to maintain reference to current worker thread under separate
     * synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;

        /**
         * Creates a new ThreadVar instance
         *
         * @param t DOCUMENT ME!
         */
        ThreadVar(Thread t) {
            thread = t;
        }

        synchronized Thread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }
}

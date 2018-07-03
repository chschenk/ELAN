package mpi.eudico.client.annotator.search.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ConcurrentModificationException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.model.ProgressListener;
import mpi.search.model.SearchEngine;
import mpi.search.query.model.Query;

/*
TODO: I suspect having too many "hits" causes exceptions somewhere in the view stack
	even a "synchronized" keyword on result.addMatch() doesn't help because it fires
	events that percolate upwards and that's what triggers the problems...

    [java] Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
     [java] 	at javax.swing.text.FlowView$FlowStrategy.layoutRow(FlowView.java:563)
     [java] 	at javax.swing.text.FlowView$FlowStrategy.layout(FlowView.java:477)
     [java] 	at javax.swing.text.FlowView.layout(FlowView.java:201)
     [java] 	at javax.swing.text.BoxView.setSize(BoxView.java:397)
     [java] 	at javax.swing.text.BoxView.updateChildSizes(BoxView.java:366)
     [java] 	at javax.swing.text.BoxView.setSpanOnAxis(BoxView.java:348)
     [java] 	at javax.swing.text.BoxView.layout(BoxView.java:708)
     [java] 	at javax.swing.text.BoxView.setSize(BoxView.java:397)
     [java] 	at javax.swing.text.BoxView.updateChildSizes(BoxView.java:366)
     [java] 	at javax.swing.text.BoxView.setSpanOnAxis(BoxView.java:348)
     [java] 	at javax.swing.text.BoxView.layout(BoxView.java:708)
     [java] 	at javax.swing.text.BoxView.setSize(BoxView.java:397)
     [java] 	at javax.swing.plaf.basic.BasicTextUI$RootView.setSize(BasicTextUI.java:1714)
     [java] 	at javax.swing.plaf.basic.BasicTextUI$RootView.paint(BasicTextUI.java:1433)
     [java] 	at javax.swing.plaf.basic.BasicTextUI.paintSafely(BasicTextUI.java:737)
     [java] 	at javax.swing.plaf.basic.BasicTextUI.paint(BasicTextUI.java:881)
     [java] 	at javax.swing.plaf.basic.BasicTextUI.update(BasicTextUI.java:860)
     [java] 	at javax.swing.JComponent.paintComponent(JComponent.java:769)
     [java] 	at javax.swing.JComponent.paint(JComponent.java:1045)
     [java] 	at javax.swing.JComponent.paintToOffscreen(JComponent.java:5212)
     [java] 	at javax.swing.BufferStrategyPaintManager.paint(BufferStrategyPaintManager.java:295)
     [java] 	at javax.swing.RepaintManager.paint(RepaintManager.java:1236)
     [java] 	at javax.swing.JComponent._paintImmediately(JComponent.java:5160)
     [java] 	at javax.swing.JComponent.paintImmediately(JComponent.java:4971)
     [java] 	at javax.swing.RepaintManager$3.run(RepaintManager.java:796)
     [java] 	at javax.swing.RepaintManager$3.run(RepaintManager.java:784)
     [java] 	at java.security.AccessController.doPrivileged(Native Method)
     [java] 	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:76)
     [java] 	at javax.swing.RepaintManager.paintDirtyRegions(RepaintManager.java:784)
     [java] 	at javax.swing.RepaintManager.paintDirtyRegions(RepaintManager.java:757)
     [java] 	at javax.swing.RepaintManager.prePaintDirtyRegions(RepaintManager.java:706)
     [java] 	at javax.swing.RepaintManager.access$1000(RepaintManager.java:62)
     [java] 	at javax.swing.RepaintManager$ProcessingRunnable.run(RepaintManager.java:1651)
     [java] 	at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:251)
     [java] 	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:727)
     [java] 	at java.awt.EventQueue.access$200(EventQueue.java:103)
     [java] 	at java.awt.EventQueue$3.run(EventQueue.java:688)
     [java] 	at java.awt.EventQueue$3.run(EventQueue.java:686)
     [java] 	at java.security.AccessController.doPrivileged(Native Method)
     [java] 	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:76)
     [java] 	at java.awt.EventQueue.dispatchEvent(EventQueue.java:697)
     [java] 	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:242)
     [java] 	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:161)
     [java] 	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:150)
     [java] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:146)
     [java] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:138)
     [java] 	at java.awt.EventDispatchThread.run(EventDispatchThread.java:91)
*/

/** 
 * The entry point for the FAST searcher
 * @author Larwan Berke, DePaul
 * @version 1.0
 * @since June 2013
 */
public class FASTSearchEngine implements SearchEngine {
    private final ProgressListener progressListener;
    private final xmlParserThreadExecutor threadPool;

    public FASTSearchEngine(ProgressListener p) {
        progressListener = p;
        
        // Create our threadpool
        threadPool = xmlParserThreadExecutor.newThreadPool();
    }

    // required to call this so we properly dispose of our threads!
    public void closeEngine() {
    	threadPool.shutdown();
    }

    /**
     * Executes the query against the search domain
     * @param query The query created earlier with {@link #createQuery(String, File[])}
     */
    @Override
	public void performSearch(Query query) throws Exception {
        File[] files = ((ContentQuery)query).getFiles();

        // inform our threadpool that we have a new query so it can prepare itself
    	threadPool.newQuery( (ContentQuery)query );
    	
    	// Get the search pattern to use ( method calls are expensive in a loop... )
    	AnchorConstraint search = ((ContentQuery)query).getAnchorConstraint();

    	// We need to keep track of our tasks
    	AtomicInteger pendingTasks = new AtomicInteger();

        try {
            // iterate over the EAF Files to do the searching stuff
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                
                // hand it off to our parsing threads!
                pendingTasks.incrementAndGet();
                threadPool.execute(new xmlParserTask(file, search, pendingTasks));

               	// don't set to 100% yet if we're on the last file! ( have to wait for tasks to complete )
               	if ( progressListener != null && files.length != (i + 1) ) {
               		progressListener.setProgress((int) (((i + 1) * 100.0) / files.length));
               	}
            }
            
            // this function MUST return when it's done due to the code in mpi.search.model.DefaultSearchController.execute()
            // so... we wait until we're done processing all the files!
            while (pendingTasks.get() != 0) {
                Thread.sleep(5);
            }
            
            // Finally, we set progress to 100%!
            if ( progressListener != null) {
            	progressListener.setProgress(100);
            }
        } catch (ConcurrentModificationException e) {
        	// stop of thread can cause ConcurrentModificationException
            // (will be ignored since it has no further consequences)
        }
    }

    private static class xmlParserTask implements Runnable {
    	private final File file;
    	private final AtomicInteger pendingTasks;
    	private final AnchorConstraint search;
    	private SAXParser saxParser;
    	private FASTSearchHandler handler;
    	
    	public xmlParserTask( File f, AnchorConstraint s, AtomicInteger c ) {
    		file = f;
    		search = s;
    		pendingTasks = c;
    	}
    	
    	// will be called by xmlParserThreadExecutor.beforeExecute()
    	public void setSAXParser( SAXParser s ) {
    		saxParser = s;
    	}

    	// will be called by xmlParserThreadExecutor.beforeExecute()
    	public void setHandler( FASTSearchHandler h ) {
    		handler = h;
    	}

    	@Override
		public void run() {
    		// Catch any exceptions, we don't want it to bubble up!
    		try {
    			// prefetch the file into memory, this is much better than letting
    			// the SAX parser load it due to our threadpool design
    			byte[] fileContents = preFetch(file);

    			// execute a fulltext search on the file to see if we can skip the XML parser
    			if ( fullTextSearch(fileContents, search) == true ) {
    				handler.newFile(file);
                    saxParser.parse(new ByteArrayInputStream(fileContents), handler);
    			}

                // decrement the count, we are done parsing!
                pendingTasks.decrementAndGet();
    		} catch (Exception e) {
    			System.out.println("xmlParserThread (ID:" + Thread.currentThread().getId() + ") error: " + e.toString() );
    		}
    	}
    	
    	private byte[] preFetch(final File file) {
    		FileInputStream reader = null;
            byte[] fileContents = new byte[(int)file.length()];
            try {
            	reader = new FileInputStream(file);
                reader.read(fileContents);
            } catch (Exception e) {
            	System.out.println("Error in reading file:" + e.toString() );
            } finally {
            	if ( reader != null ) {
            		try {
            			reader.close();
            		} catch ( Exception e ) {
            			System.out.println("Error in closing file:" + e.toString() );
            		}
            	}
            }

            return fileContents;
    	}

        private boolean fullTextSearch(final byte[] fileContents, final AnchorConstraint search) {
        	// We don't want to run an "expensive" regex as our fulltext search!
        	// If the search is non-regex we can go ahead and use String.indexOf() which is awesomer than Pattern/Matcher!
        	// However, indexOf is case-sensitive!
        	if ( ! search.isRegEx() && search.isCaseSensitive() ) {
        		String str = new String(fileContents);
            	if ( str.indexOf( search.getPattern() ) >= 0 ) {
            		return true;
            	} else {
            		return false;
            	}
        	} else {
        		// TODO find ways to optimize the regex search?
        		return true;
        	}
        }
    }
    
    // The SAX Parser isn't threadsafe so we have to jump through hoops to have a per-thread parser
	// [java] ThreadPool error: org.xml.sax.SAXException: FWK005 parse may not be called while parsing.
    // Furthermore, FASTSearchHandler isn't threadsafe either so we clone it for each search
    // P.S. ContentResult.addMatch is now 'synchronized' so I think we've covered all bases!
    // TODO add code to handle stopping search
    private static class xmlParserThreadExecutor extends ThreadPoolExecutor {
    	private ContentQuery query = null;
    	
    	public xmlParserThreadExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {

    		// Auto-generated constructor stub
    		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		}

		public static xmlParserThreadExecutor newThreadPool() {
			// simple algorithm to determine number of threads = cores * 2
			// TODO this might oversaturate the disks...
			int numThreads = Runtime.getRuntime().availableProcessors() * 2;
//			System.out.println("Number of threads: " + numThreads );
    	    xmlParserThreadExecutor threadpool = new xmlParserThreadExecutor(numThreads, numThreads,
    	    		0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(numThreads),
    	    		new xmlParserThreadFactory()
    	    		);

    	    // implements blocking execute
    		// http://stackoverflow.com/questions/3446011/threadpoolexecutor-block-when-queue-is-full
    	    threadpool.setRejectedExecutionHandler( new RejectedExecutionHandler() {
    	    	@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    	    		try {
    	    			executor.getQueue().put( r );
    	    		} catch (InterruptedException e) {
    	    			// we can safely ignore this...
    	    		}
    	    	}
    	    } );
    	    
    	    return threadpool;
    	}

		// Informs the threadpool that we have a new query so we can discard the old handlers
		public void newQuery(ContentQuery q) {
			query = q;
		}
		
		@Override
		protected void beforeExecute(Thread t, Runnable r) {
		    super.beforeExecute(t, r);
		    
		    // copy the parser from thread to task
		    ((xmlParserTask)r).setSAXParser( ((xmlParserThread)t).getSAXParser() );
		    
		    // we have to compare the queries in case we started a new search
		    // If the user just clicked "search" again, the same query object will be used, but a new result object!
		    // If the user entered a new search, a new query object will be used
	    	if (((xmlParserThread)t).getHandler() == null
	    			|| ! ((xmlParserThread)t).getHandler().getQuery().equals(query)
	    			|| ! ((xmlParserThread)t).getHandler().getQuery().getResult().equals(query.getResult()) ) {
	    		// new query, init/replace the handler!
	    		((xmlParserThread)t).setHandler( new FASTSearchHandler(query) );
		    }
		    
		    // copy the handler from thread to task
		    ((xmlParserTask)r).setHandler( ((xmlParserThread)t).getHandler() );
		}
		
		private static class xmlParserThreadFactory implements ThreadFactory {
			private final SAXParserFactory factory;

			// we keep track of this to give a nice name to our threads
			private Integer numThreadsCreated = 0;
			
			public xmlParserThreadFactory() {
				factory = SAXParserFactory.newInstance();
	            factory.setValidating(false);
	            factory.setNamespaceAware(false);
	            
//	            System.out.println("XML Parser: " + factory.getClass() );
			}
			
			@Override
			public Thread newThread(Runnable r) {
				// Create the SAX parser for this thread
				SAXParser p;
	            try {
	            	p = factory.newSAXParser();
	            } catch (Exception e) {
	            	System.out.println("XML Factory error: " + e.toString() );
	            	return null; // to satisfy "This method must return a result of type Thread"
	            }

	            return new xmlParserThread(r, p, ++numThreadsCreated);
			}
		}
		
		private static class xmlParserThread extends Thread {
			// we need a parser per-thread so we store them here (init once)
			private final SAXParser saxParser;
			
			// we need a handler per-thread but it will be re-initialized for a new search
			private FASTSearchHandler handler = null;
			
			public xmlParserThread(Runnable r, SAXParser p, Integer id) {
				super(null, r, "xmlParserThread-" + id);

				saxParser = p;
			}
			
			public SAXParser getSAXParser() {
				return saxParser;
			}
			
			public FASTSearchHandler getHandler() {
				return handler;
			}
			
			public void setHandler( FASTSearchHandler h ) {
				handler = h;
			}
		}
    }
}

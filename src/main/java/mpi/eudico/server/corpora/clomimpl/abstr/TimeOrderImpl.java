package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;


/**
 * TimeOrder encapsulates the ordering of TimeSlots in a Transcription. It is
 * considered to be part of the Transcription.  The TimeOrder is used when
 * comparing TimeSlots in the TimeSlot's compareTo method. Given a constructed
 * TimeOrder, it is then sufficient to add TimeSlots to a TreeSet, they will
 * be ordered according to the TimeOrder automatically.
 *
 * @author Hennie Brugman
 */
public class TimeOrderImpl implements TimeOrder {
    private ArrayList<TimeSlot> orderedTimeSlotList; // indeed requires an array
    private TranscriptionImpl transcription;

    /**
     * Creates a new TimeOrderImpl instance
     *
     * @param theTranscription DOCUMENT ME!
     */
    public TimeOrderImpl(TranscriptionImpl theTranscription) {
        transcription = theTranscription;
        orderedTimeSlotList = new ArrayList<TimeSlot>();
    }

    /**
     * Adds a TimeSlot to the TimeOrder at the latest possible position. The
     * TimeSlot can be either time-aligned or not time-aligned.
     *
     * @param theTimeSlot the TimeSlot to be inserted.
     */
    @Override
	public void insertTimeSlot(TimeSlot theTimeSlot) {
        if (theTimeSlot.isTimeAligned()) {
            int index = 0;
            long time = theTimeSlot.getTime();

            if (orderedTimeSlotList.size() > 0) {
                Iterator<TimeSlot> tsIter = orderedTimeSlotList.iterator();

                /*
                 * Puts the new timeslot just before the one that's later.
                 * UNALIGNED slots are 0 or -1 or so, so it skips them too.
                 * It is probably better to put aligned slots of the same time
                 * all together. I have not checked what other effects that may have.
                 */
                while (tsIter.hasNext()) {
                    if (tsIter.next().getTime() > time) {
                        break;
                    }

                    index++;
                }
            }

            orderedTimeSlotList.add(index, theTimeSlot);
            reindex(index);
        } else { // not time aligned
            orderedTimeSlotList.add(theTimeSlot); // at end
            reindex(orderedTimeSlotList.size() - 1);
        }

        //reindex();
    }

    /**
     * Assign its sequence number to each timeslot such that
     * <code>orderedTimeSlotList.get(i).getIndex() == i</code>.
     */
    private void reindex() {
    	reindex(0);
    }
    
    /**
     * A refined version of reindex: change the index of the slots from a certain
     * point, e.g. after insertion of a slot at a certain index.
     *  
     * @param fromIndex the index to start reindexing from
     */
    private void reindex(int fromIndex) {
    	final int size = orderedTimeSlotList.size();
		for (int i = fromIndex; i < size; i++) {
			orderedTimeSlotList.get(i).setIndex(i);
    	}
    	/*
    	System.out.print("Size: " + orderedTimeSlotList.size() + " from: " + fromIndex);
    	if (orderedTimeSlotList.size() > 0) {
			System.out.print(" first: " + ((TimeSlot) orderedTimeSlotList.get(0)).getIndex());
			System.out.println(" last: " + ((TimeSlot) orderedTimeSlotList.get(
				orderedTimeSlotList.size() - 1)).getIndex());
    	}
		*/
    }

    /**
     * Adds a TimeSlot to the TimeOrder at current position. The TimeSlot can
     * be either time-aligned or not time-aligned. The TimeSlot is inserted
     * after 'afterSlot' and before 'beforeSlot'.
     *
     * @param theTimeSlot the TimeSlot to be inserted.
     * @param afterSlot Put it after this slot
     * @param beforeSlot may be null if theTimeSlot is unaligned
     */
    @Override
	public void insertTimeSlot(TimeSlot theTimeSlot, TimeSlot afterSlot,
        TimeSlot beforeSlot) {
        int index = 0;
        boolean positioned = false;
        long time = theTimeSlot.getTime();

        // iterate until afterSlot
        Iterator<TimeSlot> tsIter = orderedTimeSlotList.iterator();

        while (tsIter.hasNext()) {
            index++;

            if (tsIter.next() == afterSlot) {
                break;
            }
        }
        
        if (!tsIter.hasNext()) {	// at end of time order
        	positioned = true;
        }

        // iterate until time > theTimeSlot's time, or until beforeSlot is reached
        while (tsIter.hasNext()) {
            TimeSlot ts = tsIter.next();

            if ((ts.isTimeAligned()) && (ts.getTime() > time)) {// must be >= ? test!
                positioned = true;

                //	if (!theTimeSlot.isTimeAligned() && (beforeSlot == null)) {	// AD HOC !!!???
                //		index++;
                //	}
                break;
            } else {
                if (!theTimeSlot.isTimeAligned() && (beforeSlot == null)) { // AD HOC !!!???

                    //	    	index++;
                    positioned = true;

                    break;
                }
            }

            if (ts == beforeSlot) {
                if (!ts.isTimeAligned()) {
                    positioned = true;
                }

                break;
            }

            index++;
        }

        // insert
        tsIter = null; // to prevent ConcurrentModificationExceptions

        if (positioned) {
            orderedTimeSlotList.add(index, theTimeSlot);
			reindex(index);
        } else {
        	System.out.println("Not positioned...");
			reindex();
        }

        // reindex
        //reindex();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theSlot DOCUMENT ME!
     */
    @Override
	public void removeTimeSlot(TimeSlot theSlot) {
        orderedTimeSlotList.remove(theSlot);
        reindex();
    }

    /**
     * A utility method to print the current state of TimeOrder to standard
     * output.
     */
    @Override
	public void printTimeOrder() {
        System.out.println("");

        Iterator<TimeSlot> iter = orderedTimeSlotList.iterator();

        while (iter.hasNext()) {
            TimeSlot t = iter.next();
            System.out.println(t.getIndex() + " " + t.getTime());
        }
    }

    /**
     * Returns true if timeSlot1 starts before timeSlot2, according to the
     * order specified by the TimeOrder. Each TimeSlot can be either
     * time-aligned or  not time-aligned.
     *
     * @param timeSlot1 first TimeSlot to be compared.
     * @param timeSlot2 second TimeSlot to be compared.
     *
     * @return true if timeSlot1 starts before timeSlot2.
     */
    @Override
	public boolean isBefore(TimeSlot timeSlot1, TimeSlot timeSlot2) {
        boolean before = true;
        //System.out.println("isBefore, ts1:" + timeSlot1.getTime() + " ts2: " +
        //    timeSlot2.getTime());

        if (timeSlot1.getIndex() > timeSlot2.getIndex()) {
            before = false;
        } else {
            before = true;
        }

        return before;
    }

    /**
     * Gets the predecessor TimeSlots,
     * according to the ordering of the TimeSlots.
     */
    @Override
	public TimeSlot getPredecessorOf(TimeSlot timeSlot) {
    	int index = timeSlot.getIndex();

    	// Do a quick consistency check if the recorded index seems correct.
    	if (index > 0 && orderedTimeSlotList.get(index) == timeSlot) {
    		return orderedTimeSlotList.get(index - 1);
    	}
    	
    	// First timeslot, or index not correct.
    	return null;
    }

    /**
     * Returns number of elements of TimeOrder.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int size() {
        return orderedTimeSlotList.size();
    }

    /**
     * DOCUMENT ME!
     */
    /* old implementation
    public void pruneTimeSlots() {
        ArrayList slotsToBeDeleted = new ArrayList();

        Iterator slotIter = orderedTimeSlotList.iterator();

        while (slotIter.hasNext()) {
            TimeSlot sl = (TimeSlot) slotIter.next();

            if (((TranscriptionImpl) transcription).getAnnotationsUsingTimeSlot(
                        sl).size() == 0) {
                slotsToBeDeleted.add(sl);
            }
        }

        //System.out.println("num of ts before: " + orderedTimeSlotList.size());
        //System.out.println("num of slots to be deleted: " + slotsToBeDeleted.size());		
        orderedTimeSlotList.removeAll(slotsToBeDeleted);

        //System.out.println("num of ts after: " + orderedTimeSlotList.size());
        // re-index
        reindex();
    }
    */
    
    /**
     * Removes TimeSlots that are no longer in use.
     */
    @Override
	public void pruneTimeSlots() {
        List<TimeSlot> slotsToBeDeleted = new ArrayList<TimeSlot>();

        Set<TimeSlot> usedSlots = transcription.getTimeSlotsInUse();
        TimeSlot ts = null;
        
        for (int i = orderedTimeSlotList.size() - 1; i >= 0; i--) {
        	ts = orderedTimeSlotList.get(i);
        	
        	if (!usedSlots.contains(ts)) {
        		slotsToBeDeleted.add(ts);
        	}
        }

        if (!slotsToBeDeleted.isEmpty()) {
	        //System.out.println("num of ts before: " + orderedTimeSlotList.size());
	        //System.out.println("num of slots to be deleted: " + slotsToBeDeleted.size());		
	        orderedTimeSlotList.removeAll(slotsToBeDeleted);
	
	        //System.out.println("num of ts after: " + orderedTimeSlotList.size());
	        // re-index
	        reindex();
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param theSlot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long proposeTimeFor(TimeSlot theSlot) {
        if (theSlot.isTimeAligned()) {
            return theSlot.getTime();
        } else {
            // find nearest aligned neighbours
            TimeSlot lastAlignedSlot = null;
            long segmentBegin = 0;
            long segmentEnd = 0;
            boolean slotFound = false;
            int numOfUnalignedSlots = 0;
            int slotCount = 0;

            Iterator<TimeSlot> slotIter = orderedTimeSlotList.iterator();

            while (slotIter.hasNext()) {
                TimeSlot sl = slotIter.next();

                if (sl.isTimeAligned()) {
                    lastAlignedSlot = sl;

                    if (slotFound) {
                        segmentEnd = lastAlignedSlot.getTime();

                        break;
                    } else {
                        numOfUnalignedSlots = 0; // reset
                    }
                } else { // unaligned
                    numOfUnalignedSlots += 1;
                }

                if (sl == theSlot) {
                    if (lastAlignedSlot != null) {
                        segmentBegin = lastAlignedSlot.getTime();
                    }

                    slotFound = true;
                    slotCount = numOfUnalignedSlots;
                }
            }

            long deltaT = (segmentEnd - segmentBegin) / (numOfUnalignedSlots +
                1);

            return segmentBegin + (slotCount * deltaT);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    
    @Override
	public Iterator<TimeSlot> iterator() {
        return orderedTimeSlotList.iterator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theSlot DOCUMENT ME!
     * @param newTime DOCUMENT ME!
     */
    @Override
	public void modifyTimeSlot(TimeSlot theSlot, long newTime) {
        // find theSlot
        int currentIndex = orderedTimeSlotList.indexOf(theSlot);
        int newIndex = -1;
		
        // if newTime > theSlot time, check if to be moved right
        if (!theSlot.isTimeAligned() || (newTime > theSlot.getTime())) {
            for (int i = currentIndex + 1; i < orderedTimeSlotList.size();
                    i++) {
                TimeSlot ts = (orderedTimeSlotList.get(i));

                if (ts.isTimeAligned() && (ts.getTime() <= newTime)) {
                    newIndex = i;
                } else if (ts.getTime() >= newTime) {
                    break;
                }
            }
        }

        // if newTime < theSlot time, check if to be moved left
        if ((newIndex == -1) && // not repositioned yet
                (!theSlot.isTimeAligned() || (newTime < theSlot.getTime()))) {
            for (int i = currentIndex - 1; i >= 0; i--) {
                TimeSlot ts = (orderedTimeSlotList.get(i));

                if (ts.isTimeAligned() && (ts.getTime() >= newTime)) {
                    newIndex = i;
                } else if ((ts.getTime() <= newTime) && ts.isTimeAligned()) {
                    break;
                }
            }
        }

        theSlot.updateTime(newTime); // don't call setTime !!!

        if ((newIndex >= 0) && (currentIndex >= 0)) {
            orderedTimeSlotList.remove(currentIndex);

            if (newIndex < currentIndex) {
                orderedTimeSlotList.add(newIndex, theSlot);
            } else {
                orderedTimeSlotList.add(newIndex /* -1 */, theSlot);
            }

            reindex();
        }
    }

    /**
     * Shift all times of all aligned timeslots later than fromTimeSlot. Used
     * for 'shift mode'.
     *
     * @param fromTimeSlot DOCUMENT ME!
     * @param shift DOCUMENT ME!
     */
/*	  public void shift(long fromTime, long shift, Vector fixedSlots) {
         
        Vector slotsToShift = new Vector();        
        Enumeration enum = elements();
        
//        System.out.println("\nbefore:");
//        printTimeOrder();

        while (enum.hasMoreElements()) {
            TimeSlot ts = (TimeSlot) enum.nextElement();
        	if (ts.isTimeAligned() && ts.getTime() >= fromTime && !fixedSlots.contains(ts)) {
        		slotsToShift.add(ts);
        	}	
        }
        
        for (int i = 0; i < slotsToShift.size(); i++) {
        	TimeSlot slot = (TimeSlot) slotsToShift.elementAt(i);
        	slot.setTime(slot.getTime() + shift);
        }
        
//        System.out.println("\nafter:");
//        printTimeOrder();
    } */
    
	/**
	 * Shift all times of all aligned timeslots later than fromTime. Used
	 * for 'shift mode'.<br>
	 * jan 05, HS: at edit time the order is not always predictable; 
	 * especially slots that should not be shifted (from the source annotation 
	 * or child annotations from the source annotation) may be positioned 
	 * 'to the right' of fromTime.
	 *  
	 * @param fromTime only slots with time values greater than this value will be changed 
	 * @param shift the amount of ms to add to the time value
	 * @param lastFixedSlot the last slot that should not be changed 
	 * (the end time slot of a source annotation)
	 * @param otherFixedSlots optional other slots that should not be changed
	 */
	  @Override
	public void shift(long fromTime, long shift, TimeSlot lastFixedSlot, 
	  	List<TimeSlot> otherFixedSlots) {
        
        if (shift < 0) {	// maintain simple implementation of shift for operation from undo
			Iterator en = iterator();
			while (en.hasNext()) {
				TimeSlot ts = (TimeSlot) en.next();

				if (ts.isTimeAligned() && ts.getTime() > fromTime && ts != lastFixedSlot ) {
					if(otherFixedSlots == null || (otherFixedSlots != null && !otherFixedSlots.contains(ts))){
						ts.updateTime(ts.getTime() + shift);
					}					
				}	
			} 
			return;       	
        }      
        
        // the lastFixedSlot might or might not be in the otherFixedSlots vector,
        // remove it first 
		if (otherFixedSlots != null && otherFixedSlots.contains(lastFixedSlot)) {
			otherFixedSlots.remove(lastFixedSlot);
		}
		
        // Algorithm:
        // - iterate over timeslots
        // - from first time slot later than fromTime, remember all slots in slotsToShift
        // - until fixedSlot, remember slots that have to be relocated to after fixedSlot
        // - remember first slot after fixedSlot, to insert before, especially if it is unaligned 
        // - shift all slot that are to be shifted
        // - reorder all slots that are to be reordered

		List<TimeSlot> slotsToShift = new ArrayList<TimeSlot>();  
		List<TimeSlot> slotsToReorder = new ArrayList<TimeSlot>();      
		Iterator enum1 = iterator();
        
//		  System.out.println("\nbefore:");
//		  printTimeOrder();
		boolean startAdding = false;
		boolean pastFixedSlot = false;
		TimeSlot beforeSlot = null; // remember next slot from fixedSlot
	
		while (enum1.hasNext()) {
			TimeSlot ts = (TimeSlot) enum1.next();
			
			if (otherFixedSlots != null && otherFixedSlots.contains(ts)) {
				continue; // skip this one
			}
			
			if (ts.isTimeAligned() && ts.getTime() >= fromTime) {
				startAdding = true;	
			}
			if (startAdding) {
				if (ts != lastFixedSlot) {
					slotsToShift.add(ts);	
				}
				else {
					pastFixedSlot = true;	
				}
				if (!pastFixedSlot) {
					slotsToReorder.add(ts);	
				}
				else if(beforeSlot == null && ts != lastFixedSlot) {
					beforeSlot = ts;	
				}
			}	
		}
                
        TimeSlot afterSlot = lastFixedSlot;
        
        // first shift all times
        for (int k = 0; k < slotsToShift.size(); k++) {
			TimeSlot slot = slotsToShift.get(k);
			
			if (slot.isTimeAligned()) {
				slot.updateTime(slot.getTime() + shift);
			}
        }
        
        // then correct order
		for (int i = 0; i < slotsToReorder.size(); i++) {
			TimeSlot slot = slotsToReorder.get(i);
						
			removeTimeSlot(slot);
			insertTimeSlot(slot, afterSlot, beforeSlot);
				
			afterSlot = slot;
		}
        
//		  System.out.println("\nafter:");
//		  printTimeOrder();
	}
	
	/**
	 * Shifts all aligned timeslots with the specified amount of ms.<br>
	 * When an attempt is made to shift slots such that one or more slots 
	 * would have a negative time value an exception is thrown. 
	 * Slots will never implicitely be deleted.
	 * 
	 * @param shiftValue the number of ms to add to the time value of aligned timeslots, 
	 *    can be less than zero
	 * @throws IllegalArgumentException when the shift value is such that any aligned 
	 *    slot would get a negative time value 
	 */
	@Override
	public void shiftAll(long shiftValue) throws IllegalArgumentException {
		if(orderedTimeSlotList.size() <= 0){
			return;
		}
		long firstAlignedTime = 0L;
		
		for (TimeSlot slot : orderedTimeSlotList) {
			if (slot.isTimeAligned()) {
				firstAlignedTime = slot.getTime();
				break;
			}	
		}
		
		if (firstAlignedTime + shiftValue < 0) {
			throw new IllegalArgumentException("The shift value should be greater than: " + 
			 firstAlignedTime + " otherwise slots would get a negative time value.");
		}
		
		// no exception, so shift
		for (TimeSlot slot : orderedTimeSlotList) {
			if (slot.isTimeAligned()) {
				slot.updateTime(slot.getTime() + shiftValue);
			}	
		}
	} 
	
	/**
	 * Add a list of TimeSlots in one operation. <br>
	 * <b>Note: </b> it is assumed that the TimeSlots in the list are ordered!
	 * They are appended to any existing TimeSlots, without performing any checks.
	 * This method is intended to be used at loading time, i.e. where an .eaf (or other
	 * source file) is transformed into a Transcription object.
	 *  
 	 * @param slots a collection of ordered Time Slots
	 */
	public void insertOrderedSlots(List<TimeSlot> slots) {
		if (slots == null) {
			return;
		}
		
		orderedTimeSlotList.addAll(slots);
		reindex();
	}
}

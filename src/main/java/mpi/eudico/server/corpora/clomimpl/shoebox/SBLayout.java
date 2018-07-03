package mpi.eudico.server.corpora.clomimpl.shoebox;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.utr22.SimpleConverter;


/**
 * computes a "Shoebox" style layout
 */
public class SBLayout {
    // ref to refer tier

    /** Holds value of property DOCUMENT ME! */
    protected Tier _ref_tier;

    /** Holds value of property DOCUMENT ME! */
    protected List<Annotation> _vBlockOrder;

    /** Holds value of property DOCUMENT ME! */
    protected int _nBlockPos;

    /** Holds value of property DOCUMENT ME! */
    protected TranscriptionImpl _transcription;
    private ShoeboxTypFile _sbxtf;
    private SimpleConverter _simpleConverter;

    // the current ref tag that is in current view time

    /** Holds value of property DOCUMENT ME! */
    protected List<Annotation> _vRefTags;

    /** Holds value of property DOCUMENT ME! */
    protected Annotation _ref_tag;

    // pos in the the Reference tier that the current ref_tag is from

    /** Holds value of property DOCUMENT ME! */
    protected int _ref_tag_pos;
    private Writer _writer;
    //private List<SBTag> _vSBTags = new ArrayList<SBTag>();

    /**
     * Ctor for SBLayout will create a layout based on the  first segment in
     * the reference tier
     *
     * @param trans DOCUMENT ME!
     */
    public SBLayout(TranscriptionImpl trans) {
        _transcription = trans;

        try {
            _simpleConverter = new SimpleConverter(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param trans Transcription
     * @param sbxtf a Shoebox Typ file (contains characterset of a shoebox
     *        tier)
     */
    public SBLayout(TranscriptionImpl trans, ShoeboxTypFile sbxtf) {
        this(trans);
        _sbxtf = sbxtf;
    }

    /*
     * gets all the reference tiers
     * and sorts them based on their begin times
     * this is the order that the blocks will be displayed
     *
     */
    public void getRefTierOrder() {
        if (_transcription == null) {
        	_vBlockOrder = new ArrayList<Annotation>();
        	return;
        }
        List<AnnotationContainer> vSort = new ArrayList<AnnotationContainer>();
        int i;

        try {
            List<TierImpl> v = _transcription.getTopTiers();

            for (i = 0; i < v.size(); i++) {
                TierImpl ti = v.get(i);

                for (Annotation ann : ti.getAnnotations()) {
                    vSort.add(new AnnotationContainer(ann));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(vSort);

        List<Annotation> vecRet = new ArrayList<Annotation>();

        for (i = 0; i < vSort.size(); i++) {
            Annotation a = vSort.get(i).getAnnotation();
            vecRet.add(a);
        }

        _vBlockOrder = vecRet;
    }

    /**
     * DOCUMENT ME!
     */
    public void getSegOrder() {
        getRefTierOrder(); // not here;
        _nBlockPos = 0;
    }

    // sets the current working segment to show
    // range - howmany segs to show
    // pos = -1 back , 1 forward
    public void setWorkingSegmentsRange(int size, int pos) {
        List<Annotation> retVec = new ArrayList<Annotation>();

        if (_nBlockPos >= _vBlockOrder.size()) {
            if (_nBlockPos > _vBlockOrder.size()) {
                _nBlockPos = _vBlockOrder.size() - 1;
            }
        }

        for (int i = 0; i < size; i++) {
            if (pos == 1) {
                _nBlockPos++;
            } else {
                _nBlockPos--;
            }

            if (_nBlockPos >= _vBlockOrder.size()) {
                _nBlockPos = _vBlockOrder.size() - 1;
            }

            if (_nBlockPos < 0) {
                _nBlockPos = 0;
            }

            retVec.add(_vBlockOrder.get(_nBlockPos));
        }

        if (retVec.size() > 0) {
            _vRefTags = retVec;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param time DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean setBlocksVisibleAtTime(long time) {
        // first check where we are at
        Annotation af = null;
        Annotation al = null;

        // HS 18 nov 03 - prevent NullPointerException at this point
        if ((_vRefTags == null) || (_vRefTags.size() == 0)) {
            return false;
        }

        af = _vRefTags.get(0);
        al = _vRefTags.get(_vRefTags.size() - 1);

        if ((af.getBeginTimeBoundary() <= time) &&
                (al.getEndTimeBoundary() >= time)) {
            return false;
        }

        boolean bf = false;
        long lasttime = -1;

        while (af.getEndTimeBoundary() < time) {
            // go back
            bf = true;

            if (lasttime == af.getEndTimeBoundary()) {
                return false;
            }

            lasttime = af.getEndTimeBoundary();

            setWorkingSegmentsRange(1, 1);
            af = _vRefTags.get(0);
            al = _vRefTags.get(_vRefTags.size() - 1);
        }

        if (bf) {
            //buildLayout();
            return true;
        }

        lasttime = -1;

        while (al.getBeginTimeBoundary() > time) {
            //go forward
            bf = true;

            if (lasttime == af.getBeginTimeBoundary()) {
                return false;
            }

            lasttime = af.getBeginTimeBoundary();

            setWorkingSegmentsRange(1, -1);
            af = _vRefTags.get(0);
            al = _vRefTags.get(_vRefTags.size() - 1);
        }

        if (bf) {
            return true;
        }

        return false;
    }

    /*
       public List  buildAllTiers()
       {
           List vRet = new List();
           Enumeration e = _vRefTags.elements();
           while (e.hasMoreElements())
           {
               Annotation a = (Annotation) e.nextElement();
               TierImpl ti = (TierImpl) a.getTier();
               Hashtable hm = new Hashtable();
               // add the refenece tier
               hm.put(ti, buildTier(ti,a));
               // get the dependent tiers
               List vt = ti.getDependentTiers(null);
               Enumeration en = vt.elements();
               while(en.hasMoreElements())
               {
                   TierImpl timp = (TierImpl) en.nextElement();
                   List v =  buildTier(timp,a);
                   if (v == null)
                       break;
                   hm.put(timp,v);
               }
               vRet.add(hm);
           }
           return vRet;
       }
     */

    /**
     * build the tier in the interlin view if the timeslot has no annotation
     * but is a legal positon for a future annotation a new Long is inserted
     * w/  the current time
     *
     * @param tier DOCUMENT ME!
     * @param start DOCUMENT ME!
     * @param end DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */

    /*
       public List buildTier(Tier tier,Annotation a)
       {
           List vecRet = new ArrayList();
           TierImpl timp = (TierImpl) tier;
           List timeslots = _annSize.getSegmentTimeSlots(_transcription,a);
       System.out.println(a.getBeginTimeBoundary() + "  "+a.getValue() + " seed");
                   List vans = getAnnBetweenTime(timp, a.getBeginTimeBoundary(),a.getEndTimeBoundary() );
                   Enumeration enumTime = timeslots.elements();
                   long currentTime = ((Long) enumTime.nextElement()).longValue();
       try {
                   for (int ii = 0; ii < vans.size() ; ii++ )
                   {
                       Annotation aa = (Annotation) vans.elementAt(ii);
       System.out.println(tier.getName() + "  "+ aa.getBeginTimeBoundary() + " "+ currentTime);
                       while (aa.getBeginTimeBoundary() != currentTime
                              && enumTime.hasMoreElements())
                       {
                           vecRet.add(new Long(currentTime));
                           currentTime = ((Long) enumTime.nextElement()).longValue();
       System.out.println("added null at "+ tier.getName() + "  "+ aa.getBeginTimeBoundary() + " "+ currentTime);
                       }
                       // problem if we could not find
                       // a timeslot for our annotation
                       // report error and bail out
                       if (aa.getBeginTimeBoundary() != currentTime)
                       {
                           System.err.println("ERROR:could not find matching time for "+aa.getValue()+" "+aa.getBeginTimeBoundary() );
                           return null;
                       }
    
                       // we are sure we have matched the time boundry
                       // dont add the annotation to the vector
                       vecRet.add(aa);
                       if (enumTime.hasMoreElements())
                           currentTime = ((Long) enumTime.nextElement()).longValue();
                   }
       } catch (Exception e) {}
                   return vecRet;
               }
     */
    protected List<Annotation> getAnnBetweenTime(Tier tier, long start, long end) {
        List<? extends Annotation> v = null;
        TierImpl ti = (TierImpl) tier;

        List<Annotation> vecRet = new ArrayList<Annotation>();

        try {
            v = ti.getAnnotations();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        for (int i = 0; i < v.size(); i++) {
            Annotation a = v.get(i);

            if ((a.getBeginTimeBoundary() >= start) &&
                    (a.getEndTimeBoundary() <= end)) {
                vecRet.add(a);
            }
        }

        return vecRet;
    }

    /*
       public void recalc(double time)
       {
           _ref_tier = getReferenceTier();
           setRefTagForTime(time);
           if (_ref_tag == null)
           {
               return;
           }
           calcuateTierWidths();
       }
     */

    /**
     * recalulates the layout with a media time of null
     *
     * @return DOCUMENT ME!
     */

    /*
       public void recalc()
       {
           recalc(0);
           repaint();
       }
     */

    /**
     * recomputes the layout of the tiers (this method is usefull if the
     * Annotations size change)
     *
     * @return DOCUMENT ME!
     */

    /*
       public void rebuildTiers()
       {
           createTimeSegHash();
           calcuateTierWidths();
       }
     */

    /**
     * have the objects in the tier image been added/removed
     *
     * @return boolean
     */

    /*
       public boolean isNewImage()
       {
           boolean b = _recal;
           _recal = false;
           return b;
       }
     */

    /**
     * returns a list of SBTags that will be inserted into the scrollpane
     *
     * @return List of SBTag
     *
     * @see SBTag
     */
//    public List<SBTag> getSBTags() {
//        return _vSBTags;
//    }

    /**
     * returns current reference segement start time
     *
     * @return long time value
     */
    public long getCurrentRefStartTime() {
        if (_ref_tag == null) {
            return 0;
        }

        return (_ref_tag.getBeginTimeBoundary());
    }

    /**
     * returns current reference segement end time
     *
     * @return long time value
     */
    public long getCurrentRefEndTime() {
        if (_ref_tag == null) {
            return 0;
        }

        return (_ref_tag.getEndTimeBoundary());
    }

    /**
     * sets reference tier to the prev Annotation based on the current _ref_tag
     *
     * @return true for is next otherwise false
     */
    public boolean getPrevRef() {
        Annotation an = null;

        if (_ref_tier == null) {
            return false;
        }

        an = ((TierImpl) _ref_tier).getAnnotationBefore(_ref_tag);

        if (an == null) {
            return false;
        }

        return true;
    }

    /**
     * this is WRONG it breaks the ability to return more then one block in a
     * view it is here for a quick hack to get the new layout stuff working
     *
     * @return DOCUMENT ME!
     */
    public Annotation getRefAnn() {
        Annotation refann = _vBlockOrder.get(_nBlockPos);

        return refann;
    }

    /**
     * Very temporary method to avoid ArrayIndexOutOfBoundsExceptions in Elan
     * 2.0  without introducing NullPointerExceptions (in older and current
     * Elan version). This is no good...
     *
     * @return whether it is save or not to call getRefAnn()
     */
    public boolean isRefAnnAvailable() {
        if (_vBlockOrder == null) {
            return false;
        }

        return ((_nBlockPos < _vBlockOrder.size()) && !(_nBlockPos < 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @param filename DOCUMENT ME!
     * @param header DOCUMENT ME!
     */
    public void exportAll(String filename, String header) {
        try {
            openWriter(filename); // open fp for writting
            getSegOrder(); // get all segments or blocks
            write(header + "\r\r\n");

            for (int i = 0; i < _vBlockOrder.size(); i++) {
                Annotation refann = _vBlockOrder.get(i);
                AnnotationSize as = new AnnotationSize(_transcription, refann);
                List<TierImpl> vtiers = as.getTiers();

                for (Tier tier : vtiers) {

                    // write tier name
                    write(chopAtChar(tier.getName()) + " ");

                    boolean isSILIPAcharacterset = (_sbxtf != null) &&
                        _sbxtf.isIPAtier(chopAtChar(tier.getName()));

                    //List tieranns = as.getTierLayoutInChar(tier);
                    //Enumeration ee = tieranns.elements();
                    // changed HS 19-01-2004
                    List<AnnotationSizeContainer> tierAnn = as.getTierLayoutInPixels(tier, null);

                    for (AnnotationSizeContainer asc : tierAnn) {
                        Annotation a = asc.getAnnotation();
                        String wstr = "";

                        if (a == null) {
                            wstr = (padString("", asc.getSize() + 1));
                        } else {
                            String trimedValueofa = a.getValue().trim();

                            if (isSILIPAcharacterset) {
                                trimedValueofa = _simpleConverter.toBinary(trimedValueofa);
                            }

                            wstr = padString(trimedValueofa, asc.getSize() + 1);
                        }

                        // write annotation
                        write(padString(wstr, asc.getSize()));

                        // write the tier speaker and 
                        // time stamp block when the 
                        // current annotation is the 
                        // ref annotation
                        writeBlockStamp(a, refann);
                    }

                    write("\r\n");
                }

                write("\r\n");
            }

            _writer.flush();
            _writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
       public StringBuilder exportAll2(String filename,String header)
       {
           try {
           openWriter(filename);
           getSegOrder(); // get all segments or blocks
           write(header+"\n");
           setWorkingSegmentsRange(1,0); // set to load 1 block at a time at n block
           exportSeg();
           for (int i = 0 ; i < _vBlockOrder.size()-1; i++)
           {
               setWorkingSegmentsRange(1,1); // set to load 1 block at a time at n block
               exportSeg();
               write("\r\r\n");
           }
               _writer.flush();
               _writer.close();
           } catch(Exception e) {}
           return null;
       }
       private StringBuilder exportSeg()
       {
           try {
           List v = buildAllTiers(); // get all tiers for current block
           Enumeration e = v.elements(); // get enumeration of current tier/block
           while(e.hasMoreElements())
           {
               Enumeration eVisTier = null;
               Hashtable ht = (Hashtable) e.nextElement(); // get next tier in block
               eVisTier = _transcription.getTiers(null).elements(); // get all the tiers
    
               while(eVisTier.hasMoreElements())
               {
                   Tier tiercurrent = (Tier)eVisTier.nextElement();
                   List vTier = (List) ht.get(tiercurrent);  // get all tags at tiercurrent
                   if (vTier != null)
                   {
                       // write the tier name
                       write(chopAtChar(tiercurrent.getName())+" ");
                       // make enum of tier elements
                       Enumeration eTier = vTier.elements();
                       String wkstr = "";
                       int sz = 0;
                       Annotation last = null;
                       // loop thu all the elements
                       while(eTier.hasMoreElements())
                       {
                           Annotation a = null;
                           Object obj = (Object)eTier.nextElement();
                           if (obj instanceof Annotation)
                           {
                               a = (Annotation) obj;
                               // get spacing of tag
                               sz = _annSize.getAnnotationGroupSpacing(_transcription,a)+1;
                               wkstr = a.getValue();
                               last = a;
    
                           } else if( obj instanceof Long) {
                               Long l = (Long) obj;
                               sz = (_annSize.getSpacingForTime(_transcription,last,l.longValue()))+1; // get spacing of blank item
                               wkstr = "";
                           }
                           // write annotation
                           write(padString(wkstr,sz));
                           // write the tier speaker and
                           // time stamp block when the
                           // current annotation is the
                           // ref annotation
                           //writeBlockStamp(a);
                       }
                      }
               write("\r\r\n");
               }
           }
           } catch (Exception ex) {
               ex.printStackTrace();
               return null;
           }
           return null;
       }
     */
    private String padString(String orig, int totallen) {
        orig.trim();

        int len = totallen - orig.length();

        if (len < 0) {
            return orig;
        }

        for (int i = 0; i < len; i++) {
			orig += " ";
		}

        return orig;
    }

    private String chopAtChar(String str) {
        int in = str.indexOf("@");

        if (in == -1) {
            return addSlash(str);
        }

        return addSlash(str.substring(0, in));
    }

    private String addSlash(String str) {
        int in = str.indexOf("\\");

        if (in == -1) {
            str = "\\" + str;
        }

        return str;
    }

    private String getSpeakerFormat(String str) {
        int in = str.indexOf("@");

        if (in == -1) {
            return "\\EUDICOp unknown";
        }

        return "\\EUDICOp " + str.substring(in + 1);
    }

    private void writeBlockStamp(Annotation a, Annotation refann)
        throws IOException {
        if (a == null) {
            return;
        }

        if (a.equals(refann)) {
            String wkstr = "\r\n";

            try {
                Tier t = a.getTier();
                wkstr += (getSpeakerFormat(t.getName()) + "\r\n");
            } catch (Exception e) {
            }

            wkstr += ("\\EUDICOt0 " + (a.getBeginTimeBoundary() * .001) +
            "\r\n\\EUDICOt1 " + (a.getEndTimeBoundary() * .001));
            write(wkstr + "\r\n");
        }

        return;
    }

    private void openWriter(String file) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            _writer = new OutputStreamWriter(fos, "ISO-8859-1");
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    private void write(String text) throws IOException {
        try {
            _writer.write(text);
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    /**
     * DOCUMENT ME!
     * $Id: SBLayout.java 43699 2015-04-16 11:47:41Z olasei $
     * @author $Author$
     * @version $Revision$
     */
    public class AnnotationContainer implements Comparable<AnnotationContainer> {
        /** Holds value of property DOCUMENT ME! */
        Annotation _ann;

        /**
         * Creates a new AnnotationContainer instance
         *
         * @param a DOCUMENT ME!
         */
        public AnnotationContainer(Annotation a) {
            _ann = a;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Annotation getAnnotation() {
            return _ann;
        }

        /**
         * DOCUMENT ME!
         *
         * @param obj DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
		public int compareTo(AnnotationContainer obj) {
            AnnotationContainer ac = obj;
            Annotation a = ac.getAnnotation();

            if (_ann.getBeginTimeBoundary() > a.getBeginTimeBoundary()) {
                return 1;
            }

            if (_ann.getBeginTimeBoundary() < a.getBeginTimeBoundary()) {
                return -1;
            }

            return 0;
        }
    }
}

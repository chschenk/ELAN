package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.server.corpora.clom.Annotation;

import java.awt.Rectangle;


/**
 * DOCUMENT ME!
 * $Id: AnnotationSizeContainer.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class AnnotationSizeContainer implements Comparable {
    /** Holds value of property DOCUMENT ME! */
    public static int PIXELS = 1;

    /** Holds value of property DOCUMENT ME! */
    public static int SPACES = 2;
    private int _size;
    private Annotation _ann;
    private int _type = 0;
    private long _stime = 0;

    /** Holds value of property DOCUMENT ME! */
    public Long _lstime = null;
    private long _etime = 0;
    private Rectangle _rect = null;

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann DOCUMENT ME!
     * @param size DOCUMENT ME!
     * @param st DOCUMENT ME!
     * @param et DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public AnnotationSizeContainer(Annotation ann, Integer size, long st,
        long et, int type) {
        _ann = ann;
        _size = size.intValue();
        _type = type;
        _stime = st;
        _etime = et;
        _lstime = new Long(_stime);
    }

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann DOCUMENT ME!
     * @param size DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public AnnotationSizeContainer(Annotation ann, Integer size, int type) {
        _ann = ann;
        _size = size.intValue();
        _type = type;

        if (ann != null) {
            _lstime = new Long(ann.getBeginTimeBoundary());
        }
    }

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann DOCUMENT ME!
     * @param size DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public AnnotationSizeContainer(Annotation ann, int size, int type) {
        _ann = ann;
        _size = size;
        _type = type;

        if (ann != null) {
            _lstime = new Long(ann.getBeginTimeBoundary());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param rect DOCUMENT ME!
     */
    public void setRect(Rectangle rect) {
        _rect = rect;
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
     * @return DOCUMENT ME!
     */
    public int getSize() {
        return _size;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getType() {
        return _type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getStartTime() {
        return _stime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getEndTime() {
        return _etime;
    }

    // compoare to interface for locatet
    @Override
	public int compareTo(Object o) {
        if (_lstime == null) {
            System.out.println("NULL STIME");

            return -1;
        }

        Long l = null;
        Long l1 = null;

        if (_ann != null) {
            l1 = new Long(_ann.getBeginTimeBoundary());
        }

        if (o instanceof Long) {
            l = (Long) o;
        } else {
            l = new Long(((AnnotationSizeContainer) o).getAnnotation()
                          .getBeginTimeBoundary());
        }

        System.out.println(l1 + " " + l1);

        return l1.compareTo(l);
    }
}

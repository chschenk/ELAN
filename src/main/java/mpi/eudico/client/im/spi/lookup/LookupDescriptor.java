/**
 * A truly easy implementation.
 */
package mpi.eudico.client.im.spi.lookup;

import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;

import java.util.Locale;


/**
 * DOCUMENT ME!
 * $Id: LookupDescriptor.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class LookupDescriptor implements InputMethodDescriptor {
    /**
     * Creates a new LookupDescriptor instance
     */
    public LookupDescriptor() {
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getAvailableLocales
     */
    @Override
	public Locale[] getAvailableLocales() {
        return Lookup2.SUPPORTED_LOCALES;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#hasDynamicLocaleList
     */
    @Override
	public boolean hasDynamicLocaleList() {
        return false;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodDisplayName
     */
    @Override
	public synchronized String getInputMethodDisplayName(Locale il, Locale dl) {
        return "mpi.nl";
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodIcon
     */
    @Override
	public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodClassName
     */
    public String getInputMethodClassName() {
        return null;
    }

    /**
     * Creates a new Lookup instance.
     *
     * @return the input method
     *
     * @throws Exception any
     */
    @Override
	public InputMethod createInputMethod() throws Exception {
        return new Lookup2();
    }
}

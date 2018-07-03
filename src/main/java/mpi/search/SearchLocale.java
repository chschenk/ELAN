package mpi.search;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created on Jun 10, 2004
 * 
 * @author Alexander Klassmann
 * @version July 2004
 */
public class SearchLocale {
    private static Locale locale;

    private static ResourceBundle resourcebundle;

    /**
     * Constructor
     */
    static {
        setLocale(Locale.getDefault());
    }

    /**
     * Gets the current locale
     * 
     * @return The current locale
     */
    final public static Locale getLocale() {
        return locale;
    }

    /**
     * Sets the current locale
     * 
     * @param locale_in
     *            The new locale
     */
    final public static void setLocale(Locale locale_in) {
		if (locale != null && locale.equals(locale_in)) {
			return;
		}
    	//if (locale != null && locale.getCountry().equals(locale_in.getCountry())) {
        //    return;
        //}

        locale = locale_in;
        try {
            resourcebundle = ResourceBundle.getBundle("mpi.search.resources.SearchLanguage",
                    locale);
        } catch (MissingResourceException e) {
        }

        if (resourcebundle == null) {
            System.out.println("WARNING: no language resources for "
                    + locale.getDisplayLanguage());
        }
    }

    /**
     * Alternative for {@link #setLocale(Locale)}. In case a bundle has to be loaded from a different 
     * location than the standard location.
     * 
     * @param resBundle the resource bundle, loaded from an alternative location
     */
    public static final void setResourceBundle(ResourceBundle resBundle) {
    	if (resBundle != null) {
    		resourcebundle = resBundle;
    	}
    }
    
    /**
     * Gets the string in the right language from the right resource file
     * 
     * @param str
     *            The string which has to be mapped to the right language
     * 
     * @return The string in the right language
     */
    final public static String getString(String str) {
        if (locale == null) {
            setLocale(Locale.getDefault());
        }
        if (resourcebundle != null) {
            try {
                return resourcebundle.getString(str);
            } catch (MissingResourceException ex) {
                System.out.println("Warning: no localization for " + str
                        + " found in language " + locale.getDisplayCountry());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }
}

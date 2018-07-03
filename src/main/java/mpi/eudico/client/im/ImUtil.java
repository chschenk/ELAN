package mpi.eudico.client.im;

import guk.im.GateIM;
import guk.im.GateIMDescriptor;

import java.awt.AWTException;
import java.awt.Component;
import java.util.HashSet;
import java.util.Locale;

import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.im.spi.lookup.Lookup2;
import mpi.eudico.client.im.spi.lookup.LookupDescriptor;
/**
   <p>
   This is a MPI-PL utility for the input methods of Java1.3 (java.awt.im.spi).
   Clients of this Class are editors.
   An editor will either let the user select the language via a menu or
   will use other information to set the language.
   </p>
   
   <p>
   The class encapsulates the GUK input method and the input methods written 
   at the MPI-PL. A client will not notice which input method is used.
   </p>   
*/
public class ImUtil {

	// for testing if the requested locale exists.
	private static final HashSet<String> allLocales = new HashSet<String>();
	public static boolean showKeyboard = true;
	/* HS 01-2004 use all available input methods */
	// private static final ArrayList localesList = new ArrayList(30);

	static {
		// read all locales
		//System.out.println("Default locale: "+ Locale.getDefault());
		
		try {
			GateIMDescriptor gukDescriptor = new GateIMDescriptor();
			Locale[] gukLocales = gukDescriptor.getAvailableLocales();
			for (int i = 0; i < gukLocales.length; i++) {
				//localesList.add(gukLocales[i]);
				allLocales.add(gukLocales[i].toString());
			}
		}
		catch (AWTException ae) { /* nop */
		}
		LookupDescriptor lupDescriptor = new LookupDescriptor();
		Locale[] lupLocales = lupDescriptor.getAvailableLocales();
		for (int i = 0; i < lupLocales.length; i++) {
			//localesList.add(lupLocales[i]);
			allLocales.add(lupLocales[i].toString());
		}
	}

	/*
	  Locales defined in mpi.eudico.client.im.spi.lookup.
	  The Chinese Locales are taken from Sun. 
	*/
	public static final Locale IPA96_RTR = Lookup2.IPA96_RTR;

	/*
	  The following locales are implemented in GUK.
	  GUK loads locales only at startup. 
	  At compile-time, there are no constants I could refer to.
	  ImUtil assumes that GUK supports a specific locale.
	  I took the locale variant from 
	  GUK version 1.1, file guk/resources/guk/im/data/im.list
	  If a new version of GUK is used, the Locale has to be verified.
	  The Locale must match character per character, in all 3 arguments.
	*/
	public static final Locale IPA96_SAMPA = new Locale("IPA-96", "", "SAMPA");
	public static final Locale Cyrillic = new Locale("RU", "", "YAWERTY (Phonetic)");
	public static final Locale Arabic1 = new Locale("AR", "", "MLT Arabic");
	public static final Locale Arabic2 = new Locale("AR", "", "Windows");
	public static final Locale Hebrew = new Locale("HE", "", "Standard");
	public static final Locale GEORGIAN_HEI = new Locale("ka", "", "Heinecke");
	public static final Locale GEORGIAN_IMNA = new Locale("ka", "", "Imnaishvili Arrangement");
	public static final Locale GEORGIAN_MLT = new Locale("ka", "", "MLT");
	public static final Locale KOREAN = new Locale("ko", "", "Standard Hangul");
	public static final Locale TURKISH = new Locale("tr", "", "Standard");
	public static final Locale IPA_EXT_VK = new Locale("ipa-ext", "", "IPA Extended");
	public static final Locale ENGLISH = new Locale("en", "", "ASCII");
	

	/**
	   An ImUtil client requests a list of supported locales with getLanguages().
	   The order in which the user sees the locales is defined here.
	   @param visual the visual component of the editor.
	   @return an array of languages as {@see java.util.Locale}.
	 */
	public static final Locale[] getLanguages(Component component) {
		Locale defaultLocale;
		if ((component != null) && (component.getLocale() != null))
			defaultLocale = component.getLocale();
		else
			defaultLocale = Locale.getDefault();

		/* use this when all available languages should be returned
		if (!localesList.contains(defaultLocale)) {
			localesList.add(0, defaultLocale);
		}
		
		try {
			Locale[] result = (Locale[])localesList.toArray(new Locale[0]);
			return result;
		} catch (ArrayStoreException ase){
			System.out.println("Warning: could not load locales");
			return new Locale[]{defaultLocale};
		}
		*/
		return new Locale[] {
			defaultLocale,
			ImUtil.Arabic1,
			ImUtil.Arabic2,
			Lookup2.CHINESE_SIM,
			Lookup2.CHINESE_TRA,
			ImUtil.ENGLISH,
			ImUtil.GEORGIAN_HEI,
			ImUtil.GEORGIAN_IMNA,
			ImUtil.GEORGIAN_MLT,
			ImUtil.Hebrew,
			Lookup2.IPA96_RTR,
			ImUtil.IPA96_SAMPA,
			ImUtil.IPA_EXT_VK,
			ImUtil.KOREAN,
			ImUtil.Cyrillic,
			ImUtil.TURKISH
			};

	}

	/**
	   An ImUtil client requests a list of supported locales with getLanguages().
	   The order in which the user sees the locales is defined here.
	   @return an array of languages as {@see java.util.Locale}.
	 */
	public static final Locale[] getLanguages() {
		return getLanguages(null);
	}

	/**
	   An ImUtil client must contain a Component, 
	   for which the input method and the font will be set
	   @param component the component for which the font has to be set.
	   @param language the language to be set. 
	*/
	public static final void setLanguage(Component component, Locale language) {

		// set IM
		component.setLocale(language);	
		
		try {
			if (component.getInputContext() != null) {
				boolean success = component.getInputContext().selectInputMethod(language);
				//System.out.println("Successfully changed the locale: " + success);
				// setting the locale to the system default on Mac OS X doesn't seem to disable or
				// deselect the current locale for the component
				if (SystemReporting.isMacOS()) {
					component.enableInputMethods(success);
				}				
				
				Object imObject = component.getInputContext().getInputMethodControlObject();
				if (imObject != null && imObject instanceof GateIM && showKeyboard) {
					((GateIM) imObject).setMapVisible(success);// makes the keyboard invisible in case switching back to default
					// but the overall input method on Mac OS is not set back to default
				}
			}

		}
		catch (NullPointerException npe) {
			System.out.println(
				"Component "
					+ component.getClass()
					+ " has no InputContext - no input method set!");
		}
	}

}

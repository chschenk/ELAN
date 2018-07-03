package mpi.eudico.server.corpora.clomimpl.shoebox.utr22;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
   This class converts a 8-bit "binary" font Unicode and vice versa.
   The Conversion is specified in utr-22 format.
   It is Assumed that each character can be converted without context.
   This is true for the sil-ipa font.
   UTR-22 is documented elsewhere.
*/
public class SimpleConverter {

	/** b-->u conversion */
	private String[] a   = new String[256];
	private String[] fbu = new String[256];
	/** number of bad chararcters encountered */
	private int badchars;

	/** u-->b conversion */
	private HashMap u = new HashMap();
	/**
	   @param inps The utr22 description
	 */
	public SimpleConverter(InputSource inps) throws IOException, SAXException, ParserConfigurationException {
		loadUtr22(inps);
		test();
	}


	/**
	 * Used for loading the UTR description
	   @param y u-value may be a list
	   @return String from y
	 */
	private String decodeList(String y) {
		if (y == null ) return "";
		if (y.indexOf(' ') != -1) {
			String[] arrb = y.split(" ");
			String result = "";
			for (int ii = 0; ii<arrb.length; ii++) {
				result += decode(arrb[ii]);
			}
			return result;
		} else { 
			return decode(y);
		}
	}

	/**
	   @param y u-value may not be a list
	 */
	private String decode(String y) {
		char x = (char)Integer.parseInt(y, 16);
		return "" + x;
	}

	/**
	   Binary to Unicode.
	   @param eightbit 8-bit, non-unicode character
	   @return Unicode value or input
	 */
	private final String toUnicode(char eightbit) {
//		System.out.print("toUnicode char " + eightbit);
		if ((int)eightbit>256) {
			// something is wrong
			badchars += 1;
//			System.out.println(" error");
			return ""+eightbit; // safety: isnt eightbit really.
		}
		String y = a[(int)eightbit];
		if (y==null) {
			badchars += 1;
//			System.out.println(" error null");
			return "\uFFFD";
		} else {
//			System.out.println(" " + y);
			return y;
		}
	}

	/**
	   Unicode to Binary.
	   @param unicode Character
	   @return 8bit or escape
	 */
	private final String toBinary(Character unicode) {
		int i = (int)(unicode.charValue());
//		System.out.println("  toB " + unicode + "(" + i);
		if (i<128) {
			// ASCII!
			return ""+unicode;
		} 
		// IPA?
		if (u.containsKey(unicode.toString())) {
			char x = (char)(Integer.decode(""+u.get(unicode.toString()))).intValue();
			String rr = ""+ new Character(x);
//			System.out.println(rr + "--" + (int)x + "--" + u.get(unicode.toString()));
			return rr;
		} 
		// has to be escaped
		//MK:02/08/27 quick fix, because fixed-length format would be destroyed by &x....;
		if (true) return "?";
		String xs = Integer.toHexString(i);
//		System.out.println("--"+ xs);
		if (xs.length()<4) xs = "0"+xs;
		return "&x" + xs+";";
	}
	
	/**
	 * Unicode to Binary
	 * @param u Unicode String
	 * @return escaped binary equivalent of input	 
	 */
	public final String toBinary(String u) {
		String result = "";
//		System.out.println("to Binary" + u);
		for (int i=0; i<u.length(); i++) {
			result += toBinary(new Character(u.charAt(i)));
		}
		return result;
	}


	/**
	 * Binary to Unicode.
	 * @param eightbit 8-bit binary, non-unicode String
	 * @return Unicode equivalent of input
	 */
	public final String toUnicode(String eightbit) {
		String result = "";
		eightbit = decodeAmpersands(eightbit);
		for (int i=0; i<eightbit.length(); i++) {
			result += toUnicode(eightbit.charAt(i));
		}
		return result;
	}

	private String decodeAmpersands(String eightbit) {
		return decodeAmpersands(eightbit, 0);
	}

	private String decodeAmpersands(String eightbit, int start) {
		if (eightbit == null) return "";
		int dix = eightbit.indexOf("&x", start);
		if (dix<0) return eightbit;
		if (eightbit.length() < dix+6) return eightbit;
		if (!";".equals(""+eightbit.charAt(dix+6))) return eightbit;
		String numbers = eightbit.substring(dix+2, dix+6);
//		System.out.println("numbers--"+numbers);
		String result = eightbit;
		try {
			char x = (char)Integer.parseInt(numbers, 16);
//			System.out.println("x--"+(int)x);
			String a = eightbit.substring(0, dix);
			String z = eightbit.substring(dix+7, eightbit.length());
			result = a + new Character(x) + z;
		}
		catch (NumberFormatException ex) {
			//NOP
		}		
		return decodeAmpersands(result, dix);
	}

	/**
	 * Method must be called, even with null argument.
	 * The method does a quick scan of the UTR22 defintion. It is by no means complete.
	 * @param inps an UTR22 definition. Defaults to silipa93
	 * */
    private final void loadUtr22 (InputSource inps) throws  IOException, SAXException, ParserConfigurationException {
		// MK:02/10/24 Why an inputsource? Because doc.parse() chokes on file from within jar...
		if (inps == null) {
			URL url = getClass().getResource("/mpi/eudico/server/corpora/clomimpl/shoebox/utr22/silipa93.xml");
			inps = new InputSource(url.openStream());
		}		
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
		dbf.setValidating(false); 
		DocumentBuilder db = dbf.newDocumentBuilder ();
		Document doc = null;
		doc = db.parse(inps);
//		System.out.println("utr22: first element = " + doc.getDocumentElement());
		NodeList nla;
		nla = doc.getElementsByTagName("a");
		for (int ii = 0; ii<nla.getLength(); ii++) {
			Element ea = (Element) nla.item(ii);
			String ubctxt = ea.getAttribute("ubctxt");
			if (ubctxt.length()>0) continue;
			String encb = ea.getAttribute("b");
			String encu = ea.getAttribute("u");
			int x = Integer.parseInt(encb, 16);
			a[x] = decodeList(encu);
		}
		nla = doc.getElementsByTagName("fbu");
		for (int ii = 0; ii<nla.getLength(); ii++) {
			Element ea = (Element) nla.item(ii);
			String encb = ea.getAttribute("b");
			String encu = ea.getAttribute("u");
			int x = Integer.parseInt(encb, 16);
			fbu[x] = decodeList(encu);
		}

		// Use fallbacks if present!
		for (int i = 0; i<256; i++) {
			if (a[i]   != null) continue;
			if (fbu[i] == null) continue;
			a[i] = fbu[i];
		}

		//for the way u-->b I will use a hash
		for (int i = 0; i<256; i++) {
			if (a[i] == null) continue;
			u.put(a[i], Integer.valueOf(i));
		}
		

	}
	
	void test() {
		/*
		if (!"yes".equals(System.getProperty("testing"))) return;

		try {
			String inStr;
			String out;			
			
			inStr= "\u0087 QWERT YUIOP ASDFG HJKL ZXCVB NM\r\n"
		+ "o# o$ o% o^\r\n"
		+ "a&x05d0;leph\r\n"
		+ "b&x05d1;et\r\n";
			System.out.println("testing the string\n" + inStr);
			out = toUnicode(inStr);
			StringUtil.writeEncodedFile("UTF-8", "testout.unicode.txt", out);

			out = toBinary(out);
			StringUtil.writeEncodedFile("ISO-8859-1", "testout.silipa.txt", out);
		}  catch (Exception e) {e.printStackTrace();}
		System.out.println("bad chars " + badchars);
		*/
	}
	
	public static void main (String[] args) throws Exception{
		SimpleConverter sc = new SimpleConverter(null);
		
	}

}


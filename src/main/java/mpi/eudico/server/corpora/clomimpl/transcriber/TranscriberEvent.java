/*
 * Created on Feb 4, 2005
 *
 */
package mpi.eudico.server.corpora.clomimpl.transcriber;

/**
 * Utility class to map Transcriber Events to Strings that can be embedded
 * in Annotations.
 * 
 * @author hennie
 */
public class TranscriberEvent {

	public static String getEventString(String desc, String extent) {
		//String enclosedPart = desc;
		String prefix = "";
		String suffix = "";
		
		if (extent != null) {
			if (extent.equals("begin")) {
				desc += "-";
			}
			else if (extent.equals("end")) {
				desc = "-" + desc;
			}
			else if (extent.equals("previous")) {
				prefix = "+";
			}
			else if (extent.equals("next")) {
				suffix = "+";
			}
		}
		
		return prefix + " [" + desc + "] " + suffix;
	}
}

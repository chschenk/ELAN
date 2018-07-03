package mpi.eudico.client.annotator.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UrlOpener {
	public static void openUrl(String url) throws Exception{
		//System.out.println("openUrl: " + url);
		if (url == null) {
            return;
        }
 		
        URI uri;
		try {
			uri = new URI(url);
			if (url.startsWith("mailto:")) {
				Desktop.getDesktop().mail(uri);
			} else {
				Desktop.getDesktop().browse(uri);
			}
		} catch (URISyntaxException use) {
			ClientLogger.LOG.warning("Error opening webpage: " + use.getMessage());
			throw(use);
			//use.printStackTrace();
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Error opening webpage: " + ioe.getMessage());
			throw(ioe);
			//ioe.printStackTrace();
		}
	}
}

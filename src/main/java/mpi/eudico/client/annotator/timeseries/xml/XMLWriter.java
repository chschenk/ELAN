package mpi.eudico.client.annotator.timeseries.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.NonContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeValue;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.TimeFormatter;

/**
 * A class to export a timeseries track to a (recognizer) timeseries file,
 * as xml file.
 * 
 * TODO: add support for exporting multiple tracks to one file
 * 
 * @author Han Sloetjes
 */
public class XMLWriter {

	/**
	 * Constructor
	 */
	public XMLWriter() {
		super();
	}
	
	/**
	 * Write one track to an xml file.
	 * 
	 * @param f the destination file
	 * @param track the track to export
	 * @throws IOException any io exception
	 */
	public void writeTrackToXML(File f, AbstractTSTrack track) throws IOException {
		if (f == null) {
			ClientLogger.LOG.warning("File is null!");
			throw new NullPointerException("File is null");
		}
		if (track == null) {
			ClientLogger.LOG.warning("Track is null!");
			throw new NullPointerException("Track is null");
		}
		//final String SC = ";";
		DecimalFormat decFormat = new DecimalFormat("#0.000",
	            new DecimalFormatSymbols(Locale.US));
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f), "UTF-8")));
			
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.print("<TIMESERIES xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
			writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-timeseries.xsd\" ");
			writer.print("columns=\"");
			if (track.getName().startsWith("#")) {
				writer.println(track.getName() + "\">");
			} else {
				writer.println("#" + track.getName() + "\">");
			}
			
			if (track instanceof ContinuousRateTSTrack) {
				ContinuousRateTSTrack crt = (ContinuousRateTSTrack) track;
				// check for 0?
				float msPerSample = 1000 / crt.getSampleRate();
				
				if (crt.getData() instanceof float[]) {
					float[] data = crt.getData();
					
					for (int i = 0; i < data.length; i++) {
						writer.print("<i t=\"");
						writer.print(TimeFormatter.toSSMSString((long)(i * msPerSample)));
						writer.print("\"><v>");
						if (Float.isNaN(data[i])) {
							writer.print("n/a");
						} else {
							writer.print(decFormat.format(data[i]));
						}
						writer.println("</v></i>");
					}
					
					writer.println("</TIMESERIES>");
				} else {
					ClientLogger.LOG.warning("Unknown type of track data, array of float expected.");
				}
			} else if (track instanceof NonContinuousRateTSTrack) {
				NonContinuousRateTSTrack ntrack = (NonContinuousRateTSTrack) track;
				
				if (ntrack.getData() instanceof List) {
					List<TimeValue> dataList = ntrack.getData();
					Object iter;
					TimeValue tv;
					for (int i = 0; i < dataList.size(); i++) {
						iter = dataList.get(i);
						if (iter instanceof TimeValue) {
							tv = (TimeValue) iter;
							writer.print("<i t=\"");
							writer.print(TimeFormatter.toSSMSString(tv.time));
							writer.print("\"><v>");
							if (Float.isNaN(tv.value)) {
								writer.print("n/a");
							} else {
								writer.print(decFormat.format(tv.value));
							}
							writer.println("</v></i>");
						}
					}
					writer.println("</TIMESERIES>");
				} else {
					ClientLogger.LOG.warning("Unknown type of track data, List of TimeValue objects expected.");
				}
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception ex) {
					// any exception
				}
			}
		}
	}
}

/*
 * Created on Sep 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * @author hennie
 */
public class ByteRenderer extends Renderer {

	public static String[] render(Metrics metrics) {
		String[] outputLines = new String[metrics.getMaxVerticalPosition() + 1];

		// initialize
		for (int i = 0; i < outputLines.length; i++) {
			outputLines[i] = "";		
		}
		
//		renderTierLabels(metrics, outputLines);
		renderAnnotationValues(metrics, outputLines);	
		
		return outputLines;	
	}
	
	public static void renderTierLabels(Metrics metrics, String[] outputLines) {
		Integer vPos = null;
		String tierLabel = "";

		List<Integer> vPositions = metrics.getPositionsOfNonEmptyTiers();
	
		Iterator posIter = vPositions.iterator();
		while (posIter.hasNext()) {
			vPos = (Integer) posIter.next();
			tierLabel = metrics.getTierLabelAt(vPos.intValue());			
									
			outputLines[vPos.intValue()] += tierLabel + " ";
		}
	}
	
	public static void renderAnnotationValues(Metrics metrics, String[] outputLines) {		
		List<Annotation> annots = metrics.getBlockWiseOrdered();

		Iterator<Annotation> annIter = annots.iterator();
		while (annIter.hasNext()) {
			Annotation a = annIter.next();
			int vPos = metrics.getVerticalPosition(a);
			int hPos = metrics.getHorizontalPosition(a);
			
			String tierLabel = metrics.getTierLabelAt(vPos);
			if (!(metrics.getInterlinearizer().getCharEncoding(tierLabel) == Interlinearizer.UTF8)) {
				outputLines[vPos] += nSpaces(hPos - outputLines[vPos].length()) + a.getValue();
			}
			else {
				outputLines[vPos] += nSpaces(hPos - 
						SizeCalculator.getNumOfBytes(outputLines[vPos])) + a.getValue();
			}
		}		
	}
	
	private static String nSpaces(int n) {
		String ret = "";
		for (int i = 0; i < n; i++) {
			ret += " ";
		}
		return ret;
	}
}
package mpi.eudico.client.annotator.interlinear;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * @author Han Sloetjes
 */
public class PrintTester {
	TranscriptionImpl transcription;

	public PrintTester(Transcription transcription) {
		this.transcription = (TranscriptionImpl)transcription;
	}

	public void test() {
		/*				
			Vector tiers = transcription.getTiers(null);
			List<TierImpl visTiers = new ArrayList(tiers.size());
			AbstractAnnotation aa = null;
			for (int i = 0; i < tiers.size(); i++) {
				TierImpl t = (TierImpl) tiers.get(i);
				if (!t.getName().equals("W-Words") && !t.getName().equals("W-POS")) {
					visTiers.add(t);
				}
				if (t.getName().equals("W-Spch")) {
					Vector ann = t.getAnnotations(null);
					if (ann.size() > 0) {
						aa = (AbstractAnnotation) ann.get(0);
					}
				}
			}
			if (aa != null) {
				AnnotationBlockCreator cr = new AnnotationBlockCreator();
				cr.createBlockForAnnotation(aa, visTiers);
			} else {
				System.out.println("Annotation null");
			}

		*/
		Interlinear inter = new Interlinear(transcription, Interlinear.INTERLINEAR_TEXT);
		inter.setTimeCodeShown(true);
		inter.setBlockWrapStyle(Interlinear.WITHIN_BLOCKS);
		inter.setLineWrapStyle(Interlinear.NEXT_LINE);
		//inter.setWidth(550);
		//inter.setHeight(600);
		//BufferedImage buf = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		//inter.renderView(buf);
		
		new InterlinearPreviewDlg(null, false, inter).setVisible(true);
	}
}

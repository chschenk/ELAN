package mpi.eudico.client.annotator.grid;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

/**
 * This class makes the GridViewer editable and let him responds to changes in
 * ACM elsewhere
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
abstract public class AbstractEditableGridViewer extends AbstractGridViewer implements
        ACMEditListener {

    protected GridEditor gridEditor;

    /**
     * Constructor
     * 
     */
    public AbstractEditableGridViewer(AnnotationTable table) {
        super(table);
    }

    @Override
	protected void initTable() {
        super.initTable();
        gridEditor = new GridEditor(this, dataModel);
        table.setDefaultEditor(Object.class, gridEditor);
    }

    /**
     * Update method from ActiveAnnotationUser.
     */
    @Override
	public void updateActiveAnnotation() {
        if (dataModel.getRowCount() == 0) {
            return;
        }
        repaint();
        if (getActiveAnnotation() != null) {
            doUpdate();
        }
    }

    /**
     * Checks the kind of edit that has happened and updates the table when
     * necessary.
     * 
     * @param e
     *            the ACMEditEvent
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
        if (dataModel.getRowCount() == 0) {
            return;
        }
        switch (e.getOperation()) {
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
            repaint();
            break;
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE:
            repaint();
            break;
        default:
            repaint();
        }
    }

    protected void updateDataModel(List<? extends AnnotationCore> annotations) {
        gridEditor.cancelCellEditing();
        if (annotations != null) {
			dataModel.updateAnnotations(annotations);
		}
    }

    /**
     * method from ElanLocaleListener not implemented in AbstractViewer
     */
    @Override
	public void updateLocale() {
        gridEditor.updateLocale();
        super.updateLocale();
    }

    /**
     * Look up colors in the "preferences" (really in the Controlled Vocabularies) to be
     * used in the cells of the grid viewer. With that it creates a Tier Name -> CveId -> Color mapping.
     * <p>
     * Since the preferences contain a CV name -> CveId -> Color mapping, it could potentially
     * re-use parts of that to build the new mapping.
     */
	@Override
	public void preferencesChanged() {
		// Controlled Vocabulary based colors
		final TranscriptionImpl transcription = (TranscriptionImpl)getViewerManager().getTranscription();
		Map cvPrefObj = Preferences.getMap(Preferences.CV_PREFS, transcription);
		if (cvPrefObj != null) {// there are preferred colors, get them from the vocabularies?
			Map<String, String> CV2TierMap = new HashMap<String, String>();
			Map<String, Map<String, Color>> colMap = new HashMap<String, Map<String, Color>>();
			//iterate over tiers!
			List<TierImpl> tiers = transcription.getTiers();
			String cvName;
			TierImpl t;
			ControlledVocabulary cv;
			// iterate over tiers, though this potentially leads to duplicates of CV entry to color mappings
			for (int i = 0; i < tiers.size(); i++) {
				t = tiers.get(i);
				// could check whether the tier is in the table model
//				if (t != this.tier && !childTiers.contains(t)) {
//					continue;
//				}
				final LinguisticType linguisticType = t.getLinguisticType();
				if (linguisticType == null) {
					continue;
				}
				cvName = linguisticType.getControlledVocabularyName();
				if (cvName != null) {
					// If we saw this CV before with another tier, re-use the color mapping we made for it.
					Map<String, Color> eMap;
					if (CV2TierMap.get(cvName) != null) {
						eMap = colMap.get(CV2TierMap.get(cvName));
						colMap.put(t.getName(), eMap);
					} else {
						cv = transcription.getControlledVocabulary(cvName);
						if (cv != null) {
							eMap = new HashMap<String, Color>();
							for (CVEntry cve : cv) {
								if (cve.getPrefColor() != null) {
									eMap.put(cve.getId(), cve.getPrefColor());
								}
							}
							colMap.put(t.getName(), eMap);
							CV2TierMap.put(cvName, t.getName());					
						}
					}
				}
			}
			table.setColorsForAnnotations(colMap);
		}
	}
}

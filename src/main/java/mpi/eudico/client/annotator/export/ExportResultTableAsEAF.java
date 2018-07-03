package mpi.eudico.client.annotator.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.client.annotator.grid.GridViewerTableModel;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.search.result.model.ElanMatch;
import mpi.eudico.client.annotator.search.result.viewer.EAFResultViewerTableModel;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.TierTree;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

/**
 * Export the annotations in the search result table with their context (the annotation block the annotations
 * is part of) to a new eaf file.
 */
public class ExportResultTableAsEAF implements ClientLogger {
    private ProgressMonitor monitor;
    
    /**
     * Creates new instance.
     */
    public ExportResultTableAsEAF() {
        super();
    }

    /**
     * Prompts for a file name, extracts the tiers (and thus types and cv's) that need to be copied 
     * and copies the toplevel parent annotations of all annotations in the annotation table.
     * 
     * @param table the search result table
     */
    public void exportTableAsEAF(final AnnotationTable table) {
        if (table == null || table.getRowCount() == 0) {
            // warn?
            return;
        }
        final String fileName = promptForFileName();
        if (fileName == null) {
            return;
        }
        
        monitor = new ProgressMonitor(null,
                ElanLocale.getString("SaveDialog.Message.Title"), "", 0, 100);
        monitor.setMillisToDecideToPopup(10);
        monitor.setMillisToPopup(10);

        new Thread() {
            @Override
			public void run() {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }

		        TranscriptionImpl transcription = null;
		        ///GridViewerTableModel dataModel = (GridViewerTableModel) table.getModel();
		        EAFResultViewerTableModel dataModel = (EAFResultViewerTableModel) table.getModel();
		        AnnotationCore anc = null;
		        Annotation ann = null;
		        AlignableAnnotation aa = null;
		        // first get the transcription
		        anc = dataModel.getAnnotationCore(0);
		        if (anc instanceof Annotation) {
		            ann = (Annotation) anc;
		            transcription = (TranscriptionImpl) ann.getTier().getTranscription();
		        }
		        if (transcription == null) {
		            //warn message
		            LOG.warning("Could not retrieve the transcription, no results exported");
		            progressUpdate(null, 100);
		            return;
		        }
		        progressUpdate(null, 10);
		        
		        if (isCancelled()) {
		            return;
		        }
		        
		        ArrayList<Annotation> topAnnos = new ArrayList<Annotation>();
		        ArrayList<Tier> topTiers = new ArrayList<Tier>();
		        int annColumn = -1;
		        Object val;
		        ElanMatch match;
		        for (int i = 0; i < dataModel.getColumnCount(); i++) {
		            if (dataModel.getColumnName(i).equals(GridViewerTableModel.ANNOTATION)) {
		                annColumn = i;
		                break;
		            }
		        }
		        if (annColumn < 0) {
		            LOG.warning("Could not find the matches column in the table");
		            progressUpdate(null, 100);
		            return;
		        }
		        
		        for (int i = 0; i < dataModel.getRowCount(); i++) {
		            val = dataModel.getValueAt(i, annColumn);
		            if (val instanceof ElanMatch) {
		                match = (ElanMatch) val;
		                //System.out.println("" + i + " " + match.getValue());
		                // only process toplevel matches; their children will all be checked
		                if (match.getParent() != null) {
		                    continue;
		                }
		                ann = match.getAnnotation();
		                aa = rootAnnotationOf(ann);
		                if (aa != null && !topAnnos.contains(aa)) {
		                    topAnnos.add(aa);
		                }
		                if (aa != null && !topTiers.contains(aa.getTier())) {
		                    topTiers.add(aa.getTier());
		                }
		                // process children
		                extractSubMatches(match, topAnnos, topTiers);
		                
		            } else {
		                anc = dataModel.getAnnotationCore(i);
			            //System.out.println("" + i + " " + anc.getClass());
			            if (anc instanceof Annotation) {
			                ann = (Annotation) anc;
			                aa = rootAnnotationOf(ann);
			                if (aa != null && !topAnnos.contains(aa)) {
			                    topAnnos.add(aa);
			                }
			                if (!topTiers.contains(aa.getTier())) {
			                    topTiers.add(aa.getTier());
			                }
			            }    
		            }
		            
		        }
		        
		        // now create a new transcription, either with only the tiers, types and cv's in use by
		        // the annotations or all tiers, types and cv's?
		        TranscriptionImpl nextTrans = new TranscriptionImpl();
		        nextTrans.setNotifying(false);
		        progressUpdate(null, 15);
		        
		        if (isCancelled()) {
		            return;
		        }
		
		        copyDescriptors(transcription, nextTrans);
		        
		        if (isCancelled()) {
		            return;
		        }
		        
		        copyTiersTypesCvs(transcription, nextTrans, topTiers);
		        
		        if (isCancelled()) {
		            return;
		        }
		        copyAnnotations(transcription, nextTrans, topAnnos);
		        progressUpdate(null, 90);
		        
		        if (isCancelled()) {
		            return;
		        }
		
		        TranscriptionStore store = ACMTranscriptionStore.getCurrentTranscriptionStore();
				int saveAsType = SaveAs27Preferences.saveAsType(nextTrans);
		        try {
		            store.storeTranscription(nextTrans, null, new ArrayList<TierImpl>(), fileName,
		                    saveAsType);
		        } catch (IOException ioe) {
		            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription),
	                        //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
	                        "Unable to save the file: " +
	                        "(" + ioe.getMessage() + ")",
	                        ElanLocale.getString("Message.Error"),
	                        JOptionPane.ERROR_MESSAGE);
		        }
		        progressUpdate(null, 100);
            }
        }.start();
    }
    
    /**
     * Recursively extract annotations from sub matches.
     * @param match the (sub)match
     * @param topAnnos the list of toplevel annotations in the matches
     * @param topTiers the list of involved toplevel tiers
     */
    private void extractSubMatches(ElanMatch match, ArrayList<Annotation> topAnnos, ArrayList<Tier> topTiers) {
        ElanMatch chMatch;
        Annotation ann;
        for (int j = 0; j < match.getChildCount(); j++) {
            chMatch = (ElanMatch) match.getChildAt(j);
            ann = chMatch.getAnnotation();
            ann = rootAnnotationOf(ann);
            if (ann != null && !topAnnos.contains(ann)) {
                topAnnos.add(ann);
            }
            if (ann != null && !topTiers.contains(ann.getTier())) {
                topTiers.add(ann.getTier());
            }
            // recursive extraction
            if (!chMatch.isLeaf()) {
                extractSubMatches(chMatch, topAnnos, topTiers);
            }
        }    
    }
    
    /**
     * Copies media and linked files descriptors.
     * @param transcription source transcription
     * @param nextTrans destination transcription
     */
    private void copyDescriptors(TranscriptionImpl transcription, TranscriptionImpl nextTrans) {
        if (transcription == null || nextTrans == null) {
            return;
        }
        List<MediaDescriptor> mds = transcription.getMediaDescriptors();
        List<MediaDescriptor> cmds = new ArrayList<MediaDescriptor>(mds.size());
        MediaDescriptor md;

        for (int i = 0; i < mds.size(); i++) {
            md = mds.get(i);
            cmds.add((MediaDescriptor) md.clone());
        }

        nextTrans.setMediaDescriptors(cmds);
        // copy/clone linked files descriptors
        List<LinkedFileDescriptor> lfds = transcription.getLinkedFileDescriptors();
        List<LinkedFileDescriptor> clfds = new ArrayList<LinkedFileDescriptor>(lfds.size());
        LinkedFileDescriptor lfd;

        for (int i = 0; i < lfds.size(); i++) {
            lfd = lfds.get(i);
            clfds.add((LinkedFileDescriptor) lfd.clone());
        }

        nextTrans.setLinkedFileDescriptors(clfds);
        progressUpdate(null, 20);
    }

    /**
     * Copies tiers, linguistic types and cv's.
     * @param transcription source transcription
     * @param nextTrans destination transcription
     * @param topTiers a list of top level tiers, all depending tiers will also be copied
     */
    private void copyTiersTypesCvs(TranscriptionImpl transcription, TranscriptionImpl nextTrans, 
            List<Tier> topTiers) {
        if (transcription == null || nextTrans == null || topTiers == null) {
            return;
        }
        TierImpl tier;
        List<TierImpl> tiersToCopy = new ArrayList<TierImpl>();
        
        for (int i = 0; i < topTiers.size(); i++) {
            tier = (TierImpl) topTiers.get(i);
            tiersToCopy.add(tier);
            tiersToCopy.addAll(tier.getDependentTiers());
        }
        
        List<LinguisticType> typesToCopy = new ArrayList<LinguisticType>();
        List<ControlledVocabulary> cvsToCopy = new ArrayList<ControlledVocabulary>();
        ControlledVocabulary cv;
        String cvName;
        for (int i = 0; i < tiersToCopy.size(); i++) {
            tier = tiersToCopy.get(i);
            if (!typesToCopy.contains(tier.getLinguisticType())) {
                typesToCopy.add(tier.getLinguisticType());
                if (tier.getLinguisticType().isUsingControlledVocabulary()) {
                    cvName = tier.getLinguisticType().getControlledVocabularyName();
                    cv = transcription.getControlledVocabulary(cvName);
                    if (cv != null && !cvsToCopy.contains(cv)) {
                        cvsToCopy.add(cv);    
                    }
                }
            }
        }
        
        // copy cv's
        List<ControlledVocabulary> cvc = new ArrayList<ControlledVocabulary>(cvsToCopy.size());
        ControlledVocabulary cv1;
        ControlledVocabulary cv2;
//        CVEntry[] entries;
//        CVEntry ent1;
        CVEntry ent2;

        for (int i = 0; i < cvsToCopy.size(); i++) {
            cv1 = cvsToCopy.get(i);
            cv2 = new ControlledVocabulary(cv1.getName());
            cv2.cloneStructure(cv1);
//            entries = cv1.getEntries();

//            for (int j = 0; j < entries.length; j++) {
//                ent1 = entries[j];
            for (CVEntry ent1 : cv1) {	
                ent2 = new CVEntry(cv2, ent1);
                cv2.addEntry(ent2);
            }

            cvc.add(cv2);
        }

        nextTrans.setControlledVocabularies(cvc);
        
        // copy ling types
        List<LinguisticType> typc = new ArrayList<LinguisticType>(typesToCopy.size());
        LinguisticType lt1;
        LinguisticType lt2;
        Constraint con1;
        Constraint con2 = null;

        for (int i = 0; i < typesToCopy.size(); i++) {
            lt1 = typesToCopy.get(i);
            lt2 = new LinguisticType(lt1.getLinguisticTypeName());
            lt2.setTimeAlignable(lt1.isTimeAlignable());
            lt2.setControlledVocabularyName(lt1.getControlledVocabularyName());
            con1 = lt1.getConstraints();

            if (con1 != null) {
                switch (con1.getStereoType()) {
                case Constraint.TIME_SUBDIVISION:
                    con2 = new TimeSubdivision();

                    break;

                case Constraint.SYMBOLIC_SUBDIVISION:
                    con2 = new SymbolicSubdivision();

                    break;

                case Constraint.SYMBOLIC_ASSOCIATION:
                    con2 = new SymbolicAssociation();

                    break;
                    
                case Constraint.INCLUDED_IN:
                    con2 = new IncludedIn();
                }

                lt2.addConstraint(con2);
            }

            typc.add(lt2);
        }

        nextTrans.setLinguisticTypes(typc);
        
        // copy tiers

        TierTree tree = new TierTree(transcription);
        DefaultMutableTreeNode root = tree.getTree();
        DefaultMutableTreeNode node;
        TierImpl t1;
        TierImpl t2;
        TierImpl copyTier;
        String name;
        String parentName;

        Enumeration en = root.breadthFirstEnumeration();
        en.nextElement();

        while (en.hasMoreElements()) {
            copyTier = null; // reset
            node = (DefaultMutableTreeNode) en.nextElement();
            name = (String) node.getUserObject();
            t1 = transcription.getTierWithId(name);

            if (!tiersToCopy.contains(t1)) {
                continue;
            }
            if (t1 != null) {
                lt1 = t1.getLinguisticType();
                lt2 = nextTrans.getLinguisticTypeByName(lt1.getLinguisticTypeName());

                if (lt2 != null) {
                    if (t1.hasParentTier()) {
                        parentName = t1.getParentTier().getName();
                        t2 = nextTrans.getTierWithId(parentName);

                        if (t2 != null) {
                            copyTier = new TierImpl(t2, name,
                                    t1.getParticipant(), nextTrans, lt2);
                            copyTier.setDefaultLocale(t1.getDefaultLocale());
                            copyTier.setAnnotator(t1.getAnnotator());
                            copyTier.setLangRef(t1.getLangRef());
                        } else {
                            LOG.warning("The parent tier: " + parentName +
                                " for tier: " + name +
                                " was not found in the destination transcription");
                        }
                    } else {
                        copyTier = new TierImpl(name, t1.getParticipant(),
                                nextTrans, lt2);
                        copyTier.setDefaultLocale(t1.getDefaultLocale());
                        copyTier.setAnnotator(t1.getAnnotator());
                        copyTier.setLangRef(t1.getLangRef());
                    }
                } else {
                    LOG.warning("Could not add tier: " + name +
                        " because the Linguistic Type was not found in the destination transcription.");
                }
            }

            if (copyTier != null) {
                nextTrans.addTier(copyTier);
            }
        }
        progressUpdate(null, 30);
    }
    
    /**
     * Copies (toplevel) annotations and all depending annotations.
     * @param transcription source transcription
     * @param nextTrans destination transcription
     * @param topAnnos the toplevel annotations
     */
    private void copyAnnotations(Transcription transcription, Transcription nextTrans, 
            ArrayList<Annotation> topAnnos) {
        if (transcription == null || nextTrans == null || topAnnos == null) {
            return;
        }
        // copy the annotations
        AlignableAnnotation aa;
        AlignableAnnotation copyAnn;
        DefaultMutableTreeNode record;
        int tp = 90 - 30;
        float incr = (float)tp / Math.max(topAnnos.size(), 1);//prevent division by 0
        
        for (int i = 0; i < topAnnos.size(); i++)  {
            aa = (AlignableAnnotation) topAnnos.get(i);
            record = AnnotationRecreator.createTreeForAnnotation(aa);
            copyAnn = (AlignableAnnotation) AnnotationRecreator.createAnnotationFromTree(nextTrans,
                    record);
            if (copyAnn == null) {
                LOG.warning("Could not copy annotation: " + aa.getValue() + " (" + aa.getBeginTimeBoundary() +
                        " - " + aa.getEndTimeBoundary() + ")");
            }
            progressUpdate(null, (30 + (int)(i * incr)));
            
            if (isCancelled()) {
                return;
            }
        }
    }
    
    /**
     * Finds the root or toplevel annotation for the specified annotation.
     * @param ann the annotation to find the root for
     * @return the toplevel annotation
     */
    private AlignableAnnotation rootAnnotationOf(Annotation ann) {
        if (ann == null) {
            return null;
        }
        TierImpl tier = (TierImpl) ann.getTier();
        if (tier.hasParentTier()) {
            tier = tier.getRootTier();
            return (AlignableAnnotation) tier.getAnnotationAtTime(
                    (ann.getBeginTimeBoundary() + ann.getEndTimeBoundary()) / 2);
        } else {
            return (AlignableAnnotation) ann;
        }
    }
    
    /**
     * Prompt for a filename and location.
     *
     * @return a path as a string
     */
    private String promptForFileName() {   
        FileChooser chooser = new FileChooser(null);
        chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, FileExtension.EAF_EXT, "LastUsedEAFDir");
        File exportFile = chooser.getSelectedFile();
        if (exportFile != null) {
            return exportFile.getAbsolutePath();            
        } else {
            return null;
        }
    }
    
    /**
     * Updates the progress bar and the message of the monitor.
     * @param note the progress message
     * @param progress the progress value
     */
    private void progressUpdate(String note, int progress) {
        if (monitor != null) {
            if (note != null) {
                monitor.setNote(note);
            }

            monitor.setProgress(progress);
        }
    }
    
    /**
     * Checks whether the operation has been canceled via the progress monitor.
     * 
     * @return true if the cancel button of the monitor has been clicked, false otherwise
     */
    private boolean isCancelled() {
        if (monitor != null) {
            return monitor.isCanceled();
        }
        return false;
    }
}

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
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.commands.CommandAction;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.TierTree;
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
 * Creates a new transcription document with the annotations that are found
 * within  a selected time interval in the source document.<br>
 * First all MediaDescriptors, Controlled Vocabularies, Linguistic Types and
 * Tiers  are copied to a new Transcription. Next all annotations that overlap
 * the selected time  interval are copied. The copied annotations are forced
 * into the interval (i.e. if  the begintime is smaller than the interval
 * begin time, the annotation's begin time  is updated to the interval begin
 * time. Same if the annotation's end time is greater than the interval end
 * time. <br>
 * Finally all annotations are shifted in time by a value of -(selection begin
 * time),  making the original selection begin time the time origin (point 0)
 * of the new  transcription.
 *
 * @author Han Sloetjes
 * @version 1.0 Oct 2005
 */
public class ExportSelectionAsEAF implements ClientLogger{
    private TranscriptionImpl transcription;
    private TranscriptionImpl nextTrans;
    private String path;
    private String mediaFolder;
    private long beginTime;
    private long endTime;
    private boolean clipMedia;
    private ProgressMonitor monitor;     
    
    /**
     * Creates a new ExportSelectionAsEAF instance
     *
     * @param transcription the source transcription
     * @param beginTime interval begin time
     * @param endTime interval end time
     */
    public ExportSelectionAsEAF(TranscriptionImpl transcription, long beginTime, long endTime) {
    	
        this.transcription = transcription;
        this.beginTime = beginTime;
        this.endTime = endTime; 
        
        if (transcription != null) {
            startExport();
        }
        
    }   

    /**
     * Prompts for a file name, creates a progress monitor and spawns a new
     * thread for the actual work.
     */
    private void startExport() {
        // get a file path to save to
        path = promptForFileName();

        if (path == null) {
            return;
        }

        nextTrans = new TranscriptionImpl();
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

                    ExportSelectionAsEAF.this.startCopy();
                }
            }.start();

        //copy(transcription, nextTrans, beginTime, endTime);
    }

    /**
     * Starts the copy process.
     */
    private void startCopy() {
        if ((transcription == null) || (nextTrans == null)) {
            progressUpdate(null, 100);

            return;
        }

        progressUpdate(null, 10);
        copy(transcription, nextTrans, beginTime, endTime, clipMedia);
        progressUpdate(null, 100);
    }
    
    private String createClipMediaFileName(String fileName, long begin, long end, boolean relativePath){
    	if(fileName !=null){
    		int index = fileName.lastIndexOf(".");
    		if (index > -1) {
    			fileName = fileName.substring(0, index) + "_" + begin + "_" + end + fileName.substring(index);
    		} else {
    			fileName = fileName + "_" + begin  + "_" + end;
    		}	
    		
    		if(mediaFolder != null && !relativePath){
    			fileName = FileUtility.fileNameFromPath(fileName);
    			fileName = mediaFolder + fileName;    			
    			fileName = FileUtility.pathToURLString(fileName);
    		}
    		
    	}
		return fileName;
    }

    /**
     * Copies MediaDescriptors, Controlled Vocabularies, Linguistic Types,
     * Tiers and  the relevant Annotations to the new Transcription. The
     * annotations are forced  into the selection interval. Finally the
     * annotations are shifted with a value of -begin and the transcription is
     * written to file.  This method could be split into separate parts for
     * copying each group of  objects, when there is a need for this. Maybe in
     * its own helper class.
     *
     * @param sourceTrans the source transcription
     * @param copyTrans the copy transcription
     * @param begin the selection begin time
     * @param end the selection end time
     */
    private void copy(TranscriptionImpl sourceTrans,
        TranscriptionImpl copyTrans, long begin, long end, boolean clipMedia) {
        copyTrans.setNotifying(false);        
           		
        if(clipMedia){
        	CommandAction ca = ELANCommandFactory.getCommandAction(sourceTrans, ELANCommandFactory.CLIP_MEDIA);  
        	mediaFolder = path;  	
        	
        	// replace (back)slashes to the system default
			if (File.separatorChar == '/') {
				mediaFolder = mediaFolder.replace('\\', File.separatorChar);
			} else {
				mediaFolder = mediaFolder.replace('/', File.separatorChar);
			}
        	
        	
        	int index = mediaFolder.lastIndexOf(File.separatorChar);
            if (index >= 0) {
            	mediaFolder = mediaFolder.substring(0, index);
            }
					
			if(!mediaFolder.endsWith(File.separator)){
				mediaFolder = mediaFolder + File.separatorChar;
			}
			
			((mpi.eudico.client.annotator.commands.ClipMediaCA)ca).setPath(mediaFolder);
        	ca.actionPerformed(null);
        }

        // copy/clone media descriptors
        List<MediaDescriptor> mds = sourceTrans.getMediaDescriptors();
        List<MediaDescriptor> cmds = new ArrayList<MediaDescriptor>(mds.size());
        MediaDescriptor md;
        

        for (int i = 0; i < mds.size(); i++) {
            md = (MediaDescriptor) mds.get(i).clone();
            if(clipMedia){  				
            	long offset = md.timeOrigin;				
				md.mediaURL = createClipMediaFileName(md.mediaURL, begin+offset, end+offset, false);
				
				if(md.extractedFrom != null){
					md.extractedFrom = createClipMediaFileName(md.extractedFrom, begin+offset, end+offset, false);					
				}
				
				if(md.relativeMediaURL != null){
					md.relativeMediaURL = createClipMediaFileName(md.relativeMediaURL, begin+offset, end+offset, true);					
				}	
			} else {
				md.timeOrigin = md.timeOrigin + begin;
			}
            cmds.add(md);
        }

        copyTrans.setMediaDescriptors(cmds);
        // copy/clone linked files descriptors
        List<LinkedFileDescriptor> lfds = sourceTrans.getLinkedFileDescriptors();
        List<LinkedFileDescriptor> clfds = new ArrayList<LinkedFileDescriptor>(lfds.size());
        LinkedFileDescriptor lfd;

        for (int i = 0; i < lfds.size(); i++) {
            lfd = lfds.get(i);
            clfds.add((LinkedFileDescriptor) lfd.clone());
        }

        copyTrans.setLinkedFileDescriptors(clfds);
        progressUpdate(null, 20);
        
        if (isCancelled()) {
            return;
        }
        
        // cv's
        List<ControlledVocabulary> cvs = sourceTrans.getControlledVocabularies();
        List<ControlledVocabulary> cvc = new ArrayList<ControlledVocabulary>(cvs.size());
        ControlledVocabulary cv1;
        ControlledVocabulary cv2;
        CVEntry ent2;

        for (int i = 0; i < cvs.size(); i++) {
            cv1 = cvs.get(i);
            cv2 = new ControlledVocabulary(cv1.getName());
            cv2.cloneStructure(cv1);

            for (CVEntry ent1 : cv1) {
                ent2 = new CVEntry(cv2, ent1);
                cv2.addEntry(ent2);
            }

            cvc.add(cv2);
        }

        copyTrans.setControlledVocabularies(cvc);
        progressUpdate(null, 30);

        if (isCancelled()) {
            return;
        }
        
        // linguistic types
        List<LinguisticType> types = sourceTrans.getLinguisticTypes();
        List<LinguisticType> typc = new ArrayList<LinguisticType>(types.size());
        LinguisticType lt1;
        LinguisticType lt2;
        Constraint con1;
        Constraint con2 = null;

        for (int i = 0; i < types.size(); i++) {
            lt1 = types.get(i);
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

        copyTrans.setLinguisticTypes(typc);
        progressUpdate(null, 35);

        if (isCancelled()) {
            return;
        }
        
        
        TierTree tree = new TierTree(sourceTrans);
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
            t1 = (TierImpl) sourceTrans.getTierWithId(name);

            if (t1 != null) {
                lt1 = t1.getLinguisticType();
                lt2 = copyTrans.getLinguisticTypeByName(lt1.getLinguisticTypeName());

                if (lt2 != null) {
                    if (t1.hasParentTier()) {
                        parentName = t1.getParentTier().getName();
                        t2 = (TierImpl) copyTrans.getTierWithId(parentName);

                        if (t2 != null) {
                            copyTier = new TierImpl(t2, name,
                                    t1.getParticipant(), copyTrans, lt2);
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
                                copyTrans, lt2);
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
                copyTrans.addTier(copyTier);
            }
        }

        progressUpdate(null, 40);

        if (isCancelled()) {
            return;
        }
        
        // copy annotations within the interval, loop over toplevel tiers
        List<AlignableAnnotation> srcAnnos;
        AlignableAnnotation ann;
        AlignableAnnotation copyAnn;
        DefaultMutableTreeNode record;

        float incr = (float)(80 - 40) / Math.max(root.getChildCount(), 1); // prevent division by 0
        
        for (int i = 0; i < root.getChildCount(); i++) {
            node = (DefaultMutableTreeNode) root.getChildAt(i);
            name = (String) node.getUserObject();
            t1 = (TierImpl) sourceTrans.getTierWithId(name);
            srcAnnos = t1.getAlignableAnnotations();

            for (int j = 0; j < srcAnnos.size(); j++) {
                ann = srcAnnos.get(j);

                if (ann.getEndTimeBoundary() <= begin) {
                    continue;
                }

                if (ann.getBeginTimeBoundary() >= end) {
                    break;
                }

                record = AnnotationRecreator.createTreeForAnnotation(ann);
                copyAnn = (AlignableAnnotation) AnnotationRecreator.createAnnotationFromTree(copyTrans,
                        record);

                // force in interval
                if (copyAnn.getBeginTimeBoundary() < begin) {
                    copyAnn.updateTimeInterval(begin,
                        copyAnn.getEndTimeBoundary());
                }

                if (copyAnn.getEndTimeBoundary() > end) {
                    copyAnn.updateTimeInterval(copyAnn.getBeginTimeBoundary(),
                        end);
                }
            }
            progressUpdate(null, 40 + (int)(i * incr));
            
            if (isCancelled()) {
                return;
            }
        }

        progressUpdate(null, 80);

        if (isCancelled()) {
            return;
        }
        
        copyTrans.shiftAllAnnotations(-begin);

        TranscriptionStore store = ACMTranscriptionStore.getCurrentTranscriptionStore();
		int saveAsType = SaveAs27Preferences.saveAsType(copyTrans);
        try {
            store.storeTranscription(copyTrans, null, new ArrayList<TierImpl>(), path,
                    saveAsType);
            copyTrans.setPathName(path);
            // try to copy the preferences
            copyPreferences(sourceTrans, copyTrans);
        } catch (IOException ioe) {
        	JOptionPane.showMessageDialog(null, 
        			String.format("%s: %s", ElanLocale.getString("Message.Error.Save"), ioe.getMessage()) , 
        			ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
        }
        LOG.info("Selection saved as new .eaf");
    }
    
    /**
     * Copy the preferences as well, if possible.
     */
    private void copyPreferences(TranscriptionImpl sourceTrans,
            TranscriptionImpl copyTrans) {
    	String sourcePath = sourceTrans.getFullPath();
    	if (sourcePath != null && !sourcePath.equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
    		if (sourcePath.endsWith(FileExtension.EAF_EXT[0])) {
    			String prefFile = sourcePath.substring(0, sourcePath.length() - 
    					FileExtension.EAF_EXT[0].length()) + FileExtension.ELAN_XML_PREFS_EXT[0];
    			prefFile = FileUtility.urlToAbsPath(prefFile);

    			Preferences.importPreferences(copyTrans, prefFile);
    			 // maybe load the new preferences and remove some preferences (selection, media time)?
    		}
    	}       
    }

    /**
     * Prompt for a filename and location.
     *
     * @return a path as a string
     */
    private String promptForFileName() {
        FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
        chooser.createAndShowFileAndClipMediaDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, 
        		null, FileExtension.EAF_EXT, "LastUsedEAFDir");
        File exportFile = chooser.getSelectedFile();
        clipMedia = chooser.doClipMedia();
        if (exportFile != null) {
           return exportFile.getAbsolutePath();
        } else {
        	return null;
        }
    }

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

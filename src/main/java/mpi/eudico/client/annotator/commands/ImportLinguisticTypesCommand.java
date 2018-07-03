package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.TranscriptionECVLoader;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

/**
 * A Command to import Linguistic Types (and referenced Controlled Vocabularies) from an .eaf or .etf 
 * file into an existing transcription. 
 */
public class ImportLinguisticTypesCommand implements UndoableCommand,
        ClientLogger {
    private String commandName;
    
    // receiver
    private TranscriptionImpl transcription;
    
    private List<LinguisticType> typesAdded = new ArrayList<LinguisticType>();
    private List<ControlledVocabulary> cvsAdded = new ArrayList<ControlledVocabulary>();
    private List<LexiconLink> lexLinksAdded = new ArrayList<LexiconLink>();
    
    /**
     * Creates a new instance of the command
     * 
     * @param name the name of the command
     */
    public ImportLinguisticTypesCommand(String name) {
        commandName = name;
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        if(transcription == null) {
            LOG.warning("The transcription is null.");
            return;
        }
        for (LinguisticType lt : typesAdded) {
            transcription.removeLinguisticType(lt);
        }
        for (ControlledVocabulary cv : cvsAdded) {
            transcription.removeControlledVocabulary(cv);
        }
        for (LexiconLink ll : lexLinksAdded) {
        	transcription.removeLexiconLink(ll);
        }
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if(transcription == null) {
            LOG.warning("The transcription is null.");
            return;
        }
        for (LexiconLink ll : lexLinksAdded) {
        	transcription.addLexiconLink(ll);
        }
        for (ControlledVocabulary cv : cvsAdded) {
            transcription.addControlledVocabulary(cv);
        }       
        for (LinguisticType lt : typesAdded) {
            transcription.addLinguisticType(lt);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the fileName of an eaf
     *        or etf file (String)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        String fileName = (String) arguments[0];
        if (fileName == null) {
            LOG.warning("The filename is null");
            return; // report??
        }
        
        fileName = FileUtility.pathToURLString(fileName).substring(5);
        List<LinguisticType> impTypes;
        List<ControlledVocabulary> impCVs;
        try {
            EAFSkeletonParser parser = new EAFSkeletonParser(fileName);
            parser.parse();

            impTypes = parser.getLinguisticTypes();
            List<ControlledVocabulary> cvs = parser.getControlledVocabularies();
            impCVs = new ArrayList<ControlledVocabulary>(cvs.size());
            LinguisticType lt;
            ControlledVocabulary cv;
            String cvName;
            
            typeloop:
            for (int i = 0; i < impTypes.size(); i++) {
                lt = impTypes.get(i);
                if (lt.getControlledVocabularyName() != null && lt.getControlledVocabularyName().length() > 0) {
                    cvName = lt.getControlledVocabularyName();
                    for (int j = 0; j < cvs.size(); j++) {
                        cv = cvs.get(j);
                        if (cv.getName().equals(cvName)) {
                            impCVs.add(cv);
                            continue typeloop;
                        }
                    }
                }
            }
           
        } catch (ParseException pe) {
            LOG.warning(pe.getMessage());
            pe.printStackTrace();
            return;
        }
        
        List<LinguisticType> currentTypes = new ArrayList<LinguisticType>(transcription.getLinguisticTypes());
        List<ControlledVocabulary> currentCvs = new ArrayList<ControlledVocabulary>(transcription.getControlledVocabularies());
        List<LexiconLink> currentLexLinks = new ArrayList<LexiconLink>(transcription.getLexiconLinks().values());
        
        addCVsAndTypes(impCVs, impTypes);
        
        for (LinguisticType lt : transcription.getLinguisticTypes()) {
            if (!currentTypes.contains(lt)) {
                typesAdded.add(lt);
                
                if (lt.getLexiconQueryBundle() != null) {
                	if (!currentLexLinks.contains(lt.getLexiconQueryBundle().getLink())) {
                		lexLinksAdded.add(lt.getLexiconQueryBundle().getLink());
                	}
                }
            }
        }
        
        TranscriptionECVLoader ecvLoader = new TranscriptionECVLoader();
        for (ControlledVocabulary cv : transcription.getControlledVocabularies()) {
            if (!currentCvs.contains(cv)) {
                cvsAdded.add(cv);
                if (cv instanceof ExternalCV) {        	
                	ecvLoader.loadExternalCVs(transcription, null);
                }
            }
        }
    }

    /**
     * Returns the name of the command.
     * 
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
    
    /**
     * Adds and if necessary renames Controlled Vocabularies and Linguistic Types.
     * 
     * @param cvs the list of CV's to add
     * @param typesToAdd the list of Linguistic Types to add
     */
    private void addCVsAndTypes(List<ControlledVocabulary> cvs, List<LinguisticType> typesToAdd) {
        if (cvs == null) {
            LOG.info("No Controlled Vocabularies to add");
            cvs = new ArrayList<ControlledVocabulary>(0);
            //return;
        }
        if (typesToAdd == null) {
            LOG.info("No Linguistic Types to add.");
            return;
        }
        Map<String, ControlledVocabulary> renamedCVS = new HashMap<String, ControlledVocabulary>(5);
        ControlledVocabulary cv;
        ControlledVocabulary cv2 = null;
        LinguisticType lt;
        String typeName;
        
        // add CV's, renaming when necessary
        for (int i = 0; i < cvs.size(); i++) {
            cv = cvs.get(i);
            cv2 = transcription.getControlledVocabulary(cv.getName());

            if (cv2 == null) {
                transcription.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            } else if (!cv.equals(cv2)) {
                // rename
                String newCVName = cv.getName() + "-cp";
                int c = 1;
                while (transcription.getControlledVocabulary(newCVName + c) != null) {
                    c++;
                }
                newCVName = newCVName + c;
                LOG.info("Renamed Controlled Vocabulary: " + cv.getName() +
                    " to " + newCVName);
                renamedCVS.put(cv.getName(), cv);
                cv.setName(newCVName);
                transcription.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            }
        }
        // add linguistic types
        for (int i = 0; i < typesToAdd.size(); i++) {
            lt = typesToAdd.get(i);

            typeName = lt.getLinguisticTypeName();

            if (lt.isUsingControlledVocabulary() &&
                    renamedCVS.containsKey(lt.getControlledVocabularyName())) {
                cv2 = renamedCVS.get(lt.getControlledVocabularyName());
                lt.setControlledVocabularyName(cv2.getName());
            }

            if (transcription.getLinguisticTypeByName(typeName) != null) {
                LOG.warning("Transcription already contains a Linguistic Type named: " + typeName);
                continue;
            }
            transcription.addLinguisticType(lt);
            LOG.info("Added Linguistic Type: " +
                        typeName);
        } // end linguistic types
    }

}

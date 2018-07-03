package mpi.eudico.client.annotator.imports;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.util.TierTree;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A class for copying a complete transcription.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TranscriptionCopier implements ClientLogger {
    /**
     * Creates a new TranscriptionCopier instance
     */
    public TranscriptionCopier() {
    }

    /**
     * Copies the contents of one transcription to another.<br>
     * Note: there may be quite a bit of overlap with classes that copy a tier
     * etc.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyTranscription(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        copyHeader(src, dest);
        copyMediaDescriptors(src, dest);
        copyLinkedFileDescriptors(src, dest);
        copyControlledVocabularies(src, dest);
        copyLinguisticTypes(src, dest);
        copyTiers(src, dest);
        copyAnnotations(src, dest);
    }

    /**
     * Copies information stored in the header of an .eaf Currently only the
     * author field is copied.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyHeader(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        dest.setAuthor(src.getAuthor()); //??
    }

    /**
     * Copies the Media Descriptors.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyMediaDescriptors(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        List<MediaDescriptor> mediaDescs = src.getMediaDescriptors();
        List<MediaDescriptor> copyDesc = new ArrayList<MediaDescriptor>(mediaDescs.size());
        MediaDescriptor srcMd;
        MediaDescriptor copyMd;

        for (int i = 0; i < mediaDescs.size(); i++) {
            srcMd = mediaDescs.get(i);
            copyMd = (MediaDescriptor) srcMd.clone();
            copyDesc.add(copyMd);
        }

        dest.setMediaDescriptors(copyDesc);
    }
    
    /**
     * Copies the Linked file Descriptors.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyLinkedFileDescriptors(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        java.util.List<LinkedFileDescriptor> linkedfileDescs = src.getLinkedFileDescriptors();
        List<LinkedFileDescriptor> copyDesc = new ArrayList<LinkedFileDescriptor>(linkedfileDescs.size());
        LinkedFileDescriptor srcFileDesc;
        LinkedFileDescriptor copyFileDesc;

        for (int i = 0; i < linkedfileDescs.size(); i++) {
        	srcFileDesc = linkedfileDescs.get(i);
        	copyFileDesc = (LinkedFileDescriptor) srcFileDesc.clone();
            copyDesc.add(copyFileDesc);
        }

        dest.setLinkedFileDescriptors(copyDesc);
    }    

    /**
     * Copies the Controlled Vocabularies.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyControlledVocabularies(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        List<ControlledVocabulary> srcCVS = src.getControlledVocabularies();
        List<ControlledVocabulary> copyCVS = new ArrayList<ControlledVocabulary>(srcCVS.size());

        for (ControlledVocabulary cv : srcCVS) {
        	ControlledVocabulary cpCV = cv.clone();	// also works for ECVs
        	
            copyCVS.add(cpCV);
        }

        dest.setControlledVocabularies(copyCVS);
    }

    /**
     * Copies the LinguisticTypes.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyLinguisticTypes(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        List<LinguisticType> srcTypes = src.getLinguisticTypes();
        List<LinguisticType> destTypes = new ArrayList<LinguisticType>(srcTypes.size());
        LinguisticType srcLt;
        LinguisticType cpLt;

        for (int i = 0; i < srcTypes.size(); i++) {
            srcLt = srcTypes.get(i);
            cpLt = new LinguisticType(srcLt.getLinguisticTypeName());
            cpLt.setControlledVocabularyName(srcLt.getControlledVocabularyName());
            cpLt.setTimeAlignable(srcLt.isTimeAlignable()); //??
            cpLt.addConstraint(srcLt.getConstraints());
            destTypes.add(cpLt);
        }

        dest.setLinguisticTypes(destTypes);
    }

    /**
     * Copies the tiers.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyTiers(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        TierImpl srcTier;
        TierImpl parTier;
        TierImpl cpTier;
        String parentName = null;
        String typeName = null;
        LinguisticType lt;
        LinguisticType destLt;
        List<LinguisticType> destTypes = dest.getLinguisticTypes();

        // create a tree structure of the tiers
        TierTree tierTree = new TierTree(src);
        DefaultMutableTreeNode root = tierTree.getTree();
        Enumeration ten = root.breadthFirstEnumeration();
        ten.nextElement(); // skip the empty root

        Object next;
        DefaultMutableTreeNode node;

        while (ten.hasMoreElements()) {
            node = (DefaultMutableTreeNode) ten.nextElement();
            next = node.getUserObject();

            if (next instanceof String) {
                srcTier = src.getTierWithId((String) next);
                if (srcTier == null) {
                    LOG.warning("A tier could not be found in the source transcription: " +
                            next);
                    continue;
                }
                parTier = srcTier.getParentTier();

                if (parTier != null) {
                    parentName = parTier.getName();
                }

                lt = srcTier.getLinguisticType();
                typeName = lt.getLinguisticTypeName();

                cpTier = null;

                if (parTier == null) {
                    cpTier = new TierImpl(srcTier.getName(),
                            srcTier.getParticipant(), dest, null);
                } else {
                    parTier = dest.getTierWithId(parentName);

                    if (parTier != null) {
                        cpTier = new TierImpl(parTier, srcTier.getName(),
                                srcTier.getParticipant(), dest, null);
                    } else {
                        LOG.warning("The parent tier: " + parentName +
                            " for tier: (null)" +
                            " was not found in the destination transcription");
                    }
                }

                if (cpTier != null) {
                    destLt = null;

                    for (int i = 0; i < destTypes.size(); i++) {
                        lt = destTypes.get(i);

                        if (lt.getLinguisticTypeName().equals(typeName)) {
                            destLt = lt;

                            break;
                        }
                    }

                    if (destLt != null) {
                        // transcription does not perform any checks..
                        if (dest.getTierWithId(cpTier.getName()) == null) {
                            dest.addTier(cpTier);
                            LOG.info("Created and added tier to destination: " +
                                cpTier.getName());
                        } else {
                            LOG.info("Could not add tier to destination: " +
                                cpTier.getName() +
                                " already exists in the transcription");
                        }
                        cpTier.setLinguisticType(destLt);
                    } else {
                        LOG.warning("Could not add tier: " + cpTier.getName() +
                            " because the Linguistic Type was not found in the destination transcription.");
                    }
                    
                    cpTier.setDefaultLocale(srcTier.getDefaultLocale());
                    cpTier.setAnnotator(srcTier.getAnnotator());
                    cpTier.setLangRef(srcTier.getLangRef());
                }
            } else {
                LOG.warning("Unknown object in the tier tree.");
            }
        }
    }

    /**
     * Copies the annotations.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyAnnotations(TranscriptionImpl src, Transcription dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        List<TierImpl> tiers = src.getTiers();

        for (int i = 0; i < tiers.size(); i++) {
        	TierImpl srcTier = tiers.get(i);

            // only toplevel tiers
            if (!srcTier.hasParentTier()) {
                List<AlignableAnnotation> annos = srcTier.getAlignableAnnotations();

                int size = annos.size();

                for (int j = 0; j < size; j++) {
                	AlignableAnnotation aa = annos.get(j);
                	DefaultMutableTreeNode annNode = AnnotationRecreator.createTreeForAnnotation(aa);
                    AnnotationRecreator.createAnnotationFromTree(dest, annNode);
                }
            }
        }
    }
}

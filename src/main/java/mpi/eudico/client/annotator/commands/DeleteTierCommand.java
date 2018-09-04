package mpi.eudico.client.annotator.commands;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Removes a tier from the transcription.
 *
 * @author Han Sloetjes
 */
public class DeleteTierCommand implements UndoableCommand {
    private String commandName;

    //state 
    private TierImpl tier;
    private List<TierImpl> depTiers;
    private List<DefaultMutableTreeNode> annotationsNodes;
    private Map<String, Color> colorPrefs;
    private Map<String, Font> fontPrefs;

    // receiver
    private TranscriptionImpl transcription;

    /**
     * A command to delete a tier (and depending tiers) from a transcription.
     *
     * @param name the name of the command
     */
    public DeleteTierCommand(String name) {
        commandName = name;
    }

    /**
     * Adds the removed tier to the transcription.
     */
    @Override
	public void undo() {
        if ((transcription != null) && (tier != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);

            TierImpl deptier;

            if (transcription.getTierWithId(tier.getName()) == null) {
                transcription.addTier(tier);
            }

            if (depTiers != null) {
                for (int i = 0; i < depTiers.size(); i++) {
                    deptier = depTiers.get(i);

                    if (transcription.getTierWithId(deptier.getName()) == null) {
                        transcription.addTier(deptier);
                    }
                }
            }

            if (annotationsNodes.size() > 0) {
                transcription.setNotifying(false);

                DefaultMutableTreeNode node;

                if (tier.hasParentTier()) {
                    AnnotationRecreator.createAnnotationsSequentially(transcription,
                        annotationsNodes, true);
                } else {
                    for (int i = 0; i < annotationsNodes.size(); i++) {
                        node = annotationsNodes.get(i);
                        AnnotationRecreator.createAnnotationFromTree(transcription,
                            node, true);
                    }
                }

                transcription.setNotifying(true);
            }

            setWaitCursor(false);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
            
            // restore preferences ??
            if (colorPrefs != null) {
            	Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
        		if (colors != null) {
        			colors.putAll(colorPrefs);
        			
        			Preferences.set("TierColors", colors, transcription, true);
        		}
            }
            if (fontPrefs != null) {
            	Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
            	if (fonts != null) {
            		fonts.putAll(fontPrefs);
            		
            		Preferences.set("TierFonts", fonts, transcription, true);
            	}
            }
        }
    }

    /**
     * Again removes the tier from the transcription.
     */
    @Override
	public void redo() {
        if ((transcription == null) || (tier == null)) {
            return;
        }

        transcription.removeTier(tier);

        if (depTiers != null) {
            for (int i = 0; i < depTiers.size(); i++) {
                transcription.removeTier(depTiers.get(i));
            }
        }
        
        // again delete preferences
		Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
		if (colors != null) {
			for (TierImpl t : depTiers) {
				colors.remove(t.getName());;
			}
			colors.remove(tier.getName());
			//Preferences.set("TierColors", colors, transcription);
		}
		Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
		if (fonts != null) {
			for (TierImpl t : depTiers) {
				fonts.remove(t.getName());
			}
			fonts.remove(tier.getName());
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the tier to remove
     *        (TierImpl)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof TranscriptionImpl) {
            transcription = (TranscriptionImpl) receiver;
        } else {
            return;
        }

        tier = (TierImpl) arguments[0];

        if (tier == null) {
            return;
        }

        depTiers = tier.getDependentTiers();

        // first store all annotations
        annotationsNodes = new ArrayList<DefaultMutableTreeNode>();

        for (AbstractAnnotation ann  : tier.getAnnotations()) {
            annotationsNodes.add(AnnotationRecreator.createTreeForAnnotation(
                    ann));
        }

        // then remove the tiers			
        if (depTiers != null) {
            for (int i = 0; i < depTiers.size(); i++) {
                transcription.removeTier(depTiers.get(i));
            }
        }

        transcription.removeTier(tier);
        // store preferred colors
        Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
		if (colors != null) {
			colorPrefs = new HashMap<String, Color>(colors.size());
			for (TierImpl t : depTiers) {
				Color col = colors.remove(t.getName());;
				if (col != null) {
					colorPrefs.put(t.getName(), col);
				}
			}
			Color col = colors.remove(tier.getName());
			if (col != null) {
				colorPrefs.put(tier.getName(), col);
			}
		}
		Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
		if (fonts != null) {
			fontPrefs = new HashMap<String, Font>(fonts.size());
			for (TierImpl t : depTiers) {
				Font fon = fonts.remove(t.getName());
				if (fon != null) {
					fontPrefs.put(t.getName(), fon);
				}
			}
			Font fon = fonts.remove(tier.getName());
			if (fon != null) {
				fontPrefs.put(tier.getName(), fon);
			}
		}
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }
}

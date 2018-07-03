package mpi.eudico.client.annotator.commands;

import java.awt.Font;
import java.util.Map;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.server.corpora.clom.Annotation;


/**
 * Brings up an edit box for the selected annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ModifyAnnotationDlgCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public ModifyAnnotationDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the active Annotation
     * @param arguments the arguments: null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Annotation activeAnn = (Annotation) receiver;

        if (activeAnn != null) {
            InlineEditBox box = new InlineEditBox(false);
            box.setAnnotation(activeAnn);
            // set preferred font
    		Map<String, Font> foMap = Preferences.getMapOfFont("TierFonts", activeAnn.getTier().getTranscription());
    		if (foMap != null) {
    			Font f = foMap.get(activeAnn.getTier().getName());
    			if (f != null) {
    				box.setFont(f);
    			}
    		}
            box.detachEditor();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}

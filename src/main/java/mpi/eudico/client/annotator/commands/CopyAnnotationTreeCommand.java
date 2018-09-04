package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.TransferableAnnotationTree;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * A Command to copy an annotation with it's dependent annotations (i.e. a transferable DefaultMutableTreeNode) 
 * to the System's Clipboard.
 */
public class CopyAnnotationTreeCommand extends CopyAnnotationCommand {
    
    /**
     * Creates a new CopyAnnotationTreeCommand instance
     * 
     * @param name the name of the command
     */
    public CopyAnnotationTreeCommand(String name) {
        super(name);
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the (active) annotation
     *        (Annotation)</li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof AbstractAnnotation) {
            DefaultMutableTreeNode node = AnnotationRecreator.createTreeForAnnotation(
                    (AbstractAnnotation) arguments[0]);
            TransferableAnnotationTree ta = new TransferableAnnotationTree(node);
            
            if (canAccessSystemClipboard()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ta, ta);
            }
        }
    }

}

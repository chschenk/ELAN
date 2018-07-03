package mpi.dcr;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Class that can create a tree based on the broader concept generic attribute
 * of a data category.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DCTree {
    /**
     * Creates a new DCTree instance
     */
    public DCTree() {
        super();
    }

    /**
     * Creates a tree based on the broader concept generic attributes of
     * categories  and returns the root node.
     *
     * @param datcats the list of data categories!
     *
     * @return the root node of the tree
     */
    public DefaultMutableTreeNode getBroaderGenericConceptTree(List<DCSmall> datcats) {
        if (datcats == null) {
            return null;
        }

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>(datcats.size());
        DefaultMutableTreeNode node = null;
        DCSmall dcm = null;

        for (int i = 0; i < datcats.size(); i++) {
            dcm = datcats.get(i);
            node = new DefaultMutableTreeNode(dcm);

            if (dcm.getBroaderDCId() == null) {
                rootNode.add(node);
            } else {
                nodes.add(new DefaultMutableTreeNode(datcats.get(i)));
            }
        }

nodeloop: 
        for (int i = 0; i < nodes.size(); i++) {
            node = nodes.get(i);
            dcm = (DCSmall) node.getUserObject();
            // first check the nodes in the tree
            Enumeration<DefaultMutableTreeNode> en = rootNode.children();

            while (en.hasMoreElements()) {
            	DefaultMutableTreeNode nextNode = en.nextElement();
            	DCSmall nextdcm = (DCSmall) nextNode.getUserObject();

                if (nextdcm.getIdentifier().equals(dcm.getBroaderDCId())) {
                    nextNode.add(node);

                    continue nodeloop;
                }
            }

            // not added yet
            for (int j = 0; j < nodes.size(); j++) {
            	DefaultMutableTreeNode nextNode = nodes.get(j);
                DCSmall nextdcm = (DCSmall) nextNode.getUserObject();

                if (nextdcm.getIdentifier().equals(dcm.getBroaderDCId())) {
                    nextNode.add(node);

                    continue nodeloop;
                }
            }

            // still not added, the node object has a broader concept, but that one is not in the list
            rootNode.add(node);
        }

        return rootNode;
    }
}

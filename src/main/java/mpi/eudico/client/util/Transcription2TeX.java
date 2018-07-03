/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.TimeRelation;

/**
 * $Id: Transcription2TeX.java 43754 2015-04-23 14:42:55Z olasei $
 * 
 * export annotation values and their dependencies into a tree, using the
 * LaTeX-pstree format
 * 
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
public class Transcription2TeX {
    /**
     * Export annotations in tier and annotations in dependent tiers as tree For
     * each tier maximal one direct dependent tier can be selected (otherwise
     * tree structure wouldn't be meaningful)
     * 
     * @param transcription
     * @param tierNames
     * @param exportFile
     * @throws IOException
     */
    static public void exportTiers(Transcription transcription, 
            String[] tierNames, File exportFile) throws IOException{
        exportTiers(transcription, tierNames, exportFile, 0, Long.MAX_VALUE);
    }

    /**
     * Exports annotations that overlapp with specified time interval
     * 
     * @param transcription
     * @param tierNames
     * @param exportFile
     * @param beginTime
     * @param endTime
     * @throws IOException
     */
    static public void exportTiers(Transcription transcription, 
            String[] tierNames, File exportFile, long beginTime, long endTime)
            throws IOException{
        if (exportFile == null) {
            return;
        }

        List<Tier> rootTiers = new ArrayList<Tier>();

        for (String tierName : tierNames) {
            rootTiers.add(transcription.getTierWithId(tierName));
        }

        if (!hasOnlyLinearDependencies(rootTiers)) {
            throw new IOException("Export.TeX.TierSelectionException");
        }

        removeDependentTiers(rootTiers);

        FileOutputStream out = new FileOutputStream(exportFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

        writer.write("\\documentclass{article}\n");
        writer.write("\\usepackage{pstricks,pst-node,pst-tree}\n");
        writer.write("\\begin{document}\n");
        writer.write("\\begin{center}\n");

        for (int i = 0; i < rootTiers.size(); i++) {
            writer.write(((TierImpl) rootTiers.get(i)).getName() + "\\\\\n");

            List<? extends Annotation> annotations = rootTiers.get(i).getAnnotations();

            int j = 0;

            while (j < annotations.size()) {
                if (TimeRelation.overlaps(annotations.get(j),
                                beginTime, endTime)) {
                    j++;
                }
                else {
                    annotations.remove(j);
                }
            }

            exportAnnotations(writer, tierNames, annotations, exportFile);
        }

        writer.write("\\end{center}\n");
        writer.write("\\end{document}\n");
        writer.close();
    }

    /**
     * filters all tiers, that have an ancestor in the selected set
     * 
     * @param rootTiers
     */
    private static void removeDependentTiers(List<Tier> rootTiers) {
        TierImpl tier1;
        TierImpl tier2;

        for (int i = 0; i < (rootTiers.size() - 1); i++) {
            tier1 = (TierImpl) rootTiers.get(i);

            for (int j = i + 1; j < rootTiers.size(); j++) {
                tier2 = (TierImpl) rootTiers.get(j);

                if (tier2.hasAncestor(tier1)) {
                    rootTiers.remove(tier2);
                    j--;
                }

                if (tier1.hasAncestor(tier2)) {
                    rootTiers.remove(tier1);
                    j--;
                    i--;
                }
            }
        }
    }

    /**
     * check if each selected tier has maximal one direct child tier
     * 
     * @param rootTiers
     * @throws Exception
     */
    private static boolean hasOnlyLinearDependencies(List<Tier> rootTiers) {
        TierImpl tier1;
        TierImpl tier2;

        for (int i = 0; i < (rootTiers.size() - 1); i++) {
            tier1 = (TierImpl) rootTiers.get(i);

            for (int j = i + 1; j < rootTiers.size(); j++) {
                tier2 = (TierImpl) rootTiers.get(j);

                if ((tier1.getParentTier() == tier2.getParentTier())
                        && rootTiers.contains(tier1.getParentTier())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Exports a List of AnnotationCores to Tab limited text
     * 
     * @param annotations
     * @param exportFile
     * @throws IOException
     */
    static private void exportAnnotations(BufferedWriter writer, String[] tierNames,
            List<? extends AnnotationCore> annotations, File exportFile) throws IOException {
        if (exportFile == null) {
            return;
        }

        for (int i = 0; i < annotations.size(); i++) {
            if (annotations.get(i) instanceof Annotation) {
                // writer.write("\\pstree[nodesep=2pt,levelsep=20pt]\n");
                writer.write(getTeXTree(tierNames, (Annotation) annotations.get(i))
                        + "\\\\[5mm]\n");
            }
        }
    }

    /**
     * construcs tex tree for a single annotation (and its dependent
     * annotations)
     * 
     * @param tierNames
     *            list of tier names; only children annotations in those tiers
     *            are used
     * @param annotation
     *            "root" or starting annotation (has not to be root in the
     *            complete ACM)
     * 
     * @return String contains a tree in LaTeX-pstree format for the given
     *         annotation
     */
    static private String getTeXTree(String[] tierNames, Annotation annotation) {
        StringBuilder sb = new StringBuilder();
        TierImpl tier = (TierImpl) annotation.getTier();
        List<TierImpl> childrenTiers = tier.getChildTiers();
        StringBuilder childrenTree = new StringBuilder();

        try {
        	for (Tier childTier : childrenTiers) {
				String childrenTierName = childTier.getName();

                for (String tierName : tierNames) {
                    if (tierName.equals(childrenTierName)) {
                        List<Annotation> children = annotation
                                .getChildrenOnTier(childTier);

                        if (children.size() > 0) {
                        	for (Annotation anno : children) {
                                childrenTree.append(getTeXTree(tierNames,
                                        anno));
                            }
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String annotationValue = annotation.getValue();
        if (annotationValue != null && !annotationValue.equals("")) {
            if (childrenTree.length() > 0) {
                sb.append("\\pstree");
            }

            sb.append("{\\TR{" + annotationValue + "}}\n");

            if (childrenTree.length() > 0) {
                sb.append("{" + childrenTree + "}");
            }
        }
        return sb.toString();
    }
}

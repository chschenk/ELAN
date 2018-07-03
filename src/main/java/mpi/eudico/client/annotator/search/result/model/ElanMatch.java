package mpi.eudico.client.annotator.search.result.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.search.content.result.model.AbstractContentMatch;
/**
 * Created on Jul 22, 2004
 * @author Alexander Klassmann
 * @version Jul 22, 2004
 */
@SuppressWarnings("serial")
public class ElanMatch extends AbstractContentMatch
                       implements AnnotationMatch, TreeNode {
	final private Annotation annotation;
	final private ElanMatch parentMatch;
	// id of constraint which this match belongs to; used to distinguish between matches of sibling constraints
	final private String constraintId;
	final private List<ElanMatch> children = new ArrayList<ElanMatch>();
	private Annotation leftContextAnnotation;
	private Annotation rightContextAnnotation;
	private Annotation parentContextAnnotation;
	
	public ElanMatch(ElanMatch parentMatch , Annotation annotation, String constraintId, int indexWithinTier, int[][] substringIndices){
		this(parentMatch, annotation, constraintId, indexWithinTier, "", "", substringIndices);
	}
	
	public ElanMatch(
			ElanMatch parentMatch,
		Annotation annotation,
		String constraintId,
		int indexWithinTier,
		String leftContext,
		String rightContext,
		int[][] substringIndices) {
			
		this.parentMatch = parentMatch;
		this.annotation = annotation;
		this.constraintId = constraintId;
		setIndex(indexWithinTier);

		setLeftContext(leftContext);
		setRightContext(rightContext);
		setMatchedSubstringIndices(substringIndices);
	}
	
	/**
	* Create new ElanMatch instance with parent and children informations mod. Coralie Villes
	*/
    public ElanMatch(ElanMatch parentMatch, Annotation annotation,
            String constraintId, int indexWithinTier, String leftContext,
            String rightContext, int[][] substringIndices, String parentContext,
            String childrenContext) {
        this.parentMatch = parentMatch;
        this.annotation = annotation;
        this.constraintId = constraintId;
        setIndex(indexWithinTier);

        setLeftContext(leftContext);
        setRightContext(rightContext);
        setMatchedSubstringIndices(substringIndices);
        setParentContext(parentContext);
        setChildrenContext(childrenContext);
    }

	public void addChild(ElanMatch subMatch){
		children.add(subMatch);
	}
	
	public void addChildren(Collection<ElanMatch> subMatches){
		children.addAll(subMatches);
	}
	
	public void setFileName(String fileName){
	    this.fileName = fileName;
	}
	
	public String getConstraintId(){
		return constraintId;
	}
	
	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getTierName()
	 */
	@Override
	public String getTierName() {
		String name = "";
		try {
			name = annotation.getTier().getName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getValue()
	 */
	@Override
	public String getValue() {
		return annotation.getValue();
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getBeginTime()
	 */
	@Override
	public long getBeginTimeBoundary() {
		return annotation.getBeginTimeBoundary();
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getEndTime()
	 */
	@Override
	public long getEndTimeBoundary() {
		return annotation.getEndTimeBoundary();
	}

	public Annotation getAnnotation() {
		return annotation;
	}
	
	@Override
    public String getParentContext() {
        return parentContext;
    }
    
	/**
	 * Question: push this up to the ContentMatch interface?
	 * Or perhaps to a separate interface.
	 */
	@Override
    public Annotation getParentContextAnnotation() {
        return parentContextAnnotation;
    }
    
	/**
	 * Question: push this up to the ContentMatch interface?
	 * Or perhaps to a separate interface.
	 */
	@Override
    public Annotation getLeftContextAnnotation() {
        return leftContextAnnotation;
    }
    
	/**
	 * Question: push this up to the ContentMatch interface?
	 * Or perhaps to a separate interface.
	 */
	@Override
    public Annotation getRightContextAnnotation() {
        return rightContextAnnotation;
    }
    
	@Override
    public String getChildrenContext() {
        return childrenContext;
    }

	public void setLeftContext(String context) {
		leftContext = context;
	}

	public void setLeftContext(Annotation context) {
		leftContextAnnotation = context;
		setLeftContext(context.getValue());
	}

	public void setRightContext(String context) {
		rightContext = context;
	}

	public void setRightContext(Annotation context) {
		rightContextAnnotation = context;
		setRightContext(context.getValue());
	}

    public void setParentContext(String context){
    	parentContext = context;
    }
    
	public void setParentContext(Annotation context) {
		parentContextAnnotation = context;
		setParentContext(context.getValue());
	}

    public void setChildrenContext(String context) {
    	childrenContext = context;
	}
    
	public void setMatchedSubstringIndices(int[][] substringIndices) {
		this.matchedSubstringIndices = substringIndices;
	}

	@Override
	public Enumeration<ElanMatch> children(){
		return Collections.enumeration(children);
	}
	
	@Override
	public boolean getAllowsChildren(){
		return true;
	}
	
	@Override
	public TreeNode getChildAt(int index){
		return children.get(index);
	}
	
	@Override
	public int getChildCount(){
		return children.size();
	}
	
	@Override
	public int getIndex(TreeNode node){
		return children.indexOf(node);
	}
	
	@Override
	public TreeNode getParent(){
		return parentMatch;
	}
	
	@Override
	public boolean isLeaf(){
		return children.size() == 0;
	}
	
	@Override
	public String toString(){
		return annotation.getValue();
		/*
		StringBuilder sb = new StringBuilder();
		TreeNode loopNode = parentMatch;
		while(loopNode != null){
			sb.append("\t");
			loopNode = loopNode.getParent();
		}
		sb.append(annotation.getValue()+"\n");
		for(int i=0; i<children.size(); i++){
			sb.append(children.get(i));
		}
		return sb.toString();*/
	}
	
	public String toHTML(){
		StringBuilder sb = new StringBuilder("<HTML><BODY>");
		TreeNode loopNode = parentMatch;
		while(loopNode != null){
			loopNode = loopNode.getParent();
		}
		sb.append(annotation.getValue()+"<ul>");
		for(int i=0; i<children.size(); i++){
			sb.append(children.get(i));
		}
		sb.append("</ul>");
		sb.append("</BODY></HTML>");
		return sb.toString();
	}
}

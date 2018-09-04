package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.List;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTBlockRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionRenderInfo;
import nl.mpi.lexan.analyzers.helpers.Position;
import nl.mpi.lexan.analyzers.helpers.Suggestion;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;


/**
 * A data model for a suggestion set.
 * Mainly used to store the data structure of a set of suggestions for rendering purposes.
 * 
 * @author Han Sloetjes
 */
public class IGTSuggestionModel extends IGTAbstractDataModel {
	private SuggestionSet suggestionSet;
	private long beginTime = Long.MAX_VALUE;
	private long endTime = -1;
	private IGTSuggestionRenderInfo suggestRenderInfo;

	/**
	 * Constructor with a Suggestion Set as parameter. 
	 * 
	 * @param suggestionSet a set of suggestions for (possibly hierarchically grouped) annotations 
	 */
	public IGTSuggestionModel(SuggestionSet suggestionSet) {
		super();
		this.suggestionSet = suggestionSet;;
		fillModel();
		suggestRenderInfo = new IGTSuggestionRenderInfo();
		//suggestRenderInfo.tierLabelsVisible = true;// default
	}
	
	/**
	 * Returns the SuggestionSet which is the basis for the model.
	 * 
	 * @return the suggestion set
	 */
	public SuggestionSet getSuggestionSet() {
		return suggestionSet;
	}
	
	/**
	 * This is a shortcut to getting this flag from the renderInfo class 
	 * @return the flag for the  visibility of tier labels
	 */
	public boolean showTierLabels() {
		return suggestRenderInfo.tierLabelsVisible;
	}
	/**
	 * A shortcut to setting this flag in the render info class.
	 * @param maybe whether tiers should be shown in front of this set
	 */
	public void showTierLabels(boolean maybe) {
		suggestRenderInfo.tierLabelsVisible = maybe;
	}
	
	/**
	 * Extracts information from the suggestion set and builds the model.
	 * Currently adds an empty, place holder tier as the root,
	 * and treat the first level suggestions as "subdivision" (word level root).
	 */
	private void fillModel() {
		if (suggestionSet != null) {
			// add invisible root tier for all other tiers
			IGTTier rootTier = new IGTTier("ROOT", IGTTierType.ROOT);
			rowData.add(rootTier);
			rowHeader.addHeader("ROOT");
			IGTAnnotation rootAnn = new IGTAnnotation("ROOT");
			rootTier.addAnnotation(rootAnn);
			
			final List<Suggestion> suggestions = suggestionSet.getSuggestions();
			
			for (Suggestion suggestion : suggestions) {
				Position pos = suggestion.getPosition();
				
				IGTTier wlRootTier = getRowDataForTier(pos.getTierId());
				
				if (wlRootTier == null) {
					// special treatment of the first suggestion, determines the "root"
					wlRootTier = new IGTTier(pos.getTierId(), IGTTierType.WORD_LEVEL_ROOT, true);
					rowData.add(wlRootTier);
					rowHeader.addHeader(pos.getTierId());
					rootTier.addChildTier(wlRootTier);
					wlRootTier.setParentTier(rootTier);
				}
				
				IGTAnnotation igtAnn = new IGTAnnotation(suggestion.getContent());
				wlRootTier.addAnnotation(igtAnn);
				rootAnn.addChild(igtAnn);
				
				if (pos.getBeginTime() < beginTime) {
					beginTime = pos.getBeginTime();
				}
				if (pos.getEndTime() > endTime) {
					endTime = pos.getEndTime();
				}
				// add children
				addChildNodes(wlRootTier, igtAnn, suggestion.getChildren());
			}
		}
	}
	
	/**
	 * Adds the child suggestions to the model
	 *  
	 * @param parentTier the parent tier
	 * @param parentAnn the parent annotation
	 * @param children the list of child suggestions
	 */
	private void addChildNodes(IGTTier parentTier, IGTAnnotation parentAnn, List<Suggestion> children) {
		if (children != null) {
			for (Suggestion sug : children) {
				Position pos = sug.getPosition();
				
				IGTTier childTier = getRowDataForTier(pos.getTierId());
				
				if (childTier == null) {
					// set all tiers to be in a word level block for now
					childTier = new IGTTier(pos.getTierId(), IGTTierType.SUBDIVISION, true);
					rowData.add(childTier);
					rowHeader.addHeader(pos.getTierId());
					parentTier.addChildTier(childTier);
					childTier.setParentTier(parentTier);
				}
				
				IGTAnnotation igtAnn = new IGTAnnotation(sug.getContent());
				childTier.addAnnotation(igtAnn);
				parentAnn.addChild(igtAnn);
				
				if (pos.getBeginTime() < beginTime) {
					beginTime = pos.getBeginTime();
				}
				if (pos.getEndTime() > endTime) {
					endTime = pos.getEndTime();
				}
				
				// add children
				addChildNodes(childTier, igtAnn, sug.getChildren());
			}
		}
	}

	@Override
	public long getBeginTime() {
		if (beginTime != Long.MAX_VALUE) {
			return beginTime;
		}
		
		return 0;
	}

	@Override
	public long getEndTime() {
		if (endTime > -1) {
			return endTime;
		}
		return 0;
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getRenderInfo()
	 */
	@Override
	public IGTBlockRenderInfo getRenderInfo() {
		return suggestRenderInfo;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("IGTSuggestionModel:[");
		buf.append(suggestionSet.toString());
		buf.append(" showTierLabels=");
		buf.append(String.valueOf(suggestRenderInfo.tierLabelsVisible));
		buf.append(" beginTime=");
		buf.append(String.valueOf(beginTime));
		buf.append(" endTime=");
		buf.append(String.valueOf(endTime));
		IGTBlockRenderInfo ri = getRenderInfo();
		buf.append(String.valueOf(ri));
		buf.append("]");
		
		return buf.toString();
	}
}

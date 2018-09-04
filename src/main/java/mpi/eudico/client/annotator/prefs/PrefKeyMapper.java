package mpi.eudico.client.annotator.prefs;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that maps old preference key values to new key values and vice versa.
 * Introduced with the transition to an xml based preferences storage.
 * (ELAN 3.2)
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
public class PrefKeyMapper {
	/** maps old keys to new keys */
	public static Map<String, String> keyMapper;
	
	static {
		keyMapper = new HashMap<String, String>(20);
		keyMapper.put("GridMultiMode", "GridViewer.MultiTierMode");
		keyMapper.put("GridTierName", "GridViewer.TierName");
		keyMapper.put("GridFontSize", "GridViewer.FontSize");
		keyMapper.put("TextTierName", "TextViewer.TierName");
		keyMapper.put("TextFontSize", "TextViewer.FontSize");
		keyMapper.put("TextDotSeparated", "TextViewer.DotSeparated");
		keyMapper.put("TextCenterVertical", "TextViewer.CenterVertical");
		keyMapper.put("SubTitleTierName", "SubTitleViewer.TierName-"); // 1 - x
		keyMapper.put("SubTitleFontSize", "SubTitleViewer.FontSize-"); // 1 - x
		keyMapper.put("TimeLineFontSize", "TimeLineViewer.FontSize");
		keyMapper.put("InterlinearFontSize", "InterlinearViewer.FontSize");
		keyMapper.put("TimeSeriesNumPanels", "TimeSeriesViewer.NumPanels"); 
		keyMapper.put("SelectedTabIndex", "LayoutManager.SelectedTabIndex");
		keyMapper.put("VisibleMultiTierViewer", "LayoutManager.VisibleMultiTierViewer");
		keyMapper.put("TierSortingMode", "MultiTierViewer.TierSortingMode");
		keyMapper.put("ActiveTierName", "MultiTierViewer.ActiveTierName");		
		keyMapper.put("TierOrder", "MultiTierViewer.TierOrder");
		keyMapper.put("TimeSeriesNumPanels", "TimeSeriesViewer.NumberOfPanels");
		keyMapper.put("SbxMarkerDir", "LastUsedShoeboxMarkerDir");
		//TimeSeriesPanelMap -> TimeSeriesViewer.Panel-x
	}
}

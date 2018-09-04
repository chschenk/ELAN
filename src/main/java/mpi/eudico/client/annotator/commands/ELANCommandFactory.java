package mpi.eudico.client.annotator.commands;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * DOCUMENT ME!
 * $Id: ELANCommandFactory.java 46470 2018-07-25 12:44:50Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class ELANCommandFactory {
	// all HashMaps have Transcription as their key
	private static Map<Transcription, Map<String, CommandAction>> commandActionHash = new HashMap<Transcription, Map<String, CommandAction>>();
	private static Map<Transcription, UndoCA> undoCAHash = new HashMap<Transcription, UndoCA>();
	private static Map<Transcription, RedoCA> redoCAHash = new HashMap<Transcription, RedoCA>();
	private static Map<Transcription, CommandHistory> commandHistoryHash = new HashMap<Transcription, CommandHistory>();

	private static Map<Transcription, ViewerManager2> viewerManagerHash = new HashMap<Transcription, ViewerManager2>();
	private static Map<Transcription, ElanLayoutManager> layoutManagerHash = new HashMap<Transcription, ElanLayoutManager>();
	private static Map<Transcription, TSTrackManager> trackManagerHash = new HashMap<Transcription, TSTrackManager>();

	// root frame for dialogs
	private static Map<Transcription, JFrame> rootFrameHash = new HashMap<Transcription, JFrame>();
	
	// a table for the available languages 
	private static final Map<String, Locale> languages = new HashMap<String, Locale>();

	// list of commands/command actions
	public static final String SET_TIER_NAME = "CommandActions.SetTierName";
	public static final String CHANGE_TIER = "Menu.Tier.ChangeTier";
	public static final String ADD_TIER = "Menu.Tier.AddNewTier";
	public static final String DELETE_TIER = "Menu.Tier.DeleteTier";
	public static final String DELETE_TIERS = "Menu.Tier.DeleteTiers"; 
	public static final String EDIT_TIER = "CommandActions.EditTier";
	public static final String IMPORT_TIERS = "Menu.Tier.ImportTiers";
	
	public static final String ADD_PARTICIPANT= "Menu.Tier.AddParticipant";
	public static final String ADD_PARTICIPANT_DLG = "AddParticipantDlg";
	
	public static final String EDIT_TYPE = "CommandActions.EditType";
	public static final String IMPORT_TYPES =  "Menu.Type.ImportTypes";
	public static final String ADD_TYPE = "Menu.Type.AddNewType";
	public static final String CHANGE_TYPE = "Menu.Type.ChangeType";
	public static final String DELETE_TYPE = "Menu.Type.DeleteType";
	
	public static final String ADD_CV = "CommandActions.AddCV";
	public static final String CHANGE_CV = "CommandActions.ChangeCV";
	public static final String DELETE_CV = "CommandActions.DeleteCV";
	public static final String REPLACE_CV = "CommandActions.ReplaceCV";
	public static final String MERGE_CVS = "CommandActions.MergeCV";
	public static final String ADD_CV_ENTRY = "CommandActions.AddCVEntry";
	public static final String CHANGE_CV_ENTRY = "CommandActions.ChangeCVEntry";
	public static final String DELETE_CV_ENTRY = "CommandActions.DeleteCVEntry";
	public static final String MOVE_CV_ENTRIES = "MoveEntries";
	public static final String REPLACE_CV_ENTRIES = "ReplaceEntries";
	public static final String EDIT_CV_DLG = "Menu.Edit.EditCV"; 

	public static final String NEW_ANNOTATION = "Menu.Annotation.NewAnnotation";
	public static final String NEW_ANNOTATION_REC = "Menu.Annotation.NewAnnotationRecursive";
	public static final String CREATE_DEPEND_ANN = "Menu.Annotation.CreateDependingAnnotations";
	public static final String NEW_ANNOTATION_ALT = "NA_Alt";
	public static final String NEW_ANNOTATION_BEFORE = "Menu.Annotation.NewAnnotationBefore";
	public static final String NEW_ANNOTATION_AFTER = "Menu.Annotation.NewAnnotationAfter";
	/* for a command that creates one or two annotations in an empty space, a gap, of a tier */
	public static final String NEW_ANNOTATIONS_IN_GAP = "Menu.Annotation.NewAnnotationsInGap";
	public static final String MODIFY_ANNOTATION = "Menu.Annotation.ModifyAnnotation";
	public static final String MODIFY_ANNOTATION_ALT = "MA_Alt";
	public static final String MODIFY_ANNOTATION_DC = "Menu.Annotation.ModifyAnnotationDatCat";
	public static final String MODIFY_ANNOTATION_DC_DLG = "ModifyAnnotationDCDlg";
	public static final String SPLIT_ANNOTATION = "Menu.Annotation.SplitAnnotation";
	public static final String MODIFY_ANNOTATION_DLG = "ModifyAnnotationDialog";
    public static final String REMOVE_ANNOTATION_VALUE = "Menu.Annotation.RemoveAnnotationValue";
	public static final String MODIFY_ANNOTATION_TIME_DLG = "Menu.Annotation.ModifyAnnotationTimeDialog";

	public static final String DELETE_ANNOTATION = "Menu.Annotation.DeleteAnnotation";
	public static final String DELETE_ANNOTATION_ALT = "DA_Alt"; 
	public static final String DELETE_ANNOS_IN_SELECTION = "Menu.Annotation.DeleteAnnotationsInSelection";
	public static final String DELETE_ANNOS_LEFT_OF = "Menu.Annotation.DeleteAnnotationsLeftOf";
	public static final String DELETE_ANNOS_RIGHT_OF = "Menu.Annotation.DeleteAnnotationsRightOf";
	public static final String DELETE_ALL_ANNOS_LEFT_OF = "Menu.Annotation.DeleteAllLeftOf";
	public static final String DELETE_ALL_ANNOS_RIGHT_OF = "Menu.Annotation.DeleteAllRightOf";
	public static final String DELETE_MULTIPLE_ANNOS = "Menu.Annotation.DeleteSelectedAnnotations";
	
	public static final String COPY_ANNOTATION = "Menu.Annotation.CopyAnnotation";
	public static final String COPY_ANNOTATION_TREE = "Menu.Annotation.CopyAnnotationTree";
	public static final String PASTE_ANNOTATION = "Menu.Annotation.PasteAnnotation";
	public static final String PASTE_ANNOTATION_HERE = "Menu.Annotation.PasteAnnotationHere";
	public static final String PASTE_ANNOTATION_TREE = "Menu.Annotation.PasteAnnotationTree";
	public static final String PASTE_ANNOTATION_TREE_HERE = "Menu.Annotation.PasteAnnotationTreeHere";
	public static final String DUPLICATE_ANNOTATION = "Menu.Annotation.DuplicateAnnotation";
	public static final String DUPLICATE_REMOVE_ANNOTATION = "Menu.Annotation.DuplicateRemoveAnnotation";
	public static final String MERGE_ANNOTATION_WN = "Menu.Annotation.MergeWithNext";
	public static final String MERGE_ANNOTATION_WB = "Menu.Annotation.MergeWithBefore";
	public static final String MOVE_ANNOTATION_TO_TIER = "Menu.Annotation.MoveAnnotationToTier";// not visible in ui
	public static final String ADD_COMMENT = "Menu.Annotation.AddComment";
	public static final String DELETE_COMMENT = "Menu.Annotation.DeleteComment";
	public static final String CHANGE_COMMENT = "Menu.Annotation.ChangeComment";
	public static final String ANALYZE_ANNOTATION = "Menu.Annotation.Analyze";
	public static final String ADD_TO_LEXICON = "Menu.Annotation.AddToLexicon";
	public static final String SHOW_IN_BROWSER = "Menu.Annotation.ShowInBrowser";
		
	public static final String MODIFY_OR_ADD_DEPENDENT_ANNOTATIONS = "LexiconEntryViewer.ChangeAnnotations";
	
    public static final String COPY_TO_NEXT_ANNOTATION = "CommandActions.CopyToNextAnnotation";    
	public static final String MODIFY_ANNOTATION_TIME = "CommandActions.ModifyAnnotationTime";
	public static final String SHIFT_ALL_DLG = "ShiftAllAnn";
	public static final String SHIFT_ANN_DLG = "ShiftAnn";
	public static final String SHIFT_ANN_ALLTIER_DLG = "ShiftAnnAllTier";
	public static final String SHIFT_ANNOTATIONS = "CommandActions.ShiftAnnotations";
	public static final String SHIFT_ALL_ANNOTATIONS = "Menu.Annotation.ShiftAll";
	public static final String SHIFT_ALL_ANNOTATIONS_LROf = "CommandActions.ShiftAnnotationsLROf";
	public static final String SHIFT_ACTIVE_ANNOTATION = "Menu.Annotation.ShiftActiveAnnotation";
	public static final String SHIFT_ANNOS_IN_SELECTION = "Menu.Annotation.ShiftAnnotationsInSelection";
	public static final String SHIFT_ANNOS_LEFT_OF = "Menu.Annotation.ShiftAnnotationsLeftOf";
	public static final String SHIFT_ANNOS_RIGHT_OF = "Menu.Annotation.ShiftAnnotationsRightOf";
	public static final String SHIFT_ALL_ANNOS_LEFT_OF = "Menu.Annotation.ShiftAllLeftOf";
	public static final String SHIFT_ALL_ANNOS_RIGHT_OF = "Menu.Annotation.ShiftAllRightOf";
	
	public static final String TRANS_TABLE_CLM_NO = "TranscriptionTable.Column.No";
	
	/* constants for viewers that can be shown or hidden in Annotation Mode */
	public static final String GRID_VIEWER = "Menu.View.Viewers.Grid";
	public static final String TEXT_VIEWER = "Menu.View.Viewers.Text";
	public static final String SUBTITLE_VIEWER = "Menu.View.Viewers.Subtitles";
	public static final String LEXICON_VIEWER = "LexiconEntryViewer.Lexicon";
	public static final String COMMENT_VIEWER = "CommentViewer.Comment";
	public static final String RECOGNIZER = "Menu.View.Viewers.Recognizer";
	public static final String METADATA_VIEWER = "Menu.View.Viewers.MetaData";
	public static final String SIGNAL_VIEWER = "Menu.View.Viewers.Signal";	
	public static final String INTERLINEAR_VIEWER = "Menu.View.Viewers.InterLinear";
	public static final String INTERLINEAR_LEXICON_VIEWER = "Menu.View.Viewers.InterLinearize";
	public static final String TIMESERIES_VIEWER = "Menu.View.Viewers.TimeSeries";
	public static final String SYNTAX_VIEWER = "CommandActions.SyntaxViewer";
	public static final String MEDIA_PLAYERS = "Menu.View.MediaPlayer";
	public static final String WAVEFORMS = "Menu.View.Waveform";
	public static final String VIEWERS = "Menu.View.Viewers";
	
	public static final String TOKENIZE_DLG = "Menu.Tier.Tokenize";
	public static final String ANN_ON_DEPENDENT_TIER = "Menu.Tier.AnnotationsOnDependentTiers";
	public static final String ANN_FROM_OVERLAP = "Menu.Tier.AnnotationsFromOverlaps";
	public static final String ANN_FROM_OVERLAP_CLAS = "Menu.Tier.AnnotationsFromOverlapsClas";
	public static final String ANN_FROM_SUBTRACTION = "Menu.Tier.AnnotationsFromSubtraction";
	public static final String ANN_FROM_GAPS = "Menu.Tier.AnnotationsFromGaps";
	//public static final String COMPARE_ANNOTATORS_DLG = "Menu.Tier.CompareAnnotators";
	public static final String CHANGE_CASE = "Menu.Tier.ChangeCase";
	
	/* few constants only used as internal identifier for a command! */
	public static final String ANN_ON_DEPENDENT_TIER_COM = "AnnsOnDependentTier";
	public static final String ANN_FROM_OVERLAP_COM = "AnnsFromOverlaps";
	public static final String ANN_FROM_SUBTRACTION_COM = "AnnsFromSubtraction";
	public static final String ANN_FROM_OVERLAP_COM_CLAS = "AnnsFromOverlapsClas";
	public static final String ANN_FROM_GAPS_COM = "AnnsFromGaps";
	public static final String CHANGE_CASE_COM = "ChangeCase";
	
	public static final String MERGE_TIERS = "Menu.Tier.MergeTiers";
	public static final String MERGE_TIERS_COM = "MergeTiers";
	public static final String MERGE_TIERS_CLAS = "Menu.Tier.MergeTiersClassic";
	/* only internal */
	public static final String MERGE_TIERS_DLG_CLAS = "MergeTiersDlgClas";	
	public static final String MERGE_TIER_GROUP = "Menu.Tier.MergeTierGroup";
	/* only internal */
	public static final String MERGE_TIER_GROUP_DLG = "MergeTierGroupDlg";
	
	public static final String COPY_ANN_OF_TIER = "Menu.Tier.CopyAnnotationsOfTier";
	public static final String COPY_ANN_OF_TIER_DLG = "Menu.Tier.CopyAnnotationsOfTierDialog";
	public static final String COPY_ANN_OF_TIER_COM = "Menu.Tier.CopyAnnotationsOfTierCommand";
	
	public static final String TOKENIZE_TIER = "CommandActions.Tokenize";
    public static final String REGULAR_ANNOTATION_DLG = "Menu.Tier.RegularAnnotation";   
    public static final String REGULAR_ANNOTATION = "CommandActions.RegularAnnotation";
    
	public static final String SHOW_TIMELINE = "Menu.View.ShowTimeline";
	public static final String SHOW_INTERLINEAR = "Menu.View.ShowInterlinear";
	public static final String SHOW_MULTITIER_VIEWER = "Commands.ShowMultitierViewer";

	public static final String GOTO_DLG = "Menu.Search.GoTo";
	public static final String SEARCH_DLG = "Menu.Search.Find";
	/** Holds string for search in multiple files */
	public static final String SEARCH_MULTIPLE_DLG = "Menu.Search.Multiple";	
	/** Holds string for FAST search in multiple files */
	public static final String FASTSEARCH_DLG = "Menu.Search.FASTSearch";
	/** Holds string for structured search in multiple files */
	public static final String STRUCTURED_SEARCH_MULTIPLE_DLG = "Menu.Search.StructuredMultiple";
	/** Command action name of replacing matches with string */
	public static final String REPLACE = "CommandActions.Replace";
	public static final String TIER_DEPENDENCIES = "Menu.View.Dependencies";
	public static final String SHORTCUTS = "Menu.View.Shortcuts";
    public static final String SPREADSHEET = "Menu.View.SpreadSheet";
    public static final String STATISTICS = "Menu.View.Statistics";
    /* constants for the different perspectives or working modes */
	public static final String SYNC_MODE = "Menu.Options.SyncMode";
	public static final String ANNOTATION_MODE = "Menu.Options.AnnotationMode";
	public static final String TRANSCRIPTION_MODE = "Menu.Options.TranscriptionMode";
	public static final String SEGMENTATION_MODE = "Menu.Options.SegmentationMode";	
	public static final String INTERLINEARIZATION_MODE = "Menu.Options.InterlinearizationMode";	
	/* though initially not in the Options menu but as a separate application, follow naming convention */
	public static final String TURNS_SCENE_MODE = "Menu.Options.TurnsAndSceneMode";
	
	/* three different ways of updating surrounding annotations when creating a new or modifying an existing annotation */
	public static final String BULLDOZER_MODE = "Menu.Options.BulldozerMode";
	public static final String TIMEPROP_NORMAL = "Menu.Options.NormalPropagationMode";
	public static final String SHIFT_MODE = "Menu.Options.ShiftMode";

	public static final String SELECTION_MODE = "CommandActions.SelectionMode";
	public static final String LOOP_MODE = "CommandActions.LoopMode";
	public static final String CONTINUOUS_PLAYBACK_MODE = "CommandActions.ContinuousPlaybackMode";
	public static final String CLEAR_SELECTION = "Menu.Play.ClearSelection";
	public static final String CLEAR_SELECTION_ALT = "CS_Alt";
	public static final String CLEAR_SELECTION_AND_MODE = "Menu.Play.ClearSelectionAndMode";
	public static final String PLAY_SELECTION = "Menu.Play.PlaySelection";
	public static final String PLAY_SELECTION_SLOW = "CommandActions.PlaySelectionSlow";
	public static final String PLAY_SELECTION_NORMAL_SPEED = "CommandActions.PlaySelectionNormalSpeed";
	public static final String PLAY_AROUND_SELECTION = "CommandActions.PlayAroundSelection";
	public static final String PLAY_AROUND_SELECTION_DLG = "Menu.Options.PlayAroundSelectionDialog";
	public static final String PLAYBACK_TOGGLE_DLG = "Menu.Options.PlaybackToggleDialog";
	public static final String PLAYBACK_TOGGLE = "PLAYBACK_TOGGLE";
	public static final String PLAYBACK_RATE_TOGGLE = "CommandActions.PlaybackRateToggle";
	public static final String PLAYBACK_VOLUME_TOGGLE = "CommandActions.PlaybackVolumeToggle";
	/* three frame rate related menu options */
	public static final String SET_PAL = "Menu.Options.FrameLength.PAL";
	public static final String SET_PAL_50 = "Menu.Options.FrameLength.PAL50";
	public static final String SET_NTSC = "Menu.Options.FrameLength.NTSC";
	/* constants for stepping through the media with different step sizes */
	public static final String NEXT_FRAME = "Menu.Play.Next";
	public static final String PREVIOUS_FRAME = "Menu.Play.Previous";
	public static final String PLAY_PAUSE = "Menu.Play.PlayPause";
	public static final String GO_TO_BEGIN = "Menu.Play.GoToBegin";
	public static final String GO_TO_END = "Menu.Play.GoToEnd";
	public static final String PREVIOUS_SCROLLVIEW = "Menu.Play.GoToPreviousScrollview";
	public static final String NEXT_SCROLLVIEW = "Menu.Play.GoToNextScrollview";
	public static final String PIXEL_LEFT = "Menu.Play.1PixelLeft";
	public static final String PIXEL_RIGHT = "Menu.Play.1PixelRight";
	public static final String SECOND_LEFT = "Menu.Play.1SecLeft";
	public static final String SECOND_RIGHT = "Menu.Play.1SecRight";

	public static final String SELECTION_BOUNDARY = "Menu.Play.ToggleCrosshairInSelection";
	/** alternative due to keyboard problems */
	public static final String SELECTION_BOUNDARY_ALT = "SB_Alt";
	public static final String SELECTION_CENTER = "Menu.Play.MoveCrosshairToCenterOfSelection";
	public static final String SELECTION_BEGIN = "Menu.Play.MoveCrosshairToBeginOfSelection";
	public static final String SELECTION_END = "Menu.Play.MoveCrosshairToEndOfSelection";
	
	public static final String ACTIVE_ANNOTATION = "Commands.ActiveAnnotation";// not in language file
	public static final String ACTIVE_ANNOTATION_EDIT = "CommandActions.OpenInlineEditBox";

	public static final String PREVIOUS_ANNOTATION = "CommandActions.PreviousAnnotation";
	public static final String PREVIOUS_ANNOTATION_EDIT = "CommandActions.PreviousAnnotationEdit";
	public static final String NEXT_ANNOTATION = "CommandActions.NextAnnotation";
	public static final String NEXT_ANNOTATION_EDIT = "CommandActions.NextAnnotationEdit";
	
	public static final String COPY_CURRENT_TIME = "CommandActions.CopyCurrentTime";
	public static final String ANNOTATION_UP = "CommandActions.AnnotationUp";
	public static final String ANNOTATION_DOWN = "CommandActions.AnnotationDown";
	public static final String ACTIVE_ANNOTATION_CURRENT_TIME = "CommandActions.AnnotationAtCurrentTime";
	public static final String CANCEL_ANNOTATION_EDIT = "CommandActions.CancelAnnotationEdit";
	
	public static final String SET_LOCALE = "Menu.Options.Language";
	/* some constants for available languages for the UI, need not be hard-coded here probably */
	public static final String CATALAN = "Catal\u00E0";
	public static final String DUTCH = "Nederlands";
	public static final String ENGLISH = "English";
	public static final String SPANISH = "Espa\u00F1ol";
	public static final String SWEDISH = "Svenska";
	public static final String GERMAN ="Deutsch";
	public static final String PORTUGUESE = "Portugu\u00EAs";
	public static final String FRENCH = "Fran\u00E7ais";
	public static final String JAPANESE = "\u65e5\u672c\u8a9e";
	public static final String CHINESE_SIMPL = "\uFEFF\u7B80\u4F53\u4E2D\u6587";
	public static final String RUSSIAN = "\u0420\u0443\u0441\u0441\u043a\u0438\u0439";
	public static final String KOREAN = "\ud55c\uad6d\uc5b4";
	public static final String CUSTOM_LANG = "Menu.Options.Language.Custom";

	public static final String SAVE = "Menu.File.Save";
	public static final String SAVE_AS = "Menu.File.SaveAs";
	public static final String SAVE_AS_TEMPLATE = "Menu.File.SaveAsTemplate";	
	public static final String SAVE_SELECTION_AS_EAF = "Menu.File.SaveSelectionAsEAF";
	public static final String STORE = "Commands.Store";

	public static final String EXPORT_TAB = "Menu.File.Export.Tab";
	public static final String EXPORT_TEX = "Menu.File.Export.TeX";
	public static final String EXPORT_TIGER = "Menu.File.Export.Tiger";
	public static final String EXPORT_EAF_2_7 = "Menu.File.Export.EAF2.7";
	public static final String EXPORT_QT_SUB = "Menu.File.Export.QtSub";
	public static final String EXPORT_SUBTITLES = "Menu.File.Export.Subtitles";
	public static final String EXPORT_SMIL_RT = "Menu.File.Export.Smil.RealPlayer";
	public static final String EXPORT_SMIL_QT = "Menu.File.Export.Smil.QuickTime";
	public static final String EXPORT_SHOEBOX = "Menu.File.Export.Shoebox";
	public static final String EXPORT_CHAT = "Menu.File.Export.CHAT";
	public static final String EXPORT_MEDIA = "Menu.File.Export.Media";
	public static final String EXPORT_IMAGE_FROM_WINDOW = "Menu.File.Export.ImageFromWindow";
	public static final String EXPORT_TOOLBOX = "Menu.File.Export.Toolbox";
	public static final String EXPORT_FLEX = "Menu.File.Export.Flex";
	public static final String EXPORT_FILMSTRIP = "Menu.File.Export.FilmStrip";
	public static final String EXPORT_RECOG_TIER = "Menu.File.Export.RecognizerTiers";
	public static final String EXPORT_TRAD_TRANSCRIPT = "Menu.File.Export.TraditionalTranscript";
	public static final String EXPORT_INTERLINEAR = "Menu.File.Export.Interlinear";
	public static final String EXPORT_HTML = "Menu.File.Export.HTML";
	public static final String EXPORT_REGULAR_MULTITIER_EAF = "Menu.File.Export.RegularMultitierEAF";
	
	public static final String BACKUP = "CommandActions.Backup";
	public static final String BACKUP_NEVER = "Menu.File.Backup.Never";
	public static final String BACKUP_1 = "Menu.File.Backup.1";
	public static final String BACKUP_5 = "Menu.File.Backup.5";
	public static final String BACKUP_10 = "Menu.File.Backup.10";
	public static final String BACKUP_20 = "Menu.File.Backup.20";
	public static final String BACKUP_30 = "Menu.File.Backup.30";

	public static final String PRINT = "Menu.File.Print";
	public static final String PREVIEW = "Menu.File.PrintPreview";
	public static final String PAGESETUP = "Menu.File.PageSetup";

	public static final String REDO = "Menu.Edit.Redo";
	public static final String UNDO = "Menu.Edit.Undo";
	
	public static final String LINKED_FILES_DLG = "Menu.Edit.LinkedFiles";
    public static final String EDIT_LANGUAGES_LIST = "Menu.Edit.LanguagesList";
    public static final String EDIT_TIER_SET = "Menu.Edit.TierSet";
	public static final String CHANGE_LINKED_FILES = "CommandActions.ChangeLinkedFiles";
	public static final String ADD_SEGMENTATION = "CommandActions.AddSegmentation";
	public static final String FILTER_TIER = "Menu.Tier.FilterTier";
	public static final String FILTER_TIER_DLG = "Menu.Tier.FilterTierDlg";
	public static final String REPARENT_TIER_DLG = "Menu.Tier.ReparentTierDialog";
	public static final String REPARENT_TIER = "Menu.Tier.ReparentTier";
	public static final String COPY_TIER = "Menu.Tier.CopyTier";
	public static final String COPY_TIER_DLG = "Menu.Tier.CopyTierDialog";
	public static final String MERGE_TRANSCRIPTIONS = "Menu.File.MergeTranscriptions";
	public static final String MERGE_TRANSCRIPTIONS_UNDOABLE = "Menu.File.MergeTranscriptionsUndoable";
	public static final String WEBLICHT_MERGE_TRANSCRIPTIONS = "Menu.Options.WebServices.WebLicht";
	
	public static final String NEXT_ACTIVE_TIER = "CommandActions.NextActiveTier";
	public static final String PREVIOUS_ACTIVE_TIER = "CommandActions.PreviousActiveTier";
	public static final String ACTIVE_TIER = "ActiveTier";
	public static final String CLOSE = "Menu.File.Close";
	
	public static final String EXT_TRACK_DATA = "CommandActions.ExtractTrackData";
	public static final String KIOSK_MODE = "Menu.Options.KioskMode";
	public static final String IMPORT_PRAAT_GRID = "Menu.File.Import.PraatTiers";
	public static final String IMPORT_PRAAT_GRID_DLG = "Praat_Grid_Dlg";
	public static final String EXPORT_PRAAT_GRID = "Menu.File.Export.Praat";
	public static final String IMPORT_RECOG_TIERS = "Menu.File.Import.RecognizerTiers";
	public static final String REMOVE_ANNOTATIONS_OR_VALUES = "Menu.Tier.RemoveAnnotationsOrValues";
	public static final String REMOVE_ANNOTATIONS_OR_VALUES_DLG = "RemoveAnnotationsOrValuesDlg"; 
	public static final String ANNOTATIONS_TO_TIERS = "Menu.Tier.AnnotationValuesToTiers";
	public static final String ANNOTATIONS_TO_TIERS_DLG = "AnnotationValuesToTiersDlg";
	public static final String LABEL_AND_NUMBER = "Menu.Tier.LabelAndNumber";
  	public static final String LABEL_N_NUM_DLG = "LabelNumDlg";
	public static final String SEGMENTS_2_TIER_DLG = "Seg2TierDlg";
	public static final String SEGMENTS_2_TIER = "CommandActions.SegmentsToTiers";
	public static final String KEY_CREATE_ANNOTATION = "CommandActions.KeyCreateAnnotation";
	public static final String EXPORT_WORDS = "Menu.File.Export.WordList";
	public static final String EXPORT_PREFS = "Menu.Edit.Preferences.Export";
	public static final String IMPORT_PREFS = "Menu.Edit.Preferences.Import";
	public static final String FONT_BROWSER = "Menu.View.FontBrowser";

	public static final String CENTER_SELECTION = "TimeLineViewer.CenterSelection";
	public static final String SET_AUTHOR = "Menu.Edit.Author";

	public static final String MOVE_ANNOTATION_LBOUNDARY_LEFT ="CommandActions.Annotation_LBound_Left";
	public static final String MOVE_ANNOTATION_LBOUNDARY_RIGHT ="CommandActions.Annotation_LBound_Right";
	public static final String MOVE_ANNOTATION_RBOUNDARY_LEFT ="CommandActions.Annotation_RBound_Left";
	public static final String MOVE_ANNOTATION_RBOUNDARY_RIGHT ="CommandActions.Annotation_RBound_Right";		
	
	// action keys for global, document independent actions
	public static final String NEXT_WINDOW = "Menu.Window.Next";
	public static final String PREV_WINDOW = "Menu.Window.Previous";
	public static final String EDIT_PREFS = "Menu.Edit.Preferences.Edit";
	public static final String EDIT_SHORTCUTS = "Menu.Edit.Preferences.Shortcut";
	public static final String REPLACE_MULTIPLE = "Menu.Search.FindReplaceMulti";
	public static final String NEW_DOC = "Menu.File.New";
	public static final String OPEN_DOC = "Menu.File.Open";	
	
	public static final String EXPORT_TOOLBOX_MULTI = "Menu.File.MultipleExport.Toolbox";	
	public static final String EXPORT_PRAAT_MULTI = "Menu.File.MultipleExport.Praat";
	public static final String EXPORT_TAB_MULTI = "Menu.File.MultipleExport.Tab";
	public static final String EXPORT_ANNLIST_MULTI = "Menu.File.Export.AnnotationListMulti";
	public static final String EXPORT_WORDLIST_MULTI = "Menu.File.MultipleExport.WordList";
	public static final String EXPORT_TIERS_MULTI = "Menu.File.Export.Tiers";
	public static final String EXPORT_OVERLAPS_MULTI = "Menu.File.Export.OverlapsMulti";
	public static final String EXPORT_FLEX_MULTI = "Menu.File.MultipleExport.Flex";
	public static final String EXPORT_THEME_MULTI = "Menu.File.MultipleExport.Theme";
	
	public static final String IMPORT_SHOEBOX = "Menu.File.Import.Shoebox";
	public static final String IMPORT_TOOLBOX = "Menu.File.Import.Toolbox";
	public static final String IMPORT_CHAT = "Menu.File.Import.CHAT";
	public static final String IMPORT_TRANS = "Menu.File.Import.Transcriber";
	public static final String IMPORT_TAB = "Menu.File.Import.Delimited";
	public static final String IMPORT_FLEX = "Menu.File.Import.FLEx";
	
	public static final String IMPORT_TOOLBOX_MULTI = "Menu.File.MultipleImport.Toolbox";
	public static final String IMPORT_PRAAT_GRID_MULTI = "Menu.File.MultipleImport.PraatTiers";
	public static final String IMPORT_FLEX_MULTI = "Menu.File.MultipleImport.FLEx";
	
	public static final String EXIT = "Menu.File.Exit";
	public static final String HELP = "Menu.Help.Contents";
	public static final String ABOUT ="Menu.Help.About";
	public static final String CLIP_MEDIA = "Menu.File.Export.MediaWithScript";
	public static final String ADD_TRACK_AND_PANEL = "AddTSTrackAndPanel";
	// TODO add to shortcuts / actions that can have a shortcut assigned
	public static final String CREATE_NEW_MULTI = "Menu.File.MultiEAFCreation";
	public static final String EDIT_MULTIPLE_FILES = "Menu.File.Process.EditMF";
	public static final String SCRUB_MULTIPLE_FILES = "Menu.File.ScrubTranscriptions";
	public static final String ANNOTATION_OVERLAP_MULTI = "Menu.File.MultipleFileAnnotationFromOverlaps";
	public static final String ANNOTATOR_COMPARE_MULTI = "Menu.File.MultipleFileCompareAnnotators";
	public static final String ANNOTATION_SUBTRACTION_MULTI = "Menu.File.MultipleFileAnnotationFromSubtraction";
	public static final String STATISTICS_MULTI = "Menu.File.MultiFileStatistics";
	public static final String NGRAMSTATS_MULTI = "Menu.File.MultiFileNgramStats";
	public static final String CLIP_MEDIA_MULTI = "Menu.File.MultipleMediaClips";
	public static final String MERGE_TIERS_MULTI = "Menu.File.MultipleFileMergeTiers";
	public static final String UPDATE_TRANSCRIPTIONS_FOR_ECV = "Menu.File.MultiEAFECVUpdater";
	public static final String UPDATE_TRANSCRIPTIONS_WITH_TEMPLATE = "Menu.File.MultipleFileUpdateWithTemplate";
	
	public static final String EDIT_LEX_SRVC_DLG = "Menu.Edit.EditLexSrvc";
	public static final String ADD_LEX_LINK = "CommandActions.AddLexLink";
	public static final String CHANGE_LEX_LINK = "CommandActions.ChangeLexLink";
	public static final String DELETE_LEX_LINK = "CommandActions.DeleteLexLink";
	public static final String PLAY_STEP_AND_REPEAT = "Menu.Play.PlayStepAndRepeat";	
	public static final String EDIT_SPELL_CHECKER_DLG = "Menu.Edit.EditSpellChecker";
	
	// Transcription mode actions
	public static final String COMMIT_CHANGES = "TranscriptionMode.Actions.CommitChanges";
	public static final String CANCEL_CHANGES = "TranscriptionMode.Actions.CancelChanges";
	public static final String MOVE_UP = "TranscriptionMode.Actions.MoveUp";
	public static final String MOVE_DOWN = "TranscriptionMode.Actions.MoveDown";
	public static final String MOVE_LEFT = "TranscriptionMode.Actions.MoveLeft";
	public static final String MOVE_RIGHT = "TranscriptionMode.Actions.MoveRight";
	public static final String PLAY_FROM_START = "TranscriptionMode.Actions.PlayFromStart";
	public static final String HIDE_TIER = "TranscriptionTable.Label.HideLinkedTiers";
	public static final String FREEZE_TIER = "TranscriptionMode.Actions.FreezeTier";
	public static final String EDIT_IN_ANN_MODE = "TranscriptionTableEditBox.EditInAnnotationMode";
	
	// Segmentation mode actions	
	public static final String SEGMENT = "SegmentationMode.Actions.Segment";
	
	public static final String COMMON_SHORTCUTS = "Shortcuts.Common";
	
	public static final String UPDATE_ELAN = "Menu.Options.CheckForUpdate";
	public static final String WEBSERVICES_DLG = "Menu.Options.WebServices";
	public static final String WEBLICHT_DLG = "WebServicesDialog.WebService.WebLicht";
	public static final String TYPECRAFT_DLG = "WebServicesDialog.WebService.TypeCraft";
	public static final String ZOOM_IN = "Menu.View.ZoomIn";// not really a menu item. TimescaleViewer.ZoomIn?
	public static final String ZOOM_OUT = "Menu.View.ZoomOut";
	public static final String ZOOM_DEFAULT = "Menu.View.ZoomDefault";
	public static final String CYCLE_TIER_SETS = "CommandActions.CycleTierSets";
	public static final String ANNS_FROM_SUGGESTION_SET = "CommandActions.AnnotationsFromSuggestionSet";

	
	static {
		languages.put(CATALAN, ElanLocale.CATALAN);
		languages.put(CHINESE_SIMPL, ElanLocale.CHINESE_SIMP);
		languages.put(DUTCH, ElanLocale.DUTCH);
		languages.put(ENGLISH, ElanLocale.ENGLISH);
		languages.put(RUSSIAN, ElanLocale.RUSSIAN);
		languages.put(SPANISH, ElanLocale.SPANISH);
		languages.put(SWEDISH, ElanLocale.SWEDISH);
		languages.put(GERMAN, ElanLocale.GERMAN);
		languages.put(PORTUGUESE, ElanLocale.PORTUGUESE);
		languages.put(FRENCH, ElanLocale.FRENCH);
		languages.put(JAPANESE, ElanLocale.JAPANESE);
		languages.put(KOREAN, ElanLocale.KOREAN);
		languages.put(CUSTOM_LANG, ElanLocale.CUSTOM);
	}
	
	public static synchronized void addDocument(JFrame fr, ViewerManager2 vm, ElanLayoutManager lm) {
		Transcription t = vm.getTranscription();

		if (rootFrameHash.get(t) == null) {
			rootFrameHash.put(t, fr);
		}

		if (viewerManagerHash.get(t) == null) {
			viewerManagerHash.put(t, vm);
		}

		if (layoutManagerHash.get(t) == null) {
			layoutManagerHash.put(t, lm);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param vm DOCUMENT ME!
	 */
	public static synchronized void removeDocument(ViewerManager2 vm) {
		if (vm != null) {
			Transcription t = vm.getTranscription();

			commandActionHash.remove(t);
			undoCAHash.remove(t);
			redoCAHash.remove(t);
			commandHistoryHash.remove(t);
			viewerManagerHash.remove(t);
			layoutManagerHash.remove(t);
			rootFrameHash.remove(t);
			trackManagerHash.remove(t);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param forTranscription DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static synchronized JFrame getRootFrame(Transcription forTranscription) {
		if(forTranscription == null){
			return null;
		}
		return rootFrameHash.get(forTranscription);
	}

	public static synchronized ViewerManager2 getViewerManager(Transcription forTranscription) {
		return viewerManagerHash.get(forTranscription);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param forTranscription DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static synchronized ElanLayoutManager getLayoutManager(Transcription forTranscription) {
		return layoutManagerHash.get(forTranscription);
	}

	/**
	 * Creation of the a track manager is postponed until it is necessary: when at least 
	 * one time series source has been added.
	 * 
	 * @param forTranscription the document / transcription
	 * @param trackManager the manager for tracks and track sources
	 */
	public static synchronized void addTrackManager(Transcription forTranscription, TSTrackManager trackManager) {
		if (forTranscription != null && trackManager != null) {
			trackManagerHash.put(forTranscription, trackManager);		
		}
	}
	
	/**
	 * Returns the time series track manager for the transcription.
	 * 
	 * @param forTranscription the transcription
	 * @return the track manager or null
	 */
	public static synchronized TSTrackManager getTrackManager(Transcription forTranscription) {
		return trackManagerHash.get(forTranscription);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 * @param caName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static CommandAction getCommandAction(Transcription tr, String caName) {
		CommandAction ca = null;

		synchronized(ELANCommandFactory.class) {
			if (commandActionHash.get(tr) == null) {
				commandActionHash.put(tr, new HashMap<String, CommandAction>());
			}

			if (commandHistoryHash.get(tr) == null) {
				commandHistoryHash.put(tr, new CommandHistory(CommandHistory.historySize, tr));
			}
		}

		ViewerManager2 viewerManager = viewerManagerHash.get(tr);
		ElanLayoutManager layoutManager = layoutManagerHash.get(tr);
		
		ca = commandActionHash.get(tr).get(caName);
		if (ca == null) {
			if (caName.equals(SET_TIER_NAME)) {
				ca = new SetTierNameCA(viewerManager);
			}
			else if (caName.equals(ADD_TIER)) {
				ca = new AddTierDlgCA(viewerManager);
			}
			else if (caName.equals(CHANGE_TIER)) {
				ca = new ChangeTierDlgCA(viewerManager);
			}
			else if (caName.equals(DELETE_TIER) || caName.equals(DELETE_TIERS)) {
				ca = new DeleteTierDlgCA(viewerManager);
			}
			else if (caName.equals(ADD_PARTICIPANT)) {
				ca = new AddParticipantCA(viewerManager);
			}
			else if (caName.equals(IMPORT_TIERS)) {
				ca = new ImportTiersDlgCA(viewerManager);
			}
			else if (caName.equals(ADD_TYPE)) {
				ca = new AddLingTypeDlgCA(viewerManager);
			}
			else if (caName.equals(CHANGE_TYPE)) {
				ca = new ChangeLingTypeDlgCA(viewerManager);
			}
			else if (caName.equals(DELETE_TYPE)) {
				ca = new DeleteLingTypeDlgCA(viewerManager);
			}
			else if (caName.equals(IMPORT_TYPES)) {
				ca = new ImportTypesDlgCA(viewerManager);
			}
			else if (caName.equals(EDIT_CV_DLG)) {
				ca = new EditCVDlgCA(viewerManager);
			}
			else if (caName.equals(NEW_ANNOTATION)) {
				ca = new NewAnnotationCA(viewerManager);
			}
			else if (caName.equals(CREATE_DEPEND_ANN)) {
				ca = new CreateDependentAnnotationsCA(viewerManager);
			}
			else if (caName.equals(NEW_ANNOTATION_REC)) {
				ca = new NewAnnotationRecursiveCA(viewerManager);
			}
			else if (caName.equals(NEW_ANNOTATION_ALT)) {
				ca = new NewAnnotationAltCA(viewerManager);
			}
			else if (caName.equals(NEW_ANNOTATION_BEFORE)) {
				ca = new AnnotationBeforeCA(viewerManager);
			}
			else if (caName.equals(NEW_ANNOTATION_AFTER)) {
				ca = new AnnotationAfterCA(viewerManager);
			}
			else if (caName.equals(MODIFY_ANNOTATION)) {
				ca = new ModifyAnnotationCA(viewerManager);
			}
			else if (caName.equals(MODIFY_ANNOTATION_ALT)) {
				ca = new ModifyAnnotationAltCA(viewerManager);
			}
			else if (caName.equals(SPLIT_ANNOTATION)) {
				ca = new SplitAnnotationCA(viewerManager);
			}
			else if (caName.equals(REMOVE_ANNOTATION_VALUE)) {
				ca = new RemoveAnnotationValueCA(viewerManager);
			}
			else if (caName.equals(DELETE_ANNOTATION)) {
				ca = new DeleteAnnotationCA(viewerManager);
			}
			else if (caName.equals(DELETE_ANNOTATION_ALT)) {
				ca = new DeleteAnnotationAltCA(viewerManager);
			}
			else if (caName.equals(DELETE_ANNOS_IN_SELECTION)) {
				ca = new DeleteAnnotationsInSelectionCA(viewerManager);
			}
			else if (caName.equals(DELETE_ANNOS_LEFT_OF)) {
				ca = new DeleteAnnotationsLeftOfCA(viewerManager);
			}
			else if (caName.equals(DELETE_ANNOS_RIGHT_OF)) {
				ca = new DeleteAnnotationsRightOfCA(viewerManager);
			}
			else if (caName.equals(DELETE_ALL_ANNOS_LEFT_OF)) {
				ca = new DeleteAllAnnotationsLeftOfCA(viewerManager);
			}
			else if (caName.equals(DELETE_ALL_ANNOS_RIGHT_OF)) {
				ca = new DeleteAllAnnotationsRightOfCA(viewerManager);
			}
			else if (caName.equals(DUPLICATE_ANNOTATION)) {
				ca = new DuplicateAnnotationCA(viewerManager);
			}
			else if (caName.equals(MERGE_ANNOTATION_WN)) {
				ca = new MergeAnnotationWithNextCA(viewerManager);
			}
			else if (caName.equals(MERGE_ANNOTATION_WB)) {
				ca = new MergeAnnotationWithBeforeCA(viewerManager);
			}
			else if (caName.equals(COPY_TO_NEXT_ANNOTATION)) {
				ca = new CopyToNextAnnotationCA(viewerManager);
			}
			else if (caName.equals(COPY_ANNOTATION)) {
				ca = new CopyAnnotationCA(viewerManager);
			}
			else if (caName.equals(COPY_ANNOTATION_TREE)) {
				ca = new CopyAnnotationTreeCA(viewerManager);
			}
			else if (caName.equals(PASTE_ANNOTATION)) {
				ca = new PasteAnnotationCA(viewerManager);
			}
			else if (caName.equals(PASTE_ANNOTATION_HERE)) {
				ca = new PasteAnnotationHereCA(viewerManager);
			}
			else if (caName.equals(PASTE_ANNOTATION_TREE)) {
				ca = new PasteAnnotationTreeCA(viewerManager);
			}
			else if (caName.equals(PASTE_ANNOTATION_TREE_HERE)) {
				ca = new PasteAnnotationTreeHereCA(viewerManager);
			}
			else if (caName.equals(MODIFY_ANNOTATION_TIME)) {
				ca = new ModifyAnnotationTimeCA(viewerManager);
			}
			else if (caName.equals(MODIFY_ANNOTATION_DC_DLG)) {
				ca = new ModifyAnnotationDatCatCA(viewerManager);
			}
			else if (caName.equals(SHOW_IN_BROWSER)) {
			 	ca = new ShowInBrowserCA(viewerManager);
			}
			else if (caName == SHIFT_ALL_ANNOTATIONS) {
				ca = new ShiftAllAnnotationsDlgCA(viewerManager);
			}
			else if (caName == SHIFT_ACTIVE_ANNOTATION) {
				ca = new ShiftActiveAnnotationCA(viewerManager);
			}
			else if (caName == SHIFT_ANNOS_IN_SELECTION) {
				ca = new ShiftAnnotationsInSelectionCA(viewerManager);
			}
			else if (caName == SHIFT_ANNOS_LEFT_OF) {
				ca = new ShiftAnnotationsLeftOfCA(viewerManager);
			}
			else if (caName == SHIFT_ANNOS_RIGHT_OF) {
				ca = new ShiftAnnotationsRightOfCA(viewerManager);
			}
			else if (caName == SHIFT_ALL_ANNOS_LEFT_OF) {
				ca = new ShiftAllAnnotationsLeftOfCA(viewerManager);
			}
			else if (caName == SHIFT_ALL_ANNOS_RIGHT_OF) {
				ca = new ShiftAllAnnotationsRightOfCA(viewerManager);
			}
			else if (caName == TOKENIZE_DLG) {
				ca = new TokenizeDlgCA(viewerManager);
			}
			else if (caName == REGULAR_ANNOTATION_DLG) {
				ca = new RegularAnnotationDlgCA(viewerManager);
			}
			else if (caName == REMOVE_ANNOTATIONS_OR_VALUES) {
				ca = new RemoveAnnotationsOrValuesCA(viewerManager);
			}
			else if (caName == ANN_FROM_OVERLAP) {
				ca = new AnnotationsFromOverlapsDlgCA(viewerManager);
			}
			else if (caName == ANN_FROM_SUBTRACTION) {
				ca = new AnnotationsFromSubtractionDlgCA(viewerManager);
			}
			//temp
			else if (caName == ANN_FROM_OVERLAP_CLAS) {
				ca = new AnnotationsFromOverlapsClasDlgCA(viewerManager);
			}
			else if (caName == MERGE_TIERS) {
				ca = new MergeTiersDlgCA(viewerManager);
			}
			else if (caName == MERGE_TIERS_CLAS) {
				ca = new MergeTiersClasDlgCA(viewerManager);
			}
			else if (caName == MERGE_TIER_GROUP) {
				ca = new MergeTierGroupDlgCA(viewerManager);
			}
			else if (caName == ANN_ON_DEPENDENT_TIER) {
				ca = new CreateAnnsOnDependentTiersDlgCA(viewerManager);
			}
			else if (caName == ANN_FROM_GAPS) {
				ca = new AnnotationsFromGapsDlgCA(viewerManager);
			}
			else if (caName == ANNOTATOR_COMPARE_MULTI) {
				ca = new CompareAnnotatorsDlgCA(viewerManager);
			}
			else if (caName.equals(SHOW_TIMELINE)) {
				ca = new ShowTimelineCA(viewerManager, layoutManager);
			}
			else if (caName.equals(SHOW_INTERLINEAR)) {
				ca = new ShowInterlinearCA(viewerManager, layoutManager);
			}	
			else if (caName.equals(SEARCH_DLG)) {
				ca = new SearchDialogCA(viewerManager);
			}		    
			else if (caName.equals(GOTO_DLG)) {
				ca = new GoToDialogCA(viewerManager);
			}
			else if (caName.equals(TIER_DEPENDENCIES)) {
				ca = new TierDependenciesCA(viewerManager);
			}
			else if (caName.equals(SPREADSHEET)) {
				ca = new SpreadSheetCA(viewerManager);
			}
			else if (caName.equals(STATISTICS)) {
				ca = new StatisticsCA(viewerManager);
			}
			else if (caName.equals(SYNC_MODE)) {
				//ca = new SyncModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, SYNC_MODE);
			}
			else if (caName.equals(ANNOTATION_MODE)) {
				//ca = new AnnotationModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, ANNOTATION_MODE);
			}
			else if (caName.equals(TRANSCRIPTION_MODE)) {
				//ca = new TranscriptionModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, TRANSCRIPTION_MODE);
			}
			else if (caName.equals(SEGMENTATION_MODE)) {
				//ca = new SegmentationModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, SEGMENTATION_MODE);
			}
			else if (caName.equals(INTERLINEARIZATION_MODE)) {
				ca = new ChangeModeCA(viewerManager, layoutManager, INTERLINEARIZATION_MODE);
			}
			else if (caName.equals(SELECTION_MODE)) {
				ca = new SelectionModeCA(viewerManager);
			}
			else if (caName.equals(LOOP_MODE)) {
				ca = new LoopModeCA(viewerManager);
			}
			else if (caName.equals(BULLDOZER_MODE)) {
				ca = new BulldozerModeCA(viewerManager);
			}
			else if (caName.equals(TIMEPROP_NORMAL)) {
				ca = new NormalTimePropCA(viewerManager);
			}
			else if (caName.equals(SHIFT_MODE)) {
				ca = new ShiftModeCA(viewerManager);
			}
			else if (caName.equals(SET_PAL)) {
				ca = new SetPALCA(viewerManager);
			}
			else if (caName.equals(SET_PAL_50)) {
				ca = new SetPAL50CA(viewerManager);
			}
			else if (caName.equals(SET_NTSC)) {
				ca = new SetNTSCCA(viewerManager);
			}
			else if (caName.equals(CLEAR_SELECTION)) {
				ca = new ClearSelectionCA(viewerManager);
			}
			else if (caName.equals(CLEAR_SELECTION_ALT)) {
				ca = new ClearSelectionAltCA(viewerManager);
			}
			else if (caName.equals(CLEAR_SELECTION_AND_MODE)) {
				ca = new ClearSelectionAndModeCA(viewerManager);
			}
			else if (caName.equals(PLAY_SELECTION)) {
				ca = new PlaySelectionCA(viewerManager);
			}
			else if (caName.equals(PLAY_AROUND_SELECTION)) {
				ca = new PlayAroundSelectionCA(viewerManager);
			}
			else if (caName.equals(PLAY_SELECTION_SLOW)) {
				ca = new PlaySelectionSlowCA(viewerManager, (float) 0.5);
			}
			else if (caName.equals(PLAY_SELECTION_NORMAL_SPEED)) {
				ca = new PlaySelectionSlowCA(viewerManager, (float) 1.0);
			}
			else if (caName.equals(NEXT_FRAME)) {
				ca = new NextFrameCA(viewerManager);
			}
			else if (caName.equals(PREVIOUS_FRAME)) {
				ca = new PreviousFrameCA(viewerManager);
			}
			else if (caName.equals(PLAY_PAUSE)) {
				ca = new PlayPauseCA(viewerManager);
			}
			else if (caName.equals(GO_TO_BEGIN)) {
				ca = new GoToBeginCA(viewerManager);
			}
			else if (caName.equals(GO_TO_END)) {
				ca = new GoToEndCA(viewerManager);
			}
			else if (caName.equals(PREVIOUS_SCROLLVIEW)) {
				ca = new PreviousScrollViewCA(viewerManager);
			}
			else if (caName.equals(NEXT_SCROLLVIEW)) {
				ca = new NextScrollViewCA(viewerManager);
			}
			else if (caName.equals(PIXEL_LEFT)) {
				ca = new PixelLeftCA(viewerManager);
			}
			else if (caName.equals(PIXEL_RIGHT)) {
				ca = new PixelRightCA(viewerManager);
			}
			else if (caName.equals(SECOND_LEFT)) {
				ca = new SecondLeftCA(viewerManager);
			}
			else if (caName.equals(SECOND_RIGHT)) {
				ca = new SecondRightCA(viewerManager);
			}
			else if (caName.equals(SELECTION_BOUNDARY)) {
				ca = new ActiveSelectionBoundaryCA(viewerManager);
			}
			else if (caName.equals(SELECTION_CENTER)) {
				ca = new ActiveSelectionCenterCA(viewerManager);
			}
			else if (caName.equals(SELECTION_BEGIN)) {
				ca = new ActiveSelectionBeginCA(viewerManager);
			}
			else if (caName.equals(SELECTION_END)) {
				ca = new ActiveSelectionEndCA(viewerManager);
			}
			else if (caName.equals(SELECTION_BOUNDARY_ALT)) {
				ca = new ActiveSelectionBoundaryAltCA(viewerManager);
			}
			else if (caName.equals(PREVIOUS_ANNOTATION)) {
				ca = new PreviousAnnotationCA(viewerManager);
			}
			else if (caName.equals(PREVIOUS_ANNOTATION_EDIT)) {
				ca = new PreviousAnnotationEditCA(viewerManager);
			}
			else if (caName.equals(NEXT_ANNOTATION)) {
				ca = new NextAnnotationCA(viewerManager);
			}
			else if (caName.equals(NEXT_ANNOTATION_EDIT)) {
				ca = new NextAnnotationEditCA(viewerManager);
			}
			else if (caName.equals(ACTIVE_ANNOTATION_EDIT)) {
				ca = new ActiveAnnotationEditCA(viewerManager);
			}
			else if (caName.equals(ANNOTATION_UP)) {
				ca = new AnnotationUpCA(viewerManager);
			}
			else if (caName.equals(ANNOTATION_DOWN)) {
				ca = new AnnotationDownCA(viewerManager);
			}
			else if (caName.equals(SAVE)) {
				ca = new SaveCA(viewerManager);
			}
			else if (caName.equals(SAVE_AS)) {
				ca = new SaveAsCA(viewerManager);
			}
			else if (caName.equals(EXPORT_EAF_2_7)) {
				ca = new SaveAs2_7CA(viewerManager);
			}
			else if (caName.equals(SAVE_AS_TEMPLATE)) {
				ca = new SaveAsTemplateCA(viewerManager);
			}
			else if (caName.equals(SAVE_SELECTION_AS_EAF)) {
				ca = new SaveSelectionAsEafCA(viewerManager);
			}
			else if (caName.equals(BACKUP)) {
				ca = new BackupCA(viewerManager);
			}
			else if (caName.equals(BACKUP_NEVER)) {
				ca = new BackupNeverCA(viewerManager);
			}
			else if (caName.equals(BACKUP_1)) {
				ca = new Backup1CA(viewerManager);
			}
			else if (caName.equals(BACKUP_5)) {
				ca = new Backup5CA(viewerManager);
			}
			else if (caName.equals(BACKUP_10)) {
				ca = new Backup10CA(viewerManager);
			}
			else if (caName.equals(BACKUP_20)) {
				ca = new Backup20CA(viewerManager);
			}
			else if (caName.equals(BACKUP_30)) {
				ca = new Backup30CA(viewerManager);
			}
			else if (caName.equals(PRINT)) {
				ca = new PrintCA(viewerManager);
			}
			else if (caName.equals(PREVIEW)) {
				ca = new PrintPreviewCA(viewerManager);
			}
			else if (caName.equals(PAGESETUP)) {
				ca = new PageSetupCA(viewerManager);
			}
			else if (caName.equals(EXPORT_TAB)) {
				ca = new ExportTabDelDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_TEX)) {
				ca = new ExportTeXDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_TIGER)) {
				ca = new ExportTigerDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_QT_SUB)) {
				ca = new ExportQtSubCA(viewerManager);
			}
			else if (caName.equals(EXPORT_SMIL_RT)) {
				ca = new ExportSmilCA(viewerManager);
			}
			else if(caName.equals(EXPORT_SMIL_QT)){
				ca = new ExportSmilQTCA(viewerManager);
			}
			else if (caName.equals(EXPORT_SHOEBOX)) {
				ca = new ExportShoeboxCA(viewerManager);
			}
			else if (caName.equals(EXPORT_CHAT)) {
				ca = new ExportCHATCA(viewerManager);
			}
			else if (caName.equals(EXPORT_MEDIA)) {
				ca = new ExportMediaCA(viewerManager);
			}
			else if (caName.equals(EXPORT_IMAGE_FROM_WINDOW)) {
				ca = new ExportImageFromWindowCA(viewerManager);
			}
			else if (caName.equals(LINKED_FILES_DLG)) {
				ca = new LinkedFilesDlgCA(viewerManager);
			}
			else if (caName.equals(PLAYBACK_RATE_TOGGLE)) {
				ca = new PlaybackRateToggleCA(viewerManager);
			}
			else if (caName.equals(PLAYBACK_VOLUME_TOGGLE)) {
				ca = new PlaybackVolumeToggleCA(viewerManager);
			}		
			else if (caName.equals(FILTER_TIER_DLG)) {
				ca = new FilterTierDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_TRAD_TRANSCRIPT)) {
				ca = new ExportTradTranscriptDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_INTERLINEAR)) {
				ca = new ExportInterlinearDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_HTML)) {
				ca = new ExportHTMLDlgCA(viewerManager);
			}
			else if (caName.equals(REPARENT_TIER)) {
				ca = new ReparentTierDlgCA(viewerManager);
			}
			else if (caName.equals(COPY_CURRENT_TIME)) {
				ca = new CopyCurrentTimeToPasteBoardCA(viewerManager);
			}
			else if (caName.equals(COPY_TIER_DLG)) {
				ca = new CopyTierDlgCA(viewerManager);
			}
			else if (caName.equals(NEXT_ACTIVE_TIER)) {
				ca = new NextActiveTierCA(viewerManager);
			}
			else if (caName.equals(PREVIOUS_ACTIVE_TIER)) {
				ca = new PreviousActiveTierCA(viewerManager);
			}
			else if (caName.equals(MERGE_TRANSCRIPTIONS)) {
				ca = new MergeTranscriptionDlgCA(viewerManager);
			}	
			else if (caName.equals(SYNTAX_VIEWER)) {
				if (SyntaxViewerCommand.isEnabled()) {
					ca = new SyntaxViewerCA(viewerManager);
				}				
			}
			else if (caName.equals(CLOSE)) {
				ca = new CloseCA(viewerManager);
			}
			else if (caName.equals(KIOSK_MODE)) {
				ca = new KioskModeCA(viewerManager);
			}
			else if (caName.equals(IMPORT_PRAAT_GRID)) {
				ca = new ImportPraatGridCA(viewerManager);
			}
			else if (caName.equals(EXPORT_PRAAT_GRID)) {
				ca = new ExportPraatGridCA(viewerManager);
			}
			else if (caName.equals(LABEL_AND_NUMBER)) {
				ca = new LabelAndNumberCA(viewerManager);
			}
			else if (caName.equals(KEY_CREATE_ANNOTATION)) {
				ca = new KeyCreateAnnotationCA(viewerManager);
			}
			else if (caName.equals(EXPORT_WORDS)) {
				ca = new ExportWordsDialogCA(viewerManager);
			}
			else if (caName.equals(IMPORT_PREFS)) {
				ca = new ImportPrefsCA(viewerManager);
			}
			else if (caName.equals(EXPORT_PREFS)) {
				ca = new ExportPrefsCA(viewerManager);
			}
			else if (caName.equals(EXPORT_TOOLBOX)) {
				ca = new ExportToolboxDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_FLEX)) {
				ca = new ExportFlexDlgCA(viewerManager);
			}
			else if (caName.equals(EXPORT_SUBTITLES)) {
				ca = new ExportSubtitlesCA(viewerManager);
			}
			else if (caName.equals(EXPORT_FILMSTRIP)) {
				ca = new ExportFilmStripCA(viewerManager);
			}
			else if (caName.equals(CENTER_SELECTION)) {
				ca = new CenterSelectionCA(viewerManager);
			}
			else if (caName.equals(SET_AUTHOR)) {
				ca = new SetAuthorCA(viewerManager);
			}
			else if (caName.equals(CHANGE_CASE)) {
				ca = new ChangeCaseDlgCA(viewerManager);
			}
			else if (caName.equals(CLIP_MEDIA)) {
				ca = new ClipMediaCA(viewerManager);
			}
			else if (caName.equals(EXPORT_RECOG_TIER)) {
				ca = new ExportTiersForRecognizerCA(viewerManager);
			}
			else if (caName.equals(IMPORT_RECOG_TIERS)) {
				ca = new ImportRecogTiersCA(viewerManager);
			}
			// For opening a Edit Lexicon Service Dialog:
			else if (caName.equals(EDIT_LEX_SRVC_DLG)) {
				ca = new EditLexSrvcDlgCA(viewerManager);
			}
			else if (caName.equals(MOVE_ANNOTATION_LBOUNDARY_LEFT)) {
				ca = new MoveActiveAnnLBoundarytoLeftCA(viewerManager);
			}
			else if (caName.equals(MOVE_ANNOTATION_LBOUNDARY_RIGHT)) {
				ca = new MoveActiveAnnLBoundarytoRightCA(viewerManager);
			}
			else if (caName.equals(MOVE_ANNOTATION_RBOUNDARY_LEFT)) {
				ca = new MoveActiveAnnRBoundarytoLeftCA(viewerManager);
			}
			else if (caName.equals(MOVE_ANNOTATION_RBOUNDARY_RIGHT)) {
				ca = new MoveActiveAnnRBoundarytoRightCA(viewerManager);
			}
			else if (caName.equals(PLAY_STEP_AND_REPEAT)) {
				ca = new PlayStepAndRepeatCA(viewerManager);
			}
			else if (caName.equals(WEBLICHT_DLG)) {
				ca = new WebLichtDlgCA(viewerManager);
			}
			else if (caName.equals(TYPECRAFT_DLG)) {
				ca = new TypeCraftDlgCA(viewerManager);
			}
			else if (caName.equals(ANNOTATIONS_TO_TIERS)) {
				ca = new AnnotationValuesToTiersDlgCA(viewerManager);
			}
			else if (caName.equals(ADD_COMMENT)) {
				ca = new AddCommentCA(viewerManager);
			}
			else if (caName.equals(ZOOM_IN)) {
				ca = new ZoomInCA(viewerManager);
			}
			else if (caName.equals(ZOOM_OUT)) {
				ca = new ZoomOutCA(viewerManager);
			}
			else if (caName.equals(ZOOM_DEFAULT)) {
				ca = new ZoomToDefaultCA(viewerManager);
			}
			else if (caName.equals(CYCLE_TIER_SETS)) {
				ca = new CycleTierSetsCA(viewerManager);
			}
			else if (caName.equals(EXPORT_REGULAR_MULTITIER_EAF)) {
				ca = new ExportRegularMultitierEafCA(viewerManager);
			}
			else if (caName.equals(COPY_ANN_OF_TIER)) {
				ca = new CopyAnnotationsOfTierDlgCA(viewerManager);
			}
			else if (caName.equals(MODIFY_ANNOTATION_TIME_DLG)) {
				ca = new ModifyAnnotationTimeDlgCA(viewerManager);
			}
			
			if (ca != null) {
				synchronized (ELANCommandFactory.class) {
					commandActionHash.get(tr).put(caName, ca);
				}
			}
		}
		return ca;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 * @param cName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static Command createCommand(Transcription tr, String cName) {
		Command c = null;

		if (cName.equals(SET_TIER_NAME)) {
			c = new SetTierNameCommand(cName);
		}

		if (cName.equals(EDIT_TIER)) {
			c = new EditTierDlgCommand(cName);
		}
		else if (cName.equals(CHANGE_TIER)) {
			c = new ChangeTierAttributesCommand(cName);
		}
		else if (cName.equals(ADD_TIER)) {
			c = new AddTierCommand(cName);
		}
		else if (cName.equals(DELETE_TIER)) {
			c = new DeleteTierCommand(cName);
		}
		else if (cName.equals(DELETE_TIERS)) {
	        c = new DeleteTiersCommand(cName);
	    } 
		else if (cName.equals(ADD_PARTICIPANT)) {
			c = new AddParticipantCommand(cName);
		}		
		else if (cName.equals(ADD_PARTICIPANT_DLG)) {
			c = new AddParticipantDlgCommand(cName);
		}
		else if (cName.equals(IMPORT_TIERS)) {
			c = new ImportTiersCommand(cName);
		}
		else if (cName.equals(EDIT_TYPE)) {
			c = new EditLingTypeDlgCommand(cName);
		}
		else if (cName.equals(ADD_TYPE)) {
			c = new AddTypeCommand(cName);
		}
		else if (cName.equals(CHANGE_TYPE)) {
			c = new ChangeTypeCommand(cName);
		}
		else if (cName.equals(DELETE_TYPE)) {
			c = new DeleteTypeCommand(cName);
		}
		else if (cName.equals(IMPORT_TYPES)) {
			c = new ImportLinguisticTypesCommand(cName);
		}
		else if (cName.equals(EDIT_CV_DLG)) {
			c = new EditCVDlgCommand(cName);
		}
		else if (cName.equals(ADD_CV)) {
			c = new AddCVCommand(cName);
		}
		else if (cName.equals(CHANGE_CV)) {
			c = new ChangeCVCommand(cName);
		}
		else if (cName.equals(DELETE_CV)) {
			c = new DeleteCVCommand(cName);
		}
		else if (cName.equals(REPLACE_CV)) {
			c = new ReplaceCVCommand(cName);
		}
		else if (cName.equals(ADD_CV_ENTRY)) {
			c = new AddCVEntryCommand(cName);
		}
		else if (cName.equals(CHANGE_CV_ENTRY)) {
			c = new ChangeCVEntryCommand(cName);
		}
		else if (cName.equals(DELETE_CV_ENTRY)) {
			c = new DeleteCVEntryCommand(cName);
		}
		else if (cName.equals(MOVE_CV_ENTRIES)) {
			c = new MoveCVEntriesCommand(cName);
		}
		else if (cName.equals(REPLACE_CV_ENTRIES)) {
			c = new ReplaceCVEntriesCommand(cName);
		}
		else if (cName.equals(MERGE_CVS)) {
			c = new MergeCVSCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION)) {
			c = new NewAnnotationCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION_REC)) {
			c = new NewAnnotationRecursiveCommand(cName);
		}
		else if (cName.equals(CREATE_DEPEND_ANN)) {
			c = new CreateDependentAnnotationsCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION_BEFORE)) {
			c = new AnnotationBeforeCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION_AFTER)) {
			c = new AnnotationAfterCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATIONS_IN_GAP)) {
			c = new NewAnnotationsInGap(cName);
		}
		else if (cName.equals(DUPLICATE_ANNOTATION)) {
			c = new DuplicateAnnotationCommand(cName);
		}
		else if (cName.equals(MERGE_ANNOTATION_WN)) {
			c = new MergeAnnotationsCommand(cName);
		} 
		else if (cName.equals(MERGE_ANNOTATION_WB)) {
			c = new MergeAnnotationsCommand(cName);
		}
		else if (cName.equals(COPY_TO_NEXT_ANNOTATION)) {
            c = new CopyPreviousAnnotationCommand(cName);   
        }
		else if (cName.equals(COPY_CURRENT_TIME)) {
			c = new CopyCurrentTimeToPasteBoardCommand(cName);
		}
		else if (cName.equals(COPY_ANNOTATION)) {
			c = new CopyAnnotationCommand(cName);
		}
		else if (cName.equals(COPY_ANNOTATION_TREE)) {
			c = new CopyAnnotationTreeCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION)) {
			c = new PasteAnnotationCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION_HERE)) {
			c = new PasteAnnotationCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION_TREE)) {
			c = new PasteAnnotationTreeCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION_TREE_HERE)) {
			c = new PasteAnnotationTreeCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_DLG)) {
			c = new ModifyAnnotationDlgCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION)) {
			c = new ModifyAnnotationCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_DC_DLG)) {
			c = new ModifyAnnotationDatCatDlgCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_DC)) {
			c = new ModifyAnnotationDatCatCommand(cName);
		}
		else if (cName.equals(SHOW_IN_BROWSER)) {
			c = new ShowInBrowserCommand(cName);
		}
		else if (cName.equals(SPLIT_ANNOTATION)) {
			c = new SplitAnnotationCommand(cName);
		}
		else if (cName.equals(REMOVE_ANNOTATION_VALUE)) {
	        c = new RemoveAnnotationValueCommand(cName);
	    }
		else if (cName.equals(DELETE_ANNOTATION)) {
			c = new DeleteAnnotationCommand(cName);
		}
		else if (cName.equals(DELETE_ANNOS_IN_SELECTION)) {
			c = new DeleteAnnotationsCommand(cName);
		}
		else if (cName.equals(DELETE_MULTIPLE_ANNOS)) {
			c = new DeleteSelectedAnnotationsCommand(cName);
		}		
		else if (cName.equals(MODIFY_ANNOTATION_TIME)) {
			c = new ModifyAnnotationTimeCommand(cName);
		}
		else if (cName == MOVE_ANNOTATION_TO_TIER) {
			c = new MoveAnnotationToTierCommand(cName);
		}
		else if (cName == SHIFT_ALL_ANNOTATIONS) {
			c = new ShiftAllAnnotationsCommand(cName);
		}
		else if (cName == SHIFT_ANNOTATIONS) {
			c = new ShiftAnnotationsCommand(cName);
		}
		else if (cName == SHIFT_ALL_ANNOTATIONS_LROf) {
			c = new ShiftAnnotationsLROfCommand(cName);
		}
		else if (cName == SHIFT_ALL_DLG) {
			c = new ShiftAllAnnotationsDlgCommand(cName);
		}
		else if (cName == SHIFT_ANN_DLG) {
			c = new ShiftAnnotationsDlgCommand(cName);
		}
		else if (cName == SHIFT_ANN_ALLTIER_DLG) {
			c = new ShiftAnnotationsLROfDlgCommand(cName);
		}
		else if (cName == TOKENIZE_DLG) {
			c = new TokenizeDlgCommand(cName);
		}
		else if (cName == REGULAR_ANNOTATION_DLG) {
	        c = new RegularAnnotationDlgCommand(cName);
	    } 
		else if (cName == REMOVE_ANNOTATIONS_OR_VALUES) {
	        c = new RemoveAnnotationsOrValuesCommand(cName);
	    } 
		else if (cName == REGULAR_ANNOTATION) {
	        c = new RegularAnnotationCommand(cName);
	    }
		else if (cName == TOKENIZE_TIER) {
			c = new TokenizeCommand(cName);
		}
		else if (cName == ANN_FROM_OVERLAP ) {
			c = new AnnotationsFromOverlapsUndoableCommand(cName);
		}
		else if (cName == ANN_FROM_SUBTRACTION) {
			c = new AnnotationsFromSubtractionUndoableCommand(cName);
		}
		else if (cName == ANN_FROM_OVERLAP_COM ) {
			c = new AnnotationsFromOverlapsDlgCommand(cName);
		}
		else if (cName == ANN_FROM_SUBTRACTION_COM) {
			c = new AnnotationsFromOverlapsDlgCommand(cName, true);
		}
		// temp
		else if (cName == ANN_FROM_OVERLAP_CLAS) {
			c = new AnnotationsFromOverlapsClasCommand(cName);
		}
		else if (cName == ANN_FROM_OVERLAP_COM_CLAS) {
			c = new AnnotationsFromOverlapsClasDlgCommand(cName);
		}
		else if (cName == MERGE_TIERS_COM ) {
			c = new MergeTiersDlgCommand(cName);
		}
		else if (cName == MERGE_TIERS ) {
			c = new MergeTiersUndoableCommand(cName);
		}
		// temp
		else if (cName == MERGE_TIERS_DLG_CLAS) {
			c = new MergeTiersClasDlgCommand(cName);
		}
		else if (cName == MERGE_TIERS_CLAS) {
			c = new MergeTiersClasCommand(cName);
		}
		else if (cName == MERGE_TIER_GROUP_DLG) {
			c = new MergeTierGroupDlgCommand(cName);
		}
		else if (cName == MERGE_TIER_GROUP) {
			c = new MergeTierGroupCommand(cName);
		}
		else if (cName == ANN_ON_DEPENDENT_TIER) {
			c = new CreateAnnsOnDependentTiersCommand(cName);
		}
		else if (cName == ANN_ON_DEPENDENT_TIER_COM) {
			c = new CreateAnnsOnDependentTiersDlgCommand(cName);
		}
		else if (cName == ANN_FROM_GAPS) {
			c = new AnnotationsFromGapsCommand(cName);
		}
		else if (cName == ANN_FROM_GAPS_COM) {
			c = new AnnotationsFromGapsDlgCommand(cName);
		}
		else if (cName == ANNOTATOR_COMPARE_MULTI) {
			c = new CompareAnnotatorsDlgCommand(cName);
		}
		else if (cName.equals(SHOW_MULTITIER_VIEWER)) {
			c = new ShowMultitierViewerCommand(cName);
		}
		else if (cName.equals(SEARCH_DLG)) {
			c = new SearchDialogCommand(cName);
		}
		else if(cName.equals(REPLACE)){
		    c = new ReplaceCommand(cName);
		}
		else if (cName.equals(GOTO_DLG)) {
			c = new GoToDialogCommand(cName);
		}
		else if (cName.equals(TIER_DEPENDENCIES)) {
			c = new TierDependenciesCommand(cName);
		}
		else if (cName.equals(SPREADSHEET)) {
	         c = new SpreadSheetCommand(cName);
	    } 
		else if (cName.equals(STATISTICS)) {
	         c = new StatisticsCommand(cName);
	    } 
		else if (cName.equals(SYNC_MODE) ||
				cName.equals(ANNOTATION_MODE) ||
				cName.equals(TRANSCRIPTION_MODE)||
				cName.equals(SEGMENTATION_MODE) ||
				cName.equals(INTERLINEARIZATION_MODE)) {
			c = new ChangeModeCommand(cName);
		}
		else if (cName.equals(SELECTION_MODE)) {
			c = new SelectionModeCommand(cName);
		}
		else if (cName.equals(LOOP_MODE)) {
			c = new LoopModeCommand(cName);
		}
		else if (cName.equals(BULLDOZER_MODE)) {
			c = new BulldozerModeCommand(cName);
		}
		else if (cName.equals(TIMEPROP_NORMAL)) {
			c = new NormalTimePropCommand(cName);
		}
		else if (cName.equals(SHIFT_MODE)) {
			c = new ShiftModeCommand(cName);
		}
		else if (cName.equals(SET_PAL)) {
			c = new SetMsPerFrameCommand(cName);
		}
		else if (cName.equals(SET_PAL_50)) {
			c = new SetMsPerFrameCommand(cName);
		}
		else if (cName.equals(SET_NTSC)) {
			c = new SetMsPerFrameCommand(cName);
		}
		else if (cName.equals(CLEAR_SELECTION)) {
			c = new ClearSelectionCommand(cName);
		}
		else if (cName.equals(CLEAR_SELECTION_AND_MODE)) {
			c = new ClearSelectionAndModeCommand(cName);
		}
		else if (cName.equals(PLAY_SELECTION)) {
			c = new PlaySelectionCommand(cName);
		}
		else if (cName.equals(NEXT_FRAME)) {
			c = new NextFrameCommand(cName);
		}
		else if (cName.equals(PREVIOUS_FRAME)) {
			c = new PreviousFrameCommand(cName);
		}
		else if (cName.equals(PLAY_PAUSE)) {
			c = new PlayPauseCommand(cName);
		}
		else if (cName.equals(GO_TO_BEGIN)) {
			c = new GoToBeginCommand(cName);
		}
		else if (cName.equals(GO_TO_END)) {
			c = new GoToEndCommand(cName);
		}
		else if (cName.equals(PREVIOUS_SCROLLVIEW)) {
			c = new PreviousScrollViewCommand(cName);
		}
		else if (cName.equals(NEXT_SCROLLVIEW)) {
			c = new NextScrollViewCommand(cName);
		}
		else if (cName.equals(PIXEL_LEFT)) {
			c = new PixelLeftCommand(cName);
		}
		else if (cName.equals(PIXEL_RIGHT)) {
			c = new PixelRightCommand(cName);
		}
		else if (cName.equals(SECOND_LEFT)) {
			c = new SecondLeftCommand(cName);
		}
		else if (cName.equals(SECOND_RIGHT)) {
			c = new SecondRightCommand(cName);
		}
		else if (cName.equals(SELECTION_BOUNDARY)) {
			c = new ActiveSelectionBoundaryCommand(cName);
		} 
		else if (cName.equals(SELECTION_CENTER)) {
			c = new ActiveSelectionCenterCommand(cName);
		}
		else if (cName.equals(SELECTION_BEGIN) || cName.equals(SELECTION_END)) {
			c = new ActiveSelectionBeginOrEndCommand(cName);
		}
		else if (cName.equals(ACTIVE_ANNOTATION)) {
			c = new ActiveAnnotationCommand(cName);
		}
		else if (cName.equals(ACTIVE_ANNOTATION_EDIT)) {
			c = new ActiveAnnotationEditCommand(cName);
		}
		else if (cName.equals(STORE)) {
			c = new StoreCommand(cName);
		}
		else if (cName.equals(BACKUP)) {
			c = new SetBackupDelayCommand(cName);
		}
		else if (cName.equals(PRINT)) {
			c = new PrintCommand(cName);
		}
		else if (cName.equals(PREVIEW)) {
			c = new PrintPreviewCommand(cName);
		}
		else if (cName.equals(PAGESETUP)) {
			c = new PageSetupCommand(cName);
		}
		else if (cName.equals(EXPORT_TAB)) {
			c = new ExportTabDelDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_TEX)) {
			c = new ExportTeXDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_TIGER)) {
			c = new ExportTigerDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_SMIL_RT)){
			c = new ExportSmilCommand(cName);
		}
		else if (cName.equals(EXPORT_SMIL_QT)){
			c = new ExportSmilQTCommand(cName);
		}
		else if (cName.equals(EXPORT_QT_SUB)){
			c = new ExportQtSubCommand(cName);
		}		
		else if (cName.equals(EXPORT_MEDIA)){
			c = new ExportMediaCommand(cName);
		}
		else if (cName.equals(EXPORT_IMAGE_FROM_WINDOW)){
			c = new ExportImageFromWindowCommand(cName);
		}			
		else if (cName.equals(EXPORT_SHOEBOX)) {
			c = new ExportShoeboxCommand(cName);
		}
		else if (cName.equals(EXPORT_CHAT)) {
			c = new ExportCHATCommand(cName);
		}
		else if (cName.equals(LINKED_FILES_DLG)) {
			c = new LinkedFilesDlgCommand(cName);
		}		
		else if (cName.equals(CHANGE_LINKED_FILES)) {
			c = new ChangeLinkedFilesCommand(cName);
		}
		else if (cName.equals(PLAYBACK_RATE_TOGGLE)) {
			c = new PlaybackRateToggleCommand(cName);
		}
		else if (cName.equals(PLAYBACK_VOLUME_TOGGLE)) {
			c = new PlaybackVolumeToggleCommand(cName);
		}
		else if (cName.equals(ADD_SEGMENTATION)) {
			c = new AddSegmentationCommand(cName);
		}
		else if (cName.equals(FILTER_TIER_DLG)) {
			c = new FilterTierDlgCommand(cName);
		}
		else if (cName.equals(FILTER_TIER)) {
			c = new FilterTierCommand(cName);
		}
		else if (cName.equals(EXPORT_TRAD_TRANSCRIPT)) {
			c = new ExportTradTranscriptDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_INTERLINEAR)) {
			c = new ExportInterlinearDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_HTML)) {
			c = new ExportHTMLDlgCommand(cName);
		}
		else if (cName.equals(REPARENT_TIER_DLG)) {
			c = new ReparentTierDlgCommand(cName);
		}
		else if (cName.equals(COPY_TIER_DLG)) {
			c = new CopyTierDlgCommand(cName);
		}
		else if (cName.equals(COPY_TIER) || cName.equals(REPARENT_TIER)) {
			c = new CopyTierCommand(cName);
		}
		else if (cName.equals(SAVE_SELECTION_AS_EAF)) {
			c = new SaveSelectionAsEafCommand(cName);
		}
		else if (cName.equals(ACTIVE_TIER)) {
			c = new ActiveTierCommand(cName);
		}
		else if (cName.equals(MERGE_TRANSCRIPTIONS)) {
			c = new MergeTranscriptionsDlgCommand(cName);
		}
		else if (cName.equals(SYNTAX_VIEWER)){
			c = new SyntaxViewerCommand(cName);
		}
		else if (cName.equals(EXT_TRACK_DATA)) {
			c = new ExtractTrackDataCommand(cName);
		}
		else if (cName.equals(CLOSE)) {
			c = new CloseCommand(cName);
		}
		else if (cName.equals(KIOSK_MODE)) {
			c = new KioskModeCommand(cName);
		}
		else if (cName.equals(IMPORT_PRAAT_GRID)) {
			c = new ImportPraatGridCommand(cName);
		}
		else if (cName.equals(IMPORT_PRAAT_GRID_DLG)) {
			c = new ImportPraatGridDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_PRAAT_GRID)) {
			c = new ExportPraatGridCommand(cName);
		}
		else if (cName.equals(LABEL_N_NUM_DLG)) {
			c = new LabelAndNumberDlgCommand(cName);
		}
		else if (cName.equals(REMOVE_ANNOTATIONS_OR_VALUES_DLG)) {
			c = new RemoveAnnotationsOrValuesDlgCommand(cName);
		}
		else if (cName.equals(LABEL_AND_NUMBER)) {
			c = new LabelAndNumberCommand(cName);
		}
		else if (cName.equals(EXPORT_WORDS)) {
			c = new ExportWordsDialogCommand(cName);
		}
		else if (cName.equals(IMPORT_PREFS)) {
			c = new ImportPrefsCommand(cName);
		}
		else if (cName.equals(EXPORT_PREFS)) {
			c = new ExportPrefsCommand(cName);
		}
		else if (cName.equals(EXPORT_TOOLBOX)) {
			c = new ExportToolboxDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_FLEX)) {
			c = new ExportFlexDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_SUBTITLES)) {
			c = new ExportSubtitlesCommand(cName);
		}
		else if (cName.equals(EXPORT_FILMSTRIP)) {
			c = new ExportFilmStripDlgCommand(cName);
		}
		else if (cName.equals(SEGMENTS_2_TIER_DLG)) {
			c = new SegmentsToTiersDlgCommand(cName);
		}
		else if (cName.equals(SEGMENTS_2_TIER)) {
			c = new SegmentsToTiersCommand(cName);
		}
		else if (cName.equals(CENTER_SELECTION)) {
			c = new CenterSelectionCommand(cName);
		}
		else if (cName.equals(SET_AUTHOR)) {
			c = new SetAuthorCommand(cName);
		}
		else if (cName.equals(CHANGE_CASE)) {
			c = new ChangeCaseCommand(cName);
		}
		else if (cName.equals(CHANGE_CASE_COM)) {
			c = new ChangeCaseDlgCommand(cName);
		}
		else if (cName.equals(CLIP_MEDIA)) {
			c = new ClipMediaCommand(cName);
		}
		else if (cName.equals(EXPORT_RECOG_TIER)) {
			c = new ExportTiersForRecognizerCommand(cName);
		}
		else if (cName.equals(ADD_TRACK_AND_PANEL)) {
			c = new AddTSTrackAndPanelCommand(cName);
		}
		else if (cName.equals(IMPORT_RECOG_TIERS)) {
			c = new ImportRecogTiersCommand(cName);
		}
		// For Lexicon Service editing:
		else if (cName.equals(EDIT_LEX_SRVC_DLG)) {
			c = new EditLexSrvcDlgCommand(cName);
		} 
		else if (cName.equals(ADD_LEX_LINK)) {
			c = new AddLexLinkCommand(cName);
		} 
		else if (cName.equals(CHANGE_LEX_LINK)) {
			c = new ChangeLexLinkCommand(cName);
		} 
		else if (cName.equals(DELETE_LEX_LINK)) {
			c = new DeleteLexLinkCommand(cName);
		}
		else if (cName.equals(PLAY_STEP_AND_REPEAT)) {
			c = new PlayStepAndRepeatCommand(cName);
		}
		else if (cName.equals(WEBSERVICES_DLG)) {
			c = new WebServicesDlgCommand(cName);
		}
		else if (cName.equals(MERGE_TRANSCRIPTIONS_UNDOABLE)) {
			c = new MergeTranscriptionsByAddingCommand(cName);
		}
		else if (cName.equals(WEBLICHT_MERGE_TRANSCRIPTIONS)) {
			c = new MergeTranscriptionsByAddingCommand(cName);
		}
		else if (cName.equals(ANNOTATIONS_TO_TIERS_DLG)) {
			c = new AnnotationValuesToTiersDlgCommand(cName);
		}
		else if (cName.equals(ANNOTATIONS_TO_TIERS)) {
			c = new AnnotationValuesToTiersCommand(cName);
		}
		else if (cName.equals(ADD_COMMENT)) {
			c = new AddCommentCommand(cName);
		}
		else if (cName.equals(DELETE_COMMENT)) {
			c = new DeleteCommentCommand(cName);
		}
		else if (cName.equals(CHANGE_COMMENT)) {
			c = new ChangeCommentCommand(cName);
		}
		else if (cName.equals(MODIFY_OR_ADD_DEPENDENT_ANNOTATIONS)) {
			c = new ModifyOrAddDependentAnnotationsCommand(cName);
		}
		else if (cName.equals(ZOOM_IN) || cName.equals(ZOOM_OUT) || cName.equals(ZOOM_DEFAULT)) {
			c = new ZoomCommand(cName);
		}
		else if (cName.equals(CYCLE_TIER_SETS)) {
			c = new CycleTierSetsCommand(cName);
		}
		else if (cName.equals(ANNS_FROM_SUGGESTION_SET)) {
			c = new AnnotationsFromSuggestionSetCommand(cName);
		}
		else if (cName.equals(EXPORT_REGULAR_MULTITIER_EAF)) {
			c = new ExportRegularMultitierEafCommand(cName);
		}
		else if (cName.equals(COPY_ANN_OF_TIER_DLG)) {
			c = new CopyAnnotationsOfTierDlgCommand(cName);
		} 
		else if (cName.equals(COPY_ANN_OF_TIER)) {
			c = new CopyAnnotationsOfTierCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_TIME_DLG)) {
			c = new ModifyAnnotationTimeDlgCommand(cName);
		}
		
		if (c instanceof UndoableCommand) {
			synchronized (commandHistoryHash) {
				commandHistoryHash.get(tr).addCommand((UndoableCommand) c);
			}
		}

		return c;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static synchronized UndoCA getUndoCA(Transcription tr) {
		UndoCA undoCA = undoCAHash.get(tr);

		if (undoCAHash.get(tr) == null) {
			undoCA =
				new UndoCA(
					viewerManagerHash.get(tr),
					commandHistoryHash.get(tr));
			commandHistoryHash.get(tr).setUndoCA(undoCA);

			undoCAHash.put(tr, undoCA);
		}

		return undoCA;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static synchronized RedoCA getRedoCA(Transcription tr) {
		RedoCA redoCA = redoCAHash.get(tr);

		if (redoCA == null) {
			redoCA =
				new RedoCA(
					viewerManagerHash.get(tr),
					commandHistoryHash.get(tr));
			commandHistoryHash.get(tr).setRedoCA(redoCA);

			redoCAHash.put(tr, redoCA);
		}

		return redoCA;
	}
	
	/**
	 * Returns the Locale for the specified key.
	 * 
	 * @param key a CommandAction language key
	 * @return the associated Locale, defaults to English
	 */
	public static Locale getLocaleForKey(Object key) {
		if (key != null) {
			Locale l = languages.get(key);
			if (l != null) {
				return l;
			}
		}
		// default english
		return ElanLocale.ENGLISH;
	}

	/**
	 * Returns a Set view of the registered Locales.
	 * @return a Set view of the registered Locales
	 */
	public static Collection<Locale> getLocales() {
	    return languages.values();
	}
	
	/**
	 * Refinement of the shortcuts table texts with grouping of related actions and with 
	 * a sub-header per group.
	 * The method now returns a 2 dim. array of Objects instead of Strings
	 * <p>
	 * This function is unused, but if it were used, it could be simplified a lot
	 * by using a few separate commandConstants arrays instead of one,
	 * and abstracting the handling loop into a separate function instead
	 * of it being duplicated.
	 * 
	 * @param tr the transcription
	 * @return a 2 dimensional array of Objects
	 */
//	public static Object[][] getShortCutText(Transcription tr) {
//	    ArrayList<Object> shortCuts = new ArrayList<Object>();
//	    ArrayList<Object> descriptions = new ArrayList<Object>();
//		CommandAction ca;
//		KeyStroke acc;
//		String accString;
//		String descString;
//		int index = 0;
//		
//		// start with subheader for the annotation editing group
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.AnnotationEdit")));
//		descriptions.add(new TableSubHeaderObject(null));
//		for (int i = 0; i < 20; i++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}
//		// annotation navigation group
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.AnnotationNavigation")));
//		descriptions.add(new TableSubHeaderObject(null));
//		for (int i = ++index, j = 0; j < 6; i++, j++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}
//		// tier and type
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.TierType")));
//		descriptions.add(new TableSubHeaderObject(null));
//		for (int i = ++index, j = 0; j < 5; i++, j++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}
//		// selection
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Selection")));
//		descriptions.add(new TableSubHeaderObject(null));
//		for (int i = ++index, j = 0; j < 7; i++, j++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}		
//		// media navigation group
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.MediaNavigation")));
//		descriptions.add(new TableSubHeaderObject(null));
//		for (int i = ++index, j = 0; j < 15; i++, j++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}				
//		// document group
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Document")));
//		descriptions.add(new TableSubHeaderObject(null));
//		
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_N, 
//			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		descString = ElanLocale.getString("Menu.File.NewToolTip");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
//			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		descString = ElanLocale.getString("Menu.File.OpenToolTip");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//			
//		for (int i = ++index, j = 0; j < 6; i++, j++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}
//		
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 
//			ActionEvent.SHIFT_MASK));
//		descString = ElanLocale.getString("Menu.Window.NextToolTip");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 
//			ActionEvent.SHIFT_MASK));
//		descString = ElanLocale.getString("Menu.Window.PreviousToolTip");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//			
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_W, 
//			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		descString = ElanLocale.getString("Menu.File.CloseToolTip");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
//			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		descString = ElanLocale.getString("Menu.File.ExitToolTip");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//		// miscellaneous
//		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Misc")));
//		descriptions.add(new TableSubHeaderObject(null));
//		
//		ca = getUndoCA(tr);
//		accString = convertAccKey((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY));
//		descString = ElanLocale.getString("Menu.Edit.Undo");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//
//		ca = getRedoCA(tr);
//		accString = convertAccKey((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY));
//		descString = ElanLocale.getString("Menu.Edit.Redo");
//
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//
//		for (int i = ++index, j = 0; j < 7; i++, j++) {
//			ca = getCommandAction(tr, commandConstants[i]);
//			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
//
//			if (acc != null) {
//				accString = convertAccKey(acc);
//				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
//
//				if (descString == null) {
//					descString = "";
//				}
//
//				if (accString != null) {
//					shortCuts.add(accString);
//					descriptions.add(descString);
//				}
//			}
//			index = i;
//		}
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_H, 
//				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		descString = ElanLocale.getString("Menu.Help.Contents");
//		if (accString != null) {
//			shortCuts.add(accString);
//			descriptions.add(descString);
//		}
//			
//		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_1, 
//				ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK +
//				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		accString = accString.substring(0, accString.length() - 2);
//		shortCuts.add(accString);
//		descriptions.add(ElanLocale.getString("MultiTierViewer.ShiftToolTip"));
//		
//		// create array
//		Object[][] resultTable = new Object[shortCuts.size()][2];
//
//		for (int j = 0; j < shortCuts.size(); j++) {
//			resultTable[j][0] = shortCuts.get(j);
//			resultTable[j][1] = descriptions.get(j);
//		}
//	    return resultTable;
//	}

	//Input is something like: 'Keycode Ctrl+Alt+ShiftB-P'
	//Matching output: 'Ctrl+Alt+Shift+B'
	//
	//The order of Ctrl, Alt and Shift is always like this, regardless of the order
	//when the accelerator was made.
	/**
	 * The String representation has changed in J1.5. Therefore the construction 
	 * of the shortcut (accelerator) text is now based only only the modifiers 
	 * and the KeyCode or KeyChar.
	 */
	public static String convertAccKey(KeyStroke acc) {
		// special case for the Mac
		if (System.getProperty("os.name").startsWith("Mac")) {
			return convertMacAccKey(acc);
		}
		int modifier = acc.getModifiers();
		String nwAcc = "";
		if ((modifier & InputEvent.CTRL_MASK) != 0) {
			nwAcc += "Ctrl+";
		}
		if ((modifier & InputEvent.ALT_MASK) != 0) {
			nwAcc += "Alt+";
		}
		if ((modifier & InputEvent.SHIFT_MASK) != 0) {
			nwAcc += "Shift+";
		}
		if (acc.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			nwAcc += KeyEvent.getKeyText(acc.getKeyCode());
		} else {
			nwAcc += String.valueOf(acc.getKeyChar());
		}
		return nwAcc;
	}
	
	/**
	 * @see #convertAccKey(KeyStroke)
	 * @param acc the KeyStroke
	 * @return a String representation
	 */
	private static String convertMacAccKey(KeyStroke acc) {
		int modifier = acc.getModifiers();
		String nwAcc = "";
		if ((modifier & InputEvent.META_MASK) != 0) {
			nwAcc += "Command+";
		}
		if ((modifier & InputEvent.CTRL_MASK) != 0) {
			nwAcc += "Ctrl+";
		}
		if ((modifier & InputEvent.ALT_MASK) != 0) {
			nwAcc += "Alt+";
		}
		if ((modifier & InputEvent.SHIFT_MASK) != 0) {
			nwAcc += "Shift+";
		}
		if (acc.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			nwAcc += KeyEvent.getKeyText(acc.getKeyCode());
		} else {
			nwAcc += String.valueOf(acc.getKeyChar());
		}
		return nwAcc;
	}
}

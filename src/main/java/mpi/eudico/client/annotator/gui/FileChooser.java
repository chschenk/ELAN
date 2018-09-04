package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.ElanFileFilter;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.SystemReporting;

/**
 * A class that deals with re-occurring steps and tasks when prompting the user
 * to open, save or otherwise select one or more files. This file chooser
 * encapsulates platform specific features and issues and deals with user
 * preferences too.
 */
public class FileChooser {

	public static final String UTF_8 = "UTF-8";

	public static final String UTF_16 = "UTF-16";

	public static final String ISO_LATIN = "ISO-8859-1";

	public static final int OPEN_DIALOG = 0;

	public static final int SAVE_DIALOG = 1;

	/** Display only files */
	public static final int FILES_ONLY = JFileChooser.FILES_ONLY;

	/** Display only directories */
	public static final int DIRECTORIES_ONLY = JFileChooser.DIRECTORIES_ONLY;

	/** Displays both files and directories */
	public static final int FILES_AND_DIRECTORIES = JFileChooser.FILES_AND_DIRECTORIES;

	/** generic multiple file chooser */
	public static final int GENERIC = 0;

	/**
	 * a multiple file chooser that let the user switch between media file filtering
	 * and template file filtering
	 */
	public static final int MEDIA_TEMPLATE = 1;

	/**
	 * a multiple file chooser that let the user chooser a media file
	 */
	public static final int MEDIA = 2;

	/** default encoding options */
	public static String[] encodings = new String[] { UTF_8, UTF_16, ISO_LATIN };

	private static final int ENCODING_DIALOG = 0;

	private static final int CLIPMEDIA_DIALOG = 1;

	private static final int MULTIFILE_DIALOG = 2;

	private static final String OPEN_DIALOG_TITLE = ElanLocale.getString("FileChooser.openTitleText");

	private static final String SAVE_DIALOG_TITLE = ElanLocale.getString("FileChooser.saveTitleText");

	private static Locale locale = null;

	private FileDialog dialog;
	private JFileChooser chooser;

	private Component parent;
	private String prefStringToLoadtheCurrentPath;
	private int mainDialogType = -1;
	private int customizedDialogType = -1;
	private int fileSelectionMode = FILES_ONLY;
	private List<String> extensions;
	private File selectedFile;
	private Object[] selectedFiles; // Can this be File[]? Or are there urls as String in it?
	private String selectedEncoding;
	private String currentDirectory = null;
	private boolean clipMedia = false;
	private boolean acceptAllFilesTypes = false;
	private int multiFileDialogMode = GENERIC;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FileChooser(Component parent) {
		this.parent = parent;
	}

	/**
	 * Creates a file dialog
	 * 
	 * @param title,
	 *            title for the dialog
	 * @param dialogType,
	 *            type of the dialog (FileChooser.OPEN_DIALOG or
	 *            FileChooser.SAVE_DIALOG)
	 * @param aprroveButtonText,
	 *            approve button text value
	 * @param extensionList,
	 *            list of all valid/supported extensions
	 * @param mainFilterExt,
	 *            the main extension / file type of the dialog
	 * @param acceptAllFilesFilter,
	 *            if true, acceptAllFilters added if false not
	 * @param prefStringToLoadtheCurrentPath,
	 *            the key string to read the preference of the last used directory
	 * @param selectionMode,
	 *            should one of these constant s(FileChooser.FILES_ONLY or
	 *            FileChooser.DIRECTORIES_ONLY or
	 *            FileChooser.FILES_AND_DIRECTORIES);
	 * @param selectedFileName,
	 *            selected file name
	 */
	private void createDialog(String title, int dialogType, String aprroveButtonText, List<String[]> extensionList,
			String[] mainFilterExt, boolean allFilesFilter, String prefStringToLoadtheCurrentPath, int selectionMode,
			String selectedFileName) {
		this.prefStringToLoadtheCurrentPath = prefStringToLoadtheCurrentPath;
		mainDialogType = dialogType;
		extensions = new ArrayList<String>();

		acceptAllFilesTypes = allFilesFilter;

		// if no supported file extensions are specified, add the acceptAllFileFilter
		if (extensionList == null && mainFilterExt == null) {
			acceptAllFilesTypes = true;
		}

		if (selectionMode == DIRECTORIES_ONLY || selectionMode == FILES_AND_DIRECTORIES) {
			fileSelectionMode = selectionMode;
		}

		if (fileSelectionMode == DIRECTORIES_ONLY) {
			acceptAllFilesTypes = true;
		}

		if (currentDirectory == null && prefStringToLoadtheCurrentPath != null) {
			currentDirectory = Preferences.getString(prefStringToLoadtheCurrentPath, null);
		}

		// only in case of eaf files (changes in the merge transcription eaf
		// preferences)
		if (currentDirectory == null && mainFilterExt != null && mainFilterExt.equals(FileExtension.EAF_EXT)) {
			currentDirectory = Preferences.getString("LastUsedEAFDir", null);
		}

		if (currentDirectory == null) {
			currentDirectory = System.getProperty("user.dir");
		}

		if (title == null) {
			switch (mainDialogType) {
			case SAVE_DIALOG:
				title = SAVE_DIALOG_TITLE;
				break;
			case OPEN_DIALOG:
				title = OPEN_DIALOG_TITLE;
				break;
			}
		}

		if (mainFilterExt != null) {
			for (int i = 0; i < mainFilterExt.length; i++) {
				if (!extensions.contains(mainFilterExt[i])) {
					extensions.add(mainFilterExt[i]);
				}
			}

			if (extensionList != null && extensionList.contains(mainFilterExt)) {
				extensionList.remove(mainFilterExt);
			}
		}

		if (!acceptAllFilesTypes) {
			for (String ext : FileExtension.MEDIA_EXT) {
				if (extensions.contains(ext)) {
					acceptAllFilesTypes = true;
					break;
				}
			}
		}

		if (extensionList != null) {
			String[] extArray;
			for (int i = 0; i < extensionList.size(); i++) {
				extArray = extensionList.get(i);
				if (extArray != null) {
					for (String ext : extArray) {
						if (!extensions.contains(ext)) {
							extensions.add(ext);
						}
					}
				}
			}
		}

		if (mainDialogType == FileChooser.SAVE_DIALOG && selectedFileName != null) {
			int li = selectedFileName.lastIndexOf(".");
			selectedFileName = selectedFileName.substring(0, li);
		}

		// read preference
		boolean macFileDialog = false;

		if (SystemReporting.isMacOS()) {
			macFileDialog = true;
			Boolean val = Preferences.getBool("UseMacFileDialog", null);
			if (val != null) {
				macFileDialog = val.booleanValue();
			}
		}

		if (macFileDialog) {
			if (parent instanceof Frame) {
				dialog = new FileDialog((Frame) parent);
			} else if (parent instanceof Dialog) {
				dialog = new FileDialog((Dialog) parent);
			} else {
				dialog = new FileDialog((Frame) null);
			}

			dialog.setMode(mainDialogType);

			// add all the extensions to the dialog title
			if (customizedDialogType != FileChooser.MULTIFILE_DIALOG) {
				title = updateTitleWithExt(title, extensions, acceptAllFilesTypes);
			}

			dialog.setTitle(title);

			dialog.setDirectory(currentDirectory);

			if (!acceptAllFilesTypes) {
				dialog.setFilenameFilter(this.new FileNameFilterList(extensions));
			}

			if (selectedFileName != null) {
				dialog.setFile(selectedFileName);
			}

			if (fileSelectionMode == DIRECTORIES_ONLY) {
				System.setProperty("apple.awt.fileDialogForDirectories", Boolean.TRUE.toString());
			} else {
				System.setProperty("apple.awt.fileDialogForDirectories", Boolean.FALSE.toString());
			}
		} else {
			// file chooser for windows and other platforms
			chooser = new JFileChooser();
			setUILanguage();
			SwingUtilities.updateComponentTreeUI(chooser);
			if (title != null) {
				chooser.setDialogTitle(title);
			}
			chooser.setFileSelectionMode(fileSelectionMode);
			chooser.setCurrentDirectory(new File(currentDirectory));
			chooser.setAcceptAllFileFilterUsed(acceptAllFilesTypes);
			chooser.setDialogType(mainDialogType);
			if (selectedFileName != null) {
				chooser.setSelectedFile(new File(currentDirectory, selectedFileName));
			}

			if (acceptAllFilesTypes) {
				chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
			}

			FileFilter mainFilter = null;
			FileFilter ff = null;

			if (mainDialogType == FileChooser.SAVE_DIALOG) {
				if (extensions.size() == 1) {
					if (mainFilterExt != null) {
						mainFilter = ElanFileFilter.createFileFilter(mainFilterExt);
						chooser.addChoosableFileFilter(mainFilter);
					} else if (extensionList != null) {
						mainFilter = ElanFileFilter.createFileFilter(extensionList.get(0));
						chooser.addChoosableFileFilter(mainFilter);
					}
				} else {
					for (int i = 0; i < extensions.size(); i++) {
						if (i == 0) {
							mainFilter = new ElanFileFilter(extensions.get(i));
							chooser.addChoosableFileFilter(mainFilter);
						} else {
							chooser.addChoosableFileFilter(new ElanFileFilter(extensions.get(i)));
						}
					}
				}
			} else {
				if (mainFilterExt != null) {
					mainFilter = ElanFileFilter.createFileFilter(mainFilterExt);
					chooser.addChoosableFileFilter(mainFilter);
				}

				if (extensionList != null) {
					for (int i = 0; i < extensionList.size(); i++) {
						if (i == 0) {
							ff = ElanFileFilter.createFileFilter(extensionList.get(i));
							chooser.addChoosableFileFilter(ff);
						} else {
							chooser.addChoosableFileFilter(ElanFileFilter.createFileFilter(extensionList.get(i)));
						}
					}
				}
			}

			if (mainFilterExt != null) {
				chooser.setFileFilter(mainFilter);
			} else if (acceptAllFilesTypes) {
				chooser.setFileFilter(chooser.getAcceptAllFileFilter());
			} else {
				chooser.setFileFilter(ff);
			}

			if (aprroveButtonText != null) {
				chooser.setApproveButtonText(aprroveButtonText);
			}
		}
	}

	/**
	 * Adds file extensions to a message to be displayed as the title of the dialog.
	 * @param title the original message
	 * @param extensions the file extensions
	 * @param acceptAllFilesTypes if true this indicates that the all files option is active
	 * @return the modified title
	 */
	private String updateTitleWithExt(String title, List<String> extensions, boolean acceptAllFilesTypes) {
		String ext = null;
		for (int i = 0; i < extensions.size(); i++) {
			if (i == 0) {
				ext = extensions.get(i);
			} else {
				ext += extensions.get(i);
			}

			if (i != extensions.size() - 1) {
				ext += ", ";
			}
		}

		if (acceptAllFilesTypes) {
			if (ext != null) {
				ext += ", *";
			} else {
				ext = "*";
			}
		}

		if (ext != null) {
			title += " (" + ext + " )";
		}

		return title;
	}

	/**
	 * Sets the UI Language of the file dialog components 
	 */
	public void setUILanguage() {
		if (locale == null || locale != ElanLocale.getLocale()) {
			locale = ElanLocale.getLocale();
		} else if (locale == ElanLocale.getLocale()) {
			return;
		}

		UIManager.put("FileChooser.acceptAllFileFilterText",
				ElanLocale.getString("FileChooser.acceptAllFileFilterText"));
		UIManager.put("FileChooser.byDateText", ElanLocale.getString("FileChooser.byDateText"));
		UIManager.put("FileChooser.byNameText", ElanLocale.getString("FileChooser.byNameText"));
		UIManager.put("FileChooser.cancelButtonText", ElanLocale.getString("FileChooser.cancelButtonText"));
		UIManager.put("FileChooser.cancelButtonToolTipText",
				ElanLocale.getString("FileChooser.cancelButtonToolTipText"));
		UIManager.put("FileChooser.detailsViewButtonToolTipText",
				ElanLocale.getString("FileChooser.detailsViewButtonToolTipText"));
		UIManager.put("FileChooser.fileNameLabelText", ElanLocale.getString("FileChooser.fileNameLabelText"));
		UIManager.put("FileChooser.filesOfTypeLabelText", ElanLocale.getString("FileChooser.filesOfTypeLabelText"));
		UIManager.put("FileChooser.lookInLabelText", ElanLocale.getString("FileChooser.lookInLabelText"));
		UIManager.put("FileChooser.listViewButtonToolTipText",
				ElanLocale.getString("FileChooser.listViewButtonToolTipText"));

		UIManager.put("FileChooser.newFolderButtonText", ElanLocale.getString("FileChooser.newFolderButtonText"));
		UIManager.put("FileChooser.newFolderButtonToolTipText",
				ElanLocale.getString("FileChooser.newFolderButtonToolTipText"));
		UIManager.put("FileChooser.newFolderToolTipText", ElanLocale.getString("FileChooser.newFolderToolTipText"));
		UIManager.put("FileChooser.openButtonText", ElanLocale.getString("FileChooser.openButtonText"));
		UIManager.put("FileChooser.openButtonToolTipText", ElanLocale.getString("FileChooser.openButtonToolTipText"));
		UIManager.put("FileChooser.openTitleText", ElanLocale.getString("FileChooser.openTitleText"));
		UIManager.put("FileChooser.saveButtonText", ElanLocale.getString("FileChooser.saveButtonText"));
		UIManager.put("FileChooser.saveDialogFileNameLabelText",
				ElanLocale.getString("FileChooser.saveDialogFileNameLabelText"));
		UIManager.put("FileChooser.saveTitleText", ElanLocale.getString("FileChooser.saveTitleText"));
		UIManager.put("FileChooser.upFolderToolTipText", ElanLocale.getString("FileChooser.upFolderToolTipText"));

		// UIManager.put("FileChooser.desktopFolderToolTipText",
		// ElanLocale.getString("FileChooser.desktopFolderToolTipText"));

	}

	/**
	 * Create and show a file dialog
	 * 
	 * 
	 * @param title
	 * @param dialogType
	 * @param mainFilterExt
	 * @param prefStringToLoadtheCurrentPath
	 */
	public void createAndShowFileDialog(String title, int dialogType, String[] mainFilterExt,
			String prefStringToLoadtheCurrentPath) {
		createAndShowFileDialog(title, dialogType, null, mainFilterExt, prefStringToLoadtheCurrentPath, null);
	}

	/**
	 * Create and show a file dialog
	 * 
	 * @param title
	 * @param dialogType
	 * @param extensions
	 * @param mainFilterExt
	 * @param prefStringToLoadtheCurrentPath
	 * @param selectedFileName
	 */
	public void createAndShowFileDialog(String title, int dialogType, List<String[]> extensions, String[] mainFilterExt,
			String prefStringToLoadtheCurrentPath, String selectedFileName) {
		createAndShowFileDialog(title, dialogType, null, extensions, mainFilterExt, acceptAllFilesTypes,
				prefStringToLoadtheCurrentPath, FileChooser.FILES_ONLY, selectedFileName);
	}

	/**
	 * Create and show a file dialog
	 * 
	 * @param title
	 * @param dialogType
	 * @param aprroveButtonText
	 * @param extensions
	 * @param mainFilterExt
	 * @param acceptAllFiles
	 * @param prefStringToLoadtheCurrentPath
	 * @param fileSelectionMode
	 * @param selectedFileName
	 */
	public void createAndShowFileDialog(String title, int dialogType, String aprroveButtonText,
			List<String[]> extensions, String[] mainFilterExt, boolean acceptAllFiles,
			String prefStringToLoadtheCurrentPath, int fileSelectionMode, String selectedFileName) {
		createDialog(title, dialogType, aprroveButtonText, extensions, mainFilterExt, acceptAllFiles,
				prefStringToLoadtheCurrentPath, fileSelectionMode, selectedFileName);
		show();
	}

	/**
	 * Create and show a file encoding dialog
	 * 
	 * @param title
	 * @param dialogType
	 * @param mainFilter
	 * @param prefStringToLoadtheCurrentPath
	 * @param selectedEncoding
	 */
	public void createAndShowFileAndEncodingDialog(String title, int dialogType, String[] mainFilter,
			String prefStringToLoadtheCurrentPath, String selectedEncoding) {
		createAndShowFileAndEncodingDialog(title, dialogType, null, mainFilter, prefStringToLoadtheCurrentPath, null,
				selectedEncoding, null);
	}

	/**
	 * Create and show a file encoding dialog
	 * 
	 * @param title
	 * @param dialogType
	 * @param extensions
	 * @param mainFilter
	 * @param prefStringToLoadtheCurrentPath
	 * @param encodings
	 * @param selectedEncoding
	 * @param selectedFileName
	 */
	public void createAndShowFileAndEncodingDialog(String title, int dialogType, List<String[]> extensions,
			String[] mainFilter, String prefStringToLoadtheCurrentPath, String[] encodings, String selectedEncoding,
			String selectedFileName) {
		createAndShowFileAndEncodingDialog(title, dialogType, null, extensions, mainFilter, acceptAllFilesTypes,
				prefStringToLoadtheCurrentPath, encodings, selectedEncoding, FileChooser.FILES_ONLY, selectedFileName);
	}

	/**
	 * Create and show a file encoding dialog
	 * 
	 * @param title
	 * @param dialogType
	 * @param aprroveButtonText
	 * @param extensions
	 * @param mainFilter
	 * @param acceptAllFilesTypes
	 * @param prefStringToLoadtheCurrentPath
	 * @param encodings
	 * @param selectedEncoding
	 * @param fileSelectionMode
	 * @param selectedFileName
	 */
	public void createAndShowFileAndEncodingDialog(String title, int dialogType, String aprroveButtonText,
			List<String[]> extensions, String[] mainFilter, boolean acceptAllFilesTypes,
			String prefStringToLoadtheCurrentPath, String[] encodings, String selectedEncoding, int fileSelectionMode,
			String selectedFileName) {
		customizedDialogType = FileChooser.ENCODING_DIALOG;
		createDialog(title, dialogType, aprroveButtonText, extensions, mainFilter, acceptAllFilesTypes,
				prefStringToLoadtheCurrentPath, fileSelectionMode, selectedFileName);

		this.selectedEncoding = selectedEncoding;
		this.setEncodings(encodings);

		if (chooser != null) {
			chooser.setControlButtonsAreShown(false);
		}
		show();
	}

	/**
	 * Create and show a file & clip media dialog
	 * 
	 * @param title
	 * @param dialogType
	 * @param extensions
	 * @param mainFilter
	 * @param prefStringToLoadtheCurrentPath
	 */
	public void createAndShowFileAndClipMediaDialog(String title, int dialogType, List<String[]> extensions,
			String[] mainFilter, String prefStringToLoadtheCurrentPath) {
		customizedDialogType = FileChooser.CLIPMEDIA_DIALOG;
		createDialog(title, dialogType, null, extensions, mainFilter, true, prefStringToLoadtheCurrentPath, -1, null);
		if (chooser != null) {
			chooser.setControlButtonsAreShown(false);
		}
		show();
	}

	/**
	 * Create and show a multifile chooser dialog
	 * 
	 * @param title
	 * @param multiFileDialogType
	 */
	public void createAndShowMultiFileDialog(String title, int multiFileDialogType) {
		createAndShowMultiFileDialog(title, multiFileDialogType, null, null);
	}

	/**
	 * Create and show a multifile chooser dialog
	 * 
	 * @param title
	 * @param multiFileDialogType
	 * @param mainFilterExt
	 * @param prefStringToLoadtheCurrentPath
	 */
	public void createAndShowMultiFileDialog(String title, int multiFileDialogType, String[] mainFilterExt,
			String prefStringToLoadtheCurrentPath) {
		createAndShowMultiFileDialog(title, multiFileDialogType, ElanLocale.getString("Button.OK"), null, mainFilterExt,
				acceptAllFilesTypes, prefStringToLoadtheCurrentPath, FileChooser.FILES_ONLY, null);
	}

	/**
	 * Create and show a multifile chooser dialog
	 * 
	 * @param title
	 * @param multiFileDialogType
	 * @param aprroveButtonText
	 * @param extensions
	 * @param mainFilter
	 * @param acceptAllFilesTypes
	 * @param prefStringToLoadtheCurrentPath
	 * @param fileSelectionMode
	 * @param files
	 */
	public void createAndShowMultiFileDialog(String title, int multiFileDialogType, String aprroveButtonText,
			List<String[]> extensions, String[] mainFilter, boolean acceptAllFilesTypes,
			String prefStringToLoadtheCurrentPath, int fileSelectionMode, File[] files) {
		if ((multiFileDialogType >= GENERIC) && (multiFileDialogType <= MEDIA)) {
			this.multiFileDialogMode = multiFileDialogType;
		}

		if (files != null) {
			this.selectedFiles = files;
		}
		customizedDialogType = FileChooser.MULTIFILE_DIALOG;

		if (multiFileDialogMode == MEDIA_TEMPLATE || multiFileDialogMode == MEDIA) {
			// start with media file filter

			ArrayList<String[]> defaultExtensions = new ArrayList<String[]>();
			defaultExtensions.add(FileExtension.MEDIA_EXT);
			defaultExtensions.add(FileExtension.MPEG_EXT);
			defaultExtensions.add(FileExtension.WAV_EXT);
			defaultExtensions.add(FileExtension.MPEG4_EXT);
			defaultExtensions.add(FileExtension.QT_EXT);

			if (extensions != null) {
				for (String e[] : extensions) {
					if (!defaultExtensions.contains(e)) {
						defaultExtensions.add(e);
					}
				}
			}

			if (mainFilter == null) {
				Integer val = Preferences.getInt("Media.LastUsedMediaType", null);
				if (val != null) {
					int type = val.intValue();
					if (type == ElanFileFilter.MEDIA_TYPE) {
						mainFilter = FileExtension.MEDIA_EXT;
					} else if (type == ElanFileFilter.MP4_TYPE) {
						mainFilter = FileExtension.MPEG4_EXT;
					} else if (type == ElanFileFilter.QT_TYPE) {
						mainFilter = FileExtension.QT_EXT;
					} else if (type == ElanFileFilter.MPEG_TYPE) {
						mainFilter = FileExtension.MPEG_EXT;
					} else if (type == ElanFileFilter.WAV_TYPE) {
						mainFilter = FileExtension.WAV_EXT;

					} else {
						mainFilter = FileExtension.MEDIA_EXT;
					}
				} else {
					mainFilter = FileExtension.MEDIA_EXT;
				}
			}

			extensions = defaultExtensions;

			if (prefStringToLoadtheCurrentPath == null) {
				prefStringToLoadtheCurrentPath = "MediaDir";
			}
			acceptAllFilesTypes = true;
		}

		createDialog(title, FileChooser.OPEN_DIALOG, aprroveButtonText, extensions, mainFilter, acceptAllFilesTypes,
				prefStringToLoadtheCurrentPath, fileSelectionMode, null);

		if (chooser != null) {
			chooser.setMultiSelectionEnabled(true);
			chooser.setControlButtonsAreShown(false);
			chooser.setPreferredSize(new Dimension((int) chooser.getPreferredSize().getWidth() - 80,
					(int) chooser.getPreferredSize().getHeight()));
		}
		show();
	}

	/**
	 * Shows a dialog, depending on settings.
	 */
	private void show() {
		selectedFile = null;
		boolean allFileFilterUsed = false;
		boolean askForOverWrite = false;
		boolean validExt = true;
		String selectedFileExt = null;
		if (dialog != null) {
			allFileFilterUsed = acceptAllFilesTypes;
			CustomizedDialogForMac cutomizedDialog = null;

			if (customizedDialogType == MULTIFILE_DIALOG) {
				boolean showFileType = (mainDialogType == FileChooser.SAVE_DIALOG);
				if (parent instanceof Frame) {
					cutomizedDialog = new CustomizedDialogForMac((Frame) parent, showFileType);
				} else if (parent instanceof Dialog) {
					cutomizedDialog = new CustomizedDialogForMac((Dialog) parent, showFileType);
				} else {
					cutomizedDialog = new CustomizedDialogForMac((Frame) null, showFileType);
				}
				selectedFiles = cutomizedDialog.getFiles();
				currentDirectory = dialog.getDirectory();
			} else {
				Window messWindow = showSubstituteTitle(dialog.getTitle());
				dialog.setVisible(true);
				hideSubstituteTitle(messWindow);

				String file = dialog.getFile();
				currentDirectory = dialog.getDirectory();
				if (file != null) {
					selectedFile = new File(dialog.getDirectory(), file);
				}

				if (selectedFile != null) {
					boolean showFileType = false;
					validExt = isValidExt(allFileFilterUsed);
					if (!validExt && extensions.size() > 1 && mainDialogType == FileChooser.SAVE_DIALOG) {
						showFileType = true;
					}
					if (customizedDialogType >= 0 || showFileType) {
						if (parent instanceof Frame) {
							cutomizedDialog = new CustomizedDialogForMac((Frame) parent, showFileType);
						} else if (parent instanceof Dialog) {
							cutomizedDialog = new CustomizedDialogForMac((Dialog) parent, showFileType);
						} else {
							cutomizedDialog = new CustomizedDialogForMac((Frame) null, showFileType);
						}
						selectedFileExt = cutomizedDialog.getSelectedExtension();
					}
				}
			}
		} else {
			askForOverWrite = true;
			int returnVal = -1;
			if (customizedDialogType >= 0) {
				CustomizedFileChooser cutomizedChooser = new CustomizedFileChooser();
				returnVal = cutomizedChooser.showDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					if (customizedDialogType == FileChooser.MULTIFILE_DIALOG) {
						selectedFiles = cutomizedChooser.getFiles();
					} else {
						selectedFile = cutomizedChooser.getSelectedFile();
						currentDirectory = chooser.getCurrentDirectory().getAbsolutePath();
					}
				}
			} else {
				returnVal = chooser.showDialog(parent, null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					selectedFile = chooser.getSelectedFile();
					currentDirectory = chooser.getCurrentDirectory().getAbsolutePath();
					if (chooser.getFileFilter() == chooser.getAcceptAllFileFilter()) {
						allFileFilterUsed = true;
					}
				}
			}
		}

		if (customizedDialogType == MULTIFILE_DIALOG) {
			if (selectedFiles == null) {
				return;
			}

			File file = null;
			List<Object> validFiles = new ArrayList<Object>();
			boolean allFilesExist = true;
			for (Object selectedFile2 : selectedFiles) {
				file = (File) selectedFile2;

				// check if file exists
				if (file.exists()) {
					validFiles.add(selectedFile2);
				} else {
					// flag that one of the files does not exist
					allFilesExist = false;
				}
			}
			if (!allFilesExist) {
				// ask if user wants to continue, if some of the files doesn't exist.
				int answer = JOptionPane.showConfirmDialog(parent,
						ElanLocale.getString("OverlapsDialog.Message.NotAllFilesExist"),
						ElanLocale.getString("OverlapsDialog.Message.NotAllFilesExistTitle"),
						JOptionPane.YES_NO_OPTION);

				// stop with exporting if user doesn't want to continue
				if (answer != JOptionPane.YES_OPTION) {
					selectedFiles = null;
					return;
				}
			}

			selectedFiles = validFiles.toArray(new Object[validFiles.size()]);
			// store last used directory
			if (prefStringToLoadtheCurrentPath != null && multiFileDialogMode != FileChooser.MEDIA_TEMPLATE) {
				Preferences.set(prefStringToLoadtheCurrentPath,
						(new File(selectedFiles[selectedFiles.length - 1].toString())).getParent(), null);
			}
		} else {
			if (selectedFile == null) {
				return;
			}

			validExt = isValidExt(allFileFilterUsed);

			if (!askForOverWrite && !validExt) {
				askForOverWrite = true;
			}

			String name = selectedFile.getAbsolutePath();

			switch (mainDialogType) {
			case OPEN_DIALOG:
				// check if file exists
				try {
					// check if file exists and is a file
					if (!selectedFile.exists()) {
						String strMessage = null;
						String strTitle = null;
						if (this.fileSelectionMode == DIRECTORIES_ONLY) {
							strMessage = ElanLocale.getString("ExportTiersDialog.Message.DirectoryDoesntExist");
							strTitle = ElanLocale.getString("ExportTiersDialog.Message.DirectoryDoesntExistTitle");

							// JOptionPane.showMessageDialog(null,
							// ElanLocale.getString("ExportTiersDialog.Message.DirectoryDoesntExist"),
							// ElanLocale.getString("ExportTiersDialog.Message.DirectoryDoesntExistTitle"),
							// JOptionPane.OK_OPTION);

						} else {
							strMessage = ElanLocale.getString("Menu.Dialog.Message1");
							strMessage += chooser.getSelectedFile().getAbsolutePath();
							strMessage += ElanLocale.getString("Menu.Dialog.Message2");
							strTitle = ElanLocale.getString("Message.Error");
						}

						JOptionPane.showMessageDialog(parent, strMessage, strTitle, JOptionPane.ERROR_MESSAGE);
						show();
						return;
					}

					// check if the file type is valid
					if (!selectedFile.isDirectory() && !validExt) {
						String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
						strMessage += name;
						strMessage += ElanLocale.getString("Menu.Dialog.Message3");

						String strError = ElanLocale.getString("Message.Error");
						JOptionPane.showMessageDialog(null, strMessage, strError, JOptionPane.ERROR_MESSAGE);
						show();
						return;
					}
				} catch (Exception exc) {
				}
				break;
			case SAVE_DIALOG:
				try {
					// make sure pathname finishes with correct extension
					if (!validExt) {
						if (chooser != null) {
							FileFilter filter = chooser.getFileFilter();
							// if(filter == chooser.getAcceptAllFileFilter()){
							// if(chooser.getChoosableFileFilters().length >0){
							// filter = chooser.getChoosableFileFilters()[0];
							// }
							// }
							if (filter instanceof ElanFileFilter) {
								List<String> exten = ((ElanFileFilter) filter).getFilterExtensions();
								name += ("." + exten.get(0));
								validExt = true;
							}
						} else if (selectedFileExt != null) {
							name += ("." + selectedFileExt);
							validExt = true;
						}

						if (!validExt && extensions != null && !extensions.isEmpty()) {
							name += ("." + extensions.get(0));
						}

						selectedFile = new File(name);
					}

					// check if file exists and is a file
					if (askForOverWrite && selectedFile.exists()) {
						int answer = JOptionPane.showConfirmDialog(null, ElanLocale.getString("Message.Overwrite"),
								ElanLocale.getString("SaveDialog.Message.Title"), JOptionPane.YES_NO_OPTION);

						if (answer == JOptionPane.NO_OPTION) {
							show();
							return;
						}
					}
				} catch (Exception exc) {
				}
				break;
			}
			if (prefStringToLoadtheCurrentPath != null) {
				Preferences.set(prefStringToLoadtheCurrentPath, (new File(selectedFile.getAbsolutePath())).getParent(),
						null);
			}
		}
	}

	/**
	 * Checks if the extension (if any) of the selected file is valid against one of
	 * the list of valid extensions for the current action.
	 * @param allFileFilterUsed the flag whether the All Files option was used or not 
	 * @return true if the file name has a valid extension
	 */
	private boolean isValidExt(boolean allFileFilterUsed) {
		boolean validExt = true;

		if (!allFileFilterUsed || mainDialogType == FileChooser.SAVE_DIALOG) {
			String fileExt = FileUtility.getExtension(selectedFile);
			validExt = false;
			if (fileExt != null) {
				String ext = fileExt.toLowerCase();
				for (int i = 0; i < extensions.size(); i++) {
					if (ext.equals(extensions.get(i).toLowerCase())) {
						validExt = true;
					}
				}
			}
		}

		return validExt;
	}

	/**
	 * Returns the current directory
	 * 
	 * @return current directory
	 */
	public String getCurrentDirectory() {
		return currentDirectory;
	}

	/**
	 * Returns the selected file
	 * 
	 * @return selected file
	 */
	public File getSelectedFile() {
		return selectedFile;
	}

	/**
	 * Returns an array of file objects that the user has selected.
	 * <p>
	 * Mostly these seem to be File objects, but possibly there is an URL is a
	 * String (if addRemoteFile() has been called). TODO check/improve this.
	 * 
	 * @return an array of file objects
	 */
	public Object[] getSelectedFiles() {
		return selectedFiles;
	}

	/**
	 * Set the current directory of the dialog
	 * 
	 * @param currentDirectory
	 */
	public void setCurrentDirectory(String currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	/**
	 * Returns the selected encoding.
	 *
	 * @return the selected encoding
	 */
	public String getSelectedEncoding() {
		return selectedEncoding;
	}

	/**
	 * Boolean which tells whether to clip the media or not
	 * 
	 * @return true or false
	 */
	public boolean doClipMedia() {
		return clipMedia;
	}

	/**
	 * Gets the selected values from the component Can be extended if new customized
	 * dialog types are added
	 * 
	 * @param component,
	 *            the extra component added in the file dialogs
	 */
	@SuppressWarnings("rawtypes")
	private void getSelectedValueFrom(JComponent component) {
		switch (customizedDialogType) {
		case ENCODING_DIALOG:
			selectedEncoding = (String) ((JComboBox) component).getSelectedItem();
			if (selectedEncoding == null) {
				selectedEncoding = FileChooser.UTF_8;
			}
			break;
		case CLIPMEDIA_DIALOG:
			clipMedia = ((JCheckBox) component).isSelected();
			break;
		}
	}

	/**
	 * Specifies a custom set of encodings.
	 *
	 * @param encs
	 *            the encodings
	 */
	private void setEncodings(String[] encs) {
		if ((encs != null) && (encs.length > 0)) {
			encodings = new String[encs.length];

			for (int i = 0; i < encs.length; i++) {
				encodings[i] = encs[i];
			}
			if (selectedEncoding == null) {
				selectedEncoding = encodings[0];
			}
		}
	}

	/**
	 * Scans the folders for the specified files and adds them to files list,
	 * recursively.
	 *
	 * @param dir
	 *            the or folder
	 * @param extensions
	 *            the list to extensions to be added
	 */
	public void addFiles(File dir, List<File> files, List<String> extensions) {
		if (dir == null || files == null || extensions == null) {
			return;
		}

		File[] allSubs = dir.listFiles();

		for (int i = 0; i < allSubs.length; i++) {
			if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
				addFiles(allSubs[i], files, extensions);
			} else {
				if (allSubs[i].canRead()) {
					String ext = FileUtility.getExtension(allSubs[i]);
					if (ext != null && extensions.contains(ext.toLowerCase())) {
						// test if the file is already there??
						if (!files.contains(allSubs[i])) {
							files.add(allSubs[i]);
						}
					}
				}
			}
		}
	}

	/**
	 * On macOS Sierra (10.11.x) and higher, the title bar of the native Open and
	 * Save dialog is no longer there. The title bar was used to provide information
	 * to the user on what can be or should be done in the dialog, including
	 * extensions. There seems to be no way to customize the native dialog
	 * (properties?) therefore a little message window is shown in the center of the
	 * screen, just below the main menubar.
	 * 
	 * @param title
	 *            the message to show (which used to be the title)
	 * @return the window, which has been made visible already, or null
	 * @see #hideSubstituteTitle(Window)
	 */
	private Window showSubstituteTitle(String title) {
		if (!SystemReporting.isMacOSSierraOrHigher()) {
			return null;
		}
		JWindow titleWindow = new JWindow();
		titleWindow.getContentPane().setLayout(new BorderLayout());
		// titleWindow.getContentPane().setBackground(new Color(255, 255, 220));
		titleWindow.getContentPane().add(new JLabel(title, SwingConstants.CENTER), BorderLayout.CENTER);
		// placement based on preferences or otherwise in the horizontal center
		// of the screen below the menu bar
		Rectangle winBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		titleWindow.setSize(672, 46);
		titleWindow.setLocation(winBounds.x + (winBounds.width - 672) / 2, winBounds.y);
		titleWindow.setAlwaysOnTop(false);
		titleWindow.setVisible(true);
		return titleWindow;

	}

	/**
	 * If a message window was shown to substitute the title, it should be hidden
	 * again after the save or open dialog has been closed.
	 * 
	 * @param w
	 *            the window to hide, can be null
	 * @see #showSubstituteTitle(String)
	 */
	private void hideSubstituteTitle(Window w) {
		if (w != null) {
			w.setVisible(false);
			w.dispose();
		}
	}

	/**
	 * Class to specify the filters types for the JFileChooser
	 * 
	 * @author aarsom
	 */
	private class FileNameFilterList implements FilenameFilter {

		private List<String> fileNameFilters;

		public FileNameFilterList(List<String> fileNameFilters) {
			this.fileNameFilters = fileNameFilters;
		}

		public FileNameFilterList(String[] fileNameFilter) {
			if (fileNameFilter == null) {
				fileNameFilters = null;
			} else {
				fileNameFilters = new ArrayList<String>();
				for (String ext : fileNameFilter) {
					fileNameFilters.add(ext);
				}
			}
		}

		public FileNameFilterList() {
			// stub
		}

		@Override
		public boolean accept(File dir, String name) {
			if (fileNameFilters == null || fileNameFilters.size() == 0) {
				return true;
			}

			for (int i = 0; i < fileNameFilters.size(); i++) {
				if (name.toLowerCase().endsWith(fileNameFilters.get(i).toLowerCase())) {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * CustomizedFileChooser for all the other platforms except mac-os
	 * 
	 * @author aarsom
	 *
	 */
	@SuppressWarnings("serial")
	private class CustomizedFileChooser extends JComponent implements ActionListener {
		protected JButton okButton;
		protected JButton cancelButton;
		protected JDialog dialog;
		protected int returnValue = JFileChooser.ERROR_OPTION;
		protected JComponent component;

		private JList fileList;
		private DefaultListModel model;

		private FileFilter mediaFilter;
		private FileFilter templateFilter;
		private FileFilter qtFilter;
		private FileFilter mp4Filter;
		private FileFilter mpgFilter;
		private FileFilter wavFilter;

		/**
		 * Creates a new TextExportFileChooser instance
		 */
		private CustomizedFileChooser() {
			initComponents();
		}

		/**
		 * 
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		private JPanel getCustomizedPanel() {
			JPanel panel = new JPanel();

			switch (customizedDialogType) {
			case ENCODING_DIALOG:
				component = new JComboBox();
				for (int i = 0; i < encodings.length; i++) {
					((JComboBox) component).addItem(encodings[i]);
					if (i == 0) {
						((JComboBox) component).setSelectedItem(encodings[i]);
					}
				}
				if (getSelectedEncoding() != null) {
					((JComboBox) component).setSelectedItem(getSelectedEncoding());
				}
				panel.add(new JLabel("Encoding: "));
				panel.add(component);
				break;
			case CLIPMEDIA_DIALOG:
				component = new JCheckBox(ElanLocale.getString("ExportSelectionAsEAF.Label.ClipMedia"));
				panel.add(component);
				break;

			case FileChooser.MULTIFILE_DIALOG:
				panel = getMultiFilePanel();
				break;
			}
			return panel;
		}

		private JPanel getMultiFilePanel() {
			JPanel panel = new JPanel(new GridBagLayout());

			final JRadioButton mediaRB = new JRadioButton();
			final JRadioButton templateRB = new JRadioButton();
			final JButton remoteButton = new JButton();
			final JButton removeButton = new JButton();
			removeButton.setEnabled(false);
			final JButton upButton = new JButton();
			final JButton downButton = new JButton();
			final JButton copyButton = new JButton();

			if (multiFileDialogMode == MEDIA_TEMPLATE) {
				mediaFilter = ElanFileFilter.createFileFilter(ElanFileFilter.MEDIA_TYPE);
				templateFilter = ElanFileFilter.createFileFilter(ElanFileFilter.TEMPLATE_TYPE);
				qtFilter = ElanFileFilter.createFileFilter(ElanFileFilter.QT_TYPE);
				mp4Filter = ElanFileFilter.createFileFilter(ElanFileFilter.MP4_TYPE);
				mpgFilter = ElanFileFilter.createFileFilter(ElanFileFilter.MPEG_TYPE);
				wavFilter = ElanFileFilter.createFileFilter(ElanFileFilter.WAV_TYPE);
			}

			ActionListener listener = new ActionListener() {
				/**
				 * Action Listener implementation.
				 *
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed(ActionEvent e) {
					Object src = e.getSource();

					if (src == copyButton) {
						copyFile();
					} else if (src == removeButton) {
						removeFile();
					} else if (src == upButton) {
						moveUp();
					} else if (src == downButton) {
						moveDown();
					} else if (src == chooser) {
						copyFile();
					} else if (src == mediaRB) {
						setMediaFilter();
					} else if (src == templateRB) {
						setTemplateFilter();
					} else if (src == remoteButton) {
						addRemoteFile();
					}
					if (model.size() > 0) {
						removeButton.setEnabled(true);
					} else {
						removeButton.setEnabled(false);
					}
				}
			};

			chooser.addActionListener(listener);
			copyButton.addActionListener(listener);
			removeButton.addActionListener(listener);
			upButton.addActionListener(listener);
			downButton.addActionListener(listener);
			mediaRB.addActionListener(listener);
			templateRB.addActionListener(listener);

			ImageIcon REMOVE_ICON = new ImageIcon(
					this.getClass().getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
			ImageIcon UP_ICON = new ImageIcon(
					this.getClass().getResource("/mpi/eudico/client/annotator/resources/Up.gif"));
			ImageIcon DOWN_ICON = new ImageIcon(
					this.getClass().getResource("/mpi/eudico/client/annotator/resources/Down.gif"));
			removeButton.setIcon(REMOVE_ICON);
			upButton.setIcon(UP_ICON);
			downButton.setIcon(DOWN_ICON);

			Insets insets = new Insets(2, 2, 2, 2);

			// layout for midPanel
			JPanel midPanel = new JPanel(new GridBagLayout());
			copyButton.setText(" >> ");
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(4, 6, 4, 6);
			midPanel.add(copyButton, gbc);
			if (multiFileDialogMode == MEDIA_TEMPLATE) {
				JPanel selPanel = new JPanel(new GridBagLayout());
				selPanel.setBorder(new TitledBorder(ElanLocale.getString("Frame.ElanFrame.NewDialog.RadioFileType")));

				ButtonGroup bg = new ButtonGroup();
				mediaRB.setSelected(true);
				mediaRB.setText(ElanLocale.getString("Frame.ElanFrame.NewDialog.RadioButtonMedia"));
				templateRB.setText(ElanLocale.getString("Frame.ElanFrame.NewDialog.RadioButtonTemplate"));
				bg.add(mediaRB);
				bg.add(templateRB);
				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.insets = insets;
				selPanel.add(mediaRB, gbc);

				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.insets = insets;
				selPanel.add(templateRB, gbc);

				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.anchor = GridBagConstraints.SOUTH;
				gbc.insets = new Insets(4, 6, 4, 6);
				midPanel.add(selPanel, gbc);
			} else {
				remoteButton.setVisible(false);
			}

			JPanel rightPanel = new JPanel(new GridBagLayout());
			JPanel infoPanel = new JPanel(new GridBagLayout());
			Dimension dim = new Dimension(70, 40);
			infoPanel.setPreferredSize(dim);
			infoPanel.setMinimumSize(dim);

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = insets;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weighty = 1.0;
			infoPanel.add(new JLabel(ElanLocale.getString("Frame.ElanFrame.NewDialog.Selected")));

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			rightPanel.add(infoPanel, gbc);

			model = new DefaultListModel();
			fileList = new JList(model);
			fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			if (selectedFiles != null) {
				model.clear();

				for (int i = 0; i < selectedFiles.length; i++) {
					model.insertElementAt(selectedFiles[i], i);
				}

				if (model.size() > 0) {
					removeButton.setEnabled(true);
				}

				fileList.setSelectedIndex(0);
				fileList.ensureIndexIsVisible(0);
				selectedFiles = null;
			}

			JScrollPane jsp = new JScrollPane(fileList);
			jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().getSize().width - 30,
					jsp.getPreferredSize().getSize().height));

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			rightPanel.add(jsp, gbc);

			JPanel controlPanel = new JPanel(new GridLayout(1, 3, 6, 6));
			controlPanel.add(removeButton);
			controlPanel.add(upButton);
			controlPanel.add(downButton);

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			rightPanel.add(controlPanel, gbc);

			remoteButton.setText(ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"));
			remoteButton.addActionListener(listener);
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = new Insets(7, 2, 3, 2);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			rightPanel.add(remoteButton, gbc);

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.weighty = 1.0;
			panel.add(midPanel, gbc);

			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = insets;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			panel.add(rightPanel, gbc);

			return panel;
		}

		/**
		 * Sets up the components. The standard "Save" and "Cancel" buttons are replaced
		 * because of layout considerations (the customized panel is best situated above
		 * the control buttons).
		 */
		protected void initComponents() {
			okButton = new JButton();
			cancelButton = new JButton();

			okButton.addActionListener(this);
			cancelButton.setText(ElanLocale.getString("Button.Cancel"));
			cancelButton.addActionListener(this);

			Insets insets = new Insets(0, 6, 6, 6);

			JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 6));
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);

			JPanel butBorderPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			butBorderPanel.add(new JPanel(), gbc);

			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = insets;
			butBorderPanel.add(buttonPanel, gbc);

			this.setLayout(new GridBagLayout());

			// chooser.addActionListener(this);
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			this.add(chooser, gbc);

			switch (customizedDialogType) {

			case FileChooser.MULTIFILE_DIALOG:
				gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.NORTHEAST;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.insets = insets;
				this.add(getCustomizedPanel(), gbc);

				String approveButtonText = chooser.getApproveButtonText();
				if (approveButtonText != null) {
					okButton.setText(approveButtonText);
				} else {
					okButton.setText(ElanLocale.getString("Button.OK"));
				}

				if (model.size() > 0) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}

				butBorderPanel
						.setBorder(new CompoundBorder(new LineBorder(Color.GRAY, 1), new EmptyBorder(6, 6, 6, 6)));

				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.gridwidth = 3;
				gbc.anchor = GridBagConstraints.SOUTH;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.insets = insets;
				this.add(butBorderPanel, gbc);

				break;
			default:
				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.fill = GridBagConstraints.NONE;
				gbc.insets = insets;
				this.add(getCustomizedPanel(), gbc);

				switch (chooser.getDialogType()) {
				case FileChooser.SAVE_DIALOG:
					okButton.setText(ElanLocale.getString("Menu.File.Save"));
					break;
				case FileChooser.OPEN_DIALOG:
					okButton.setText(ElanLocale.getString("Menu.File.Open"));
					break;
				}

				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 2;
				gbc.anchor = GridBagConstraints.SOUTH;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.insets = insets;
				this.add(butBorderPanel, gbc);
			}

		}

		/**
		 * Copied from JFileChooser.
		 *
		 * @param parent
		 *            the parent component, usually a frame
		 * @param approveButtonText
		 *            the text for the "OK" or approve button
		 *
		 * @return JFileChooser.ERROR_OPTION, APPROVE_OPTION or CANCEL_OPTION
		 *
		 * @throws HeadlessException
		 *
		 * @see JFileChooser.showDialog(Component)
		 */
		public int showDialog(Component parent) throws HeadlessException {
			dialog = createDialog(parent);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					returnValue = JFileChooser.CANCEL_OPTION;
				}
			});
			returnValue = JFileChooser.ERROR_OPTION;

			dialog.setVisible(true);
			dialog.dispose();
			dialog = null;

			return returnValue;
		}

		/**
		 * Copied from JFileChooser. Creates a configured dialog.
		 *
		 * @param parent
		 *            the parent
		 *
		 * @return a dialog object
		 *
		 * @throws HeadlessException
		 */
		private JDialog createDialog(Component parent) throws HeadlessException {
			Frame frame = (parent instanceof Frame) ? (Frame) parent
					: (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);

			JDialog dialog = new ClosableDialog(frame, chooser.getDialogTitle(), true);

			Container contentPane = dialog.getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(this, BorderLayout.CENTER);

			dialog.pack();
			dialog.setLocationRelativeTo(parent);

			return dialog;
		}

		/**
		 * Check if the given file is a media file
		 * 
		 * @return boolean true if media file, else false
		 */
		private boolean isMediaFile(String file) {
			String ext = FileUtility.getExtension(file);
			if (ext == null) {
				return false;
			}
			for (String element : FileExtension.MEDIA_EXT) {
				if (ext.equalsIgnoreCase(element)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Check if the given file is a template file
		 * 
		 * @return boolean true if template, else false
		 */
		private boolean isTemplateFile(String file) {
			String ext = FileUtility.getExtension(file);
			if (ext == null) {
				return false;
			}
			for (String element : FileExtension.TEMPLATE_EXT) {
				if (ext.equalsIgnoreCase(element)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Stores the last used media type preference
		 */
		private void storeLastUsedMediaTypePref() {

			FileFilter ff = chooser.getFileFilter();
			if (ff.getDescription().equals(templateFilter.getDescription())) {
				return;
			}

			int type = -1;
			if (mediaFilter.getDescription().equals(ff.getDescription())) {
				type = ElanFileFilter.MEDIA_TYPE;
			} else if (mp4Filter.getDescription().equals(ff.getDescription())) {
				type = ElanFileFilter.MP4_TYPE;
			} else if (qtFilter.getDescription().equals(ff.getDescription())) {
				type = ElanFileFilter.QT_TYPE;
			} else if (wavFilter.getDescription().equals(ff.getDescription())) {
				type = ElanFileFilter.WAV_TYPE;
			} else if (mpgFilter.getDescription().equals(ff.getDescription())) {
				type = ElanFileFilter.MPEG_TYPE;
			}
			Preferences.set("Media.LastUsedMediaType", Integer.valueOf(type), null);
		}

		/**
		 * Copies the selected files from the File chooser to the user list.
		 */
		@SuppressWarnings("unchecked")
		private void copyFile() {
			String mediaDir = null;
			String templateDir = null;

			if (chooser.isMultiSelectionEnabled()) {
				File[] files = chooser.getSelectedFiles();
				int curIndex;
				for (int i = 0; i < files.length; i++) {
					if (!model.contains(files[i])) {
						curIndex = fileList.getSelectedIndex();
						model.add(curIndex + 1, files[i]);
						fileList.setSelectedIndex(curIndex + 1);
					}

					if (mediaDir == null && isMediaFile(files[i].getAbsolutePath())) {
						mediaDir = files[i].getAbsolutePath();
						mediaDir = new File(mediaDir).getParent();
						continue;
					}

					if (templateDir == null && isTemplateFile(files[i].getAbsolutePath())) {
						templateDir = files[i].getAbsolutePath();
						templateDir = new File(templateDir).getParent();
					}
				}
			} else {
				File f = chooser.getSelectedFile();
				if (f != null) {
					int curIndex = fileList.getSelectedIndex();
					model.add(curIndex + 1, f);
					fileList.setSelectedIndex(curIndex + 1);

					if (isMediaFile(f.getAbsolutePath())) {
						mediaDir = f.getAbsolutePath();
						mediaDir = new File(mediaDir).getParent();
					} else if (isTemplateFile(f.getAbsolutePath())) {
						templateDir = f.getAbsolutePath();
						templateDir = new File(templateDir).getParent();
					}
				}
			}

			// store dir
			if (templateDir != null) {
				Preferences.set("TemplateDir", templateDir, null);
			}

			if (mediaDir != null) {
				Preferences.set("MediaDir", mediaDir, null);

				storeLastUsedMediaTypePref();
			}

			if (model.size() > 0) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}

		/**
		 * Removes the selected files from the selected files list.
		 */
		private void removeFile() {
			int[] selIndices = fileList.getSelectedIndices();
			if (selIndices.length == 0) {
				return;
			}
			for (int i = selIndices.length - 1; i >= 0; i--) {
				model.removeElementAt(selIndices[i]);
			}

			if (selIndices[0] < model.getSize()) {
				fileList.setSelectedIndex(selIndices[0]);
				fileList.ensureIndexIsVisible(selIndices[0]);
			}

			if (model.size() > 0) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}

		/**
		 * Moves the selected files up in the list.
		 */
		private void moveUp() {
			int[] selIndices = fileList.getSelectedIndices();

			if (selIndices.length == 0) {
				return;
			}

			for (int i = 0; i < selIndices.length; i++) {
				if (selIndices[i] == i) {// e.g. if rows 0, 1 and 2 are all selected, they cannot be moved
					continue;
				}
				model.insertElementAt(model.remove(selIndices[i]), selIndices[i] - 1);
			}

			int[] newSels = new int[selIndices.length];

			for (int i = 0; i < selIndices.length; i++) {
				if (selIndices[i] == i) {// e.g. if rows 0, 1 and 2 are all selected, they cannot be moved
					newSels[i] = i;
				} else {
					newSels[i] = selIndices[i] - 1;
				}
			}
			fileList.setSelectedIndices(newSels);
		}

		/**
		 * Moves the selected files down in the list.
		 */
		@SuppressWarnings("unchecked")
		private void moveDown() {
			int[] selIndices = fileList.getSelectedIndices();

			if (selIndices.length == 0) {
				return;
			}

			int unmovableIndex = model.getSize() - 1;
			int[] newSels = new int[selIndices.length];

			for (int i = selIndices.length - 1; i >= 0; i--) {
				if (selIndices[i] == unmovableIndex) {
					unmovableIndex--;
					newSels[i] = selIndices[i];
					continue;
				} else {
					model.insertElementAt(model.remove(selIndices[i]), selIndices[i] + 1);
					newSels[i] = selIndices[i] + 1;
				}
			}
			fileList.setSelectedIndices(newSels);
		}

		/**
		 * Changes the active file filter to the media file filter (.mpg + .wav).
		 */
		private void setMediaFilter() {
			boolean isMedia = false;
			FileFilter[] filters = chooser.getChoosableFileFilters();

			for (FileFilter filter : filters) {
				if (filter == mediaFilter) {
					isMedia = true;

					break;
				}
			}

			if (!isMedia) {
				File selectedFile = chooser.getSelectedFile();

				if (selectedFile != null) {
					String strTemplateDir = selectedFile.getAbsolutePath();
					strTemplateDir = (new File(strTemplateDir)).getParent();
					Preferences.set("TemplateDir", strTemplateDir, null);
				}

				for (FileFilter filter : chooser.getChoosableFileFilters()) {
					if (filter != chooser.getAcceptAllFileFilter()) {
						chooser.removeChoosableFileFilter(filter);
					}
				}

				chooser.addChoosableFileFilter(mediaFilter);
				chooser.addChoosableFileFilter(mp4Filter);
				chooser.addChoosableFileFilter(qtFilter);
				chooser.addChoosableFileFilter(mpgFilter);
				chooser.addChoosableFileFilter(wavFilter);

				// jan 2009 use a user preference
				Integer val = Preferences.getInt("Media.LastUsedMediaType", null);
				if (val != null) {
					int type = val.intValue();
					if (type == ElanFileFilter.MEDIA_TYPE) {
						chooser.setFileFilter(mediaFilter);
					} else if (type == ElanFileFilter.MP4_TYPE) {
						chooser.setFileFilter(mp4Filter);
					} else if (type == ElanFileFilter.QT_TYPE) {
						chooser.setFileFilter(qtFilter);
					} else if (type == ElanFileFilter.MPEG_TYPE) {
						chooser.setFileFilter(mpgFilter);
					} else if (type == ElanFileFilter.WAV_TYPE) {
						chooser.setFileFilter(wavFilter);
					} else {
						chooser.setFileFilter(mediaFilter);
					}
				} else {
					chooser.setFileFilter(mediaFilter);
				}
				// chooser.setFileFilter(mediaFilter);

				String mediaDir = Preferences.getString("MediaDir", null);
				chooser.setCurrentDirectory(new File((mediaDir == null) ? Constants.USERHOME : mediaDir));
			}
		}

		/**
		 * Changes the active file filter to the template file filter (.etf).
		 */
		private void setTemplateFilter() {
			boolean isTemplate = false;
			FileFilter[] filters = chooser.getChoosableFileFilters();

			for (FileFilter filter : filters) {
				if (filter.getDescription().equals(templateFilter.getDescription())) {
					isTemplate = true;
					chooser.setFileFilter(templateFilter);

					break;
				}
			}

			if (!isTemplate) {
				File selectedFile = chooser.getSelectedFile();

				if (selectedFile != null) {
					String strMediaDir = selectedFile.getAbsolutePath();
					strMediaDir = (new File(strMediaDir)).getParent();
					Preferences.set("MediaDir", strMediaDir, null);
				}

				storeLastUsedMediaTypePref();

				for (FileFilter filter : chooser.getChoosableFileFilters()) {
					if (filter != chooser.getAcceptAllFileFilter()) {
						chooser.removeChoosableFileFilter(filter);
					}
				}
				chooser.addChoosableFileFilter(templateFilter);

				String templateDir = Preferences.getString("TemplateDir", null);
				chooser.setCurrentDirectory(new File((templateDir == null) ? Constants.USERHOME : templateDir));
			}
		}

		/**
		 * Shows an input pane where the user can enter the url of a (rtsp) streaming
		 * file. The file is added to the list.
		 */
		@SuppressWarnings("unchecked")
		private void addRemoteFile() {
			Object rf = JOptionPane.showInputDialog(this, ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteLabel"),
					ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"), JOptionPane.PLAIN_MESSAGE, null,
					null, "rtsp://");
			if (rf == null) {
				return;
			}

			String url = (String) rf;
			url.replace('\\', '/');
			// try some simple repairs
			boolean valid = true;
			if (!url.startsWith("rtsp")) {
				int ds = url.indexOf("//");
				if (ds > -1) {
					url = "rtsp:" + url.substring(ds);
				} else {
					url = "rtsp://" + url;
				}
			}

			if (url.indexOf("://") != 4) {
				valid = false; // do not even try to repair
			}
			int dot = url.lastIndexOf('.');
			int slash = url.lastIndexOf('/');
			if (dot < 0 || dot < slash || dot > slash && slash <= 7) {
				valid = false;
			}

			// no use trying to check the string as an URL (doesn't know the rtsp protocol)
			// or URI (accepts almost any string)
			if (!valid) {
				JOptionPane.showMessageDialog(this,
						ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMessage") + url,
						ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
				addRemoteFile();
			} else {
				int curIndex = fileList.getSelectedIndex();
				model.add(curIndex + 1, url);
				fileList.setSelectedIndex(curIndex + 1);
			}
		}

		/**
		 * Returns the selected File.
		 *
		 * @return the selected File
		 */
		public File getSelectedFile() {
			return chooser.getSelectedFile();
		}

		/**
		 * Returns an array of file objects that the user has selected.
		 *
		 * @return an array of file objects
		 */
		public Object[] getFiles() {
			Object[] obj = new Object[model.getSize()];

			for (int i = 0; i < obj.length; i++) {
				obj[i] = model.getElementAt(i);
			}

			if (obj.length > 0) {
				return obj;
			} else {
				return null;
			}
		}

		/**
		 * Action Listener implementation.
		 *
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				// method approveSelection() does not seem to set the file name typed in
				// the textfield as the selected file
				// chooser.approveSelection();
				// find the first textfield encountered

				if (customizedDialogType != FileChooser.MULTIFILE_DIALOG) {
					Component c = getTextField(chooser);

					if (c instanceof JTextField) {
						String fileName = ((JTextField) c).getText();
						chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), fileName));
					}
					getSelectedValueFrom(component);
				}

				returnValue = JFileChooser.APPROVE_OPTION;
				dialog.setVisible(false);
			} else if (e.getSource() == cancelButton) {
				returnValue = JFileChooser.CANCEL_OPTION;
				dialog.setVisible(false);
			}
		}

		/**
		 * Recursively test all child components until a TextField is encountered.
		 *
		 * @param container
		 *            the container holding child components
		 *
		 * @return the first JTextField or null
		 */
		private Component getTextField(Container container) {
			Component c = null;

			if (container != null) {
				Component[] cc = container.getComponents();

				for (Component element : cc) {
					c = element;

					// System.out.println("C: " + c.getClass());
					if (c instanceof JTextField) {
						return c;
					} else if (c instanceof Container) {
						c = getTextField((Container) c);

						if (c instanceof JTextField) {
							return c;
						}
					}
				}
			}

			return c;
		}
	}

	/**
	 * Customized file Dialog only for MacOS
	 * 
	 * @author aarsom
	 *
	 */
	@SuppressWarnings("serial")
	private class CustomizedDialogForMac extends JDialog implements ActionListener {
		private JComponent component;
		private JButton okButton;
		private JButton cancelButton;
		private JButton backButton;
		private JComboBox saveComboBox;

		private JList fileList;
		private DefaultListModel model;

		private boolean showFileTypes;
		private String mediaDlgTitle;
		private String tempDlgTitle;
		private String genericDlgTitle;

		/**
		 * Constructor
		 * 
		 * @param owner
		 */
		public CustomizedDialogForMac(Frame owner, boolean showFileTypes) {
			super(owner, true);
			this.showFileTypes = showFileTypes;
			makeLayout();
			pack();
			this.setLocationRelativeTo(owner);
			setVisible(true);
		}

		/**
		 * Constructor
		 * 
		 * @param owner
		 */
		public CustomizedDialogForMac(Dialog owner, boolean showFileTypes) {
			super(owner, true);
			this.showFileTypes = showFileTypes;
			makeLayout();
			pack();
			this.setLocationRelativeTo(owner);
			setVisible(true);
		}

		/**
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private JPanel getCustomizedPanel() {
			// setPreferredSize(new Dimension(200, 100));
			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			switch (customizedDialogType) {
			case ENCODING_DIALOG:
				setTitle(ElanLocale.getString("FileChooser.Mac.Label.Encoding"));
				component = new JComboBox();
				for (String encoding : encodings) {
					((JComboBox) component).addItem(encoding);
				}
				if (getSelectedEncoding() != null) {
					((JComboBox) component).setSelectedItem(getSelectedEncoding());
				} else {
					((JComboBox) component).setSelectedItem(((JComboBox) component).getItemAt(0));
				}

				gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.NORTH;
				gbc.insets = new Insets(4, 6, 4, 6);
				gbc.gridx = 0;
				gbc.gridy = 0;
				panel.add(new JLabel(ElanLocale.getString("FileChooser.Mac.Label.Encoding")), gbc);

				gbc.gridx = 1;
				gbc.weightx = 1.0;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				panel.add(component, gbc);
				break;

			case CLIPMEDIA_DIALOG:
				component = new JCheckBox(ElanLocale.getString("ExportSelectionAsEAF.Label.ClipMedia"));
				gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.NORTH;
				gbc.insets = new Insets(4, 6, 4, 6);
				gbc.gridx = 0;
				gbc.gridy = 0;
				panel.add(component, gbc);
				break;

			case FileChooser.MULTIFILE_DIALOG:
				setTitle(dialog.getTitle());
				panel = getMultiFilePanel();
				break;
			}

			if (showFileTypes) {
				setTitle(ElanLocale.getString("FileChooser.Mac.Title"));
				saveComboBox = new JComboBox();
				for (int i = 0; i < extensions.size(); i++) {
					saveComboBox.addItem(extensions.get(i));
				}

				String text = ElanLocale.getString("FileChooser.Mac.Label.InvalidFormat1") + " '"
						+ selectedFile.toString() + "' " + "\n"
						+ ElanLocale.getString("FileChooser.Mac.Label.InvalidFormat2");
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.gridx = 0;
				gbc.gridy = gbc.gridy + 1;
				gbc.gridwidth = 2;
				gbc.weightx = 0.0;
				gbc.insets = new Insets(4, 6, 4, 6);
				panel.add(new JLabel(text), gbc);

				gbc.gridy = gbc.gridy + 1;
				gbc.gridwidth = 1;
				gbc.fill = GridBagConstraints.NONE;
				panel.add(new JLabel(ElanLocale.getString("FileChooser.Mac.Label.FileFormat")), gbc);

				gbc.gridx = 1;
				gbc.weightx = 1.0;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				panel.add(saveComboBox, gbc);
			}
			return panel;
		}

		private JPanel getMultiFilePanel() {
			setPreferredSize(new Dimension(600, 400));

			JPanel panel = new JPanel(new GridBagLayout());
			final JButton addMediaButton = new JButton(ElanLocale.getString("FileChooser.Button.AddMedia"));
			final JButton addTemplateButton = new JButton(ElanLocale.getString("FileChooser.Button.AddTempate"));
			final JButton remoteButton = new JButton(ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"));
			final JButton removeButton = new JButton(ElanLocale.getString("FileChooser.Button.Remove"));
			removeButton.setEnabled(false);
			final JButton upButton = new JButton();
			final JButton downButton = new JButton();

			okButton = new JButton(ElanLocale.getString("Button.OK"));
			cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));

			final String title = dialog.getTitle();

			if (multiFileDialogMode == MEDIA_TEMPLATE) {
				// update the dialog titles
				ArrayList<String[]> defaultExtensions = new ArrayList<String[]>();
				defaultExtensions.add(FileExtension.MEDIA_EXT);
				defaultExtensions.add(FileExtension.MPEG_EXT);
				defaultExtensions.add(FileExtension.WAV_EXT);
				defaultExtensions.add(FileExtension.MPEG4_EXT);
				defaultExtensions.add(FileExtension.QT_EXT);

				ArrayList<String> mediaExtensions = new ArrayList<String>();
				for (String e[] : defaultExtensions) {
					for (int i = 0; i < e.length; i++) {
						if (!mediaExtensions.contains(e[i])) {
							mediaExtensions.add(e[i]);
						}
					}
				}

				mediaDlgTitle = updateTitleWithExt(title, mediaExtensions, true);

				ArrayList<String> tempExtensions = new ArrayList<String>();
				for (int i = 0; i < FileExtension.TEMPLATE_EXT.length; i++) {
					if (!tempExtensions.contains(FileExtension.TEMPLATE_EXT[i])) {
						tempExtensions.add(FileExtension.TEMPLATE_EXT[i]);
					}
				}
				tempDlgTitle = updateTitleWithExt(title, tempExtensions, false);
			}

			if (multiFileDialogMode == GENERIC) {
				genericDlgTitle = updateTitleWithExt(title, FileChooser.this.extensions,
						FileChooser.this.acceptAllFilesTypes);
			}

			ActionListener listener = new ActionListener() {
				/**
				 * Action Listener implementation.
				 *
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed(ActionEvent e) {
					Object src = e.getSource();
					dialog.setTitle(title);

					if (src == addMediaButton) {
						if (multiFileDialogMode == MEDIA_TEMPLATE) {
							dialog.setTitle(mediaDlgTitle);
							dialog.setFilenameFilter(new FileNameFilterList());
							String templateDir = Preferences.getString("MediaDir", null);
							dialog.setDirectory(templateDir == null ? Constants.USERHOME : templateDir);
						}

						if (multiFileDialogMode == GENERIC) {
							dialog.setTitle(genericDlgTitle);
						}
						Window tw = showSubstituteTitle(dialog.getTitle());
						dialog.setVisible(true);
						hideSubstituteTitle(tw);

						String file = dialog.getFile();
						if (file != null) {
							addFile(new File(dialog.getDirectory(), dialog.getFile()));
						}

						if (multiFileDialogMode == MEDIA_TEMPLATE && dialog.getDirectory() != null) {
							Preferences.set("MediaDir", dialog.getDirectory(), null);
						}
					} else if (src == addTemplateButton) {
						if (multiFileDialogMode == MEDIA_TEMPLATE) {
							dialog.setTitle(tempDlgTitle);
							dialog.setFilenameFilter(new FileNameFilterList(FileExtension.TEMPLATE_EXT));
							String templateDir = Preferences.getString("TemplateDir", null);
							dialog.setDirectory(templateDir == null ? Constants.USERHOME : templateDir);
						}

						if (multiFileDialogMode == GENERIC) {
							System.setProperty("apple.awt.fileDialogForDirectories", Boolean.TRUE.toString());
							dialog.setTitle(genericDlgTitle);
						}

						Window tw = showSubstituteTitle(dialog.getTitle());
						dialog.setVisible(true);
						hideSubstituteTitle(tw);

						String file = dialog.getFile();
						if (file != null) {
							addFile(new File(dialog.getDirectory(), dialog.getFile()));
						}

						if (multiFileDialogMode == MEDIA_TEMPLATE && dialog.getDirectory() != null) {
							Preferences.set("TemplateDir", dialog.getDirectory(), null);
						}

						if (multiFileDialogMode == GENERIC) {
							System.setProperty("apple.awt.fileDialogForDirectories", Boolean.FALSE.toString());
						}
					} else if (src == removeButton) {
						removeFile();
					} else if (src == upButton) {
						moveUp();
					} else if (src == downButton) {
						moveDown();
					} else if (src == remoteButton) {
						addRemoteFile();
					} else if (src == okButton) {
						doClose();
					} else if (src == cancelButton) {
						model.clear();
						doClose();
					}

					if (model.size() > 0) {
						removeButton.setEnabled(true);
					} else {
						removeButton.setEnabled(false);
					}
				}
			};

			removeButton.addActionListener(listener);
			remoteButton.addActionListener(listener);
			upButton.addActionListener(listener);
			downButton.addActionListener(listener);
			addMediaButton.addActionListener(listener);
			addTemplateButton.addActionListener(listener);
			okButton.addActionListener(listener);
			cancelButton.addActionListener(listener);

			// ImageIcon REMOVE_ICON = new
			// ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
			ImageIcon UP_ICON = new ImageIcon(
					this.getClass().getResource("/mpi/eudico/client/annotator/resources/Up.gif"));
			ImageIcon DOWN_ICON = new ImageIcon(
					this.getClass().getResource("/mpi/eudico/client/annotator/resources/Down.gif"));
			// removeButton.setIcon(REMOVE_ICON);
			upButton.setIcon(UP_ICON);
			downButton.setIcon(DOWN_ICON);

			Insets insets = new Insets(2, 2, 2, 2);

			JPanel buttonsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = insets;
			// gbc.weightx = 1.0;

			if (multiFileDialogMode == MEDIA_TEMPLATE) {
				gbc.gridy = 0;
				buttonsPanel.add(addMediaButton, gbc);

				gbc.gridy = 1;
				buttonsPanel.add(addTemplateButton, gbc);

				gbc.gridy = 2;
				buttonsPanel.add(remoteButton, gbc);

				gbc.gridy = 3;
				buttonsPanel.add(removeButton, gbc);
			} else {
				switch (fileSelectionMode) {
				case FILES_ONLY:
					gbc.gridy = 0;
					buttonsPanel.add(addMediaButton, gbc);
					addMediaButton.setText(ElanLocale.getString("FileChooser.Button.AddFile"));
					break;
				case DIRECTORIES_ONLY:
					gbc.gridy = 0;
					buttonsPanel.add(addTemplateButton, gbc);
					addTemplateButton.setText(ElanLocale.getString("FileChooser.Button.AddFolder"));
					break;
				case FILES_AND_DIRECTORIES:
					gbc.gridy = 0;
					buttonsPanel.add(addMediaButton, gbc);
					addMediaButton.setText(ElanLocale.getString("FileChooser.Button.AddFile"));

					gbc.gridy = 1;
					buttonsPanel.add(addTemplateButton, gbc);
					addTemplateButton.setText(ElanLocale.getString("FileChooser.Button.AddFolder"));
					break;
				}

				gbc.gridy = gbc.gridy + 1;
				buttonsPanel.add(removeButton, gbc);
			}

			JPanel controlPanel = new JPanel(new GridLayout(1, 3, 6, 6));
			controlPanel.add(upButton);
			controlPanel.add(downButton);

			JPanel controlButtonPanel = new JPanel(new GridLayout(1, 3, 6, 6));
			controlButtonPanel.add(okButton);
			controlButtonPanel.add(cancelButton);

			model = new DefaultListModel();
			fileList = new JList(model);
			fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			if (selectedFiles != null) {
				model.clear();
				for (int i = 0; i < selectedFiles.length; i++) {
					model.insertElementAt(selectedFiles[i], i);
				}

				if (model.size() > 0) {
					removeButton.setEnabled(true);
				}

				fileList.setSelectedIndex(0);
				fileList.ensureIndexIsVisible(0);
				selectedFiles = null;
			}

			JScrollPane jsp = new JScrollPane(fileList);
			jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().getSize().width - 30,
					jsp.getPreferredSize().getSize().height));
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = insets;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weighty = 1.0;
			panel.add(new JLabel(ElanLocale.getString("Frame.ElanFrame.NewDialog.Selected")));

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			panel.add(jsp, gbc);

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = insets;
			// gbc.weightx = 1.0;
			gbc.anchor = GridBagConstraints.NORTH;
			panel.add(controlPanel, gbc);

			gbc.gridx = 1;
			gbc.gridy = 1;
			panel.add(buttonsPanel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.EAST;
			panel.add(controlButtonPanel, gbc);

			return panel;
		}

		/**
		 * Makes the layout
		 */
		private void makeLayout() {
			getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(4, 6, 4, 6);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			getContentPane().add(getCustomizedPanel(), gbc);

			if (customizedDialogType != MULTIFILE_DIALOG) {
				okButton = new JButton(ElanLocale.getString("Button.OK"));
				okButton.addActionListener(this);

				cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
				cancelButton.addActionListener(this);

				backButton = new JButton(ElanLocale.getString("Button.Back"));
				backButton.addActionListener(this);

				JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
				buttonPanel.add(backButton);
				buttonPanel.add(okButton);
				buttonPanel.add(cancelButton);

				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.anchor = GridBagConstraints.SOUTH;
				gbc.fill = GridBagConstraints.NONE;
				getContentPane().add(buttonPanel, gbc);
			}

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					doClose();
				}
			});
		}

		private String getSelectedExtension() {
			if (saveComboBox != null) {
				return (String) saveComboBox.getSelectedItem();
			}

			return null;
		}

		/**
		 * Copies the selected files from the File chooser to the user list.
		 */
		private void addFile(File file) {

			if (!model.contains(file)) {
				int curIndex = fileList.getSelectedIndex();
				model.add(curIndex + 1, file);
				fileList.setSelectedIndex(curIndex + 1);
			}

			if (model.size() > 0) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}

		/**
		 * Removes the selected files from the selected files list.
		 */
		public void removeFile() {
			int[] selIndices = fileList.getSelectedIndices();
			if (selIndices.length == 0) {
				return;
			}
			for (int i = selIndices.length - 1; i >= 0; i--) {
				model.removeElementAt(selIndices[i]);
			}

			if (model.getSize() > 0) {
				fileList.setSelectedIndex(model.getSize() - 1);
				fileList.ensureIndexIsVisible(model.getSize() - 1);
			}

			if (model.size() > 0) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}

		/**
		 * Moves the selected files up in the list.
		 */
		public void moveUp() {
			int[] selIndices = fileList.getSelectedIndices();

			if (selIndices.length == 0) {
				return;
			}

			for (int i = 0; i < selIndices.length; i++) {
				if (selIndices[i] == i) {// e.g. if rows 0, 1 and 2 are all selected, they cannot be moved
					continue;
				}
				model.insertElementAt(model.remove(selIndices[i]), selIndices[i] - 1);
			}

			int[] newSels = new int[selIndices.length];

			for (int i = 0; i < selIndices.length; i++) {
				if (selIndices[i] == i) {// e.g. if rows 0, 1 and 2 are all selected, they cannot be moved
					newSels[i] = i;
				} else {
					newSels[i] = selIndices[i] - 1;
				}
			}
			fileList.setSelectedIndices(newSels);
		}

		/**
		 * Moves the selected files down in the list.
		 */
		public void moveDown() {
			int[] selIndices = fileList.getSelectedIndices();

			if (selIndices.length == 0) {
				return;
			}

			int unmovableIndex = model.getSize() - 1;
			int[] newSels = new int[selIndices.length];

			for (int i = selIndices.length - 1; i >= 0; i--) {
				if (selIndices[i] == unmovableIndex) {
					unmovableIndex--;
					newSels[i] = selIndices[i];
					continue;
				} else {
					model.insertElementAt(model.remove(selIndices[i]), selIndices[i] + 1);
					newSels[i] = selIndices[i] + 1;
				}
			}
			fileList.setSelectedIndices(newSels);
		}

		/**
		 * Shows an input pane where the user can enter the url of a (rtsp) streaming
		 * file. The file is added to the list.
		 */
		public void addRemoteFile() {
			Object rf = JOptionPane.showInputDialog(this, ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteLabel"),
					ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"), JOptionPane.PLAIN_MESSAGE, null,
					null, "rtsp://");
			if (rf == null) {
				return;
			}

			String url = (String) rf;
			url.replace('\\', '/');
			// try some simple repairs
			boolean valid = true;
			if (!url.startsWith("rtsp")) {
				int ds = url.indexOf("//");
				if (ds > -1) {
					url = "rtsp:" + url.substring(ds);
				} else {
					url = "rtsp://" + url;
				}
			}

			if (url.indexOf("://") != 4) {
				valid = false; // do not even try to repair
			}
			int dot = url.lastIndexOf('.');
			int slash = url.lastIndexOf('/');
			if (dot < 0 || dot < slash || dot > slash && slash <= 7) {
				valid = false;
			}

			// no use trying to check the string as an URL (doesn't know the rtsp protocol)
			// or URI (accepts almost any string)
			if (!valid) {
				JOptionPane.showMessageDialog(this,
						ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMessage") + url,
						ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
				addRemoteFile();
			} else {
				int curIndex = fileList.getSelectedIndex();
				model.add(curIndex + 1, url);
				fileList.setSelectedIndex(curIndex + 1);
			}
		}

		/**
		 * Returns an array of file objects that the user has selected.
		 *
		 * @return an array of file objects
		 */
		public Object[] getFiles() {
			Object[] obj = new Object[model.getSize()];

			for (int i = 0; i < obj.length; i++) {
				obj[i] = model.getElementAt(i);
			}

			if (obj.length > 0) {
				return obj;
			} else {
				return null;
			}
		}

		private void doClose() {
			setVisible(false);
			dispose();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				getSelectedValueFrom(component);
				doClose();
			} else if (e.getSource() == cancelButton) {
				doClose();
			} else if (e.getSource() == backButton) {
				setVisible(false);
				dialog.setVisible(true);
				String file = dialog.getFile();
				currentDirectory = dialog.getDirectory();
				if (file != null) {
					selectedFile = new File(dialog.getDirectory(), file);
				}

				if (selectedFile != null) {
					boolean validExt = isValidExt(acceptAllFilesTypes);
					if (!validExt && extensions.size() > 1 && mainDialogType == FileChooser.SAVE_DIALOG) {
						showFileTypes = true;
						this.getContentPane().removeAll();
						this.makeLayout();
						setVisible(true);
					}
				}
			}
		}
	}
}

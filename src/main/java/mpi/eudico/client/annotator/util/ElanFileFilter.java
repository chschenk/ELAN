package mpi.eudico.client.annotator.util;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import mpi.eudico.client.annotator.ElanLocale;


/**
 * A FileFilter class especially for Elan.
 * Based on Sun's ExampleFileFilter.
 *
 * @author Han Sloetjes
 */
public class ElanFileFilter extends FileFilter {
    private static String TYPE_UNKNOWN = "Type Unknown";
    private static String HIDDEN_FILE = "Hidden File";

    /** Constant for mpeg file filter */
    public static final int MPEG_TYPE = 0;

    /** Constant for wav file filter */
    public static final int WAV_TYPE = 1;

    /** Constant for media file filter */
    public static final int MEDIA_TYPE = 2;

    /** Constant for eaf file filter */
    public static final int EAF_TYPE = 3;

    /** Constant for etf file filter */
    public static final int TEMPLATE_TYPE = 4;

    /** Constant for chat file filter */
    public static final int CHAT_TYPE = 5;
    
	/** Constant for wac file filter */
	public static final int WAC_TYPE = 6;
	
	/** Constant for shoebox text file filter */
	public static final int SHOEBOX_TEXT_TYPE = 7;
	
	/** Constant for shoebox typ file filter */
	public static final int SHOEBOX_TYP_TYPE = 8;
	
	/** Constant for shoebox marker file filter */
	public static final int SHOEBOX_MKR_TYPE = 9;
	
	/** Constant for image file filter */
	public static final int IMAGE_TYPE = 10;
		
	/** Constant for text file filter */
	public static final int TEXT_TYPE = 11;

	/** Constant for transcriber file filter */
	public static final int TRANSCRIBER_TYPE = 12;
	
	/** Constant for SMIL file filter */
	public static final int SMIL_TYPE = 13;
	
	/** Constant for Tiger filter */
	public static final int TIGER_TYPE = 14;
	
	/** Constant for html filter */
	public static final int HTML_TYPE = 15;
	
	/** Constant for mp4 filter */
	public static final int MP4_TYPE = 16;
	
	/** Constant for qt filter */
	public static final int QT_TYPE = 17;
	
	/** Constant for Praat TextGrid filter */
	public static final int PRAAT_TEXTGRID_TYPE = 18;
	
	/** Constant for ELAN preference filter */
	public static final int ELAN_PREFS_TYPE = 19;
	
	/** Constant for csv files (comma separated values) */
	public static final int CSV_TYPE = 20;
	
    /** Constant for subtitle files (SubRip subtitles, Spruce subtitles) */
    public static final int SUBTITLE_TYPE = 21;	
	
	/** Constant for imdi files (IMDI metadata) */
	public static final int IMDI_TYPE = 22;
	
	/** Constant for xml files (generic XML, FLEx) */
	public static final int XML_TYPE = 23;
	
	/** Constant for toolbox text file filter (alternative for Shoebox text) */
	public static final int TOOLBOX_TEXT_TYPE = 24;
	
	/** Constant for ecv files (external controlled vocabulary) */
	public static final int ECV_TYPE = 25;
	
	/** Constant for eaq files (elan annotation query) */
	public static final int EAQ_TYPE = 26;
	
    private List<String> filterList = null;
    // accept file extensions case insensitive without making extensions lower case
    private List<String> acceptFilterList = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * No-arg constructor.
     */
    public ElanFileFilter() {
        super();
        filterList = new ArrayList<String>(5);
        acceptFilterList = new ArrayList<String>(5);
    }

    /**
     * Creates a new ElanFileFilter instance
     *
     * @param extension DOCUMENT ME!
     */
    public ElanFileFilter(String extension) {
        this(extension, null);
    }

    /**
     * Creates a new ElanFileFilter instance
     *
     * @param extension DOCUMENT ME!
     * @param description DOCUMENT ME!
     */
    public ElanFileFilter(String extension, String description) {
        this();

        if (extension != null) {
            addExtension(extension);
        }

        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Creates a new ElanFileFilter instance
     *
     * @param filters DOCUMENT ME!
     */
    public ElanFileFilter(String[] filters) {
        this(filters, null);
    }

    /**
     * Creates a new ElanFileFilter instance
     *
     * @param filters DOCUMENT ME!
     * @param description DOCUMENT ME!
     */
    public ElanFileFilter(String[] filters, String description) {
        this();

        for (int i = 0; i < filters.length; i++) {
            // add filters one by one
            addExtension(filters[i]);
        }

        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Returns true if the File f should be visible in the view.
     *
     * @param f the File
     *
     * @return true if f should be visible, false otherwise
     */
    @Override
	public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = FileUtility.getExtension(f);

            if ((extension != null) && (acceptFilterList.contains(extension.toLowerCase()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the file extensions of this filter
     *
     * @return the file extension
     */
    public List<String> getFilterExtensions() {
       return filterList;
    }

    /**
     * Add an extension to the list of extensions.
     *
     * @param extension the file extension
     */
    public void addExtension(String extension) {
        if (filterList == null) {
        	filterList = new ArrayList<String>(5);
        }

        filterList.add(extension/*.toLowerCase()*/);
        acceptFilterList.add(extension.toLowerCase());
        fullDescription = null;
    }

    /**
     * Returns the description of the file filter.
     *
     * @return the description
     */
    @Override
	public String getDescription() {
        if (fullDescription == null) {
            if ((description == null) || isExtensionListInDescription()) {
                fullDescription = (description == null) ? "(" : (description +
                    " (");

                // build the description from the extension list
                
                if (filterList != null && !filterList.isEmpty()) {
                	fullDescription += ("*." + filterList.get(0));
                	if (filterList.size() > 1) {
                		for (int i = 1; i < filterList.size(); i++) {
                			fullDescription += (", *." + filterList.get(i));
                		}
                	}
                }

                fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }

        return fullDescription;
    }

    /**
     * Sets the description for this file filter.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Sets the extension list in description flag.
     *
     * @param b the new value for the flag
     */
    public void setExtensionListInDescription(boolean b) {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * Returns the value of the extension list in description flag.
     *
     * @return the value of the extension list in description flag
     */
    public boolean isExtensionListInDescription() {
        return useExtensionsInDescription;
    }

    /**
     * Returns a FilterFilter of the specified type.
     *
     * @param filterType the requested filter type
     *
     * @return a customized FileFilter
     */
    public static FileFilter createFileFilter(int filterType) {
        ElanFileFilter eff = null;

        switch (filterType) {
        case MPEG_TYPE:
            eff = new ElanFileFilter(FileExtension.MPEG_EXT,
                    ElanLocale.getString("Frame.ElanFrame.FileDescription.MPEG"));

            break;

        case WAV_TYPE:
            eff = new ElanFileFilter(FileExtension.WAV_EXT,
                    ElanLocale.getString("Frame.ElanFrame.FileDescription.WAV"));

            break;

        case MEDIA_TYPE:
            eff = new ElanFileFilter(FileExtension.MEDIA_EXT,
                    ElanLocale.getString(
                        "Frame.ElanFrame.NewDialog.MediaFilterDescription"));

            break;

        case EAF_TYPE:
            eff = new ElanFileFilter(FileExtension.EAF_EXT,
                    ElanLocale.getString(
                        "Frame.ElanFrame.OpenDialog.FileDescription"));

            break;

        case TEMPLATE_TYPE:
            eff = new ElanFileFilter(FileExtension.TEMPLATE_EXT,
                    ElanLocale.getString(
                        "Frame.ElanFrame.NewDialog.TemplateFilterDescription"));

            break;

        case CHAT_TYPE:
            eff = new ElanFileFilter(FileExtension.CHAT_EXT,
                    ElanLocale.getString(
                        "Frame.ElanFrame.OpenDialog.CHATFileDescription"));

            break;

		case WAC_TYPE:
			eff = new ElanFileFilter(FileExtension.WAC_EXT,
					ElanLocale.getString(
						"ImportDialog.FileDescription.WAC"));

			break;
			
		case SHOEBOX_TEXT_TYPE:
			eff = new ElanFileFilter(FileExtension.SHOEBOX_TEXT_EXT,
					ElanLocale.getString(
						"ImportDialog.FileDescription.Shoebox"));

			break;

		case TOOLBOX_TEXT_TYPE:
			eff = new ElanFileFilter(FileExtension.TOOLBOX_TEXT_EXT,
					ElanLocale.getString(
						"ImportDialog.FileDescription.Toolbox"));

			break;
			
		case SHOEBOX_TYP_TYPE:
			eff = new ElanFileFilter(FileExtension.SHOEBOX_TYP_EXT,
					ElanLocale.getString(
						"ImportDialog.FileDescription.ShoeboxType"));

			break;
			
		case SHOEBOX_MKR_TYPE:
			eff = new ElanFileFilter(FileExtension.SHOEBOX_MKR_EXT, 
				ElanLocale.getString("ShoeboxMarkerDialog.FileDescription.ShoeboxMarker"));
			
			break;

		case TRANSCRIBER_TYPE:
			eff = new ElanFileFilter(FileExtension.TRANSCRIBER_EXT,
					ElanLocale.getString(
						"ImportDialog.FileDescription.Transcriber"));

			break;
			
		case IMAGE_TYPE:
			eff = new ElanFileFilter(FileExtension.IMAGE_EXT, "");
			
			break;
		
		case TEXT_TYPE:
			eff = new ElanFileFilter(FileExtension.TEXT_EXT, 
				ElanLocale.getString("ExportDialog.FileDescription"));
			
			break;
			
		case TIGER_TYPE:
			eff = new ElanFileFilter(FileExtension.TIGER_EXT, 
				ElanLocale.getString("ExportDialog.FileDescription"));
			
			break;

		case SMIL_TYPE:
			eff = new ElanFileFilter(FileExtension.SMIL_EXT, 
				"");
			
			break;
		case HTML_TYPE:
		    eff = new ElanFileFilter(FileExtension.HTML_EXT, 
		        ElanLocale.getString("ExportDialog.FileDescription.Html"));
		    
		    break;
		case MP4_TYPE:
		    eff = new ElanFileFilter(FileExtension.MPEG4_EXT, 
		        ElanLocale.getString("Frame.ElanFrame.FileDescription.MP4"));
		    
		    break;
		case QT_TYPE:
		    eff = new ElanFileFilter(FileExtension.QT_EXT, 
		        ElanLocale.getString("Frame.ElanFrame.FileDescription.QT"));
		    
		    break;
		case PRAAT_TEXTGRID_TYPE:
		    eff = new ElanFileFilter(FileExtension.PRAAT_TEXTGRID_EXT, 
		        ElanLocale.getString("ImportDialog.FileDesription.Praat.TG"));
		    
		    break;
		case ELAN_PREFS_TYPE:
		    eff = new ElanFileFilter(FileExtension.ELAN_XML_PREFS_EXT, 
		    		ElanLocale.getString("ImportDialog.FileDesription.ELANPref"));
		    
		    break;
		case CSV_TYPE:
			eff = new ElanFileFilter(FileExtension.CSV_EXT, 
					ElanLocale.getString("ImportDialog.FileDesription.CSV"));
			
			break;
		case SUBTITLE_TYPE:
			eff = new ElanFileFilter(FileExtension.SUBTITLE_EXT, 
					ElanLocale.getString("ExportDialog.FileDescription.Subtitle"));
			
			break;
		case IMDI_TYPE:
			eff = new ElanFileFilter(FileExtension.IMDI_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.IMDI"));
			
			break;
		case XML_TYPE:
			eff = new ElanFileFilter(FileExtension.XML_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.XML"));
			
			break;
		case ECV_TYPE:
			eff = new ElanFileFilter(FileExtension.ECV_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.ECV"));
			
			break;
		case EAQ_TYPE:
			eff = new ElanFileFilter(FileExtension.EAQ_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.EAQ"));
		}
        return eff;
    }
    
    /**
     * Returns a FilterFilter of the specified type.
     *
     * @param fileExtArray the requested filter type
     *
     * @return a customized FileFilter
     */
    public static FileFilter createFileFilter(String[] fileExtArray) {      	
    	
    	if(fileExtArray.equals(FileExtension.MPEG_EXT)){        
            return new ElanFileFilter(FileExtension.MPEG_EXT,
                    ElanLocale.getString("Frame.ElanFrame.FileDescription.MPEG"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.WAV_EXT)){        
            return new ElanFileFilter(FileExtension.WAV_EXT,
                    ElanLocale.getString("Frame.ElanFrame.FileDescription.WAV"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.MEDIA_EXT)){        
            return new ElanFileFilter(FileExtension.MEDIA_EXT,
                    ElanLocale.getString("Frame.ElanFrame.NewDialog.MediaFilterDescription"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.EAF_EXT)){        
            return new ElanFileFilter(FileExtension.EAF_EXT,
                    ElanLocale.getString("Frame.ElanFrame.OpenDialog.FileDescription"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.TEMPLATE_EXT)){        
            return new ElanFileFilter(FileExtension.TEMPLATE_EXT,
                    ElanLocale.getString("Frame.ElanFrame.NewDialog.TemplateFilterDescription"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.CHAT_EXT)){        
            return new ElanFileFilter(FileExtension.CHAT_EXT,
                    ElanLocale.getString("Frame.ElanFrame.OpenDialog.CHATFileDescription"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.WAC_EXT)){        
            return new ElanFileFilter(FileExtension.WAC_EXT,
					ElanLocale.getString("ImportDialog.FileDescription.WAC"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.SHOEBOX_TEXT_EXT)){        
            return new ElanFileFilter(FileExtension.SHOEBOX_TEXT_EXT,
					ElanLocale.getString("ImportDialog.FileDescription.Shoebox"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.SHOEBOX_TEXT_EXT)){        
            return new ElanFileFilter(FileExtension.SHOEBOX_TEXT_EXT,
					ElanLocale.getString("ImportDialog.FileDescription.Shoebox"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.TOOLBOX_TEXT_EXT)){        
            return new ElanFileFilter(FileExtension.TOOLBOX_TEXT_EXT,
					ElanLocale.getString("ImportDialog.FileDescription.Toolbox"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.SHOEBOX_TYP_EXT)){        
            return new ElanFileFilter(FileExtension.SHOEBOX_TYP_EXT,
					ElanLocale.getString("ImportDialog.FileDescription.ShoeboxType"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.SHOEBOX_MKR_EXT)){        
            return new ElanFileFilter(FileExtension.SHOEBOX_MKR_EXT, 
    				ElanLocale.getString("ShoeboxMarkerDialog.FileDescription.ShoeboxMarker"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.TRANSCRIBER_EXT)){        
            return new ElanFileFilter(FileExtension.TRANSCRIBER_EXT,
					ElanLocale.getString("ImportDialog.FileDescription.Transcriber"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.IMAGE_EXT)){        
            return new ElanFileFilter(FileExtension.IMAGE_EXT, "");
    	}
    	
    	if(fileExtArray.equals(FileExtension.TEXT_EXT)){        
            return new ElanFileFilter(FileExtension.TEXT_EXT, 
    				ElanLocale.getString("ExportDialog.FileDescription"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.TIGER_EXT)){        
            return new ElanFileFilter(FileExtension.TIGER_EXT, 
    				ElanLocale.getString("ExportDialog.FileDescription"));
    	}	
    	
    	if(fileExtArray.equals(FileExtension.SMIL_EXT)){        
            return new ElanFileFilter(FileExtension.SMIL_EXT, "");
    	}	
    	
    	if(fileExtArray.equals(FileExtension.HTML_EXT)){        
            return new ElanFileFilter(FileExtension.HTML_EXT, 
    		        ElanLocale.getString("ExportDialog.FileDescription.Html"));
    	}	
    	
    	if(fileExtArray.equals(FileExtension.MPEG4_EXT)){        
            return new ElanFileFilter(FileExtension.MPEG4_EXT, 
    		        ElanLocale.getString("Frame.ElanFrame.FileDescription.MP4"));
    	}	
    	
    	if(fileExtArray.equals(FileExtension.QT_EXT)){        
            return new ElanFileFilter(FileExtension.QT_EXT, 
    		        ElanLocale.getString("Frame.ElanFrame.FileDescription.QT"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.PRAAT_TEXTGRID_EXT)){        
            return new ElanFileFilter(FileExtension.PRAAT_TEXTGRID_EXT, 
    		        ElanLocale.getString("ImportDialog.FileDesription.Praat.TG"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.ELAN_XML_PREFS_EXT)){        
            return new ElanFileFilter(FileExtension.ELAN_XML_PREFS_EXT, 
		    		ElanLocale.getString("ImportDialog.FileDesription.ELANPref"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.CSV_EXT)){        
            return new ElanFileFilter(FileExtension.CSV_EXT, 
					ElanLocale.getString("ImportDialog.FileDesription.CSV"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.SUBTITLE_EXT)){        
            return new ElanFileFilter(FileExtension.SUBTITLE_EXT, 
					ElanLocale.getString("ExportDialog.FileDescription.Subtitle"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.IMDI_EXT)){        
            return new ElanFileFilter(FileExtension.IMDI_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.IMDI"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.XML_EXT)){        
            return new ElanFileFilter(FileExtension.XML_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.XML"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.ECV_EXT)){        
            return new ElanFileFilter(FileExtension.ECV_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.ECV"));
    	}
    	
    	if(fileExtArray.equals(FileExtension.EAQ_EXT)){        
            return new ElanFileFilter(FileExtension.EAQ_EXT, 
					ElanLocale.getString("Frame.ElanFrame.FileDescription.EAQ"));
    	}
		
        return new ElanFileFilter(fileExtArray);
    }
}

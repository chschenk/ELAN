package mpi.eudico.client.annotator.recognizer.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for input or output file parameter.
 * 
 * @author Han Sloetjes
 */
public class FileParam extends Param {
	/** the path or url as a string */
	public String filePath;
	/** specifies input or output type */
	public char ioType = IN;
	/** one of the predefined file content types */
	public int contentType = -1;
	/** a mime-type string */
	public List<String> mimeTypes;
	/** flags whether this is optional or not */
	public boolean optional = true;
	
	/**
	 * Constructor.
	 */
	public FileParam() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id the id
	 * @param info the description
	 */
	public FileParam(String id, String info) {
		super(id, info);
	}

	/** input file */
	public static final char IN = 'i';
	/** output file */
	public static final char OUT = 'o';
	
	/** audio file */
	public static final int AUDIO = 0;
	/** video file */
	public static final int VIDEO = 1;
	/** tier xml file */
	public static final int TIER = 2;	
	/** multi tier xml file */
	public static final int MULTITIER = 7;
	/** tier csv file */
	public static final int CSV_TIER = 3;
	/** timeseries xml file */
	public static final int TIMESERIES = 4;
	/** timeseries csv file */
	public static final int CSV_TS = 5;
	/** auxiliary file or folder */
	public static final int AUX = 6;
	
	/**
	 * Creates a clone of this object.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		FileParam clonePar = new FileParam(this.id, this.info);
		clonePar.contentType = this.contentType;
		clonePar.ioType = this.ioType;
		clonePar.filePath = this.filePath;
		clonePar.optional = this.optional;
		clonePar.level = this.level;
		
		if (mimeTypes != null) {
			clonePar.mimeTypes = new ArrayList<String>(this.mimeTypes); 
		}
		
		return clonePar;
	}
}

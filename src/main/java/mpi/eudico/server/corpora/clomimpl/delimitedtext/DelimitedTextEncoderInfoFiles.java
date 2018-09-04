package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import java.io.File;
import java.util.List;

/**
 * EncoderInfo for export from a list of files.
 * Contains a reference to the list of eaf files to export and several booleans to 
 * determine if and how the transcription file names or paths should be exported.
 */
public class DelimitedTextEncoderInfoFiles extends DelimitedTextEncoderInfo {
	/** the list of eaf file to export */
	private List<File> files;
	private boolean fileNameInRow        = false;
	/** flags whether the file name should be included */
	private boolean includeFileName      = false;
	/** flags whether the file path should be included */
	private boolean includeFilePath      = true;
	/** flags whether the media headers should be included, 
	 *  consisting only of file path and media offset */
	private boolean includeMediaHeaders  = false;
	
	/**
	 * Constructor with the list of files as parameter, not null.
	 * 
	 * @param files the files to export
	 */
	public DelimitedTextEncoderInfoFiles(List<File> files) {
		super();
		if (files == null || files.isEmpty()) throw new NullPointerException("The list of files is null or empty.");
		this.files = files;
	}

	public List<File> getFiles() {
		return files;
	}
	
	public boolean isFileNameInRow() {
		return fileNameInRow;
	}
	public void setFileNameInRow(boolean fileNameInRow) {
		this.fileNameInRow = fileNameInRow;
	}
	public boolean isIncludeFileName() {
		return includeFileName;
	}
	public void setIncludeFileName(boolean includeFileName) {
		this.includeFileName = includeFileName;
	}
	public boolean isIncludeFilePath() {
		return includeFilePath;
	}
	public void setIncludeFilePath(boolean includeFilePath) {
		this.includeFilePath = includeFilePath;
	}
	public boolean isIncludeMediaHeaders() {
		return includeMediaHeaders;
	}
	public void setIncludeMediaHeaders(boolean includeMediaHeaders) {
		this.includeMediaHeaders = includeMediaHeaders;
	}

}

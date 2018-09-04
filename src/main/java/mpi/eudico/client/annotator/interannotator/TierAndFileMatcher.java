package mpi.eudico.client.annotator.interannotator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for matching tiers (tier names) or files (file names) based on 
 * same name with different prefix or suffix.
 *
 */
public class TierAndFileMatcher {
	
	public TierAndFileMatcher() {
	}
	
	/**
	 * Creates a list of matching tier names (if any) for each tier name in the list of 
	 * selected tier names.
	 *   
	 * @param allTierNames the list of all tier names to check, assumed not null
	 * @param selTierNames the list of selected tier names, the tier names to find matches for, assumed not null
	 * @param tierMatching prefix or suffix based matching
	 * @param tierNameSeparators custom character(s) to use as a delimiter of name and affix, can be null
	 * 
	 * @return a list of lists
	 */
	public List<List<String>> getMatchingTiers(List<String> allTierNames, List<String> selTierNames, 
			CompareConstants.MATCHING tierMatching, String tierNameSeparators) {
		List<List<String>> matchingNames = new ArrayList<List<String>>();
		List<String> alreadyProc = new ArrayList<String>();
		
		// prepare delimiters
		char[] delimiters = null;
		if (tierNameSeparators != null) {
			delimiters = tierNameSeparators.toCharArray();	
		} else {
			delimiters = new char[] {'-', '_'};
		}
		
		for (String selName : selTierNames) {
			if (alreadyProc.contains(selName)) {
				continue;
			}
			String substringToMatch = getSubstringNoAffix(selName, delimiters, tierMatching);
			
			if (substringToMatch == null) {
				alreadyProc.add(selName);
				continue;
			}
			// this one is not in the list yet
			List<String> curMatches = new ArrayList<String>();
			curMatches.add(selName);
			matchingNames.add(curMatches);
			alreadyProc.add(selName);
			// find matches
			for (String curName : allTierNames) {
				if (alreadyProc.contains(curName)) {
					continue;
				}
				
				if (isMatchingName(curName, substringToMatch, tierMatching)) {
					curMatches.add(curName);
					alreadyProc.add(curName);
				}
			}
		}
		
		return matchingNames;
	}
	
	/**
	 * Combines files based on the file names (excluding/ignoring the file path).
	 * If the tiers to compare are in different files, e.g. File1_A1.eaf and File1_A2.eaf, and the files
	 * can be matched based on some encoding in the file name. 
	 * 
	 * @param selFiles the files in the domain
	 * @param fileMatching either prefix or suffix based (same name, different suffix etc.)
	 * @param fileNameSeparators special delimiters for encoding of file names, can be null
	 * @param fileExtension the extension of the file which is to ignore while doing the matching. Can
	 * be null, in which case it is assumed that the bit after the last "." is the extension
	 * 
	 * @return a list of lists of matching files. In most cases there will only be two files per match
	 */
	public List<List<File>> getMatchingFiles(List<File> selFiles, CompareConstants.MATCHING fileMatching, 
			String fileNameSeparators, String fileExtension) {
		List<List<File>> matchingFiles = new ArrayList<List<File>>();
		List<File> alreadyProc = new ArrayList<File>();

		// prepare delimiters
		char[] delimiters = null;
		if (fileNameSeparators != null) {
			delimiters = fileNameSeparators.toCharArray();	
		} else {
			delimiters = new char[] {'-', '_'};
		}
		//
		String loExtension = null;
		if (fileExtension != null) {
			loExtension = fileExtension.toLowerCase();
		}
		File f = null;
		for (int i = 0; i < selFiles.size(); i++) {
			f = selFiles.get(i);
			if (alreadyProc.contains(f)) {
				continue;
			}
			
			String fileName = noExtFileName(f.getName(), loExtension);

			String substringToMatch = getSubstringNoAffix(fileName, delimiters, fileMatching);
			
			if (substringToMatch == null) {
				alreadyProc.add(f);
				continue;
			}
			// this file is not in a list yet
			List<File> currentMatches = new ArrayList<File>();
			currentMatches.add(f);
			matchingFiles.add(currentMatches);
			alreadyProc.add(f);
			
			File f2 = null;
			String fileName2;
			for (int j = i + 1; j < selFiles.size(); j++) {
				f2 = selFiles.get(j);
				if (alreadyProc.contains(f2)) {
					continue;
				}
				fileName2 = noExtFileName(f2.getName(), loExtension);
				
				if (isMatchingName(fileName2, substringToMatch, fileMatching)) {
					currentMatches.add(f2);
					alreadyProc.add(f2);
				}
			}
		}
		
		return matchingFiles;
	}
	
	/**
	 * Removes the .eaf extension from a file name
	 * @param fileName the file name
	 * @param fileExtension the extension of the file which is to ignore while doing the matching. Can
	 * be null, in which case it is assumed that the bit after the last "." is the extension
	 * 
	 * @return the file name without extension
	 */
	private String noExtFileName(String fileName, String fileExtension) {
		if (fileName != null) {
			if (fileExtension != null) {
				if (fileName.toLowerCase().endsWith(fileExtension)) {
					return fileName.substring(0, fileName.length() - 4);
				}
			} else {
				int lastDot = fileName.lastIndexOf('.');
				if (lastDot > -1) {
					return fileName.substring(0, lastDot);
				}
			}
		}
		return fileName;
	}
	
	/**
	 * Cuts off the Prefix or Suffix using the first delimiter that is found in the string.
	 * 
	 * @param inputName the input string to test and remove prefix or suffix from
	 * @param delimiters the array of delimiters
	 * @param matchType prefix or suffix
	 * 
	 * @return the affix-less string, the delimiter itself is not cut off
	 */
	private String getSubstringNoAffix(String inputName, char[] delimiters, 
			CompareConstants.MATCHING matchType) {
		String substringWoAffix = null;
		for (char ch : delimiters) {
			if (matchType == CompareConstants.MATCHING.PREFIX) {
				int index = inputName.indexOf(ch);
				if (index < 1) {
					continue;
				} else {
					substringWoAffix = inputName.substring(index);// inclusive delimiter
					break;
				}
			} else {// SUFFIX
				int index = inputName.lastIndexOf(ch);
				if (index < 1 || index > inputName.length() - 2) {
					continue;
				} else {
					substringWoAffix = inputName.substring(0, index + 1);// inclusive delimiter
					break;
				}
			}
		}
		return substringWoAffix;
	}
	
	/**
	 * Checks whether the input string matches the specified substring. The substring is the first source,
	 * without the prefix or suffix but with the delimiter as the first or last character.
	 * 
	 * @param candidateName the name to check, assumed not null
	 * @param substringToMatch the substring including the delimiter, assumed not null, length > 1
	 * @param matchType PREFIX or SUFFIX 
	 * 
	 * @return true if the substring matches, false otherwise
	 */
	private boolean isMatchingName(String candidateName, String substringToMatch, 
			CompareConstants.MATCHING matchType) {
		if (matchType == CompareConstants.MATCHING.PREFIX) {// prefix based comparison
			int subIndex = candidateName.indexOf(substringToMatch);// just take first index
			if (subIndex > 0) {// check if this is the first occurrence of the delimiter
				int delIndex = candidateName.indexOf(substringToMatch.charAt(0));
				// check length equality, implies that the substrings are equal
				return candidateName.length() - delIndex == substringToMatch.length();
//				if (delIndex == subIndex) {
//					//  double check if the substrings are equal
//					return candidateName.substring(subIndex).equals(substringToMatch);
//				}
			}				
		} else { // suffix based
			int subIndex = candidateName.lastIndexOf(substringToMatch);
			if (subIndex == 0) {// should start with substring
				// check what is the last index of the delimiter, the last character of substring to match
				int delIndex = candidateName.lastIndexOf(substringToMatch.charAt(substringToMatch.length() - 1));
				// check if the substrings are same length and (based on prior test) are equal
				return (delIndex == substringToMatch.length() - 1);
			}
		}
		
		return false;
	}

}

package mpi.eudico.client.annotator.md.spi;

/**
 * An interface for content language sensitive metadata providers.
 *   
 * @author Han Sloetjes
 */
public interface MDContentLanguageUser {
	/**
	 * Sets the preferred language for metadata content. 
	 * Preferably at least one of the arguments should not be null (although passing
	 * 3 times null could be a way to reset the content language to an undefined default). 
	 * 
	 * @param isoUrlId an ISO ID as URL e.g. http://cdb.iso.org/lg/CDB-00138580-001
	 * @param iso3L an ISO-639-3 letter code e.g. nld
	 * @param lang2L an ISO-639-2 letter code (most likely) e.g. nl
	 */
	public void setContentLanguage(String isoUrlId, String iso3L, String lang2L);
}

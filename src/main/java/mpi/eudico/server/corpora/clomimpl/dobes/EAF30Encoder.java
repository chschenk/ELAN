package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.RefLink;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.reflink.CrossRefLink;
import mpi.eudico.server.corpora.clomimpl.reflink.GroupRefLink;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.util.IoUtil;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;

import org.w3c.dom.Element;

/**
 * Encodes a Transcription to EAF 3.0 format and saves it.
 * 
 * @version xsd 3.0 adds support for CROSS_REF_LINK and GROUP_REF_LINK elements.
 * These elements were already added to the EAF28Encoder, but some additional 
 * attributes are added here. Apart from that the version changes.
 */
public class EAF30Encoder extends EAF28Encoder {
    
    /**
	 * Constructor. Sets the VERSION to 3.0
	 */
	public EAF30Encoder() {
		super();
		VERSION = "3.0";
	}

	/**
	 * Creates an EAF30 instance.
	 * @see mpi.eudico.server.corpora.clomimpl.dobes.EAF28Encoder#getEAFFactory()
	 */
	@Override
	protected EAFBase getEAFFactory() {
		try {
			return new EAF30();
		} catch(Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning(String.format("Could not create an EAF30 factory: %s", t.getMessage()));
			}
		}
		
		return null;
	}

	/**
     * Creates a DOM and saves.
     *
     * @param theTranscription the Transcription to store
     * @param encoderInfo additional information for encoding
     * @param tierOrder preferred tier ordering; should be removed
     * @param path the output path
     */
    @Override
	public void encodeAndSave(Transcription theTranscription,
        EncoderInfo encoderInfo, List<TierImpl> tierOrder, String path)
    	   throws IOException{
    	super.encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
    }

    /**
     * Saves a template eaf of the Transcription; everything is saved except for
     * the annotations
     *
     * @param theTranscription the Transcription to store
     * @param tierOrder preferred tier ordering; should be removed
     * @param path the output path
     */
    public void encodeAsTemplateAndSave(Transcription theTranscription,
        List<TierImpl> tierOrder, String path) throws IOException{
    	super.encodeAsTemplateAndSave(theTranscription, tierOrder, path);
    }
 
    /**
     * Override save() so that in the log it is visible where the encoding started.
     */
    @Override
    protected void save(Element documentElement, String path) throws IOException{
    	if (LOG.isLoggable(Level.INFO)) {
    		LOG.info(String.format("%s <---- XML output - EAF version: %s\n", path, VERSION));
    	}

        try {
            // test for errors
//            if (("" + documentElement).length() == 0) {// don't understand this test
        	if (documentElement == null || !documentElement.hasChildNodes()) {	
                throw new IOException("Unable to save this file (no content).");
            }
        	if (path == null || path.isEmpty()) {	
                throw new IOException("Unable to save this file (zero length).");
            }
            long beginTime = System.currentTimeMillis();
            //IoUtil.writeEncodedFile("UTF-8", path, documentElement);
            IoUtil.writeEncodedEAFFile("UTF-8", path, documentElement);
            
            if (LOG.isLoggable(Level.FINE)) {
            	LOG.fine(String.format("Saving file took: %d ms", (System.currentTimeMillis() - beginTime)));
            }
        } catch (Exception eee) {
            throw new IOException("Unable to save this file: " + eee.getMessage());
        }
    }
    
    
    
    /**
     * Adds the Locale elements based on the Locale references collected from the tiers.
     * Since the LANGUAGE_CODE is defined as type xsd:ID in the schema a test is added 
     * here to prevent invalid XML to be produced if two Locale's have the same language
     * code but different country codes or variants.
     * 
     * @param eafFactory
     * @param annotDocument
     * @param usedLocales the list of locale objects 
     * 
	 * @see mpi.eudico.server.corpora.clomimpl.dobes.EAF28Encoder#addLocales(mpi.eudico.server.corpora.clomimpl.dobes.EAFBase, org.w3c.dom.Element, java.util.List)
	 */
	@Override
	protected void addLocales(EAFBase eafFactory, Element annotDocument,
			List<Locale> usedLocales) {
		// try to "collapse" locales that have the same language code, give 
		// variant precedence over country
		for (int i = 0; i < usedLocales.size(); i++) {
			Locale first = usedLocales.get(i);
			
			for (int j = usedLocales.size() - 1; j > i; j--) {
				Locale second = usedLocales.get(j);
				
				if (first.getLanguage().equals(second.getLanguage())) {
					// update the first Locale, delete the second
					String variant2 = second.getVariant();
					String country2 = second.getCountry();
					
					String useVariant = first.getVariant();
					if ((useVariant == null || useVariant.isEmpty()) && (variant2 != null && !variant2.isEmpty())) {
						useVariant = variant2;
					}
					String useCountry = first.getCountry();
					if ((useCountry == null || useCountry.isEmpty()) && (country2 != null && country2.isEmpty())) {
						useCountry = country2;
					}
					
					Locale replaceLocale = new Locale(first.getLanguage(), useCountry, useVariant);
					usedLocales.set(i, replaceLocale);
					usedLocales.remove(j);
				}
			}
		}
		
		// add elements
		Iterator<Locale> locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = locIter.next();
            Element locElement = eafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }
	}

	/**
     * Adds Reference links, links between annotations.
     * <REF_LINK_SET> was introduced in the EAF 3.0 schema, but EAF28 encoder and parser already
     * support most of these elements and attributes. 
     * In this implementation more than 1 sets are supported. And both the set and the RefLink variants
     * have now a "friendly name" attribute (LINK_SET_NAME for a set, REF_LINK_NAME for a ref. link).
     * 
     * TODO: this all seems to assume that the id's for REF_LINK_SETs and for RefLinks are already set
     * (e.g. when reading an existing file). Check what happens with new reference links; probably 
     * id's need to be generated or all id's should be (re-)generated on every save action.
     * 
     * @param eafFactory EAF30 or higher. 
     * @param annotDocument
     * @param transcription
     * @param getExtRefIdParams the collected external references and ID's
     */
    @Override
    protected void addReferenceLinks(EAFBase eafFactory, Element annotDocument, Transcription transcription,
    		GetExtRefIdParams getExtRefIdParams) {
    	EAF30 eaf30Fact = (EAF30) eafFactory;
    	List<RefLinkSet> rlSetList = transcription.getRefLinkSets();

        if (rlSetList != null) {
        	for (RefLinkSet rlset : rlSetList) {       	
	           	String extRefId = getExtRefId(getExtRefIdParams, rlset.getExtRef());
	        	Element rlsElement = eaf30Fact.newRefLinkSet(
	        			rlset.getLinksID(), rlset.getLinksName(), extRefId,
	        			rlset.getLangRef(), rlset.getCvRef());
	        	
	        	annotDocument.appendChild(rlsElement);
	        	
	        	for (RefLink rl : rlset.getRefs()) {
	        		Element rlElement = null;
	               	extRefId = getExtRefId(getExtRefIdParams, rl.getExtRef());
	
	               	if (rl instanceof CrossRefLink) {
	        			CrossRefLink crl = (CrossRefLink)rl;
	        			rlElement = eaf30Fact.newCrossRefLink(
	        					rl.getId(), rl.getRefName(), extRefId, rl.getLangRef(), rl.getCveRef(), rl.getRefType(), 
	        					rl.getContent(), crl.getRef1(), crl.getRef2(), crl.getDirectionality());
	        		} else if (rl instanceof GroupRefLink) {
	        			GroupRefLink grl = (GroupRefLink)rl;
	        			rlElement = eaf30Fact.newGroupRefLink(
	        					rl.getId(), rl.getRefName(), extRefId, rl.getLangRef(), rl.getCveRef(), rl.getRefType(), 
	        					rl.getContent(), grl.getRefs());
	        		}
	        		rlsElement.appendChild(rlElement);
	        	}
        	}
        }
    }
}

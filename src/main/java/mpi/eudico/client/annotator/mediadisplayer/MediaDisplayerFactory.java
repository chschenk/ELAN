package mpi.eudico.client.annotator.mediadisplayer;

import java.util.HashMap;
import java.util.Map;

import mpi.eudico.client.annotator.lexicon.LexiconClientFactoryLoader;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;

/**
 * A class to create a media displayer given the media type. 
 * @author michahulsbosch
 *
 */
public class MediaDisplayerFactory {
	
	public static enum MEDIA_TYPE {
		VIDEO,
		IMAGE;
		
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	
	public static enum MEDIA_ORIENTATION {
		NORTH,
		SOUTH,
		EAST,
		WEST
	}
	
	/**
	 * 
	 * @param type
	 * @param mediaBundle
	 * @return
	 */
	public static MediaDisplayer createMediaDisplayer(MEDIA_TYPE type, MediaBundle mediaBundle) {
		MediaDisplayer displayer = null;
		if(type == MEDIA_TYPE.VIDEO) {
			displayer = new VideoDisplayer();
			displayer.setMediaBundle(mediaBundle);
		}
		return displayer;
	}
	
	/**
	 * Checks what type of media user it is dealing with and tries to 
	 * create a media displayer according to that type.
	 * @param mediaUser The media user object
	 * @return
	 */
	public static MediaDisplayer getMediaDisplayer(Object[] arguments) {
		Object mediaUser = arguments[0];
		
		MediaDisplayer mediaDisplayer = null;
		if(mediaUser instanceof Annotation) {
			Annotation annot = (Annotation) mediaUser;
			mediaDisplayer = getMediaDisplayerFor(annot);
		} else if(mediaUser instanceof String) {
			if(arguments[1] instanceof Annotation) {
				mediaDisplayer = getMediaDisplayerFromLexiconService(mediaUser, (Annotation) arguments[1]);
			}
		}
		
		return mediaDisplayer;
	}
	
	/**
	 * Tries to create a media displayer for an annotation.
	 * @param annotation The annotation
	 * @return
	 */
	private static MediaDisplayer getMediaDisplayerFor(Annotation annotation) {
		MediaDisplayer mediaDisplayer;
		if((mediaDisplayer = getMediaDisplayerFromExternalRef(annotation)) != null) return mediaDisplayer;
		if((mediaDisplayer = getMediaDisplayerFromLexiconService(annotation)) != null) return mediaDisplayer;
		return null;
	}
	
	/**
	 * If the annotation has resource url, a media displayer for this url is returned.
	 * @param annotation The annotation
	 * @return
	 */
	private static MediaDisplayer getMediaDisplayerFromExternalRef(Annotation annotation) {
		MediaDisplayer mediaDisplayer = null;
		if(annotation instanceof AbstractAnnotation) {
			AbstractAnnotation abstrAnnot = (AbstractAnnotation) annotation;
			ExternalReference extRef = abstrAnnot.getExtRef();
			if(extRef != null && extRef.getReferenceType() == ExternalReference.RESOURCE_URL) {
				String resourceUrl = extRef.getValue();
				MediaBundle mediaBundle = new MediaBundle();
				mediaBundle.setMediaUrl(resourceUrl);
				MEDIA_TYPE mediaType = getMediaTypeFromUrlString(resourceUrl);
				mediaDisplayer = MediaDisplayerFactory.createMediaDisplayer(mediaType, mediaBundle);
			}
		}
		return mediaDisplayer;
	}
	
	/**
	 * Checks whether there is a lexicon service connected to an annotation (via tier and linguistic type),
	 * checks whether that lexicon service is also a media provider and if so, returns a media displayer
	 * created from the provider's preferred media type and returned media bundle based on the annotation.
	 * @param annotation The annotation
	 * @return
	 */
	private static MediaDisplayer getMediaDisplayerFromLexiconService(Object mediaIdentifier, Annotation annotation) {
		MediaDisplayer mediaDisplayer = null;
		LinguisticType lingType = annotation.getTier().getLinguisticType();
		LexiconQueryBundle2 lexQueryBndl = lingType.getLexiconQueryBundle();
		if(lexQueryBndl != null) {
			// Find out if the lexicon services are loaded and if not, load them.
			TranscriptionImpl transcriptionImpl = (TranscriptionImpl) annotation.getTier().getTranscription();
			if(!transcriptionImpl.isLexiconServicesLoaded()) {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcriptionImpl);
				transcriptionImpl.setLexiconServicesLoaded(true);
			}
			LexiconServiceClient srvcClient = lexQueryBndl.getLink().getSrvcClient();
			if(srvcClient instanceof MediaProvider) {
				MediaProvider mediaProvider = (MediaProvider) srvcClient;
				MEDIA_TYPE mediaType = mediaProvider.getPreferredMediaType();
            	MediaBundle mediaBundle = mediaProvider.getMedia(mediaType, new Object[] {mediaIdentifier});
            	if(mediaBundle != null) {
            		mediaDisplayer = MediaDisplayerFactory.createMediaDisplayer(mediaType, mediaBundle);
            	}
//            	else {
//            		ClientLogger.LOG.info("mediaBundle == null");
//            	}
            } 
//			else {
//            	ClientLogger.LOG.info("!srvcClient instanceof MediaProvider");
//            }
		}
		return mediaDisplayer;
	}
	
	private static MediaDisplayer getMediaDisplayerFromLexiconService(Annotation annotation) {
		return getMediaDisplayerFromLexiconService(annotation, annotation);
	}
	
	/**
	 * Determines the media type for a url string.
	 * @param url The url to check.
	 * @return
	 */
	private static MEDIA_TYPE getMediaTypeFromUrlString(String url) {
		if(endsWithOneOf(url, FileExtension.MISC_VIDEO_EXT)) {
			return MEDIA_TYPE.VIDEO;
		}
		return null;
	}
	
	/**
	 * Determines whether the target ends in one of the suffixes.
	 * @param target The target string
	 * @param suffixes The list of suffixes to test.
	 * @return
	 */
	private static Boolean endsWithOneOf(String target, String[] suffixes) {
		for(String suffix : suffixes) {
			if(target.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}
}

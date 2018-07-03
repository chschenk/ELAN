package mpi.eudico.client.annotator;

/**
 * Interface for classes supporting zoom functionality, 
 * e.g. zoom in/out, zoom to default setting.
 * This can be used/interpreted in a flexible way; zooming of a time scale or
 * changing a font size etc.
 */
public interface Zoomable {
	/**
	 * Zoom in or increase the size.
	 */
	public void zoomInStep();
	/**
	 * Zoom out or decrease the size.
	 */
	public void zoomOutStep();
	/**
	 * Zoom to the default setting, 100% or default font size.
	 */
	public void zoomToDefault();
}

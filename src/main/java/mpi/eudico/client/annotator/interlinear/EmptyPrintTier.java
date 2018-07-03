package mpi.eudico.client.annotator.interlinear;

import java.util.List;

/**
 * A dummy tier indicating an empty line.
 * Extends InterlinearTier so that it can be in a List&lt;InterlinearTier>.
 * 
 * @author Han Sloetjes
 * @author Olaf Seibert
 */
public class EmptyPrintTier extends InterlinearTier {

	/**
	 * 
	 */
	public EmptyPrintTier() {
		super("");
	}

	@Override
	public void addAnnotation(InterlinearAnnotation ann) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<InterlinearAnnotation> getAnnotations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTierName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMarginWidth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMarginWidth(int marginWidth) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPrintHeight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPrintHeight(int printHeight) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPrintWidth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPrintWidth(int printWidth) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPrintAdvance() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTimeCode(boolean isTimeCode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSilDuration() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSilDuration(boolean silDur) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTimeCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumLines() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNumLines(int newNumLines) {
		throw new UnsupportedOperationException();
	}
}

package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.List;

import mpi.eudico.server.corpora.clom.AnnotationCore;
/**
 * Class used so as to create a "minimalist" annotation
 * This class is used to create a virtual annotation that groups subdivisions of an annotation 
 * @author Coralie
 *
 */
public class ConcatAnnotation implements AnnotationCore {

	private long begin;
	private long end;
	private String value;

	/**
	 * Construct a new annotation with a list of annotations
	 * @param annotations
	 */
	public ConcatAnnotation(List<AnnotationCore> annotations){
		String annotationBuffer="";
		begin =annotations.get(0).getBeginTimeBoundary();
		end =annotations.get(annotations.size()-1).getEndTimeBoundary();
		for(AnnotationCore ann: annotations){
			annotationBuffer+=ann.getValue()+" ";
		}
		value=annotationBuffer;
	}

	@Override
	public long getBeginTimeBoundary() {
		return begin;
	}

	public void setBeginTimeBoundary(long begin){
		this.begin=begin;
	}

	@Override
	public long getEndTimeBoundary() {
		return end;
	}

	public void setEndTimeBoundary(long end){
		this.end=end;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value){
		this.value=value;
	}
}

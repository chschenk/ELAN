package mpi.eudico.webserviceclient.weblicht;


public class TCFElement {
	private String id;
	private String idRefs;
	private String text;
	//private Map<String, String> attributes;
	
	/**
	 * @param id the id, can be null
	 * @param idRefs one or more id refs, can be null
	 * @param text the content of the element
	 */
	public TCFElement(String id, String idRefs, String text) {
		super();
		this.id = id;
		this.idRefs = idRefs;
		this.text = text;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIdRefs() {
		return idRefs;
	}
	public void setIdRefs(String idRefs) {
		this.idRefs = idRefs;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}

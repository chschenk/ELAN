package mpi.eudico.webserviceclient.weblicht;
/**
 * A minimalistic class for storing some information about WebLicht web services.
 * 
 * @author Han Sloetjes
 */
public class WLServiceDescriptor {
	public String name;
	public String description;
	public String creator;
	public String fullURL;
	
	public boolean plainTextInput;
	public boolean tcfInput;
	public boolean tcfOutput;
	
	public boolean sentenceInput;
	public boolean tokensInput;
	public boolean sentenceOutput;
	public boolean tokensOutput;
	// output
	public boolean posTagSupport;
	public boolean lemmaSupport;
	
	public WLServiceDescriptor(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}

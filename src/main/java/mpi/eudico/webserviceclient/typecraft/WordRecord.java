package mpi.eudico.webserviceclient.typecraft;

import java.util.ArrayList;
import java.util.List;

public class WordRecord {
	public String text;
	public String head;
	public String pos;
	
	public List<MorphRecord> morphs = new ArrayList<MorphRecord>();
}

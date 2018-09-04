package mpi.eudico.webserviceclient.typecraft;

import java.util.ArrayList;
import java.util.List;

public class PhraseRecord {
	public String id;
	public String text;
	public String valid;
	public String translation;
	public String description;
	
	public long bt;
	public long et;
	public String speaker;
	
	public List<WordRecord> wordRecords = new ArrayList<WordRecord>();
}

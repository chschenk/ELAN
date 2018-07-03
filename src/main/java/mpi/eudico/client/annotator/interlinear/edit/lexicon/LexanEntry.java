package mpi.eudico.client.annotator.interlinear.edit.lexicon;

import nl.mpi.lexan.analyzers.lexicon.LexAtom;
import nl.mpi.lexan.analyzers.lexicon.LexCont;
import nl.mpi.lexan.analyzers.lexicon.LexEntry;

public class LexanEntry extends LexCont implements LexEntry {
	private String id;

	/**
	 * ?
	 * @param mainType assumed to be the main field, the lexical-unit (or headword, lexeme) type  
	 * @param lexicalUnit the value of the unit
	 */
	public LexanEntry(String mainType, String lexicalUnit) {
		super(mainType);
		getItemList().add(new LexAtom(mainType, lexicalUnit));
	}

	/**
	 * @return the id of the entry
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @param id the id String of the entry
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the name of the main field in an entry
	 */
	@Override
	public String getMainTypeName() {
		return getType();
	}
	

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append("LexanEntry[id=");
		b.append(String.valueOf(id));
		b.append(", ");
		b.append(String.valueOf(getType()));
		b.append("=");
		b.append(String.valueOf(getLexItems()));
		b.append("]");
		
		return b.toString();
	}
}

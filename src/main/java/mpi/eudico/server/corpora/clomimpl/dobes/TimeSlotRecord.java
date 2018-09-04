package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * A record for temporary storage of id and time value while an annotation file is being parsed.
 * <br>
 * Note: the record classes could be moved to another package
 * @author Han Sloetjes
 *
 */
public class TimeSlotRecord {
	private int id;
	private long value = -1;
	
	public TimeSlotRecord() {
		super();
	}

	public TimeSlotRecord(int id, long value) {
		super();
		this.id = id;
		this.value = value;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return Returns the value.
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(long value) {
		this.value = value;
	}

}

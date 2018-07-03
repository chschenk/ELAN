package mpi.eudico.client.annotator.timeseries;

/**
 * @author Han Sloetjes
 */
public interface TimeSeriesConstants {

	public static final String FIXED_RATE = "Fixed rate";
	public static final String VARIABLE_RATE = "Variable rate";
	public static final String UNKNOWN_RATE_TYPE = "Unknown";
	public static final String DISCONTINUOUS_RATE = "Discontinuous Rate";
	public static final String CONTINUOUS_RATE = "Continuous Rate";
	
	// keys for storage of properties
    public static final String AUTO_DETECT_RANGE = "detect-range";
    public static final String SAMPLE_POS = "pos";
    
    // configuration xml strings
    /** file suffix */
    public static final String CONF_SUFFIX = "_tsconf.xml";
    public static final String TIMESERIES = "timeseries";
    public static final String DATE = "date";
    public static final String VERS = "version";
    public static final String SOURCE = "tracksource";
    public static final String URL = "source-url";
    public static final String ORIGIN = "time-origin";
    public static final String TIME_COLUMN = "time-column";
    public static final String SAMPLE_TYPE = "sample-type";
    public static final String PROVIDER = "provider";
    public static final String PROP = "property";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String TRACK = "track";
    public static final String NAME = "name";
    public static final String DERIVATION = "derivative";
    public static final String DESC = "description";
    public static final String UNITS = "units";
    public static final String POSITION = "sample-position";
    public static final String ROW = "row";
    public static final String COL = "col";
    public static final String RANGE = "range";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String DATA_TYPE = "data-type";
    public static final String COLOR = "color";
}

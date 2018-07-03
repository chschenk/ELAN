package mpi.search.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * This class allows only integer resp float values to be entered or a +X or -X
 * (only upper case) standing for infinity
 */
public class XNumericalJTextFieldFilter extends PlainDocument {
	/** allow only positive integers */
	public static final int POS_INTEGER = 0;

	/** allow only zero and positive integers to be typed in */
	public static final int POS_INTEGER_WITH_INFINITY = 1;

	/** allow all integers to be typed in */
	public static final int INTEGER_WITH_INFINITY = 2;

	/** allow float numbers (using point as seperator) */
	public static final int FLOAT_WITH_INFINITY = 3;

	/** allow integers with positive but without negative infinity */
	public static final int INTEGER_WITH_POS_INFINITY = 4;

	/** allow integers with negative but without positive infinity */
	public static final int INTEGER_WITH_NEG_INFINITY = 5;

	/** all possible positive integer chars */
	protected static final String POS_INTEGER_CHARS = "+0123456789";

	/** Holds value of property DOCUMENT ME! */
	protected static final String POS_INTEGER_CHARS_WITH_INFINITY = POS_INTEGER_CHARS + "X";

	/** Holds value of property DOCUMENT ME! */
	protected static final String INTEGER_CHARS = "-" + POS_INTEGER_CHARS_WITH_INFINITY;

	/** Holds value of property DOCUMENT ME! */
	protected static final String FLOAT_CHARS = INTEGER_CHARS + ".";

	/** Holds value of property DOCUMENT ME! */
	protected String acceptedChars = null;

	private final int mode;

	/**
	 * default setting to positive integers
	 *
	 * @see java.lang.Object#Object()
	 */
	public XNumericalJTextFieldFilter() {
		mode = 0;
		acceptedChars = POS_INTEGER_CHARS_WITH_INFINITY;
	}

	/**
	 * determines which characters may be typed in
	 *
	 * @param mode negative integers allowed, infinity ('X') allowed, etc.
	 */
	public XNumericalJTextFieldFilter(int mode) {
		this.mode = mode;
		switch (mode) {
			case POS_INTEGER_WITH_INFINITY :
				acceptedChars = INTEGER_CHARS;
				break;
			case FLOAT_WITH_INFINITY :
				acceptedChars = FLOAT_CHARS;
				break;
			case INTEGER_WITH_POS_INFINITY :
				acceptedChars = INTEGER_CHARS;
				break;
			case INTEGER_WITH_NEG_INFINITY :
				acceptedChars = INTEGER_CHARS;
				break;
			default :
				acceptedChars = POS_INTEGER_CHARS;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param offset DOCUMENT ME!
	 * @param str DOCUMENT ME!
	 * @param attr DOCUMENT ME!
	 *
	 * @throws BadLocationException DOCUMENT ME!
	 */
	@Override
	public void insertString(int offset, String str, AttributeSet attr)
		throws BadLocationException {
		if (str == null) {
			return;
		}

		for (int i = 0; i < str.length(); i++) {
			if (acceptedChars.indexOf(str.charAt(i)) == -1) {
				return;
			}
		}

		if (acceptedChars.equals(FLOAT_CHARS)) {
			if (str.indexOf(".") != -1) {
				if (getText(0, getLength()).indexOf(".") != -1) {
					return;
				}
			}
		}

		if (str.indexOf("-") != -1) {
			if ((str.indexOf("-") != 0) || (offset != 0)) {
				return;
			}
		}

		if (str.indexOf("+") != -1) {
			if ((str.indexOf("+") != 0) || (offset != 0)) {
				return;
			}
		}

		if ((getText(0, getLength()).indexOf('X') != -1)
			&& !((str.equals("+") && mode != INTEGER_WITH_NEG_INFINITY)
				|| (!str.equals("-") && mode != INTEGER_WITH_POS_INFINITY))) {
			return;
		}

		if (str.indexOf("X") != -1) {
			if (getLength() > 1) {
				return;
			}

			if (getLength() == 0) {
				if (!(((str.equals("+X") || str.equals("X")) && mode != INTEGER_WITH_NEG_INFINITY)
					|| (str.equals("-X") && mode != INTEGER_WITH_POS_INFINITY))) {
					return;
				}
			}
			else {
				if (!((getText(0, 1).equals("+") && mode != INTEGER_WITH_NEG_INFINITY)
					|| (getText(0, 1).equals("-") && mode != INTEGER_WITH_POS_INFINITY))
					&& str.equals("X")) {
					return;
				}
			}
		}

		super.insertString(offset, str, attr);
	}
}

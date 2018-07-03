package mpi.eudico.client.annotator.prefs;

import mpi.eudico.client.annotator.util.ClientLogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A class to convert (non-primitive wrapper) Object classes to a  String
 * representation for storage in an Pref xml element. Only a known set of
 * classes is supported, like Color, Dimension etc.<br>
 * <br>
 * Formats:<br>
 * Color: r,g,b<br>
 * Dimension: w,h<br>
 * Point: x,y <br>
 * Rectangle: x,y,w,h<br>
 * Font: font-name,font-size,font-style(int, constant)<br>
 *
 * @author Han Sloetjes
 */
public class PrefObjectConverter implements ClientLogger {
    /**
     * Constructor.
     */
    public PrefObjectConverter() {
        super();
    }

    /**
     * Creates a proprietary String representation of a limited set of Objects
     * to store  it's state.
     *
     * @param value the Object to convert
     *
     * @return a String representation
     */
    public String objectToString(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Color) {
            Color col = (Color) value;

            return new String(col.getRed() + "," + col.getGreen() + "," +
                col.getBlue());
        } else if (value instanceof Dimension) {
            Dimension dim = (Dimension) value;

            return new String(dim.width + "," + dim.height);
        } else if (value instanceof Point) {
            Point p = (Point) value;

            return new String(p.x + "," + p.y);
        } else if (value instanceof Rectangle) {
            Rectangle r = (Rectangle) value;

            return new String(r.x + "," + r.y + "," + r.width + "," + r.height);
        } else if (value instanceof Font) {
            Font f = (Font) value;

            return new String(f.getName() + "," + f.getStyle() + "," +
                f.getSize());
        } else if (value instanceof Locale) {
        	Locale l = (Locale) value;
        	
        	return new String(l.getLanguage() + "," + l.getCountry());
        } else {
            return value.toString();
        }
    }

    /**
     * Creates an object of the specified type and restores it's state  based
     * on the given string representation.
     *
     * @param className the fully qualified Java classname
     * @param value the string representation of the value or state
     *
     * @return an initialised Object
     */
    public Object stringToObject(String className, String value) {
        if (value == null) {
            return null;
        }

        try {
            Class classObj = Class.forName(className);

            if (classObj == Color.class) {
                return stringToColor(value);
            } else if (classObj == Dimension.class) {
                return stringToDim(value);
            } else if (classObj == Point.class) {
                return stringToPoint(value);
            } else if (classObj == Rectangle.class) {
                return stringToRect(value);
            } else if (classObj == Font.class) {
                return stringToFont(value);
            } else if (classObj == Locale.class) {
                return stringToLocale(value);
            }
        } catch (LinkageError le) {
            LOG.warning("Cannot create class: " + le.getMessage());
        } catch (ClassNotFoundException cnfe) {
            LOG.warning("Cannot create class: " + cnfe.getMessage());
        }

        return null;
    }

    private Color stringToColor(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ",");

        if (tokenizer.countTokens() < 3) {
            return null;
        }

        int r;
        int g;
        int b;

        try {
            r = Integer.parseInt(tokenizer.nextToken());
            g = Integer.parseInt(tokenizer.nextToken());
            b = Integer.parseInt(tokenizer.nextToken());
        } catch (NumberFormatException nfe) {
            return null;
        }

        return new Color(r, g, b);
    }

    private Dimension stringToDim(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ",");

        if (tokenizer.countTokens() < 2) {
            return null;
        }

        int w;
        int h;

        try {
            w = Integer.parseInt(tokenizer.nextToken());
            h = Integer.parseInt(tokenizer.nextToken());
        } catch (NumberFormatException nfe) {
            return null;
        }

        return new Dimension(w, h);
    }

    private Point stringToPoint(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ",");

        if (tokenizer.countTokens() < 2) {
            return null;
        }

        int x;
        int y;

        try {
            x = Integer.parseInt(tokenizer.nextToken());
            y = Integer.parseInt(tokenizer.nextToken());
        } catch (NumberFormatException nfe) {
            return null;
        }

        return new Point(x, y);
    }

    private Rectangle stringToRect(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ",");

        if (tokenizer.countTokens() < 4) {
            return null;
        }

        int x;
        int y;
        int w;
        int h;

        try {
            x = Integer.parseInt(tokenizer.nextToken());
            y = Integer.parseInt(tokenizer.nextToken());
            w = Integer.parseInt(tokenizer.nextToken());
            h = Integer.parseInt(tokenizer.nextToken());
        } catch (NumberFormatException nfe) {
            return null;
        }

        return new Rectangle(x, y, w, h);
    }

    private Font stringToFont(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ",");

        if (tokenizer.countTokens() < 3) {
            return null;
        }

        String name;
        int h;
        int style;

        try {
            name = tokenizer.nextToken();
            style = Integer.parseInt(tokenizer.nextToken());
            h = Integer.parseInt(tokenizer.nextToken());
        } catch (NumberFormatException nfe) {
            return null;
        }

        return new Font(name, style, h);
    }
    
    private Locale stringToLocale(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        
        if (tokenizer.countTokens() == 0) {
        	return new Locale("");
        } else if (tokenizer.countTokens() == 1) {
        	return new Locale(tokenizer.nextToken());
        } else if (tokenizer.countTokens() == 2) {
        	String lng = tokenizer.nextToken();
        	return new Locale(lng, tokenizer.nextToken());
        }
        
        return new Locale("");
    }
}

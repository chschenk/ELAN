package mpi.eudico.client.annotator.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;


public class IPAGIMToHTML {
    String res = "/src/resources/guk/im/data/ipa-extension.gim";
    public IPAGIMToHTML() {
        convert();
        System.out.println("Conversion completed");
    }

    	public void convert() {
    	    String sourceFile = System.getProperty("user.dir") + res;
    	    String outFile = sourceFile.replaceFirst("gim", "html");
    	    BufferedReader bufRead = null;
    	    BufferedWriter writer = null;
    	    try {
    	        // in
	    	    FileReader fileRead = new FileReader(sourceFile);
				bufRead = new BufferedReader(fileRead);
	    	    // out
	    	    FileOutputStream out = new FileOutputStream(outFile);
	    	    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				writer = new BufferedWriter(osw);
	    	    
	    	    writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">");
	    	    writer.write("\n<html><head><title>MPI - IPA extended keyboard mapping</title></head>\n");
	    	    writer.write("<body>\n<h3>MPI - IPA extended keyboard mapping (GUK)</h3>\n");
	    	    writer.write("<table>\n");
	    	    writer.write("<tr><th align=\"left\" width=\"120\">Key sequence</th><th align=\"left\" width=\"120\">Unicode code</th><th align=\"left\">IPA character</th></tr>\n");
	    	    
	    	    String li = null;	  
	    	    char ch = '1';
	    	    StringTokenizer tokenizer;
	    	    String code;
	    	    int index = 0;
	    	    while ((li = bufRead.readLine()) != null) {
	    	        index++;
	                if (li.length() == 0 || index < 120) {
	                    continue; //empty line
	                } else {
	                    if (li.charAt(0) == '#') {
	                        continue; //comment line
	                    }
	                    tokenizer = new StringTokenizer(li);
	                    if (tokenizer.countTokens() == 4) {
	                        tokenizer.nextToken();
	                        code = tokenizer.nextToken();
	                        if (code.length() > 1) {
	                            if (code.charAt(1) != ch) {
	                                // insert empty row
	                                writer.write("<tr><td colspan=\"3\">&nbsp;</td></tr>\n");
	                                ch = code.charAt(1);
	                            }
	                        }
	                        writer.write("<tr><td>");
	                        writer.write(code + "</td><td>");
	                        tokenizer.nextToken();
	                        code = tokenizer.nextToken();
	                        writer.write(code + "</td><td>");
	                        writer.write("&#" + code.substring(1) + ";");
	                        writer.write("</td></tr>\n");
	                    }
	                }
	    	    }
	    	    
	    	    writer.write("</table></body></html>");
	    	    writer.flush();
	    	    
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    } finally {
    	    	try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException ioe) {
	    	        System.out.println("Could not close output stream...");
	    	        ioe.printStackTrace();
				}
    	    	try {
					if (bufRead != null) {
						bufRead.close();
					}
				} catch (IOException ioe) {
	    	        System.out.println("Could not close input stream...");
	    	        ioe.printStackTrace();
				}
    	    }
    	}
    	
    public static void main(String[] args) {
        new IPAGIMToHTML();
    }
}

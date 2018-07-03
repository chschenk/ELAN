package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A Command that creates a Syntax viewer.
 *
 * @author
 */
public class SyntaxViewerCommand implements Command {
	private static final String className = "mpi.syntax.elan.ElanSyntaxViewer";
    private String commandName;

    public static boolean isEnabled(){
    	Object syntaxViewerClass = null;
		try{
			syntaxViewerClass = Class.forName(className);
		}
		catch(Exception e){}
		return (syntaxViewerClass != null);
    }
		
    /**
     * Creates a new SyntaxViewerCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public SyntaxViewerCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object(Transcription)</li> <li>arg[1] = the ViewerManager
     *        (ViewerManager)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ((ViewerManager2) arguments[1]).createViewer(className, 100);        
    }

    /**
     * Returns the name of the command
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}

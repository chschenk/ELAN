package mpi.eudico.client.annotator.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class SquelchOutput {
	PrintStream oldStdout = null;
	PrintStream oldStderr = null;
	
	private class SilentPrintStream extends ByteArrayOutputStream {
		@Override
		public void flush() throws IOException {
			super.reset();
		}
	}
	
	// replaces the output writers to a null version
	@SuppressWarnings("resource") // close()d in restoreOutput().
	public void squelchOutput() throws IOException {
		// sanity check
		if ( oldStdout != null) {
			throw new IOException("Output already squelched");
		}
		
		oldStdout = System.out;
		oldStderr = System.err;
		
		System.setOut( new PrintStream( new SilentPrintStream(), true ));
	    System.setErr( new PrintStream( new SilentPrintStream(), true ));
	}
	
	public void restoreOutput() throws IOException {
		// sanity check
		if (oldStdout == null) {
			throw new IOException("Output was not squelched");
		}
		
		System.out.close();
		System.err.close();
		
		System.setOut(oldStdout);
		System.setErr(oldStderr);
		
		oldStdout = null;
		oldStderr = null;
	}
}
package mpi.eudico.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper around an InputStream that prints all data that is read through
 * it to System.out.
 * 
 * @author olasei
 */
public class DebugInputStream extends FilterInputStream {
	
	public DebugInputStream(InputStream in) {
		super(in);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int b = super.read();
		System.out.printf("read: 0x%02x '%c'\n", b, b);
		return b;
	}

//	/**
//	 * @param b
//	 * @return
//	 * @throws IOException
//	 * @see java.io.InputStream#read(byte[])
//	 */
//	@Override
//	public int read(byte[] b) throws IOException {
//		return read(b, 0, b.length);
//	}
//
	/**
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = super.read(b, off, len);
		System.out.printf("read: %d bytes: ", result);
		for (int i = 0; i < result; i++) {
			//System.out.printf(" %02x", b[off + i]);
			System.out.printf("%c", b[off + i]);
		}
		System.out.print("\n");
		return result;
	}

	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		System.out.printf("skip: %d\n", n);
		return super.skip(n);
	}

	/**
	 * @param readlimit
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(int readlimit) {
		System.out.printf("mark: %d\n", readlimit);
		super.mark(readlimit);
	}

	/**
	 * @throws IOException
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		System.out.printf("reset\n");
		super.reset();
	}

	/**
	 * @return
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		System.out.printf("markSupported\n");
		return super.markSupported();
	}
}

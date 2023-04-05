import java.io.InputStream;
import java.io.IOException;

/**
 * The BitOutputStream and BitInputStream classes provide the ability to
 * write and read individual bits to a file in a compact form.  One minor
 * limitation of this approach is that the resulting file will always have
 * a number of bits that is a multiple of 8.  In effect, whatever bits are
 * output to the file are padded at the end with 0s to make the total
 * number of bits a multiple of 8.
 * 
 * @author Marty Stepp, Stuart Reges, Helene Martin, and Owen Astrachan
 * @version 2022-02-11 (Simplified and JavaDocs added for CS483)
 */
public class BitInputStream extends InputStream {
    private InputStream input;    // actual source to read from
    private int digits;           // buffer to build up next byte's digits <= 8
    private int numDigits;        // how many digits are currently in buffer
    private boolean bitMode;      // true if writing bits; false to debug ASCII

    /**
     * Creates a BitInputStream reading bits of input from the given stream source.
     * @param input the input stream to read.
     * @throws BitIOException if the input stream cannot be accessed.
     */
    public BitInputStream(InputStream input) {
        if (input == null) {
            throw new NullPointerException("should not pass a null input stream");
        }
        this.input = input;
        digits = 0;
        numDigits = 0;
    }
    
    /**
     * Returns an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking by the next invocation of a
     * method for this input stream.
     * @return an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking, or 0 when it reaches the
     * end of the input stream.
     * @throws BitIOException if the input stream cannot be accessed.
     */
    @Override
    public int available() {
        try {
        	return input.available();
        } catch (IOException ioe) {
        	throw new BitOutputStream.BitIOException(ioe);
        }
    }

    /**
     * Closes this stream for reading. 
     * @throws BitIOException if the input stream cannot be closed.
     */
    @Override
    public void close() {
        try {
        	input.close();
        } catch (IOException ioe) {
        	throw new BitOutputStream.BitIOException(ioe);
        }
    }

    /**
     * Returns whether this stream has more bits/bytes available to be read.
     * @return true if more bits are available, otherwise false.
     */
    public boolean hasNext() {
        return digits != -1;
    }
    
    /**
     * Use readNext() or read(byte[]) instead.
     * @throws UnsupportedOperationException always
     */
    @Override
    public int read() {
		throw new UnsupportedOperationException();
    }
	
	private int readPrivate() {
		try {
			int result = input.read();
			if (BitOutputStream.DEBUG) System.out.println("  ** BitInputStream read: " + result);
			return result;
		}
		catch(IOException ioe) {
			throw new BitOutputStream.BitIOException(ioe);
		}
	}

    /**
     * Reads and returns some bytes of information from this stream into
     * the given array.
     * @param bytes array of byte to fill
     * @return the number of bytes read, or -1 if no bytes remain to read or in bit mode.
     * @throws BitIOException if the input stream cannot be read.
     * @throws NullPointerException if bytes is null. 
     */
    @Override
    public int read(byte[] bytes) {
		if(bitMode) return -1;
        return read(bytes, 0, bytes.length);
    }

    /**
     * Reads and returns some bytes of information from this stream into
     * the given array.
     * @param bytes array of byte to fill
     * @return the number of bytes read, or -1 if no bytes remain to read.
     * @throws BitIOException if the input stream cannot be read.
     * @throws NullPointerException if bytes is null. 
     * @throws ArrayIndexOutOfBoundsException if offset + length is past the end
     *                                        of the array. 
     */
    @Override
    public int read(byte[] bytes, int offset, int length) {
		if(bitMode) return -1;
        if (bytes == null) {
            throw new NullPointerException("should not pass a null byte array");
        }
		
        int count = 0;
        while (count < length) {
            int b = readPrivate();
            if (b < 0) {
                if (count == 0) {
                    return -1;
                } else {
                    break;
                }
            }
            bytes[offset + count] = (byte) b;
			count++;
        }
        return count;
    }

    /**
     * Reads and returns the next single bit of input from this stream.
     * @return the bit of information read, or -1 if no bits remain to read.
     * @throws BitIOException if the input stream cannot be read.
     */
    public int readNext() {
        int result = -1;
        if (hasNext()) {
            if (bitMode) {
                // read a single bit from our 1-byte buffer
                result = digits % 2;
                if (BitOutputStream.DEBUG) System.out.println("  ** BitInputStream readBit: " + result);
                digits /= 2;
                numDigits++;
                if (numDigits == BitOutputStream.BYTE_SIZE) {
                    buffer();   // replenish buffer if empty
                }
            } else {
                // read an entire byte
                result = readPrivate();
            }
        }
        return result;
    }
    
    /**
     * Starts reading in bit mode (one bit at a time with readNext()).
     * Ignores the caller and always uses 'byte' mode if reading from System.in
     * or if the JVM bitstream.bitmode environment variable is set.
     */
    public void startBitMode() {
		this.bitMode = input != System.in && System.getProperty("bitstream.bitmode") == null;
		buffer(); //initialize the buffer
    }

    // Refreshes the internal buffer with the next BYTE_SIZE bits.
    private int buffer() {
        int result = digits;
        digits = readPrivate();
        numDigits = 0;
        return result;
    }
}

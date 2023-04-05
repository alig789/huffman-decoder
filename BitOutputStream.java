import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

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
public class BitOutputStream extends PrintStream {
    public static final boolean DEBUG = false;   // set true for debug printlns
    public static final int BYTE_SIZE = 8;       // digits per byte

    private OutputStream output;  // actual target to write to
    private boolean open;         // true if still open for writing
    private int digits;           // buffer to build up next byte's digits <= 8
    private int numDigits;        // how many digits are currently in buffer
    private boolean bitMode;      // true if writing bits; false to debug ASCII
    
    /**
     * Creates a BitOutputStream to send output to the stream in 'bit mode'.
     * Precondition: The given file is legal and can be written.
     * @param output the target output stream to write.
     * @throws BitIOException if the file cannot be opened for writing. 
     */
    public BitOutputStream(OutputStream output) {
    	super(output);
        this.output = output;
        digits = 0;
        numDigits = 0;
        open = true;
    }

    /**
     * Closes this output stream for writing and flushes any data to be written.
     */
    public void close() {
        if (open) {
            if (numDigits > 0) {
				if (!bitMode) {
                    // pad to a multiple of 8 bits to match bit mode
                    for (int i = numDigits; i < BYTE_SIZE; i++) {
						writeBit(0);
                    }
                }
				
                flush();
            }
            
            try {
				output.close();
			} catch (IOException e) {
				throw new BitIOException(e);
			}
            open = false;
        }
    }

    /**
     * Flushes the buffer.  If numDigits < BYTE_SIZE, this will 
     * effectively pad the output with extra 0s, so this should
     * be called only when numDigits == BYTE_SIZE or when we are
     * closing the output.
     */
    public void flush() {
        if (bitMode) {
        	writePrivate(digits);
            digits = 0;
            numDigits = 0;
        }
    }
    
    /**
     * Starts this stream writing one bit at a time to the file for each 0 or 1
	 * written in writeBit.
     * Ignores the caller and always uses 'byte' mode if writing to System.out
     * or if the JVM bitstream.bitmode environment variable is set.
     */
    public void startBitMode() {
        this.bitMode = output != System.out && output != System.err
                && System.getProperty("bitstream.bitmode") == null;
    }

    /**
     * Use writeBit() or writeByte() instead
     * @param b the byte value to write.
     * @throws UnsupportedOperationException always
     */
    public void write(int b) {
		throw new UnsupportedOperationException();
    }
	
	private void writePrivate(int b) {
		if (DEBUG) System.out.println("  ** BitOutputStream write: " + b);
		try {
			output.write(b);
		} catch (IOException e) {
			throw new BitIOException(e);
		}
	}
    
    /**
     * Writes given bit to output.
     * @param bit the bit value to write.
     * @throws IllegalArgumentException if bit is not 0 or 1
     */
    public void writeBit(int bit) {
		if(!bitMode) throw new BitIOException("Writing bits in byte mode!");
		
        if (bit != 0 && bit != 1) {
            throw new IllegalArgumentException("Illegal bit value: " + bit);
        }
        
		// pad shifted bit into our digit buffer; flush when we hit 8 digits
		digits += bit << numDigits;
		numDigits++;
		if (DEBUG) System.out.println("  ** BitOutputStream writeBit: " + bit);
		if (numDigits == BYTE_SIZE) {
			flush();
		}
    }
	
	public void writeByte(int byt) {
		if(bitMode) throw new BitIOException("Writing bytes in bit mode!");
        writePrivate(byt);
	}
    
    /**
     * Writes every bit in the given string of 0s and 1s.
     * @param bits A string entirely of 0s and 1s, such as "01001100110".
     * @throws IllegalArgumentException if the string contains any characters
     *                                  other than '0' or '1'.
     */
    public void writeBits(String bits) {
		if(!bitMode) throw new BitIOException("Writing bits in byte mode!");
		
        for (int i = 0; i < bits.length(); i++) {
            char ch = bits.charAt(i);
            if (ch == '0' || ch == 0) {
                writeBit(0);
            } else if (ch == '1' || ch == 1) {
                writeBit(1);
            } else {
                throw new IllegalArgumentException("Illegal bit value '" + ch + 
                        "' at index " + i + " of string \"" + bits + "\"");
            }
        }
    }

    /**
     * A class to represent bit I/O errors as runtime exceptions.
     * Used to avoid the forced checked exceptions usually thrown in Java I/O.
     */
    public static class BitIOException extends RuntimeException {
    	private static final long serialVersionUID = 0L;
    	
    	public BitIOException(String message) {
    		super(message);
    	}
    	
    	public BitIOException(Throwable cause) {
    		super(cause);
    	}
    	
    	public BitIOException(String message, Throwable cause) {
    		super(message, cause);
    	}
    }
}
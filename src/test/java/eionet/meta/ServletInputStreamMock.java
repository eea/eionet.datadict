package eionet.meta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * An attempt to mock servlet input stream.
 *
 * @author jaanus
 */
public class ServletInputStreamMock extends ServletInputStream {

    /** */
    private InputStream instream;

    /**
     * @param name
     * @throws FileNotFoundException
     */
    public ServletInputStreamMock(String name) throws FileNotFoundException {
        instream = new FileInputStream(name);
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        return instream.read();
    }
}

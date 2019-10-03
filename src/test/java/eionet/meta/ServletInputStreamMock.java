package eionet.meta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ReadListener;

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

    @Override
    public boolean isFinished() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

package eionet.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 * 
 * This class loads data for unit tests. The root location of the data must be given in <code>PropsIF.SRC_DIR</code> property,
 * in the <code>PropsIF.TEST_PROP_FILE</code> file.
 * The <code>getResource()</code> and <code>getResourceAsStream()</code> methods load the data relative to the root location.
 *
 */
public class TestingResources {

	/** */
	private static File rootLocation;

	/**
	 * Returns the root location where this class looks for resources (see comments in class header too).
	 * 
	 * @return
	 * @throws Exception
	 */
	public static File getRootLocation(){
		
		if (rootLocation==null){
			String srcDir = Props.getProperty(PropsIF.SRC_DIR);
			if (srcDir!=null)
				rootLocation = new File(srcDir);
		}
		
		return rootLocation;
	}
	
	/**
	 * A convenience method that is equal to calling <code>getResource(null, name).</code>
	 * @param name
	 * @return
	 */
	public static synchronized URL getResource(String name){
		return TestingResources.getResource(null, name);
	}
	
	/**
	 * Loads resource of the given name. The name of the input class must identify the directory
	 * (relatively to root location) where the resources resides. If input class is <code>null</code> then
	 * the resource must be in root location.
	 * 
	 * @param c
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static synchronized URL getResource(Class c, String name){
		 
		try{
			File file = c==null ? new File(TestingResources.getRootLocation(), name) :
				new File(new File(TestingResources.getRootLocation(), TestingResources.toPath(c)), name);
			
			// using toURI().toURL() as suggested by java.io.File JavaDocs
			return file.toURI().toURL();
		}
		catch (MalformedURLException e){
			return null;
		}
	}

	/**
	 * A convenience method that is equal to calling <code>getResourceAsStream(null, name).</code>
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized InputStream getResourceAsStream(String name){
		return getResourceAsStream(null, name);
	}
	
	/**
	 * Calls <code>getResource(c, name)</code> and opens a stream to the URL that is returned.
	 * 
	 * @param c
	 * @param name
	 * @return
	 */
    public static synchronized InputStream getResourceAsStream(Class c, String name){
    	URL url = TestingResources.getResource(c, name);
    	try{
    	    return url!=null ? url.openStream() : null;
    	}
    	catch (IOException e){
    	    return null;
    	}
	}
    
	/**
	 * 
	 * @param c
	 * @return
	 */
	private static String toPath(Class c){
		return c==null ? null : c.getName().substring(0, c.getName().lastIndexOf('.')).replace('.', File.separatorChar);
	}
}

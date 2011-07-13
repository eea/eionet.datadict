package eionet.meta.filters;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DDHttpServletRequestWrapper extends HttpServletRequestWrapper{
	
	/** */
	private Hashtable parameterMap = new Hashtable();

	/**
	 * 
	 * @param request
	 */
	public DDHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		parameterMap = new Hashtable(super.getParameterMap());
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addParameter(String name, String value){
		
		if (name==null || name.length()==0 || value==null)
			return;
		
		String[] values = getParameterValues(name);
		if (values==null){
			values = new String[0];
		}
		
		if (Arrays.binarySearch(values, value)<0){ // not already in the values
			String[] newValues = new String[values.length+1];
			if (values.length>0){
				System.arraycopy(values, 0, newValues, 0, values.length);
			}
			newValues[newValues.length-1] = value;
			parameterMap.put(name, newValues);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
	 */
	public String getParameter(String name){
		String[] values = getParameterValues(name);
		return values!=null && values.length>0 ? values[0] : null; 
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterNames()
	 */
	public Enumeration getParameterNames(){
		return parameterMap.keys();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterMap()
	 */
	public Map getParameterMap(){
		return parameterMap;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name){
		return (String[])parameterMap.get(name);
	}
}

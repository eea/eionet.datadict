package eionet.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class DDServletRequestWrapper extends HttpServletRequestWrapper{

    /** */
    private HashMap<String,HashSet<String>> parameters = new HashMap<String,HashSet<String>>();

    /**
     *
     * @param request
     */
    public DDServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public DDServletRequestWrapper addParameterValue(String name, String value){

        if (StringUtils.isBlank(name)){
            throw new IllegalArgumentException("Parameter name must not be blank!");
        }

        HashSet<String> values = parameters.get(name);
        if (values==null){
            values = new HashSet<String>();
            parameters.put(name, values);
        }
        values.add(value);

        return this;
    }

    /**
     *
     * @param name
     * @param values
     * @return
     */
    public DDServletRequestWrapper addParameterValues(String name, String... values){

        if (StringUtils.isBlank(name)){
            throw new IllegalArgumentException("Parameter name must not be blank!");
        }

        if (values!=null && values.length>0){
            HashSet<String> currentValues = parameters.get(name);
            if (currentValues==null){
                currentValues = new HashSet<String>();
                parameters.put(name, currentValues);
            }
            currentValues.addAll(Arrays.asList(values));
        }

        return this;
    }

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public DDServletRequestWrapper setParameter(String name, String value){

        if (StringUtils.isBlank(name)){
            throw new IllegalArgumentException("Parameter name must not be blank!");
        }

        HashSet<String> values = new HashSet<String>();
        values.add(value);
        parameters.put(name, values);
        return this;
    }

    /**
     *
     * @param name
     * @return
     */
    public DDServletRequestWrapper removeParameter(String name){

        if (StringUtils.isBlank(name)){
            throw new IllegalArgumentException("Parameter name must not be blank!");
        }

        parameters.remove(name);
        return this;
    }

    /**
     *
     * @param name
     * @param oldValue
     * @param newValue
     * @return
     */
    public DDServletRequestWrapper replaceParameterValue(String name, String oldValue, String newValue){

        if (StringUtils.isBlank(name)){
            throw new IllegalArgumentException("Parameter name must not be blank!");
        }

        HashSet<String> values = parameters.get(name);
        if (values==null){
            values = new HashSet<String>();
            parameters.put(name, values);
        }
        values.remove(oldValue);
        values.remove(newValue);

        return this;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    public String getParameter(String name){

        String result = null;

        // First, try to get the value from this parameter map,
        // if that doesn't succeed, get the value from wrapped request.

        HashSet<String> values = parameters.get(name);
        if (values!=null && !values.isEmpty()){
            result = values.iterator().next();
        }

        if (result==null){
            result = getRequest().getParameter(name);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    public Map getParameterMap(){
        throw new UnsupportedOperationException("Mehotd not implemented!");
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    public Enumeration getParameterNames(){

        Set<String> names = parameters.keySet();
        if (names==null || names.isEmpty()){
            names = new HashSet<String>();
        }

        Enumeration wrappedNames = getRequest().getParameterNames();
        while (wrappedNames!=null && wrappedNames.hasMoreElements()){
            names.add(wrappedNames.nextElement().toString());
        }

        String namesString = StringUtils.join(names, ' ');
        return new StringTokenizer(namesString);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name){

        HashSet<String> values = parameters.get(name);
        if (values==null){
            values = new HashSet<String>();
        }

        String[] wrappedValues = getRequest().getParameterValues(name);
        if (wrappedValues!=null && wrappedValues.length>0){
            values.addAll(Arrays.asList(wrappedValues));
        }

        String result[] = null;
        if (!values.isEmpty()){
            result = new String[values.size()];
            values.toArray(result);
        }
        return result;
    }
}

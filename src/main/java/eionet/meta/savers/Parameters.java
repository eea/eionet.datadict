
package eionet.meta.savers;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

public class Parameters {
    
    private HttpServletRequest req = null;
    private String id = null;
    private Hashtable params = new Hashtable();
    
    private String serverName = null;
    private int serverPort = 80;
    private String contextPath = "";
    
    public Parameters(){
        
        StringBuffer buf = new StringBuffer("eionet_meta_savers_Parameters");
        buf.append("_");
        buf.append(Thread.currentThread().getName());
        buf.append("_");
        buf.append(System.currentTimeMillis());
        
        this.id = buf.toString();
    }
    
    public Parameters(Hashtable params){
        this();
        this.params = params;
    }
    
    public Parameters(HttpServletRequest req){
        this.req = req;
        this.id = req.getRequestedSessionId();
    }
    
    public String getID(){
        return id;
    }
    
    public String getParameter(String parName){
        
        if (parName == null) return null;
        if (req != null) return req.getParameter(parName);
        
        Vector parValues = (Vector)params.get(parName);
        if (parValues == null || parValues.size()==0) return null;
        
        return (String)parValues.get(0);
    }
    
    public String[] getParameterValues(String parName){
        
        if (parName == null) return null;
        if (req != null) return req.getParameterValues(parName);
        
        Vector parValues = (Vector)params.get(parName);
        if (parValues == null || parValues.size()==0) return null;
        
        String[] vs = new String[parValues.size()];
        for (int i=0; i<parValues.size(); i++)
            vs[i] = (String)parValues.get(i);
        
        return vs;
    }
    
    public Enumeration getParameterNames(){        
        if (req != null) return req.getParameterNames();
        return params.keys();
    }
    
    public void addParameterValue(String name, String value){
        
        if (name == null || value == null) return;        
        if (req != null) return;
        
        Vector parValues = (Vector)params.get(name);
        if (parValues == null)
            parValues = new Vector();
        
        parValues.add(value);        
        params.put(name, parValues);
    }
    
    public void addParameterValues(String name, Vector values){
        
        if (name == null || values == null) return;
        if (req != null) return;
        
        params.put(name, values);
    }
    
    public void removeParameter(String name){
        
        if (name == null) return;
        if (req != null) return;
        params.remove(name);
    }
    
    public HttpServletRequest getHttpServletRequest(){
        return req;
    }
    
    public String getServerName(){
        if (req == null)
            return serverName;
        else
            return req.getServerName();
    }
    
    public int getServerPort(){
        if (req == null)
            return serverPort;
        else
            return req.getServerPort();
    }
    
    public String getContextPath(){
        if (req == null)
            return contextPath;
        else
            return req.getContextPath();
    }
    
    public void setServerName(String serverName){
        this.serverName = serverName;
    }
    
    public void setServerPort(int serverPort){
        this.serverPort = serverPort;
    }
    
    public void setContextPath(String contextPath){
        this.contextPath = contextPath;
    }
    
    /**
     * 
     * @return
     */
    public int getSize(){
        return params==null ? 0 : params.size();
    }
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args){
        
        Parameters pars = new Parameters();
        
        pars.addParameterValue("kala", "ahven");
        pars.addParameterValue("kala", "haug");
        pars.addParameterValue("kala", "siig");
        
        pars.addParameterValue("auto", "mersu");
        
        Enumeration names = pars.getParameterNames();
        while (names.hasMoreElements()){
            System.out.println(names.nextElement());
        }
        
        System.out.println("---");
        
        String[] kalad = pars.getParameterValues("kala");
        for (int i=0; kalad!=null && i<kalad.length; i++)
            System.out.println(kalad[i]);
        
        System.out.println("---");        
        System.out.println(pars.getParameter("auto"));
        System.out.println("---");        
        System.out.println(pars.getID());
    }
}

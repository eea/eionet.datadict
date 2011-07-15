/*
 * Created on 6.11.2006
 */
package eionet.meta;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author jaanus
 */
public class PublicOutputs {
    
    /** */
    public static Hashtable orders = null;
    public static Hashtable weights = null;

    /**
     * 
     */
    public static HashSet process(int number, Class c){
        
        HashSet result = new HashSet();
        Hashtable w = getWeights(c);
        for (Enumeration e=w.keys(); e.hasMoreElements();){
            String output = (String)e.nextElement();
            Integer weight = (Integer)w.get(output);
            int div = number/weight.intValue();
            if (div % 2 != 0)
                result.add(output);
        }
        
        return result;
    }
    
    /**
     * 
     * @param c
     * @return
     */
    public static Vector getOrder(Class c){
        
        if (orders==null)
            init();
        return (Vector)orders.get(c);
    }
    /**
     * @param c
     * @return
     */
    public static Hashtable getWeights(Class c){
        
        if (weights==null)
            init();
        return (Hashtable)weights.get(c);
    }
    
    /**
     * 
     */
    private static void init(){

        orders = new Hashtable();
        weights = new Hashtable();

        // init for Dataset
        Vector v = new Vector();
        v.add("PDF");
        v.add("XMLSCHEMA");
        v.add("XMLINST");
        v.add("XLS");
        v.add("ODS");
        v.add("MDB");
        Hashtable h = new Hashtable();
        h.put("PDF", new Integer(1));
        h.put("XLS", new Integer(2));
        h.put("XMLINST", new Integer(4));
        h.put("XMLSCHEMA", new Integer(8));
        h.put("MDB", new Integer(16));
        h.put("ODS", new Integer(32));
        orders.put(Dataset.class, v);
        weights.put(Dataset.class, h);
    }
}

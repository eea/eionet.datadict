/*
 * Created on 26.04.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.notif.util;

import java.util.Vector;

/*
 *
 */
public class RDFTriple {

    /** */
    private String subject = null;
    private String predicate = null;
    private String object = null;

    /*
     *
     */
    public RDFTriple() {
    }

    /*
     *
     */
    public String getObject() {
        return object;
    }

    /*
     *
     */
    public void setObject(String object) {
        this.object = object;
    }

    /*
     *
     */
    public String getPredicate() {
        return predicate;
    }

    /*
     *
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /*
     *
     */
    public String getSubject() {
        return subject;
    }

    /*
     *
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /*
     *
     */
    public Vector toVector() {

        Vector v = new Vector();
        v.add(subject);
        v.add(predicate);
        v.add(object);

        return v;
    }
}

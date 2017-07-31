/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.errors;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class XmlExportException extends Exception {

    public XmlExportException() {
    }

    public XmlExportException(String string) {
        super(string);
    }

    public XmlExportException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public XmlExportException(Throwable thrwbl) {
        super(thrwbl);
    }

    public XmlExportException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
}


// Copyright (c) 2000 TietoEnator
package eionet.meta.imp;

import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import eionet.meta.DDUser;


/**
 * A Class class.
 * <P>
 * @author Enriko Käsper
 */
public class BaseHandler extends DefaultHandler {

    private boolean errorOrWarning;   //true, if any error found
    private StringBuffer errorBuff;   // error description
    private Locator locator = null;
    
    protected DDUser user = null;
    
  /**
   * Constructor
   */
    public BaseHandler() {
      this.errorBuff = new StringBuffer();
      this.errorOrWarning = false;
    }


    public void setDocumentLocator(Locator locator){
        this.locator = locator;
    }

    public int getLine(){
        if (this.locator != null)
            return locator.getLineNumber();
        else
            return -1;
    }
    public boolean hasError(){
        return errorOrWarning;
    }
    public StringBuffer getErrorBuff(){
        return errorBuff;
    }
    public void setError(String err){
        errorOrWarning = true;
        errorBuff.append(err);
    }
    
    public void setUser(DDUser user){
        this.user = user;
    }
}


/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is "EINRC-4 / Meta Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2002 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */
 
package eionet.util;

import javax.servlet.http.*;
import com.tee.uit.security.*;

import eionet.meta.DDRuntimeException;
import eionet.meta.DDUser;

import java.util.*; 

/**
 * This is a class containing several utility methods for keeping
 * security.
 *
 * @author Jaanus Heinlaid
 */
public class SecurityUtil {
    
    public static final String REMOTEUSER = "eionet.util.SecurityUtil.user";
    
    /**
    * Returns current user, or 'null', if the current session
    * does not have user attached to it.
    */
    public static final DDUser getUser(HttpServletRequest servReq) {
        
    	DDUser user = null;
              
        HttpSession httpSession = servReq.getSession(false);
        if (httpSession != null) {
            user = (DDUser)httpSession.getAttribute(REMOTEUSER);
        }
        
        if (user != null)
            return user.isAuthentic() ? user : null;
        else 
            return null;
    }
    
    /**
    * If needed, creates new HttpSession and adds authenticated user object to it.
    * This method will be called anly by login servlet (<CODE>eionet.meta.LoginServlet</CODE>).
    * Throws GeneralException, if the passed user object is not authenticated.
    */
    public static final DDUser allocSession(HttpServletRequest servReq, DDUser user) {
    	
        HttpSession httpSession = servReq.getSession(true);
        if (user.isAuthentic())
            httpSession.setAttribute(REMOTEUSER, user);
        else
            throw new DDRuntimeException("Attempted to store unauthorised user");
                
        return user;
    }
    
    /**
    * Frees current <CODE>HttpSession</CODE> object and if it had user attached to it, invalidates the user. 
    */
    public static final void freeSession(HttpServletRequest servReq) {
        HttpSession httpSession = servReq.getSession(false);
        if (httpSession != null) {
            DDUser user = (DDUser)httpSession.getAttribute(REMOTEUSER);
            if (user != null){
            	user.invalidate();
            	httpSession.removeAttribute(REMOTEUSER);
            }
                
	        httpSession.invalidate();
        }
    }
    
    /**
     * 
     * @param usr
     * @param aclPath
     * @param prm
     * @return
     * @throws Exception
     */
    public static boolean hasPerm(String usr, String aclPath, String prm)
    														throws Exception{
    	if (!aclPath.startsWith("/")) return false;
    	
    	boolean has = false;
		AccessControlListIF acl = null;
		int i =
		aclPath.length()<=1 ? -1 : aclPath.indexOf("/", 1); // not forgetting root path ("/")
		while (i!=-1 && !has){
			String subPath = aclPath.substring(0,i);
			try{
				acl = AccessController.getAcl(subPath);
			}
			catch (Exception e){
				acl = null;
			}
			
			if (acl!=null)
				has = acl.checkPermission(usr, prm);
			
			i = aclPath.indexOf("/", i+1);
		}
		
		if (!has){
			try{
				acl = AccessController.getAcl(aclPath);
			}
			catch (Exception e){
				acl = null;
			}
			
			if (acl!=null)
				has = acl.checkPermission(usr, prm);
		}
    	
    	return has;
    }
    
	/**
	 * 
	 * @param usr
	 * @param aclPath
	 * @param prm
	 * @return
	 * @throws Exception
	 */
    public static boolean hasChildPerm(String usr, String aclPath, String prm)
															throws Exception{
		HashMap acls = AccessController.getAcls();
		Iterator aclNames = acls.keySet().iterator();
		AccessControlListIF acl;
		while (aclNames.hasNext()){
			String aclName = (String)aclNames.next();
			if (aclName.startsWith(aclPath)){
				acl = (AccessControlListIF)acls.get(aclName);
				if (acl.checkPermission(usr, prm))
					return true;
			}
		}
		
		return false;
	}
}

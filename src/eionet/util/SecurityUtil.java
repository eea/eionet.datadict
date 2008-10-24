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

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import com.tee.uit.security.*;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.meta.AfterCASLoginServlet;
import eionet.meta.DDCASUser;
import eionet.meta.DDRuntimeException;
import eionet.meta.DDUser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*; 

/**
 * This is a class containing several utility methods for keeping
 * security.
 *
 * @author Jaanus Heinlaid
 */
public class SecurityUtil {
    
	/** */
    public static final String REMOTEUSER = "eionet.util.SecurityUtil.user";
    
    /**
    * Returns current user, or null, if the current session
    * does not have user attached to it.
    */
    public static final DDUser getUser(HttpServletRequest request) {
        
        HttpSession session = request.getSession();
        DDUser user = session==null ? null : (DDUser)session.getAttribute(REMOTEUSER);
        
        if (user==null){
        	String casUserName = (String)session.getAttribute(CASFilter.CAS_FILTER_USER);
        	if (casUserName!=null){
        		user = DDCASUser.create(casUserName);
				session.setAttribute(REMOTEUSER, user);
        	}
        }
        else if (user instanceof DDCASUser){
        	String casUserName = (String)session.getAttribute(CASFilter.CAS_FILTER_USER);
        	if (casUserName==null){
        		user.invalidate();
        		user = null;
        		session.removeAttribute(REMOTEUSER);
        	}
        	else if (!casUserName.equals(user.getUserName())){
        		user.invalidate();
        		user = DDCASUser.create(casUserName);
				session.setAttribute(REMOTEUSER, user);
        	}
        }
        
        if (user != null)
            return user.isAuthentic() ? user : null;
        else 
            return null;
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

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getLoginURL(HttpServletRequest request) {
		
		String result = "javascript:login()";
		
		String casLoginUrl = request.getSession().getServletContext().getInitParameter(CASFilter.LOGIN_INIT_PARAM);
		if (casLoginUrl!=null){

			StringBuffer afterLoginUrl = new StringBuffer(request.getRequestURL());
			if (request.getQueryString()!=null)
				afterLoginUrl.append("?").append(request.getQueryString());
			request.getSession().setAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME, afterLoginUrl.toString());

			StringBuffer loginUrl = new StringBuffer(casLoginUrl);
			loginUrl.append("?service=");
			try {
				loginUrl.append(URLEncoder.encode(getUrlWithContextPath(request) + "/login", "UTF-8"));
				result = loginUrl.toString();
			}
			catch (UnsupportedEncodingException e) {
				throw new DDRuntimeException(e.toString(), e);
			}
		}
		
		return result;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getLogoutURL(HttpServletRequest request){
		
		String result = "index.jsp";
		
		String casLoginUrl = request.getSession().getServletContext().getInitParameter(CASFilter.LOGIN_INIT_PARAM);
		if (casLoginUrl!=null){
			
			StringBuffer buf = new StringBuffer(casLoginUrl.replaceFirst("/login", "/logout"));
			try {
				buf.append("?url=").append(URLEncoder.encode(getUrlWithContextPath(request), "UTF-8"));
				result = buf.toString();
			}
			catch (UnsupportedEncodingException e) {
				throw new DDRuntimeException(e.toString(), e);
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	private static String getUrlWithContextPath(HttpServletRequest request){
		
		StringBuffer url = new StringBuffer(request.getScheme());
		url.append("://").append(request.getServerName());
		if (request.getServerPort()>0)
			url.append(":").append(request.getServerPort());
		url.append(request.getContextPath());
		return url.toString();
	}
}

package eionet.meta.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.meta.DDUser;
import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;
import eionet.util.SecurityUtil;

public class EionetCASFilter extends CASFilter {
	
	private static boolean initCalled = false;
	
	public static final String EIONET_LOGIN_COOKIE_NAME = "eionetCasLogin";

	private static LogServiceIF logger = new Log4jLoggerImpl();

	private static final String EIONET_COOKIE_LOGIN_PATH = "eionetCookieLogin";

	private static String CAS_LOGIN_URL = null;

	private static String SERVER_NAME = null;

	private static String EIONET_LOGIN_COOKIE_DOMAIN = null;

	public void init(FilterConfig config) throws ServletException {
		
		// JH
		EionetCASFilter.initCalled = true;
		
		CAS_LOGIN_URL = config.getInitParameter(LOGIN_INIT_PARAM);
		SERVER_NAME = config.getInitParameter(SERVERNAME_INIT_PARAM);
		EIONET_LOGIN_COOKIE_DOMAIN = config.getInitParameter("eionetLoginCookieDomain");
		super.init(config);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws ServletException, IOException {
		
		CASFilterChain chain = new CASFilterChain();
		super.doFilter(request, response, chain);

		if (chain.isDoNext()) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpSession session = httpRequest.getSession();
			if (session != null && session.getAttribute(SecurityUtil.REMOTEUSER) == null) {
				DDUser user = new DDUser();
				String userName = (String) session.getAttribute(CAS_FILTER_USER);
				user.authenticate(userName, null);
				session.setAttribute(SecurityUtil.REMOTEUSER, user);
				logger.debug("Logged in user " + session.getAttribute(CAS_FILTER_USER));
				String requestURI = httpRequest.getRequestURI();
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				if (requestURI.indexOf(EIONET_COOKIE_LOGIN_PATH) > -1) {
					redirectAfterEionetCookieLogin(httpRequest, httpResponse);
					return;
				} else if (requestURI.endsWith("/login")) {
					attachEionetLoginCookie(httpResponse, true);
					if (session.getAttribute("afterLogin") != null)
						httpResponse.sendRedirect(session.getAttribute("afterLogin").toString());
					else
						request.getRequestDispatcher("/").forward(request,response);
					return;
				}
			}
			fc.doFilter(request, response);
			return;
		}
	}

	public static void attachEionetLoginCookie(HttpServletResponse response, boolean isLoggedIn) {
		Cookie tgc = new Cookie(EIONET_LOGIN_COOKIE_NAME, isLoggedIn ? "loggedIn" : "loggedOut");
		tgc.setMaxAge(-1);
		if (!EIONET_LOGIN_COOKIE_DOMAIN.equalsIgnoreCase("localhost"))
			tgc.setDomain(EIONET_LOGIN_COOKIE_DOMAIN);
		tgc.setPath("/");			
		response.addCookie(tgc);		
	}

	public static String getCASLoginURL(HttpServletRequest request) {
		request.getSession(true).setAttribute("afterLogin",request.getRequestURL().toString() + (request.getQueryString() != null ? ("?" +request.getQueryString()):"" ));
		return CAS_LOGIN_URL + "?service=" + request.getScheme() + "://" + SERVER_NAME + request.getContextPath() + "/login";
	}

	public static String getCASLogoutURL(HttpServletRequest request) {
		return CAS_LOGIN_URL.replaceFirst("/login", "/logout") + "?url=" + request.getScheme() + "://" + SERVER_NAME + request.getContextPath();
	}

	public static String getEionetCookieCASLoginURL(HttpServletRequest request) {

		String contextPath = request.getContextPath();
		String serviceURL =  request.getRequestURL().toString(); 
		if (request.getQueryString() != null && request.getQueryString().length() > 0){
			serviceURL = serviceURL + "?" + request.getQueryString();
		}
		String serviceURI = serviceURL.substring(serviceURL.indexOf("/", serviceURL.indexOf("://") + 3));
		
		if (contextPath.equals("")) {
			if (serviceURI.equals("/"))
				serviceURL = serviceURL + EIONET_COOKIE_LOGIN_PATH + "/";
			else
				serviceURL = serviceURL.replaceFirst(forRegex(serviceURI), "/" + EIONET_COOKIE_LOGIN_PATH + serviceURI);
		} else {
			String servletPath = serviceURI.substring(contextPath.length(), serviceURI.length());
			if (serviceURI.equals("/"))
				serviceURL = serviceURL + EIONET_COOKIE_LOGIN_PATH + "/";
			else{
				serviceURL = serviceURL.replaceFirst(forRegex(serviceURI), contextPath + "/" + EIONET_COOKIE_LOGIN_PATH + servletPath);				
			}				
		}
		try {
			serviceURL = URLEncoder.encode(serviceURL,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		
		return CAS_LOGIN_URL + "?service=" +   serviceURL ;

	}

	private void redirectAfterEionetCookieLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String requestUri = request.getRequestURI() + (request.getQueryString() != null ? ("?" +request.getQueryString()):"" );
		if (request.getQueryString() != null && request.getQueryString().length() > 0){
			requestUri = requestUri + "?" + request.getQueryString();
		}		
		String realURI = null;
		if (requestUri.endsWith(EIONET_COOKIE_LOGIN_PATH + "/"))
			realURI = requestUri.replaceFirst(EIONET_COOKIE_LOGIN_PATH + "/", "");
		else
			realURI = requestUri.replaceFirst("/" + EIONET_COOKIE_LOGIN_PATH, "");
		response.sendRedirect(realURI);
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean hasInitBeenCalled(){
		return EionetCASFilter.initCalled;
	}

	
	
	  public static String forRegex(String aRegexFragment){
	    final StringBuffer result = new StringBuffer();

	    final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
	    char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	      /*
	      * All literals need to have backslashes doubled.
	      */
	      if (character == '.') {
	        result.append("\\.");
	      }
	      else if (character == '\\') {
	        result.append("\\\\");
	      }
	      else if (character == '?') {
	        result.append("\\?");
	      }
	      else if (character == '*') {
	        result.append("\\*");
	      }
	      else if (character == '+') {
	        result.append("\\+");
	      }
	      else if (character == '&') {
	        result.append("\\&");
	      }
	      else if (character == ':') {
	        result.append("\\:");
	      }
	      else if (character == '{') {
	        result.append("\\{");
	      }
	      else if (character == '}') {
	        result.append("\\}");
	      }
	      else if (character == '[') {
	        result.append("\\[");
	      }
	      else if (character == ']') {
	        result.append("\\]");
	      }
	      else if (character == '(') {
	        result.append("\\(");
	      }
	      else if (character == ')') {
	        result.append("\\)");
	      }
	      else if (character == '^') {
	        result.append("\\^");
	      }
	      else if (character == '$') {
	        result.append("\\$");
	      }
	      else {
	        //the char is not a special one
	        //add it to the result as is
	        result.append(character);
	      }
	      character = iterator.next();
	    }
	    return result.toString();
	  }
}

class CASFilterChain implements FilterChain {

	private boolean doNext = false;

	public void doFilter(ServletRequest request, ServletResponse response) {
		doNext = true;
	}

	public boolean isDoNext() {
		return doNext;
	}
}
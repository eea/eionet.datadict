<%@ page import="eionet.meta.filters.EionetCASFilter" %>

		<meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
		
		<link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/print.css" media="print" />
		<link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/handheld.css" media="handheld" />		
		<link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/screen.css" media="screen" />
		<link rel="stylesheet" type="text/css" href="eionet2007.css" media="screen"/>
				
		<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
		<script type="text/javascript" src="script.js"></script>
		<script type="text/javascript" src="pageops.js"></script>
		<script type="text/javascript" src="mark_special_links.js"></script>

		<% if ( session.getAttribute(EionetCASFilter.CAS_FILTER_USER) == null )  {%>
		<script type="text/javascript" >
		// <![CDATA[
				function get_cookie( cookie_name )
				{
				  var results = document.cookie.match ( cookie_name + '=(.*?)(;|$)' );				
				  if ( results )
				    return ( unescape ( results[1] ) );
				  else
				    return null;
				}
				eionetLoginCookieValue = get_cookie("<%= EionetCASFilter.EIONET_LOGIN_COOKIE_NAME %>");
				if (eionetLoginCookieValue != null && eionetLoginCookieValue == "loggedIn"){	
					window.location="<%=EionetCASFilter.getEionetCookieCASLoginURL(request) %>";
				}
		// ]]>
		</script>
		<%}%>

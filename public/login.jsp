<%@page contentType="text/html" import="java.util.*,com.caucho.sql.*,java.sql.*,eionet.meta.*"%>

<%!private final static String USER_SESSION_ATTRIBUTE="DataDictionaryUser";%>

<%!
	private void allocSession(HttpServletRequest req, DDuser user) throws Exception {
		HttpSession httpSession = req.getSession(true);
    	if (user.isAuthentic() == true) {
			httpSession.setAttribute(USER_SESSION_ATTRIBUTE, user);
    	}
    	else {
        	throw new Exception("Attempted to store unauthorised user");
		}
	}
	
	private void freeSession(HttpServletRequest req) {		
		HttpSession httpSession = req.getSession(false);
      	if (httpSession != null) {
        	DDuser user = (DDuser)httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
         	if (user != null)
	            user.invalidate();
            
			httpSession.invalidate();
      	}     
	}
	private DDuser getUser(HttpServletRequest req) {
	
		DDuser user = null;
    
	    HttpSession httpSession = req.getSession(false);
		if (httpSession != null) {
    		user = (DDuser)httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
		}
      
		if (user != null)
	    	return user.isAuthentic() ? user : null;
		else 
	    	return null;
	}
%>

<%
//this is for logging out
	String logout = request.getParameter("logout");
      
    if (logout != null){
        if (logout.equals("true"))
        {
   			DDuser user = getUser(request);
            if (user != null)
                {
	                user.invalidate();
				}
                %>
				<html><body><table width='100%' height='100%'><tr><td align='center'><b>Logging out...</b></td></tr></table><script>window.opener.document.location.reload(true); window.setTimeout('window.close()', 1000);</script></body></html>
				<%
        }
    }

	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	if (appName == null) appName = "datadict";
	DDuser user = new DDuser(DBPool.getPool(appName));
	
	String username = request.getParameter("j_username");
	String password = request.getParameter("j_password");
	

	
	if (user.authenticate(username, password) == true) {
		allocSession(request, user);
		%>		
			<html>
				<script>window.close()</script>
			</html>
		<%
	}
	else{
		
		freeSession(request);
		
  		%>
  			<html>
			<head>
				<link href="eionet.css" rel="stylesheet" type="text/css"/>
				<script language="JAVASCRIPT">
					function reLogin(){
						window.location.href="login.html";
					}
				</script>
			</head>
			<body style="background-color:#f0f0f0;background-image:url('../images/eionet_background2.jpg');background-repeat:repeat-y;" topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">
			<center>
				<form>
				<table>
					<tr height="20pts"><td colspan="2">&nbsp;</td></tr>
						<tr><td colspan="2"><h2>User Authentication failed!</h2></td></tr>
						<tr><td colspan="2"><h3>You will not be able to save or delete any data!</h3></td></tr>
						<tr height="10pts"><td colspan="2">&nbsp;</td></tr>
						<tr>
							<td>
								<input width="20pts" name="OK" type="button" value="OK" onclick="window.close()"></input>
							</td>
							<td>
								<input name="BACK" type="button" value="Try Again..." onclick="reLogin()"></input>
							</td>
						</tr>
				</table>
				</form>
			</center>
			</body>
			</html>
  		<%
	}
%>
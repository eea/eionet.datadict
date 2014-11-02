<%@page contentType="text/html;charset=UTF-8" import="eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="/pages/common/taglibs.jsp"%>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary - Log in</title>
</head>
<body>
<div id="container">

	<jsp:include page="nlocation.jsp" flush="true">
	    <jsp:param name="name" value="Login"/>
	</jsp:include>
	<%@ include file="nmenu.jsp" %>

	<div id="workarea">

	    <h1>Log in</h1>

	    <%
	    if (request.getParameter("err") != null) {
	        %>
	        <div class="error-msg">
                There was a login error!<br/>
                Please check your username/password and try again. If the problem persists, please contact system administrators.
	        </div>
	        <%
	    }
	    %>

	    <form id="LOGIN" method="post" action="Login">

			<table width="100%" cellspacing="0" cellpadding="0">
			    <tr>
			        <td align="right" class="login">&nbsp;Username:&nbsp;</td>
			        <td class="login">
			            <input size="25" type="text" class="smalltext" name="j_username"/>
			        </td>
			    </tr>
			    <tr>
			        <td align="right" class="login">&nbsp;Password:&nbsp;</td>
			        <td class="login">
			            <input size="25" type="password" class="smalltext" name="j_password"/>
			        </td>
			    </tr>
			    <tr>
			        <td align="right" colspan="2">
			            <input name="SUBMIT" type="submit" class="mediumbuttonb" value="Login"/>
			        </td>
			    </tr>
			    <tr>
			        <td align="right" colspan="2">
			            <input name="RESET" type="reset" class="mediumbuttonb" value="Clear Fields"/>
			        </td>
			    </tr>
			</table>

		</form>

	</div> <!-- workarea -->
</div> <!-- container -->

<%@ include file="footer.jsp" %>

</body>
</html>

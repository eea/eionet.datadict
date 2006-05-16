<%@page import="eionet.util.SecurityUtil, com.tee.xmlserver.AppUserIF"%>

<%
boolean loggedIn = false;
AppUserIF userProtarea = SecurityUtil.getUser(request);
if (userProtarea!=null && userProtarea.isAuthentic()) loggedIn = true;
%>

<div id="login_box" class="TeaserBox">
			<h2>Protected area</h2>
				<%
				if (!loggedIn){ %>
				
					<form target="_blank" name="LOGIN" method="post" action="Login">
						<input type="hidden" name="target" value="blank"/>
		  				<table border="0" width="100%" cellspacing="0" cellpadding="2">
		                    <tr>
								<td width="31%"><input type="text" size="20" name="j_username"/></td>
								<td width="69%">(EIONET user ID)</td>
		                    </tr>
		                    <tr>
								<td width="31%"><input type="password" size="20" name="j_password"/></td>
								<td width="69%">(Password)</td>
		                    </tr>
		                    <tr>
								<td width="31%" align="right"><input type="submit" value="Login" class="loginbutton"/></td>
								<td width="69%"></td>
		                    </tr>
		  				</table>
		  			</form><%
  				}
  				else{ %>
  					<form name="LOGOUT" method="post" action="logout.jsp">
  						<input type="submit" value="Logout" class="loginbutton"/>&nbsp;(<%=userProtarea.getUserName()%>)
  					</form><%
				}
  				%>
  				
	</div>

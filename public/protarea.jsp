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
								<p><input type="text" size="20" name="j_username" id="j_username" style="margin-left:2em; width:31%"/>
								<label for="j_username">(EIONET user ID)</label></p>
								<p><input type="password" size="20" name="j_password" id="j_password" style="margin-left:2em; width:31%"/>
								<label for="j_password">(Password)</label></p>
								<p style="text-align:center">
								<input type="submit" value="Login" class="loginbutton"/>
								</p>
		  			</form><%
  				}
  				else{ %>
  					<form name="LOGOUT" method="post" action="logout.jsp">
  						<input type="submit" value="Logout" class="loginbutton"/>&nbsp;(<%=userProtarea.getUserName()%>)
  					</form><%
				}
  				%>
  				
	</div>

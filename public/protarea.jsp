<%@page import="eionet.util.SecurityUtil, com.tee.xmlserver.AppUserIF"%>

<%
boolean loggedIn = false;
AppUserIF userProtarea = SecurityUtil.getUser(request);
if (userProtarea!=null && userProtarea.isAuthentic()) loggedIn = true;
%>

<td width="50%" style="border: 1px solid #FF9900" valign="top">	
	<table border="0" width="100%" cellspacing="0" cellpadding="2">
		<tr height="20">
			<td align="center" width="100%" valign="top" class="front_page_prot_area" >
				Protected area
  			</td>
		</tr>
		<tr>
			<td width="100%" valign="top" align="left">
				
				<%
				if (!loggedIn){ %>
				
					<form acceptcharset="UTF-8" target="_blank" name="LOGIN" method="POST" action="Login">
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
  					<form acceptcharset="UTF-8" name="LOGOUT" method="POST" action="logout.jsp">
  						<input type="submit" value="Logout" class="loginbutton"/>&nbsp;(<%=userProtarea.getUserName()%>)
  					</form><%
				}
  				%>
  				
			</td>
		</tr>
	</table>
</td>

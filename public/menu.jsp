<%@page import="com.tee.uit.security.AccessController, com.tee.uit.security.AccessControlListIF, eionet.util.SecurityUtil,com.tee.xmlserver.AppUserIF"%>

<table cellspacing="0" cellpadding="0" border="0">

	<tr><td align="left"><span class="head0">Contents</span></td></tr>
	<tr><td align="right"><a onmouseout="Out('img0')" onmouseover="Over('img0')" href="datasets.jsp?SearchType=SEARCH"><img alt="" border="0" src="images/off.gif" name="img0" width="16" height="13" /><img border="0" src="images/button_datasets.gif" width="84" height="13" /></a></td></tr>
	<tr><td align="right"><a onmouseout="Out('img1')" onmouseover="Over('img1')" href="search_results_tbl.jsp?SearchType=SEARCH"><img alt="" border="0" src="images/off.gif" name="img1" width="16" height="13" /><img border="0" src="images/button_tables.gif" width="84" height="13" /></a></td></tr>
	<tr><td align="right"><a onmouseout="Out('img2')" onmouseover="Over('img2')" href="search.jsp"><img alt="" border="0" src="images/off.gif" name="img2" width="16" height="13" /><img border="0" src="images/button_dataelements.gif" width="84" height="13" /></a></td></tr>
	<tr><td>&nbsp;</td></tr>
	<%
	AppUserIF _user = SecurityUtil.getUser(request);
	%>
	<tr>
		<td align="right">
			<%
			if (_user!=null && _user.isAuthentic()) {
				%>
		        <a onmouseout="Out('img3')" onmouseover="Over('img3')" href="logout.jsp">
		        	<img alt="" border="0" src="images/off.gif" name="img3" width="16" height="13" /><img alt="Logout" height="13" width="84" border="0" src="images/button_logout.gif" />
		        </a>
				<%
			}
			else{
				%>
		        <a onmouseout="Out('img3')" onmouseover="Over('img3')" href="javascript:login()">
		        	<img alt="" border="0" src="images/off.gif" name="img3" width="16" height="13" /><img alt="Login" height="13" width="84" border="0" src="images/button_login.gif">
		        </a>
		        <%
		    }
		    %>
		</td>
	</tr>
  
	<%
  	if (_user!=null && _user.isAuthentic()) { %>
  	
	    <tr><td>&nbsp;</td></tr>
	    <tr><td align="left"><span class="head0">Administration</span></td></tr>
		<tr><td align="right"><a onmouseout="Out('img4')" onmouseover="Over('img4')" href="attributes.jsp"><img alt="" border="0" src="images/off.gif" name="img4" width="16" height="13" /><img border="0" src="images/button_attributes.gif" width="84" height="13" /></a></td></tr>
		
		<%
    	if (SecurityUtil.hasPerm(_user.getUserName(), "/import", "x")){ %>
			<tr><td align="right"><a onmouseout="Out('img6')" onmouseover="Over('img6')" href="import.jsp"><img alt="" border="0" src="images/off.gif" name="img6" width="16" height="13" /><img border="0" src="images/import.gif" width="84" height="13" /></a></td></tr> <%
		}

		if (SecurityUtil.hasPerm(_user.getUserName(), "/cleanup", "x")){ %>
			<tr><td align="right"><a onmouseout="Out('img7')" onmouseover="Over('img7')" href="clean.jsp"><img alt="" border="0" src="images/off.gif" name="img7" width="16" height="13" /><img border="0" src="images/button_cleanup.gif" width="84" height="13" /></a></td></tr> <%
		}
	}
	%>
	
	<tr><td>&nbsp;</td></tr>
	<tr><td align="left"><span class="head0">Reportnet</span></td></tr>
	<tr><td align="right"><a onmouseout="Out('img8')" onmouseover="Over('img8')" href="http://cr.eionet.eu.int"><img alt="" border="0" src="images/off.gif" name="img8" width="16" height="13" /><img border="0" src="images/dd_but_CR.jpg" width="84" height="13" /></a></td></tr>
	<tr><td align="right"><a onmouseout="Out('img9')" onmouseover="Over('img9')" href="http://dd.eionet.eu.int"><img alt="" border="0" src="images/off.gif" name="img9" width="16" height="13" /><img border="0" src="images/dd_but_DD.jpg" width="84" height="13" /></a></td></tr>
	<tr><td align="right"><a onmouseout="Out('img10')" onmouseover="Over('img10')" href="http://cdr.eionet.eu.int"><img alt="" border="0" src="images/off.gif" name="img10" width="16" height="13" /><img border="0" src="images/dd_but_CDR.jpg" width="84" height="13" /></a></td></tr>
	<tr><td align="right"><a onmouseout="Out('img11')" onmouseover="Over('img11')" href="http://rod.eionet.eu.int"><img alt="" border="0" src="images/off.gif" name="img11" width="16" height="13" /><img border="0" src="images/dd_but_ROD.jpg" width="84" height="13" /></a></td></tr>

</table>

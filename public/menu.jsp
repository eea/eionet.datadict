<%@page import="eionet.util.SecurityUtil,com.tee.xmlserver.AppUserIF"%>

<table cellspacing="0" cellpadding="0" border="0">

	<tr><td align="left"><span class="head0">Services</span></td></tr>
	<tr><td align="right"><a onMouseOut="Out('img0')" onMouseOver="Over('img0')" href="datasets.jsp?SearchType=SEARCH"><img alt="" border="0" src="../images/off.gif" name="img0" width="16" height="13"><img border="0" src="../images/button_datasets.gif" width="84" height="13"></a></td></tr>
	<tr><td align="right"><a onMouseOut="Out('img1')" onMouseOver="Over('img1')" href="search_results_tbl.jsp?SearchType=SEARCH"><img alt="" border="0" src="../images/off.gif" name="img1" width="16" height="13"><img border="0" src="../images/button_tables.gif" width="84" height="13"></a></td></tr>
	<tr><td align="right"><a onMouseOut="Out('img2')" onMouseOver="Over('img2')" href="search.jsp"><img alt="" border="0" src="../images/off.gif" name="img2" width="16" height="13"><img border="0" src="../images/button_dataelements.gif" width="84" height="13"></a></td></tr>
	<tr><td>&nbsp;</td></tr>
	<%
	AppUserIF _user = SecurityUtil.getUser(request);
	%>
	<tr>
		<td align="right">
			<%
			if (_user!=null && _user.isAuthentic()) {
				%>
		        <a onMouseOut="Out('img3')" onMouseOver="Over('img3')" href="javascript:logout()">
		        	<img alt="" border="0" src="../images/off.gif" name="img3" width="16" height="13"><img alt="Login" height="13" width="84" border="0" src="../images/button_logout.gif">
		        </a>
				<%
			}
			else{
				%>
		        <a onMouseOut="Out('img3')" onMouseOver="Over('img3')" href="javascript:login()">
		        	<img alt="" border="0" src="../images/off.gif" name="img3" width="16" height="13"><img alt="Login" height="13" width="84" border="0" src="../images/button_login.gif">
		        </a>
		        <%
		    }
		    %>
		</td>
	</tr>
  
	<%
  	if (_user!=null && _user.isAuthentic()) {
    	%>
	    <tr><td>&nbsp;</td></tr>
	    <tr><td align="left"><span class="head0">Administration</span></td></tr>	    
	    <tr><td align="right"><a onMouseOut="Out('img4')" onMouseOver="Over('img4')" href="attributes.jsp"><img alt="" border="0" src="../images/off.gif" name="img4" width="16" height="13"><img border="0" src="../images/button_attributes.gif" width="84" height="13"></a></td></tr>		
		<!--tr><td align="right"><a onMouseOut="Out('img5')" onMouseOver="Over('img5')" href="namespaces.jsp"><img alt="" border="0" src="../images/off.gif" name="img5" width="16" height="13"><img border="0" src="../images/button_namespaces.gif" width="84" height="13"></a></td></tr-->
		<tr><td align="right"><a onMouseOut="Out('img6')" onMouseOver="Over('img6')" href="import.jsp"><img alt="" border="0" src="../images/off.gif" name="img6" width="16" height="13"><img border="0" src="../images/import.gif" width="84" height="13"></a></td></tr>
		<tr><td align="right"><a onMouseOut="Out('img7')" onMouseOver="Over('img7')" href="clean.jsp"><img alt="" border="0" src="../images/off.gif" name="img7" width="16" height="13"><img border="0" src="../images/button_cleanup.gif" width="84" height="13"></a></td></tr>
		<%
	}
	%>

</table>
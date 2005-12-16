<%@page import="java.util.*"%>

<table cellspacing="0" cellpadding="0" width="100%" border="0">
	<tr>
		<td align="bottom" width="20" background="images/bar_filled.jpg" height="25">
			&#160;
		</td>
		<td background="images/bar_filled.jpg" height="25">
            <table height="10" cellSpacing="0" cellPadding="0" border="0">
				<tr>
		    		<td valign="bottom" align="left" style="padding-left:20">
						<a href="http://www.eionet.eu.int/"><span class="barfont">EIONET</span></a>
		    		</td>
		    		<td valign="bottom" width="28"><img src="images/bar_hole.jpg"/></td>
		    		<td valign="bottom" align="left">
			            <%
			            String oHName=request.getParameter("name");
			            if (oHName!=null){ %>
			            	<a href='index.jsp'><%
			            }
			            %>
			            <span class="barfont">Data Dictionary</span>
			            <%
			            if (oHName!=null){ %>
			            	</a><%
			            }
			            %>
		    		</td>
		    		
			        <%
			        if (oHName!=null){ %>
					    <td valign="bottom" width="28"><img src="images/bar_hole.jpg"/></td>
					    <td valign="bottom" nowrap="nowrap" align="left">
							<span class="barfont"><%=oHName%></span>
					    </td><%
					}
					%>
			        
		    		<td valign="bottom" width="28"><img src="images/bar_dot.jpg"/></td>
				</tr>
	    	</table>
		</td>
	</tr>
</table>


<%-- for debugging, remove the two dashes from the <%-- below, put them back later when you're done --%>
<%--
Enumeration oNames;
String oName=null;
%>
<p><b><u>Init parameters (app):</u></b><%
oNames=application.getInitParameterNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <li><%=oName%> = <%=application.getInitParameter(oName)%></li><%
  }%>
<p><b><u>Init parameters (sess):</u></b><%
oNames=config.getInitParameterNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <li><%=oName%> = <%=config.getInitParameter(oName)%></li><%
  }%>
<p><u><b>Session attributes:</u></b><%
oNames=session.getAttributeNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <li><%=oName%> = <%=session.getAttribute(oName)%></li><%
  }%>
<p><b><u>Request:</u></b>
<p><u>parameters:</u><%
oNames=request.getParameterNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <li><%=oName%> = <%=request.getParameter(oName)%></li><%
  }%>
<p><u>attributes:</u><%
oNames=request.getAttributeNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <li><%=oName%> = <%=request.getAttribute(oName)%></li><%
  }%>
<p><u>headers:</u><%
oNames=request.getHeaderNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <li><%=oName%> = <%=request.getHeader(oName)%></li><%
  }
%>
<%-- --%>

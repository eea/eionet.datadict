<%@page import="java.util.*"%>
<table cellspacing="0" cellpadding="0" width="621" border="0">
			<tr>
         	<td align="bottom" width="20" background="../images/bar_filled.jpg" height="25">&#160;</td>
          	<td width="600" background="../images/bar_filled.jpg" height="25">
            <table height="8" cellSpacing="0" cellPadding="0" border="0">
            	<tr>
		         	<td valign="bottom" align="middle">
						<a href="http://www.eionet.eu.int/"><span class="barfont">EIONET</span></a>
					</td>
		            <td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
        <td valign="BOTTOM" align="middle">
            <%
            String oHName=request.getParameter("name");
            if (oHName!=null) {%><A href='index.jsp'><%}%>
                <span class="barfont">Data Dictionary</span>
                    <%if (oHName!=null) {%></A><%}%>
        </td>
        <%if (oHName!=null) {%>
            <td valign="bottom" width="28"><img src="../images/bar_hole.jpg"/></td>
            <td valign=BOTTOM nowrap="true" align="middle">
                <span class="barfont"><%=oHName%></span>
            </td>
        <%}%>
        <td valign="bottom" width="28"><img src="../images/bar_dot.jpg"/></td>
    </tr>
				</table>
			</td></tr>
	       		<%
	            String back_button=request.getParameter("back");
	            if (back_button!=null){
	            	String back_url=(String)session.getAttribute("backUrl");
    	        	if (back_url!=null) {
						String back = back_url.length()<1 ? "javascript:history.back(-1)":back_url;	
						%>
			        	<tr><td valign="bottom" align="right" width="100%" colspan="2">
							<a href="<%=back%>">&lt;back</a>
	       				</td></tr>
	       				<%
       			}
       			}
       			%>
		</table>

<%-- for debugging, remove the two dashes from the <%-- below, put them back later when you're done --%>
<%--
Enumeration oNames;
String oName=null;
%>
<P><B><U>Init parameters (app):</U></B><%
oNames=application.getInitParameterNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <LI><%=oName%> = <%=application.getInitParameter(oName)%></LI><%
  }%>
<P><B><U>Init parameters (sess):</U></B><%
oNames=config.getInitParameterNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <LI><%=oName%> = <%=config.getInitParameter(oName)%></LI><%
  }%>
<P><U><B>Session attributes:</U></B><%
oNames=session.getAttributeNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <LI><%=oName%> = <%=session.getAttribute(oName)%></LI><%
  }%>
<P><B><U>Request:</U></B>
<P><U>parameters:</U><%
oNames=request.getParameterNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <LI><%=oName%> = <%=request.getParameter(oName)%></LI><%
  }%>
<P><U>attributes:</U><%
oNames=request.getAttributeNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <LI><%=oName%> = <%=request.getAttribute(oName)%></LI><%
  }%>
<P><U>headers:</U><%
oNames=request.getHeaderNames();
oName=null;
while (oNames.hasMoreElements()) {
  oName=(String)oNames.nextElement();%>
  <LI><%=oName%> = <%=request.getHeader(oName)%></LI><%
  }
%>
<%-- --%>

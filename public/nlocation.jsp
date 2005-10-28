<%@page import="java.util.*"%>
<div id="pagehead">
 <div id="identification">
  <a href="/"><img src="images/logo.png" alt="Logo" id="logo" border="0" /></a>
  <div class="sitetitle">Data Dictionary (DD)</div>
  <div class="sitetagline">This service is part of Reportnet</div>
 </div>
<div class="breadcrumbtrail">
 <div class="breadcrumbhead">You are here:</div>
 <div class="breadcrumbitem"><a href="http://www.eionet.eu.int">EIONET</a></div>

<%
   String oHName=request.getParameter("name");
   if (oHName==null) {  %>
 <div class="breadcrumbitemlast">Data Dictionary</div>
<% } %>
<%  if (oHName!=null) { %>
 <div class="breadcrumbitem"><a href='index.jsp'>Data Dictionary</a></div>
 <div class="breadcrumbitemlast"><%=oHName%></div>
<% } %>
 <div class="breadcrumbtail"></div>
</div>
</div> <!-- pagehead -->

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

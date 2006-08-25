<%@page import="java.util.*"%>
<div id="pagehead">
<form action="http://search.eionet.europa.eu/search" method="get">
<input onfocus="if(this.value=='Search DD')this.value='';"
       onblur="if(this.value=='')this.value='Search DD';"
       title="Search Eionet sites with Google"
       value="Search DD" size="10" type="text" name="q"/>
<input value="Eionet" name="client" type="hidden"/>
<input value="Eionet" name="site" type="hidden"/>
<input value="UTF-8" name="ie" type="hidden"/>
<input value="UTF-8" name="oe" type="hidden"/>
<input value="xml_no_dtd" name="output" type="hidden"/>
<input value="Eionet" name="proxystylesheet" type="hidden"/>
<input value="dd.eionet.europa.eu" name="domains" type="hidden"/>
<input value="dd.eionet.europa.eu" name="as_sitesearch" type="hidden"/>
</form>
 <div id="identification">
  <a href="/"><img src="images/logo.png" alt="Logo" id="logo" border="0" /></a>
  <div class="sitetitle">Data Dictionary (DD)</div>
  <div class="sitetagline">This service is part of Reportnet</div>
 </div>
<div class="breadcrumbtrail">
 <div class="breadcrumbhead">You are here:</div>
 <div class="breadcrumbitem eionetaccronym"><a href="http://www.eionet.europa.eu">Eionet</a></div>

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

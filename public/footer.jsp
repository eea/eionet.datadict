<%@page contentType="text/html;charset=UTF-8" import="java.sql.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>

<%

request.setCharacterEncoding("UTF-8");

String lastUpdated = (String)session.getAttribute("last-updated");
if (lastUpdated==null){
	
	Connection conn = null;
	DBPoolIF pool = null;
	try{
		ServletContext ctx = getServletContext();
		XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
		
		pool = xdbapp.getDBPool();	
		conn = pool.getConnection();
		
		AppUserIF user = SecurityUtil.getUser(request);
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);	
		searchEngine.setUser(user);
		
		lastUpdated = searchEngine.getLastUpdated();
		if (lastUpdated==null || lastUpdated.length()==0)
			throw new Exception();
		else
			session.setAttribute("last-updated", lastUpdated);
	}
	catch (Exception e){
		lastUpdated = "???";
	}
}

%>

<table width="100%">
          <tr>
            <td align="center" height="10"></td>
          </tr>
          <tr>
            <td align="center"><span class="barfont"><a href="javascript:history.back()"><font face="Verdana" size="1">Back</font></a><font face="Verdana" size="1">
              | <a href="mailto:dd@eionet.eu.int">E-mail</a> | <a href="doc1.jsp">Disclaimer
              </a>| Last updated: <%=lastUpdated%>
              | <a href="mailto:helpdesk@eionet.eu.int?subject=Feedback from the Data Dictionary website">Feedback</a></font></span></td>
          </tr>
          <tr height="15">
            <td align="center" height="10"></td>
          </tr>
          <tr>
            <td align="center"><span class="barfont"><font face="Verdana" size="1"><b><a href="http://www.eea.eu.int" target="_blank">European
              Environment Agency</a></b><br/>
              Kgs. Nytorv 6, DK-1050 Copenhagen K, Denmark - Phone: +45 3336
              7100</font></span></td>
          </tr>
</table>

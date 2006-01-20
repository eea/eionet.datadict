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
	finally{
		try {
			if (conn!=null) conn.close();
		}
		catch (SQLException e) {}
	}
}

%>

<div id="footer">
					<p>
              <a href="mailto:dd@eionet.eu.int">E-mail</a> |
							<a href="doc1.jsp">Disclaimer</a> |
							Last updated: <%=lastUpdated%> |
              <a href="mailto:helpdesk@eionet.eu.int?subject=Feedback from the Data Dictionary website">Feedback</a>
					</p>
          <p>
            <a href="http://www.eea.eu.int" style="font-weight:bold">European
              Environment Agency</a><br/>
              Kgs. Nytorv 6, DK-1050 Copenhagen K, Denmark - Phone: +45 3336 7100
          </p>
</div>

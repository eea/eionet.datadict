<%@page import="java.util.*,eionet.meta.DDhistory"%>
<%


DDhistory history = (DDhistory)session.getAttribute("DDhistory");
if (history==null)
	history = new DDhistory();

boolean bLog = false;
boolean bBackIsCurrent = false;
String requestURI = request.getRequestURI();
String queryString = request.getQueryString() == null ? "" : request.getQueryString();
String backUrl="";

if (request.getMethod().equals("POST")){
	if (requestURI.indexOf("datasets.jsp")>-1 ||
		requestURI.indexOf("search_results_tbl.jsp")>-1 ||
		requestURI.indexOf("search_results.jsp")>-1){
		bLog=true;
	}
}
else if (request.getMethod().equals("GET")){
	bLog=true;
}			
if (bLog){	
	String url =  requestURI + "?" + queryString;
	history.loadPage(url);
	session.setAttribute("DDhistory", history);
}

if ((requestURI.indexOf("data_element.jsp")>-1 || requestURI.indexOf("dataset.jsp")>-1 || requestURI.indexOf("dstable.jsp")>-1)
			&& queryString.indexOf("mode=edit")>-1 && request.getMethod().equals("GET")){
		backUrl=history.getLastMatching("mode=view");
}

if (backUrl.length()==0){
	if (bBackIsCurrent)
		backUrl=history.getCurrentUrl();
	else
		backUrl=history.getBackUrl();
}
session.setAttribute("backUrl", backUrl);
String currentUrl=history.getCurrentUrl();


	%><!--br><%=history.toString()%><br><%=history.getSize()%><br><%=backUrl%><br--><%
%>
<%@page import="java.util.*"%>
<%
	String back_button=request.getParameter("back");
	if (back_button!=null){
		String back_url=(String)session.getAttribute("backUrl");
		if (back_url!=null){
		String back = back_url.length()<1 ? "javascript:history.back(-1)":back_url;	
		%>
		<div class="navbuttons">
				<a class="navbutton" href="<%=back%>">&lt;back</a>
		</div>
		<%
	}
}
					
	// up link
	/*String up = request.getParameter("up");
	if (up!=null){
		String upUrl=(String)session.getAttribute("upUrl");
		if (upUrl!=null){ %>
		<div class="navbuttons">
				<a href="<%=upUrl%>"><img border="0" src="images/up.gif"/></a>
		</div>
		<%
	}
}*/
%>

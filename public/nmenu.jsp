<%@page import="eionet.util.SecurityUtil,com.tee.xmlserver.AppUserIF"%>

<div id="leftcolumn" class="localnav">
	<ul>
		<li><a href="datasets.jsp?SearchType=SEARCH">Datasets</a></li>
		<li><a href="search_results_tbl.jsp?SearchType=SEARCH">Tables</a></li>
		<li><a href="search.jsp">Data elements</a></li>
		<%
		AppUserIF _user = SecurityUtil.getUser(request);
		if (_user!=null){
			%>
			<li><a href="attributes.jsp">Attributes</a></li>
			<%	
			if (SecurityUtil.hasPerm(_user.getUserName(), "/import", "x")){ %>
				<li><a href="import.jsp">Import datasets</a></li><%
			}
			if (SecurityUtil.hasPerm(_user.getUserName(), "/cleanup", "x")){ %>
				<li><a href="clean.jsp">Cleanup</a></li> <%
			}			
			%>
			<li><a href="subscribe.jsp">Subscribe</a></li><%
		}		
		%>
	</ul>
</div>

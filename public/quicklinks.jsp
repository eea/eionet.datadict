<%@page import="java.util.*"%>

<%
Vector links = (Vector)request.getAttribute("quicklinks");
if (links!=null && links.size()>0){
	%>
	<tr>
		<td width="100%" style="border: 1px solid #FF9900" bgcolor="#FFFFFF">
			<table border="0" width="100%" cellspacing="0" cellpadding="2">
				<tr>
					<td width="100%" class="barfont">
						<b>Quick links</b><%
						
						for (int i=0; i<links.size(); i++){
							String link = (String)links.get(i);
							int j = link.indexOf("|");
							String name = link.substring(j+1).trim();
							String dispName = link.substring(0,j).trim();
							%>
							&nbsp;|&nbsp;<a href="#<%=name%>"><%=dispName%></a><%
						}
						%>
					</td>
				</tr>
			</table>
		</td>
	</tr><%
}
%>

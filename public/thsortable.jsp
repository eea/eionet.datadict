<%@page import="java.util.*"%>

<%

String title     = request.getParameter("title");
String mapName   = request.getParameter("mapName");
String sortColNr = request.getParameter("sortColNr");
String help      = request.getParameter("help");
boolean hasHelp = help!=null && help.length()!=0;

%>

<table width="98%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td width="80%">
			<%
			if (!hasHelp){ %>
				<b><%=title%></b><%
			}
			else{ %>
				<table width="100%">
					<tr>
						<td align="right" width="50%">
							<b><%=title%></b>
						</td>
						<td align="left" width="50%">
							<a target="_blank" href="<%=help%>" onclick="pop(this.href)">
								<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
							</a>
						</td>
					</tr>
				</table><%
			}
			%>
		</td>
		<td width="20%" align="right">
			<img border="0" src="images/arrows.gif" width="17" height="22" usemap="#map<%=mapName%>"/>
          	<map name="map<%=mapName%>">
		    	<area shape="rect" COORDS="0,0,17,11" href="javascript:showSortedList(<%=sortColNr%>, 1)" title="Sort ascending">
		    	<area shape="rect" COORDS="0,12,17,22" href="javascript:showSortedList(<%=sortColNr%>, -1)" title="Sort descending">
		    </map>
		</td>
	</tr>
</table>
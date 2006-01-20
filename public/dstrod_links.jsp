<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%

request.setCharacterEncoding("UTF-8");

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

Connection conn = null;
try{
	// check if the user exists
	AppUserIF user = SecurityUtil.getUser(request);
	if (user == null) throw new Exception("Not authenticated!");
	
	// get the dataset Identifier
	String dstIdf = request.getParameter("dst_idf");
	if (dstIdf == null || dstIdf.length()==0) throw new Exception("Dataset Identifier is missing!");
	
	// check if the user is authorised
	boolean prm = user!=null && dstIdf!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dstIdf, "u");
	if (!prm) throw new Exception("Not authorised!");
		
	// get dataset id
	String dstID = request.getParameter("dst_id");
	if (dstID == null || dstID.length()==0) throw new Exception("Dataset ID is missing!");
	
	// init connection
	ServletContext ctx = getServletContext();
	XDBApplication xdbapp = XDBApplication.getInstance(ctx);
	conn = xdbapp.getDBPool().getConnection();
	
	// handle the POST
	if (request.getMethod().equals("POST")){
		RodLinksHandler handler = new RodLinksHandler(conn, ctx);
		handler.execute(request);
	}
	
	// ...
	// handle the GET
	// ...
	
	// get dataset name
	String dstName = request.getParameter("dst_name");
	if (dstName == null || dstName.length()==0) dstName = "?";
	
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	Vector rodLinks = searchEngine.getRodLinks(dstID);
	%>
	
	<html>
		<head>
			<title>Data Dictionary</title>
			<meta content="text/html; charset=UTF-8" http-equiv="Content-Type">
			<link href="eionet_new.css" rel="stylesheet" type="text/css"/>
			<script language="javascript" src='script.js'></script>
			<script language="javascript">
			// <![CDATA[
				function submitAdd(raID, raTitle, liID, liTitle){
					
					document.forms["rodlinks"].elements["mode"].value = "add";
					document.forms["rodlinks"].elements["ra_id"].value = raID;
					document.forms["rodlinks"].elements["ra_title"].value = raTitle;
					document.forms["rodlinks"].elements["li_id"].value = liID;
					document.forms["rodlinks"].elements["li_title"].value = liTitle;
					document.forms["rodlinks"].submit();
				}
			// ]]>
			</script>
		</head>
		<body>
			<%@ include file="header.htm" %>
			<table border="0">
			    <tr valign="top">
			        <td nowrap="nowrap" width="125">
			            <p><center>
			                <%@ include file="menu.jsp" %>
			            </center></p>
			        </td>
			        <td>
			            <jsp:include page="location.jsp" flush='true'>
			                <jsp:param name="name" value="ROD links"/>
			            </jsp:include>
			            
			            <!-- start work area -->
						<div style="margin-left:30">
							<br/><br/>
							<span class="head00">
								ROD obligations corresponding to <a href="dataset.jsp?mode=edit&ds_id=<%=dstID%>"><%=dstName%><a> dataset
							</span>
							<p align="right">
								<a target="_blank" href="help.jsp?screen=dataset_rod&area=pagehelp" onclick="pop(this.href)">
									<img src="images/pagehelp.jpg" border="0" alt="Get some help on this page" />
								</a>
							</p>
							<form acceptcharset="UTF-8" name="rodlinks" action="dstrod_links.jsp" method="POST">
								<table width="auto" cellspacing="0" cellpadding="0">
									<tr>
										<td colspan="3">
											<input type="button" class="smallbutton" value="Add new" onclick="pop('InServices?client=webrod&method=get_activities')"/>
											<%
											if (rodLinks!=null && rodLinks.size()>0){ %>
												<input type="submit" class="smallbutton" value="Remove selected"/><%
											}
											%>
										</td>
									</tr>
									<tr>
										<th>&nbsp;</th>
										<th style="padding-left:5;padding-right:10;border-left:0">Title</th>
										<th style="padding-left:5;padding-right:10;border-right:1px solid #FF9900">Details</th>
									</tr>
									<%
									int displayed = 0;
									for (int i=0; rodLinks!=null && i<rodLinks.size(); i++){
										
										Hashtable rodLink = (Hashtable)rodLinks.get(i);
										String raID = (String)rodLink.get("ra-id");
										String raTitle = (String)rodLink.get("ra-title");
										String raDetails = (String)rodLink.get("ra-url");
										
										String colorAttr = displayed % 2 != 0 ? "bgcolor=#CCCCCC" : "";
										%>
										<tr>
											<td <%=colorAttr%>>
												<input type="checkbox" name="del_id" value="<%=raID%>"/>
											</td>
											<td style="padding-left:5;padding-right:10" <%=colorAttr%>>
												<%=raTitle%>
											</td>
											<td style="padding-left:5;padding-right:10" <%=colorAttr%>>
												<a target="_blank" href="<%=raDetails%>"><%=raDetails%></a>
											</td>
										<tr>
										<%
										displayed++;
									}
									%>
								</table>
								
								<input type="hidden" name="mode" value="rmv"/>
								<input type="hidden" name="dst_id" value="<%=dstID%>"/>
								<input type="hidden" name="dst_idf" value="<%=dstIdf%>"/>
								<input type="hidden" name="dst_name" value="<%=dstName%>"/>
								
								<input type="hidden" name="ra_id" value=""/>
								<input type="hidden" name="ra_title" value=""/>
								<input type="hidden" name="li_id" value=""/>
								<input type="hidden" name="li_title" value=""/>
							</form>
						</div>
						<!-- end work area -->
						
					</td>
				</tr>
			</table>
		</body>
	</html>
	
	<%
}
catch (Exception e){
	
	ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
	e.printStackTrace(new PrintStream(bytesOut));
	String trace = bytesOut.toString(response.getCharacterEncoding());
						
	request.setAttribute("DD_ERR_MSG", e.getMessage());
	request.setAttribute("DD_ERR_TRC", trace);
	
	/*String qryStr = request.getQueryString();
	qryStr = qryStr==null ? "" : "?" + qryStr;
	request.setAttribute("DD_ERR_BACK_LINK", request.getRequestURL().append(qryStr).toString());*/
	
	request.getRequestDispatcher("error.jsp").forward(request, response);
}
finally {
	try { if (conn!=null) conn.close(); } catch (SQLException e) {}
}
%>

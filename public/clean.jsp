<%@page contentType="text/html;charset=UTF-8" import="java.util.Vector,com.tee.xmlserver.*, eionet.meta.MrProper"%>

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/cleanup", "x")){ %>
		<b>Not allowed!</b><%
		return;
	}
%>

<html>
<head>
	<title>Data Dictionary</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="javascript" src='script.js'></script>
	<script language="javascript">
	
		var idsCleared = false;
		
		function submitForm(){
			document.forms["form1"].submit();
		}
		
		function clearIds(){
			
			if (idsCleared == false){
				document.forms["form1"].elements["rm_id"].value="";
				idsCleared = true;
			}
		}
		
		function rmCrit(){
			
			if (document.forms["form1"].elements["rm_crit"][0].checked){
				document.forms["form1"].elements["rm_id"].disabled = true;
				document.forms["form1"].elements["rm_idfier"].disabled = false;
				document.forms["form1"].elements["rm_ns"].disabled = false;
			}
			else if (document.forms["form1"].elements["rm_crit"][1].checked){				
				document.forms["form1"].elements["rm_idfier"].disabled = true;
				document.forms["form1"].elements["rm_ns"].disabled = true;
				document.forms["form1"].elements["rm_id"].disabled = false;
			}
			else{
				document.forms["form1"].elements["rm_idfier"].disabled = true;
				document.forms["form1"].elements["rm_ns"].disabled = true;
				document.forms["form1"].elements["rm_id"].disabled = true;
			}
		}
	</script>
</head>
<body>

<%@ include file="header.htm"%>

<table border="0">
    <tr valign="top">
		<td nowrap="nowrap" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></p>
        </td>
        <td>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Cleanup"/>
            </jsp:include>
            
            <%
            
            // POST
            if (request.getMethod().equals("POST")){
	            
	            // init db connection & MrProper
	            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
				DBPoolIF pool = xdbapp.getDBPool();
	            MrProper mrProper = new MrProper(pool.getConnection());
	            mrProper.setContext(getServletContext());
	            mrProper.setUser(user);
	            
	            // execute mrProper
	            mrProper.execute(request);
	            
	            // get results
	            Vector results = mrProper.getResponse();
	            if (results.size()==0)
	            	results.add("No results got back!");
	            
	            %>
	            <div style="margin-left:30">
		            <table width="auto" cellspacing="0">
		            	<tr height="30"><td>&#160;</td></tr>
		            	<tr>
		            		<td><font class="head00">Cleanup results:</font></td>
		            	</tr>
		            	<tr height="10"><td>&#160;</td></tr>
		            	
		            	<%
		            	// here come the results
		            	for (int i=0; i<results.size(); i++){
			            	String sss = (String)results.get(i);
			            	%>
			            	<tr><td>&gt; <%=sss%></td></tr><%
		            	}
		            	%>
		            	
		            	<tr height="10"><td>&#160;</td></tr>
		            	<tr>
		            		<td><a href="clean.jsp">&lt; back to cleanup page</a></td>
		            	</tr>		            	
		            </table>		            
	            </div>
	            <%
            }
            // GET
            else{
	            %>
            
	            <div style="margin-left:30">            
	            
				<table width="500">
					<tr>
						<td colspan="3"><br/><font class="head00">Cleanup functions</font><br/>
							<font color="red">
							This is a function enabling you to clean the database from all kinds of
							leftovers that might result from exceptional situations. Please use this
							with great caution as you might accidentally delete some important data!
							</font>
						</td>
				</tr>
				</table>
				
				<form name="form1" action="clean.jsp" method="POST">
				
					<table width="auto" cellspacing="0">
						<tr>
							<td>
								<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   value="<%=MrProper.RLS_DST%>">
									   <span class="smallfont">
									   		Release the dataset with the given Identifier:
									   </span>
								</input>
								<input type="text" class="smalltext" name="<%=MrProper.DST_IDFIER%>"/>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   value="<%=MrProper.ORPHAN_ELM%>">
									   <span class="smallfont">
									   	Delete all elements without parent tables.
									   </span>
								</input>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   value="<%=MrProper.ORPHAN_TBL%>">
									   <span class="smallfont">
									   	Delete all tables without parent datasets.
									   </span>
								</input>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   value="<%=MrProper.RLS_NOWC%>">
									   <span class="smallfont">
									   	Release locked objects which actually don't have a working copy.
									   </span>
								</input>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   value="<%=MrProper.RMV_WC_NORIG%>">
									   <span class="smallfont">
									   	Remove working copies which do not have any associated originals.
									   </span>
								</input>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   value="<%=MrProper.RMV_MULT_VERS%>">
									   <span class="smallfont">
									   	Remove multiple versions by leaving only the latest by timestamp.
									   </span>
								</input>
							</td>
						</tr>
						<tr height="10"><td>&#160;</td></tr>
						<tr>
							<td>
								<%
								
								String disabled = "";//clnPrm ? "" : "disabled";
								%>
								<input type="button" <%=disabled%>
									   class="smallbuttonb" value="Action" onclick="submitForm()">
								</input>
							</td>
						</tr>
						
						<%
						if (request.getParameter("smersh")!=null){ %>
							<tr height="10"><td>&#160;</td></tr>
							<tr>
								<td>
									<table cellspacing="0" cellpadding="0">
										<tr>
											<td colspan="2">&#160;</td>
											<td bgcolor="#D3D3D3">
												<span class="smallfont">
													<input type="radio" name="rm_obj_type" value="dst">datasets</input>
												</span>
											</td>
											<td>&#160;</td>
											<td  bgcolor="#D3D3D3">
												<span class="smallfont">
													<input type="radio" name="rm_crit" value="lid" onclick="rmCrit()"/>Identifier&#160;
													<input type="text" class="smalltext" name="rm_idfier" disabled/>
												</span>
											</td>
										</tr>
										<tr>
											<td>
												<input type="checkbox" name="<%=MrProper.FUNCTIONS_PAR%>"
									   				value="<%=MrProper.RMV_OBJECTS%>"/>
											</td>
											<td style="padding-left:5;padding-right:5"><span class="smallfont">Remove</span></td>
											<td bgcolor="#D3D3D3">
												<span class="smallfont">
													<input type="radio" name="rm_obj_type" value="tbl">tables</input>
												</span>
											</td>
											<td style="padding-left:5;padding-right:5"><span class="smallfont">with</span></td>
											<td  bgcolor="#D3D3D3">
												<span class="smallfont">
													&#160;&#160;&#160;&#160;&#160;&#160;& parent ns&#160;
													<input type="text" class="smalltext" name="rm_ns" disabled/>
												</span>
											</td>
										</tr>
										<tr>
											<td colspan="2">&#160;</td>
											<td bgcolor="#D3D3D3">
												<span class="smallfont">
													<input type="radio" name="rm_obj_type" value="elm">elements</input>
												</span>
											</td>
											<td>&#160;</td>
											<td  bgcolor="#D3D3D3">
												<span class="smallfont">
													<input type="radio" name="rm_crit" value="id" onclick="rmCrit()"/>ids&#160;
													<input type="text"
														   class="smalltext"
														   name="rm_id"
														   value="(delimited by space)"
														   onclick="clearIds()"
														   disabled/>
												</span>
											</td>
										</tr>
									</table>
								</td>
							</tr>
							<%
						}
						%>
					</table>
				</form>
	            </div> <%
            } // end GET
            %>
		</td>
	</tr>
</table>
</body>
</html>

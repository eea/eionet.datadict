<%@page contentType="text/html;charset=UTF-8" import="java.util.Vector,com.tee.xmlserver.*,eionet.meta.MrProper,eionet.util.Util,java.sql.Connection,java.sql.SQLException"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/cleanup", "x")){ %>
		<b>Not allowed!</b><%
		return;
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Data Dictionary</title>
	<script language="javascript" src='script.js' type="text/javascript"></script>
	<script language="javascript" type="text/javascript">
	// <![CDATA[
	
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
	// ]]>
	</script>
</head>
<body>
	<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Cleanup"/>
		<jsp:param name="back" value="true"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
            
            <%
            
            // POST
            if (request.getMethod().equals("POST")){
	            
	            Connection conn = null;
	            Vector results = new Vector();
	            try{
		            // init db connection & MrProper
		            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
					DBPoolIF pool = xdbapp.getDBPool();
					conn = pool.getConnection();
		            MrProper mrProper = new MrProper(conn);
		            mrProper.setContext(getServletContext());
		            mrProper.setUser(user);
		            
		            // execute mrProper
		            mrProper.execute(request);
		            
		            // get results
		            results = mrProper.getResponse();
		            if (results.size()==0) results.add("No results got back!");
				}
				finally{
					try{
						if (conn!=null) conn.close();
					}
					catch (SQLException e){}
				}
	            
	            %>
								<h1>Cleanup results</h1>
		            <table width="auto" cellspacing="0">
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
	            <%
            }
            // GET
            else{
	            %>
            
	            
				<h1>Cleanup functions</h1>
				<p style="color:red">
				This is a function enabling you to clean the database from all kinds of
				leftovers that might result from exceptional situations. Please use this
				with great caution as you might accidentally delete some important data!
				</p>
				
				<form name="form1" action="clean.jsp" method="post">
				
					<table width="auto" cellspacing="0">
						<tr>
							<td>
								<input type="checkbox" id="releaseds" name="<%=MrProper.FUNCTIONS_PAR%>" value="<%=MrProper.RLS_DST%>"/>
									   <label for="releaseds">
									   		Release the dataset with the given Identifier:
									   </label>
								<input type="text" class="smalltext" name="<%=MrProper.DST_IDFIER%>"/>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" id="rmelem" name="<%=MrProper.FUNCTIONS_PAR%>" value="<%=MrProper.ORPHAN_ELM%>"/>
									   <label for="rmelem">
									   	Delete all elements without parent tables.
									   </label>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" id="rmtab" name="<%=MrProper.FUNCTIONS_PAR%>" value="<%=MrProper.ORPHAN_TBL%>"/>
									   <label for="rmtab">
									   	Delete all tables without parent datasets.
									   </label>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" id="rellock" name="<%=MrProper.FUNCTIONS_PAR%>" value="<%=MrProper.RLS_NOWC%>"/>
									   <label for="rellock">
									   	Release locked objects which actually don't have a working copy.
									   </label>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" id="rmwc" name="<%=MrProper.FUNCTIONS_PAR%>" value="<%=MrProper.RMV_WC_NORIG%>"/>
									   <label for="rmwc">
									   	Remove working copies which do not have any associated originals.
									   </label>
							</td>
						</tr>
						<tr>
							<td>
								<input type="checkbox" id="rmmult" name="<%=MrProper.FUNCTIONS_PAR%>" value="<%=MrProper.RMV_MULT_VERS%>"/>
									   <label for="rmmult">
									   	Remove multiple versions by leaving only the latest by timestamp.
									   </label>
							</td>
						</tr>
						<tr style="height:10px;"><td>&#160;</td></tr>
						<tr>
							<td>
								<%
								
								String disabled = "";//clnPrm ? "" : "disabled";
								%>
								<input type="button" <%=disabled%> class="smallbuttonb" value="Action" onclick="submitForm()"/>
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
													<input type="radio" name="rm_obj_type" value="dst"/>datasets
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
													<input type="radio" name="rm_obj_type" value="tbl"/>tables
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
													<input type="radio" name="rm_obj_type" value="elm"/>elements
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
	            <%
            } // end GET
            %>
</div>
</body>
</html>

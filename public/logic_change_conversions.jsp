<%@page contentType="text/plain"%>
<%@ page import="java.sql.*,java.io.*,eionet.util.sql.ConnectionUtil,eionet.util.LogicChangeConversions" %>
<%
Connection conn = null;
PrintWriter writer = null;
try{
	conn = ConnectionUtil.getConnection();
	writer = response.getWriter();
	
	LogicChangeConversions logicChangeConversions = new LogicChangeConversions(conn, writer);
	String action = request.getParameter(LogicChangeConversions.PARAM_ACTION);
	if (action!=null && action.trim().length()>0){
		if (action.equals(LogicChangeConversions.ACTION_CLEANUP))
			logicChangeConversions.cleanup();
		else if (action.equals(LogicChangeConversions.ACTION_CREATE))
			logicChangeConversions.create();
		else if (action.equals(LogicChangeConversions.ACTION_CLEANUP_CREATE)){
			logicChangeConversions.cleanup();
			logicChangeConversions.create();
		}
		else if (action.equals(LogicChangeConversions.ACTION_BOOLEAN_VALUES))
			logicChangeConversions.createBooleanFixedValues();
		else
			throw new Exception("Unknown action requested: " + action);
	}
	else
		throw new Exception("Missing request parameter: " + LogicChangeConversions.PARAM_ACTION);
		
	logicChangeConversions.outputWriteln("");
	logicChangeConversions.outputWriteln("ALL DONE!");
}
catch (Exception e){	
	try{
		if (writer!=null){
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
			e.printStackTrace(new PrintStream(bytesOut));
			String trace = bytesOut.toString(response.getCharacterEncoding());
			writer.println(trace);
			writer.flush();
		}
	}
	catch (Exception ee){}
}
finally{
	try {
		if (conn!=null) conn.close();
	}
	catch (SQLException e){}
}
%>
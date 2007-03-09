<%@page contentType="text/plain"%>
<%@ page import="java.sql.*,java.io.*,com.tee.xmlserver.*,eionet.util.DataOperations" %>
<%
Connection conn = null;
PrintWriter writer = null;
try{
	XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = XDBApplication.getDBPool();
	conn = pool.getConnection();
	//conn = DataOperations.getTestConnection();
	writer = response.getWriter();
	DataOperations dataOperations = new DataOperations(conn, writer);
	String action = request.getParameter(DataOperations.PARAM_ACTION);
	if (action!=null && action.trim().length()>0){
		if (action.equals(DataOperations.ACTION_CLEANUP))
			dataOperations.cleanup();
		else if (action.equals(DataOperations.ACTION_CREATE))
			dataOperations.create();
		else if (action.equals(DataOperations.ACTION_BOOLEAN_VALUES))
			dataOperations.createBooleanFixedValues();
		else
			throw new Exception("Unknown action: " + action);
	}
	else{
		dataOperations.cleanup();
		dataOperations.create();
	}	
	dataOperations.outputWriteln("");
	dataOperations.outputWriteln("ALL DONE!");
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
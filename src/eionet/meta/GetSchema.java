package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.caucho.sql.DBPool;
import eionet.meta.schema.*;

public class GetSchema extends HttpServlet {
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        try{
            String delem_id = req.getParameter("delem_id");

	        if (delem_id == null || delem_id.length()==0){
	            printError("Data element ID is missing!", res.getOutputStream());
		        return;
	        }

	        ServletContext ctx = getServletContext();	
	        String appName = ctx.getInitParameter("application-name");

	        Connection conn = DBPool.getPool(appName).getConnection();
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
    	
	        DataElement dataElement = searchEngine.getDataElement(delem_id);
	        if (dataElement == null){
	            printError("Data element was not found!", res.getOutputStream());
                return;
            }
            
	        String templatePath = ctx.getInitParameter("template-" + dataElement.getType());
	        if (templatePath == null){
    	        printError("Could not get schema template path from conf!", res.getOutputStream());
    	        return;
	        }
	        
	        String basensPath = ctx.getInitParameter("basens-path");
	        if (basensPath == null){
    	        printError("Could not get base namespace url path!", res.getOutputStream());
    	        return;
	        }
	        
	        if (dataElement.getType().equalsIgnoreCase("AGG")){
	            
	            String choiceID = dataElement.getChoice();
	            String sequenceID = dataElement.getSequence();
	            
	            getServletContext().log("choiceID=" + choiceID);
	            getServletContext().log("sequenceID=" + sequenceID);
	            
	            if (choiceID != null && sequenceID != null){
	                printError("An aggregate cannot have both a sequence and a choice!", res.getOutputStream());
    	            return;
	            }
        		
		        Vector subElements = null;
		        if (choiceID != null)
		            subElements = searchEngine.getChoice(choiceID);
		        else if (sequenceID != null)
		            subElements = searchEngine.getSequence(sequenceID);
		        
	            if (subElements != null && subElements.size()!=0){
	                for (int i=0; i<subElements.size(); i++){
	                    
	                    Object o = subElements.get(i);
		                Class oClass = o.getClass();
		                String oClassName = oClass.getName();
                		
		                if (oClassName.endsWith("DataElement")){
			                DataElement elem = (DataElement)o;
	                        elem.setDefinitionUrl(basensPath + "/GetSchema?delem_id=" + elem.getID());
	                    }
	                }
        	        
	                dataElement.setSubElements(subElements);
	            }
            }
        
            if (dataElement.getType().equalsIgnoreCase("CH1")){
        		
		        Vector fixedValues = searchEngine.getFixedValues(delem_id);
		        dataElement.setFixedValues(fixedValues);
            }
        
            String ds = req.getParameter("ds");
            boolean isDataset = (ds!=null && ds.equals("true")) ? true : false;
            
            SchemaExp schemaExp = new SchemaExp(dataElement, searchEngine);//, templatePath);
            if (isDataset)
                schemaExp.setMode(SchemaExp.DATASET_MODE);
            schemaExp.setBaseNsPath(basensPath);
            //schemaExp.setServletContext(ctx);
        
            PrintWriter writer = new PrintWriter(res.getOutputStream());
	        schemaExp.export(writer);
    	    
	        writer.flush();
	        writer.close();
	    }
	    catch (Exception e){
	        printError(e.toString(), res.getOutputStream());
	        return;
	    }
    }
    
    private void printError(String msg, OutputStream out){
        
        StringBuffer buf = new StringBuffer("<html><body><b>");
        buf.append(msg);
        buf.append("</b></body></html>");
        
        PrintWriter writer = new PrintWriter(out);
        writer.println(buf.toString());
        writer.flush();
        writer.close();
    }
}
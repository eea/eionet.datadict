package eionet.meta.exports.xforms;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDSearchEngine;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

public class XFormServlet extends HttpServlet {
    
    private static final String TEMPLATE_NAME = "xform.xhtml";
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        PrintWriter writer = null;
        Connection conn = null;
        
        try{
            // get ID of table whose form to create
            String id = req.getParameter("id");
            if (Util.voidStr(id)) throw new Exception("Missing id!");
            id = parseID(id);
            
            // get url of the template
            String template = req.getParameter("template");
            if (Util.voidStr(template)) template = Props.getProperty(PropsIF.XFORM_TEMPLATE_URL);
            if (Util.voidStr(template)) throw new Exception("Missing template path!");
            
            ServletContext ctx = getServletContext();

            conn = ConnectionUtil.getConnection();
                
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            res.setContentType("text/xml; charset=UTF-8");
            OutputStreamWriter osw = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

            XFormIF xForm = new TblXForm(searchEngine, writer);
            
            // build application context (protocol + host + port + context path)
            String reqUrl = req.getRequestURL().toString();
            int i = reqUrl.lastIndexOf("/");
            if (i != -1) xForm.setAppContext(reqUrl.substring(0,i));
                
            xForm.write(id);
            xForm.flush(template);
            writer.flush();
            osw.flush();
            writer.close();
            osw.close();
        }
        catch (Exception e){
            e.printStackTrace(System.out);
            throw new ServletException(e.toString());
        }
        finally{
            try{
                if (writer != null) writer.close();
                if (conn != null) conn.close();
            }
            catch(Exception ee){}
        }
    }
    
    private String parseID(String id) throws Exception{
        
        String result = id;
        try{
            Long.parseLong(result);
        }
        catch (NumberFormatException nfe){
            String pattern = new String("id=TBL");
            int i = result.indexOf(pattern);
            if (i == -1) throw new Exception("Invalid ID!");
            result = result.substring(i + pattern.length());
            try{
                Long.parseLong(result);
            }
            catch (NumberFormatException nfe2){
                throw new Exception("Invalid ID!");
            }
        }
        
        return result;
    }
}

package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import eionet.meta.exports.pdf.*;

public class GetImportResults extends HttpServlet {
	
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        // get the text to write
        String text = req.getParameter("text");
        if (text==null)
            throw new ServletException("Text was null!");
            
	    // set up the OutputStream to write to
	    ByteArrayOutputStream barray = new ByteArrayOutputStream();
    	    
		// construct the PDF
		PdfHandoutIF pdf = new ImportResults(barray);

		try{		
	        // write the text
			pdf.write(text);
			
			// flush the text to barray
			pdf.flush();
		}
		catch (Exception e){
			e.printStackTrace(new PrintStream(res.getOutputStream()));
		}
            
		// flush the document to the servlet output stream
        res.setContentType("application/pdf");
        res.setContentLength(barray.size()); // not supported by Resin version < 2.x.x
            
        ServletOutputStream out = res.getOutputStream();
        barray.writeTo(out);
        out.flush();
    }
}
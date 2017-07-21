package eionet.meta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.exports.pdf.ImportResults;
import eionet.meta.exports.pdf.PdfHandoutIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetImportResults extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetImportResults.class);

    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        // get the text to write
        String text = req.getParameter("text");
        if (text == null)
            throw new ServletException("Text was null!");

        // set up the OutputStream to write to
        ByteArrayOutputStream barray = new ByteArrayOutputStream();

        // construct the PDF
        PdfHandoutIF pdf = new ImportResults(barray);

        try {
            // write the text
            pdf.write(text);

            // flush the text to barray
            pdf.flush();
        } catch (Exception e) {
            e.printStackTrace(new PrintStream(res.getOutputStream()));
            LOGGER.error(e.getMessage(), e);
        }

        // flush the document to the servlet output stream
        res.setContentType("application/pdf");
        res.setContentLength(barray.size()); // not supported by Resin version < 2.x.x

        ServletOutputStream out = res.getOutputStream();
        barray.writeTo(out);
        out.flush();
    }
}

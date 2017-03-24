package eionet.datadict.web;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataSet;
import eionet.datadict.services.data.DatasetDataService;
import eionet.meta.DDSearchEngine;
import static eionet.meta.GetSchema.DST;
import static eionet.meta.GetSchema.ELM;
import static eionet.meta.GetSchema.TBL;
import eionet.meta.exports.schema.DstSchema;
import eionet.meta.exports.schema.ElmSchema;
import eionet.meta.exports.schema.ElmsContainerSchema;
import eionet.meta.exports.schema.SchemaIF;
import eionet.meta.exports.schema.TblSchema;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Controller
@RequestMapping(value = "/datasets")
public class DataSetController {

    private final DatasetDataService datasetDataService;

    @Autowired
    public DataSetController(DatasetDataService datasetDataService) {
        this.datasetDataService = datasetDataService;
    }

    @RequestMapping(value = "/testmvc", method = RequestMethod.GET)
    @ResponseBody
    public String testMVCINDD() {
        return "it works";
    }

    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    @ResponseBody
    public void getDataSetSchema(@RequestParam(value = "id") String id ,HttpServletRequest request,HttpServletResponse response ) throws ResourceNotFoundException, ServletException {

     PrintWriter writer = null;
        Connection conn = null;

        try {
            // get request parameters

            String compID = id;
            if (Util.isEmpty(compID))
                throw new Exception("Schema ID missing!");

            String compType = null;
            if (compID.startsWith(DST)) {
                compType = DST;
                compID = compID.substring(DST.length());
            } else if (compID.startsWith(TBL)) {
                compType = TBL;
                compID = compID.substring(TBL.length());
            } else if (compID.startsWith(ELM)) {
                compType = ELM;
                compID = compID.substring(ELM.length());
            } else
                throw new Exception("Malformed schema ID!");

            // see if this is a "container schema"
            String servletPath = request.getServletPath();
            boolean isContainerSchema = servletPath != null && servletPath.trim().startsWith("/GetContainerSchema");


            // get the DB connection, initialize DDSearchEngine
            conn = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "");

            // set response content type
            response.setContentType("text/xml; charset=UTF-8");

            // init stream writer
            OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

            // create schema genereator
            SchemaIF schema = null;
            if (!isContainerSchema) {
                if (compType.equals(DST))
                    schema = new DstSchema(searchEngine, writer);
                else if (compType.equals(TBL))
                    schema = new TblSchema(searchEngine, writer);
                else if (compType.equals(ELM))
                    schema = new ElmSchema(searchEngine, writer);
                else
                    throw new Exception("Invalid component type!");
            } else {
                if (compType.equals(TBL))
                    schema = new ElmsContainerSchema(searchEngine, writer);
                else
                    throw new Exception("Invalid component type for a container schema!");
            }
            schema.setIdentitation("\t");

            // build application context
            String reqUri = request.getRequestURL().toString();
            int i = reqUri.lastIndexOf("/");
            if (i != -1) schema.setAppContext(reqUri.substring(0, i));

            // set content disposition header
            StringBuffer strBuf = new StringBuffer("attachment; filename=\"schema-").
            append(compType.toLowerCase()).append("-").append(compID).append(".xsd\"");
            response.setHeader("Content-Disposition", strBuf.toString());

            // write & flush schema
            schema.write(compID);
            schema.flush();

            // flush and close stream writer
            writer.flush();
            osw.flush();
            writer.close();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new ServletException(e.toString());
        } finally {
            try {
                if (writer != null) writer.close();
                if (conn != null) conn.close();
            } catch (Exception ee) {}
        }
        
        
        
    //    return this.datasetDataService.getDataset(Integer.parseInt(id));
    //     response.setContentType("application/xml");
     //    response.setHeader("Content-Disposition", "attachment;filename=thisIsTheFileName.xml");
    }
}


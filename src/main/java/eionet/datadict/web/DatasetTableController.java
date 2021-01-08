package eionet.datadict.web;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.DataSetTableService;
import eionet.datadict.services.data.DatasetTableDataService;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import eionet.meta.outservices.OutService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Controller
@RequestMapping(value = "/datasetTable")
public class DatasetTableController {

    private final DataSetTableService dataSetTableService;
    private final DatasetTableDataService datasetTableDataService;
    private OutService outService;
    private static final Logger LOGGER = Logger.getLogger(DatasetTableController.class);
    private static final String GENERIC_DD_ERROR_PAGE_URL = "/error.action?type=INTERNAL_SERVER_ERROR&message=";
    private static final String SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX = "schema-tbl-";

    @Autowired
    public DatasetTableController(DataSetTableService dataSetTableService, DatasetTableDataService datasetTableDataService, OutService outService) {
        this.dataSetTableService = dataSetTableService;
        this.datasetTableDataService = datasetTableDataService;
        this.outService = outService;
    }

    @RequestMapping(value = "/{id}/schema", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDatasetTableSchema(@PathVariable int id, HttpServletResponse response) throws ResourceNotFoundException, ServletException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {
        Document xml = this.dataSetTableService.getDataSetTableXMLSchema(id);
        String fileName = "schema-tbl-".concat(String.valueOf(id)).concat(".xsd");
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ServletOutputStream outStream = response.getOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(xml);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
        outStream.flush();
        outStream.close();
    }

    @RequestMapping(value = "/{id}/{variable:.+}")
    @ResponseBody
    public void getDatasetTableSchemaByFileName(@PathVariable int id, @PathVariable String variable, HttpServletResponse response) throws XmlExportException, IOException, ResourceNotFoundException, TransformerConfigurationException, TransformerException, BadRequestException {
        Document xml = null;
        Pattern TableFileNamePattern = Pattern.compile("\\b" + SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX + "\\d+.xsd");

        if (TableFileNamePattern.matcher(variable).matches()) {
            int tableId = Integer.parseInt(variable.replace(SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX, "").replace(".xsd", "").trim());
            xml = this.dataSetTableService.getDataSetTableXMLSchema(tableId);
            String fileName = SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX.concat(String.valueOf(tableId)).concat(".xsd");
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        } else {
            throw new BadRequestException("Could not retrieve schema with the given filename: " + variable);
        }
        ServletOutputStream outStream = response.getOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(xml);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
        outStream.flush();
        outStream.close();
    }

    
    @RequestMapping(value = "/{id}/instance", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDataSetTableInstance(@PathVariable int id, HttpServletResponse response) throws ResourceNotFoundException, ServletException, EmptyParameterException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {
        Document xml = this.dataSetTableService.getDataSetTableXMLInstance(id);
        String fileName = "table" + id + "-instance.xml";
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ServletOutputStream outStream = response.getOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(xml);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
        outStream.flush();
        outStream.close();
    }

    @RequestMapping(value = "{id}/json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DatasetTable getDataSetTable(@PathVariable int id) throws ResourceNotFoundException {

        DatasetTable dTable = this.datasetTableDataService.getFullDatasetTableDefinition(id);
        return dTable;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public void HandleResourceNotFoundException(Exception exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.log(Level.ERROR, null, exception);
        response.sendRedirect(request.getContextPath() + GENERIC_DD_ERROR_PAGE_URL + "  " + exception.getMessage());
    }

    @ExceptionHandler({IOException.class, TransformerConfigurationException.class, TransformerException.class, XmlExportException.class, DOMException.class})
    public void HandleFatalExceptions(Exception exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.log(Level.ERROR, null, exception);
        response.sendRedirect(request.getContextPath() + GENERIC_DD_ERROR_PAGE_URL + "error exporting XML. " + exception.getMessage());
    }

    @RequestMapping(value = "all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Vector getDataSetTables() throws Exception {
        Vector dsTables = outService.getDSTables();
        return dsTables;
    }
}

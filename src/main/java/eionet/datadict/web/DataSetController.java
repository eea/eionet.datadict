package eionet.datadict.web;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetService;
import java.io.IOException;
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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Controller
@RequestMapping(value = "/dataset")
public class DataSetController {

    private final DataSetService dataSetService;
    private static final Logger LOGGER = Logger.getLogger(DataSetController.class);

    @Autowired
    public DataSetController(DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    @RequestMapping(value = "/testmvc", method = RequestMethod.GET)
    @ResponseBody
    public String testMVCINDD() {
        return "it works";
    }

    @RequestMapping(value = "/{id}/schema", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDataSetSchema(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException, ServletException, EmptyParameterException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {

        Document xml = this.dataSetService.getDataSetXMLSchema(id);
        String fileName = "schema-dst-".concat(String.valueOf(id)).concat(".xsd");
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

    @RequestMapping(value = "/{id}/instance", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDataSetInstance(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException, ServletException, EmptyParameterException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {

        Document xml = this.dataSetService.getDataSetXMLInstance(id);
        String fileName = "dataset-instance.xml";
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

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND,
            reason = "Dataset with given id Not Found")
    public void HandleResourceNotFoundException(Exception exception) {
        LOGGER.error(exception.getMessage(), exception.getCause());
    }

    @ExceptionHandler({IOException.class, TransformerConfigurationException.class, TransformerException.class, XmlExportException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,
            reason = "Internal Server Error while processing the Request.")
    public void HandleFatalExceptions(Exception exception) {
    }
}

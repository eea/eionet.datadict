package eionet.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A simple utility class that provides validation services for a given XML content.
 *
 * @author Jaanus Heinlaid
 */
public class XmlValidator {

    /** Validation error encountered by the last validation service called.  */
    private Exception validationError;

    /**
     * Parses the given input stream as XML content. Returns true if it is well-formed XML, otherwise returns false. In the latter
     * case, the error returned by SAX parser is available through {@link #getValidationError()}.
     *
     * NB! The caller is responsible for managing the given input stream, including its closure.
     *
     * @param inputStream
     *            The given XML content.
     * @return
     * @throws ParserConfigurationException
     *             If there is a problem with SAX parser configuration.
     * @throws SAXException
     *             If SAX is unable to process the content for some reason.
     * @throws IOException
     *             If there is an IO exception when reading from the given input stream.
     */
    public boolean isWellFormedXml(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        // turn off validation against schema or dtd (we only need the document to be well-formed XML)
        parserFactory.setValidating(false);
        reader.setFeature("http://xml.org/sax/features/validation", false);
        reader.setFeature("http://apache.org/xml/features/validation/schema", false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader.setFeature("http://xml.org/sax/features/namespaces", true);

        try {
            reader.parse(new InputSource(inputStream));
            return true;
        } catch (SAXException saxe) {
            this.validationError = saxe;
            return false;
        }
    }

    /**
     * Parses the given input stream as XML Schema. Returns true if it is a fully valid XML Schema, otherwise returns false. In the
     * latter case, the error returned by SAX parser is available through {@link #getValidationError()}.
     *
     * NB! The caller is responsible for managing the given input stream, including its closure.
     *
     * @param inputStream
     *            The given XML content.
     * @return
     * @throws ParserConfigurationException
     *             If there is a problem with SAX parser configuration.
     * @throws SAXException
     *             If SAX is unable to process the content for some reason.
     * @throws IOException
     *             If there is an IO exception when reading from the given input stream.
     */
    public boolean isValidXmlSchema(InputStream inputStream) throws IOException {

        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        try {
            factory.newSchema(new StreamSource(inputStream));
            return true;
        } catch (SAXException saxe) {
            this.validationError = saxe;
            return false;
        }
    }

    /**
     * @return the validationError
     */
    public Exception getValidationError() {
        return validationError;
    }
}

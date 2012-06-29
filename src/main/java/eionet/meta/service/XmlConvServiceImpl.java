/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.service;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.tee.uit.client.ServiceClientIF;
import com.tee.uit.client.ServiceClients;

import eionet.meta.service.data.SchemaConversionsData;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Service for accessing remote services from XML converters application.
 *
 * @author Juhan Voolaid
 */
@Service
public class XmlConvServiceImpl implements IXmlConvService {

    /**
     * @see eionet.meta.service.IXmlConvService#getSchemaConversionsData(java.lang.String)
     */
    @Override
    public SchemaConversionsData getSchemaConversionsData(String schemaUrl) throws ServiceException {
        SchemaConversionsData result = new SchemaConversionsData();

        InputStream inputStream = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            // Conversions
            String conversionsUrl = Props.getRequiredProperty(PropsIF.XML_CONV_URL) + "/api/listConversions?schema=" + schemaUrl;
            URL url = new URL(conversionsUrl);
            inputStream = url.openStream();
            Document doc = docBuilder.parse(inputStream);
            NodeList nodeList = doc.getElementsByTagName("conversion");
            result.setNumberOfConversions(nodeList.getLength());

            // QA Scrpits
            String scriptsUrl = Props.getRequiredProperty(PropsIF.XML_CONV_URL) + "/RpcRouter";
            String scriptsService = "XQueryService";
            ServiceClientIF client = ServiceClients.getServiceClient(scriptsService, scriptsUrl);
            @SuppressWarnings("unchecked")
            Vector<String> scriptsResult = (Vector<String>) client.getValue("listQAScripts", new Vector<String>(Collections.singletonList(schemaUrl)));
            result.setNumberOfQAScripts(scriptsResult.size());

            // XML Conv schema page url
            result.setXmlConvUrl(Props.getRequiredProperty(PropsIF.XML_CONV_URL) + "/do/viewSchemaForm");

            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to get scripts and conversions info from XmlConv: " + e.getMessage(), e);
        }
        finally{
            IOUtils.closeQuietly(inputStream);
        }
    }
}

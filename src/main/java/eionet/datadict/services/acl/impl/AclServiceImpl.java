package eionet.datadict.services.acl.impl;

import eionet.acl.AccessController;
import eionet.acl.SignOnException;
import eionet.datadict.errors.UserExistsException;
import eionet.datadict.errors.XmlMalformedException;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

@Service
public class AclServiceImpl implements AclService {

    @Override
    public boolean hasPermission(DDUser user, AclEntity entity, Permission prm) {
        try {
            return SecurityUtil.hasPerm(user, entity.getPath(), prm.getValue());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean hasPermission(DDUser user, AclEntity entity, String entityId, Permission prm) {
        try {
            return SecurityUtil.hasPerm(user, this.getEntityPath(entity, entityId), prm.getValue());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void grantAccess(DDUser user, AclEntity entity, String entityId, String description) {
        try {
            AccessController.addAcl(this.getEntityPath(entity, entityId), this.getUserName(user), description);
        }
        catch (SignOnException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void removeAccessRightsForDeletedEntity(AclEntity entity, String entityId) {
        try {
            AccessController.removeAcl(this.getEntityPath(entity, entityId));
        } catch (SignOnException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addUserToAclGroup(String username, String groupName) throws UserExistsException, XmlMalformedException {
        try {
            Document document = getDocument();
            Node group = getGroupNode(groupName, document);
            for (int i = 0; i < group.getChildNodes().getLength(); i++) {
                if (group.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) group.getChildNodes().item(i);
                    if (el.getAttribute("userid").contentEquals(username)) {
                        throw new UserExistsException(username + " exists in group " + groupName);
                    }
                }
            }
            Element groupEntry = document.createElement("member");
            groupEntry.setAttribute("userid",username);
            group.appendChild(groupEntry);
            writeResultToFile(document);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException | XPathExpressionException e) {
            throw new XmlMalformedException(e.getMessage());
        }
    }

    @Override
    public void removeUserFromAclGroup(String userName, String groupName) throws XmlMalformedException {
        try {
            Document document = getDocument();
            Node group = getGroupNode(groupName, document);
            for (int i = 0; i < group.getChildNodes().getLength(); i++) {
                if (group.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) group.getChildNodes().item(i);
                    if (el.getAttribute("userid").contentEquals(userName)) {
                        group.removeChild(group.getChildNodes().item(i));
                        break;
                    }
                }
            }
            removeEmptyLines(document);
            writeResultToFile(document);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException | XPathExpressionException e) {
            throw new XmlMalformedException(e.getMessage());
        }
    }

    protected void removeEmptyLines(Document document) throws XPathExpressionException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
                "//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList)
                xpathExp.evaluate(document, XPathConstants.NODESET);
        for (int j = 0; j < emptyTextNodes.getLength(); j++) {
            Node emptyTextNode = emptyTextNodes.item(j);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
    }

    protected void writeResultToFile(Document document) throws TransformerException, IOException {
        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");

        // use a StringWriter to capture the transformation output
        StringWriter writer = new StringWriter();
        StreamResult tempResult = new StreamResult(writer);
        transformer.transform(source, tempResult);

        // ensure a newline after XML declaration as the acl library isXmlFileWannabe function checks
        // if the first line ends with ?> to identify xml files
        String output = writer.toString().replaceFirst("\\?>", "?>\n");

        // write the modified content to file
        try (FileWriter fileWriter = new FileWriter(AccessController.getAclProperties().getFileLocalgroups())) {
            fileWriter.write(output);
        }
    }

    protected Node getGroupNode(String groupName, Document document) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        String expression = "//group[@id=" + "'" + groupName + "'" + "]";

        XPathExpression expr = xpath.compile(expression);
        NodeList DDAdmingroups =(NodeList) expr.evaluate(document, XPathConstants.NODESET);
        return DDAdmingroups.item(0);
    }

    protected Document getDocument() throws ParserConfigurationException, SAXException, IOException {
        File file = new File(AccessController.getAclProperties().getFileLocalgroups());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(file);
    }

    protected String getUserName(DDUser user) {
        return user == null ? "" : user.getUserName();
    }
    
    protected String getEntityPath(AclEntity entity, String entityId) {
        return entity.getPath() + "/" + entityId;
    }
    
}

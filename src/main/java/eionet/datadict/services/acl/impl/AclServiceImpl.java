package eionet.datadict.services.acl.impl;

import eionet.acl.AccessController;
import eionet.acl.SignOnException;
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
import java.io.IOException;

@Service
public class AclServiceImpl implements AclService {

    @Override
    public boolean hasPermission(DDUser user, AclEntity entity, Permission prm) {
        try {
            return SecurityUtil.hasPerm(this.getUserName(user), entity.getPath(), prm.getValue());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean hasPermission(DDUser user, AclEntity entity, String entityId, Permission prm) {
        try {
            return SecurityUtil.hasPerm(this.getUserName(user), this.getEntityPath(entity, entityId), prm.getValue());
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
    public void addUserToAclGroup(String username, String groupName) throws ParserConfigurationException, IOException, SAXException, TransformerException, XPathExpressionException {

        File file = new File(AccessController.getAclProperties().getFileLocalgroups());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        // given a dd_group name, we get its children.
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        String expression = "//group[@id=" + "'" + groupName + "'" + "]";

        XPathExpression expr = xpath.compile(expression);
        //NodeList groupEntries = document.getElementsByTagName("dd_admin");
        NodeList DDAdmingroups =(NodeList) expr.evaluate(document, XPathConstants.NODESET);
        Node group = DDAdmingroups.item(0);
        for (int i = 0; i < group.getChildNodes().getLength(); i++) {
            if (group.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) group.getChildNodes().item(i);
                //throw appropriate exception here
                if (el.getAttribute("userid").contains(username)) {
                }
            }
        }

        Element groupEntry = document.createElement("member");
        groupEntry.setAttribute("userid",username);
        group.appendChild(groupEntry);

        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        StreamResult result = new StreamResult(AccessController.getAclProperties().getFileLocalgroups());
        transformer.transform(source, result);
    }

    protected String getUserName(DDUser user) {
        return user == null ? "" : user.getUserName();
    }
    
    protected String getEntityPath(AclEntity entity, String entityId) {
        return entity.getPath() + "/" + entityId;
    }
    
}

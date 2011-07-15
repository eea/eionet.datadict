package eionet.meta.exports.xforms;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InstanceDOM {
    
    private String xmlFile = null;
    private Document document = null;
    
    public InstanceDOM(String xmlFile) throws InstanceDOMException{
        this.xmlFile = xmlFile;     
    }
    
    private void load() throws InstanceDOMException{
                
        try {
            DocumentBuilderFactory factory = 
            DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        }
        catch (Exception e) {
            throw new InstanceDOMException(e);
        } 
    }
    
    public String getRef(String tagName) throws InstanceDOMException{
        
        if (document==null) load();
        
        NodeList nodes = document.getElementsByTagName(tagName);
        System.out.println(nodes);
        for (int i=0; nodes!=null && i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            System.out.println(node.toString());
        }
        
        Element element = nodes==null ? null : (Element)nodes.item(0);
        if (element==null)
            throw new InstanceDOMException("Could not find <" + tagName + "> element!");
        
        return getFullXmlPath(element);
    }
    
    private String getFullXmlPath(Element element) throws InstanceDOMException{
        
        StringBuffer buf = new StringBuffer(element.getTagName());
        Node parent = element.getParentNode();
        while (parent!=null && parent.getNodeType()!=Node.DOCUMENT_NODE){
            buf.insert(0, "/");
            buf.insert(0, parent.getNodeName());
            parent = parent.getParentNode();
        }
        
        String s = buf.toString();
        return buf.toString();
    }
    
    public static void main(String[] args){
        
        try{
            InstanceDOM instanceDOM = new InstanceDOM(
                        "http://localhost:8080/datadict/public/GetXmlInstance?id=1853");
            String s = instanceDOM.getRef("dd176:nuka_elem");
            System.out.println(s);
        }
        catch (InstanceDOMException ide){
            ide.printStackTrace(System.out);
        }
    }
}

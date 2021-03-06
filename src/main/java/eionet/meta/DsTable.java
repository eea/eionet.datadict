package eionet.meta;

import java.util.Vector;

import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/**
 * Domain object for the records in DS_TABLE table.
 *
 * @author Jaanus
 */
@SuppressWarnings("rawtypes")
public class DsTable implements Comparable {

    private String id = null;
    private String dsID = null;
    private String shortName = null;
    private String identifier = null;
    private String version = null;

    private String name = null;
    private String nsID = null;
    private String parentNS = null;
    private String datasetName = null;
    private String dstIdentifier = null;
    private String dstStatus = null;
    private String dstWorkingUser = null;
    private String dstDate = null;

    private String workingUser = null;
    private String workingCopy = null;

    private String compStr = null;

    private Vector elements = new Vector();
    private Vector simpleAttrs = new Vector();

    private int dstVersion = -1;

    private String owner = null;

    private int positionInDataset;

    /**
     * Constructor.
     *
     * @param id
     * @param dsID
     * @param shortName - Short name
     */
    public DsTable(String id, String dsID, String shortName) {
        this.id = id;
        this.shortName = shortName;
        this.dsID = dsID;
    }

    public String getID() {
        return id;
    }

    public String getDatasetID() {
        return dsID;
    }

    public String getShortName() {
        return shortName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addElement(DataElement element) {
        elements.add(element);
    }

    public void setElements(Vector elements) {
        this.elements = elements;
    }

    public Vector getElements() {
        return elements;
    }

    public void setNamespace(String nsID) {
        this.nsID = nsID;
    }

    public String getNamespace() {
        return nsID;
    }

    public void setSimpleAttributes(Vector v) {
        this.simpleAttrs = v;
    }

    public Vector getSimpleAttributes() {
        return simpleAttrs;
    }

    public Vector simpleAttributesTable() {
        Vector v = new Vector();
        return v;
    }

    public Vector elementsTable() {
        Vector v = new Vector();
        return v;
    }

    public boolean isWorkingCopy() {
        if (workingCopy == null) {
            return false;
        } else if (workingCopy.equals("Y")) {
            return true;
        } else {
            return false;
        }
    }

    public void setWorkingCopy(String workingCopy) {
        this.workingCopy = workingCopy;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setDatasetName(String dsName) {
        this.datasetName = dsName;
    }

    public String getDatasetName() {
        return this.datasetName;
    }

    public void setParentNs(String nsid) {
        this.parentNS = nsid;
    }

    public String getParentNs() {
        return parentNS;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public String getWorkingUser() {
        return this.workingUser;
    }

    public Vector getVersioningAttributes() {
        if (simpleAttrs == null) {
            return null;
        }

        Vector set = new Vector();
        for (int i = 0; i < simpleAttrs.size(); i++) {
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            if (attr.effectsVersion()) {
                set.add(attr);
            }
        }

        return set;
    }

    public String getAttributeValueByShortName(String name) {

        DElemAttribute attr = null;
        for (int i = 0; i < simpleAttrs.size(); i++) {
            attr = (DElemAttribute) simpleAttrs.get(i);
            if (attr.getShortName().equalsIgnoreCase(name)) {
                return attr.getValue();
            }
        }

        return null;
    }

    public DElemAttribute getAttributeByShortName(String name) {
        for (int i = 0; i < simpleAttrs.size(); i++) {
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            if (attr.getShortName().equalsIgnoreCase(name)) {
                return attr;
            }
        }
        return null;
    }

    public void setCompStr(String compStr) {
        this.compStr = compStr;
    }

    public String getCompStr() {
        return compStr;
    }

    public String getDstIdentifier() {
        return dstIdentifier;
    }

    public void setDstIdentifier(String dstIdentifier) {
        this.dstIdentifier = dstIdentifier;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int compareTo(Object o) {

        if (!o.getClass().getName().endsWith("DsTable")) {
            return 1;
        }

        DsTable oTbl = (DsTable) o;
        String oCompStr = oTbl.getCompStr();
        if (oCompStr == null && compStr == null) {
            return 0;
        } else if (oCompStr == null) {
            return 1;
        } else if (compStr == null) {
            return -1;
        }

        return compStr.compareToIgnoreCase(oCompStr);
    }

    public String getRelativeTargetNs() {

        if (Util.isEmpty(dstIdentifier)) {
            if (Util.isEmpty(parentNS)) {
                return "";
            } else {
                return "/namespaces/" + parentNS;
            }
        } else {
            return "/datasets/" + dstIdentifier;
        }
    }

    public String getRelativeCorrespNs() {

        if (Util.isEmpty(dstIdentifier)) {
            if (Util.isEmpty(nsID)) {
                return "";
            } else {
                return "/namespaces/" + nsID;
            }
        } else {
            return "/datasets/" + dstIdentifier + "/tables/" + identifier;
        }
    }

    /*
     *
     */
    public String getReferenceURL() {

        if (getIdentifier() == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer();

        String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
        if (jspUrlPrefix != null) {
            buf.append(jspUrlPrefix);
        }

        buf.append("datasets/latest/").append(getDstIdentifier()).append("/tables/").append(getIdentifier());

        return buf.toString();
    }

    public String getDstStatus() {
        return dstStatus;
    }

    public void setDstStatus(String dstStatus) {
        this.dstStatus = dstStatus;
    }

    public String getDstWorkingUser() {
        return dstWorkingUser;
    }

    public void setDstWorkingUser(String dstWorkingUser) {
        this.dstWorkingUser = dstWorkingUser;
    }

    public String getDstDate() {
        return dstDate;
    }

    public void setDstDate(String dstDate) {
        this.dstDate = dstDate;
    }

    public int getPositionInDataset() {
        return positionInDataset;
    }

    public void setPositionInDataset(int positionInDataset) {
        this.positionInDataset = positionInDataset;
    }

}

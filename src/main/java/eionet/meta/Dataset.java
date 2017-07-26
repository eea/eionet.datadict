package eionet.meta;

import java.util.Hashtable;
import java.util.Vector;

import eionet.util.Props;
import eionet.util.PropsIF;

/*
 *
 */
public class Dataset implements Comparable {

    /** */
    private String id = null;
    private String shortName = null;
    private String version = null;
    private String status = null;
    private String name = null;
    private String identifier = null;
    private String date = null;

    private String workingCopy = null;
    private String workingUser = null;

    private String nsID = null;
    private String visual = null;
    private String detailedVisual = null;

    private Vector tables = new Vector();
    private Vector simpleAttrs = new Vector();

    private int displayCreateLinks = -1;
    private static Hashtable createLinkWeights = null;

    private int sortOrder = 1;
    private String sortString = null;

    private String checkedoutCopyID = null;
    private String successorId = null;

    /** */
    private String user;

    /*
     *
     */
    public Dataset(String id, String shortName, String version) {
        this.id = id;
        this.shortName = shortName;
        this.version = version;
    }

    public String getID() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getVersion() {
        return version;
    }

    public void setNamespaceID(String nsID) {
        this.nsID = nsID;
    }

    public String getNamespaceID() {
        return this.nsID;
    }

    public void addElement(DsTable table) {
        tables.add(table);
    }

    public void setTables(Vector tables) {
        this.tables = tables;
    }

    public Vector getTables() {
        return tables;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public String getVisual() {
        return visual;
    }

    public void setDetailedVisual(String detailedVisual) {
        this.detailedVisual = detailedVisual;
    }

    public String getDetailedVisual() {
        return detailedVisual;
    }

    public void setSimpleAttributes(Vector v) {
        this.simpleAttrs = v;
    }

    public Vector getSimpleAttributes() {
        return simpleAttrs;
    }

    public String getAttributeValueByShortName(String name) {

        for (int i = 0; i < simpleAttrs.size(); i++) {
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return this.date;
    }

    public String getRelativeTargetNs() {
        return "/datasets";
    }

    public String getRelativeCorrespNs() {
        return "/datasets/" + identifier;
    }

    /*
     * The return value indicates weather the given "Create..." link should be displayed
     */
    public boolean displayCreateLink(String linkID) {

        // if not a single create link should be displayed then obviously return false
        if (displayCreateLinks == 0) {
            return false;
        }

        Hashtable weights = Dataset.getCreateLinkWeights();
        Integer weight = (Integer) weights.get(linkID);
        if (weight == null) {
            return false;
        }

        // if the integer division displayCreateLinks/weight is not a multiplicand of 2,
        // then the given link should not be displayed
        int div = displayCreateLinks / weight.intValue();
        if (div % 2 != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Hashtable getCreateLinkWeights() {

        if (createLinkWeights == null) {
            createLinkWeights = new Hashtable();
            createLinkWeights.put("PDF", new Integer(1));
            createLinkWeights.put("XLS", new Integer(2));
            createLinkWeights.put("XMLINST", new Integer(4));
            createLinkWeights.put("XMLSCHEMA", new Integer(8));
            createLinkWeights.put("MDB", new Integer(16));
            createLinkWeights.put("ODS", new Integer(32));
        }

        return createLinkWeights;
    }

    public void setDisplayCreateLinks(int displayCreateLinks) {
        this.displayCreateLinks = displayCreateLinks;
    }

    /*
     *
     */
    public void setComparation(String sortString, int sortOrder) {

        this.sortString = sortString;
        this.sortOrder = sortOrder;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.sortString;
    }

    /*
     *
     */
    public int compareTo(Object o) {
        return this.sortOrder * this.sortString.compareTo(o.toString());
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

        buf.append("datasets/latest/").append(getIdentifier());
        return buf.toString();
    }

    /**
     *
     * @return
     */
    public String getCheckedoutCopyID() {
        return checkedoutCopyID;
    }

    /**
     *
     * @param checkedoutCopyID
     */
    public void setCheckedoutCopyID(String checkedoutCopyID) {
        this.checkedoutCopyID = checkedoutCopyID;
    }

    public String getSuccessorId() {
        return successorId;
    }

    public void setSuccessorId(String successorId) {
        this.successorId = successorId;
    }
    
    

    /**
     *
     * @return
     */
    public String getWorkingUser() {
        return workingUser;
    }

    /**
     *
     * @param workingUser
     */
    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }
}

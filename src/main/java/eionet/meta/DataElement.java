package eionet.meta;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * The domain object for the record in DATAELEM table.
 *
 * @author Jaanus
 */
@SuppressWarnings("rawtypes")
public class DataElement implements Comparable {

    private String id = null;
    private String shortName = null;
    private String type = null;
    private String version = null;
    private String status = null;
    private String identifier = null;

    private String tableID = null;
    private String datasetID = null;
    private String dstShortName = null; // used in the data elements search
    private String tblShortName = null; // used in the data elements search
    private String tblIdentifier = null; // used in setting target namespaces in schemas
    private String dstIdentifier = null; // used in setting target namespaces in schemas
    private String dstWorkingUser = null;
    private String dstStatus = null;

    private String positionInTable = null;

    private String workingUser = null;
    private String workingCopy = null;

    private Namespace ns = null; // parent namespace
    private String topNS = null; // top namespace

    private String user = null; // element creator

    private Vector simpleAttrs = new Vector();
    private Vector fixedValues = null;
    private Vector fks = new Vector();

    private int sortOrder = 1;
    private String sortString = null;

    private String checkedoutCopyID = null;
    private String successorId = null;
    
    private String date = null;

    private String valueDelimiter;

    private boolean mandatoryFlag;
    private boolean primaryKey;

    private String vocabularyId;

    private boolean allConceptsValid;


    /*
     *
     */
    public DataElement() {
    }

    public DataElement(String id, String shortName, String type) {
        this.id = id;
        this.shortName = shortName;
        this.type = type;
    }

    public String getTableID() {
        return tableID;
    }

    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    public String getTblShortName() {
        return tblShortName;
    }

    public void setTblShortName(String tblShortName) {
        this.tblShortName = tblShortName;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public String getDstShortName() {
        return dstShortName;
    }

    public void setDstShortName(String dstShortName) {
        this.dstShortName = dstShortName;
    }

    public String getTblIdentifier() {
        return tblIdentifier;
    }

    public void setTblIdentifier(String tblIdentifier) {
        this.tblIdentifier = tblIdentifier;
    }

    public String getDstIdentifier() {
        return dstIdentifier;
    }

    public void setDstIdentifier(String dstIdentifier) {
        this.dstIdentifier = dstIdentifier;
    }

    public String getID() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getType() {
        return type;
    }

    public void addAttribute(Object attr) {
        simpleAttrs.add(attr);
    }

    public void setAttributes(Vector attrs) {
        simpleAttrs = attrs;
    }

    public Vector getAttributes() {
        return simpleAttrs;
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

    public void setFixedValues(Vector fixedValues) {
        this.fixedValues = fixedValues;
    }

    public Vector getFixedValues() {
        return fixedValues;
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

    public DElemAttribute getAttributeByName(String name) {

        for (int i = 0; i < simpleAttrs.size(); i++) {
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            if (attr.getName().equalsIgnoreCase(name)) {
                return attr;
            }
        }

        return null;
    }

    public DElemAttribute getAttributeById(String id) {

        for (int i = 0; i < simpleAttrs.size(); i++) {
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            if (attr.getID().equalsIgnoreCase(id)) {
                return attr;
            }
        }

        return null;
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

    public String getAttributeValueByName(String name) {

        DElemAttribute attr = null;
        for (int i = 0; i < simpleAttrs.size(); i++) {
            attr = (DElemAttribute) simpleAttrs.get(i);
            if (attr.getName().equalsIgnoreCase(name)) {
                return attr.getValue();
            }
        }

        return null;
    }

    public void setNamespace(Namespace ns) {
        this.ns = ns;
    }

    public Namespace getNamespace() {
        return ns;
    }

    public void setTopNs(String nsid) {
        this.topNS = nsid;
    }

    public String getTopNs() {
        return topNS;
    }

    public String getPositionInTable() {
        return positionInTable;
    }

    public void setPositionInTable(String pos) {
        this.positionInTable = pos;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public String getWorkingUser() {
        return this.workingUser;
    }

    public void setWorkingCopy(String workingCopy) {
        this.workingCopy = workingCopy;
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

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setFKRelations(Vector fks) {
        this.fks = fks;
    }

    public Vector getFKRelations() {
        return this.fks;
    }

    public boolean hasImages() {
        boolean hasImages = false;
        for (int t = 0; simpleAttrs != null && t < simpleAttrs.size(); t++) {
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(t);
            String dispType = attr.getDisplayType();
            Vector values = attr.getValues();
            if (dispType != null && dispType.equals("image") && values != null && values.size() > 0) {
                hasImages = true;
                break;
            }
        }

        return hasImages;
    }

    public void setComparation(String sortString, int sortOrder) {

        this.sortString = sortString;
        this.sortOrder = sortOrder;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.sortString;
    }

    /*
     *
     */
    @Override
    public int compareTo(Object o) {
        return this.sortOrder * this.sortString.compareTo(o.toString());
    }

    public String getReferenceURL() {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }

        StringBuffer result = new StringBuffer();
        String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
        if (jspUrlPrefix != null) {
            result.append(jspUrlPrefix);
        }

        boolean isCommonElement = ns == null || ns.getID() == null;
        if (isCommonElement) {
            result.append("dataelements/latest/" + identifier);
        } else {
            result.append("datasets/latest/").append(dstIdentifier).append("/tables/").append(tblIdentifier).append("/elements/")
                    .append(identifier);
        }

        return result.toString();
    }

    /**
     *
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
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
    public String getDstStatus() {
        return dstStatus;
    }

    /**
     *
     * @param dstStatus
     */
    public void setDstStatus(String dstStatus) {
        this.dstStatus = dstStatus;
    }

    /**
     *
     * @return
     */
    public String getDstWorkingUser() {
        return dstWorkingUser;
    }

    /**
     *
     * @param dstWorkingUser
     */
    public void setDstWorkingUser(String dstWorkingUser) {
        this.dstWorkingUser = dstWorkingUser;
    }

    /**
     *
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     *
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return
     */
    public boolean isCommon() {
        return (ns == null || ns.getID() == null);
    }

    /**
     * @return the valueDelimiter
     */
    public String getValueDelimiter() {
        return valueDelimiter;
    }

    /**
     * @param valueDelimiter
     *            the valueDelimiter to set
     */
    public void setValueDelimiter(String valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    /**
     * @return the mandatoryFlag
     */
    public boolean isMandatoryFlag() {
        return mandatoryFlag;
    }

    /**
     * @param mandatoryFlag
     *            the mandatoryFlag to set
     */
    public void setMandatoryFlag(boolean mandatoryFlag) {
        this.mandatoryFlag = mandatoryFlag;
    }

    /**
     * @return the primaryKey
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /**
     * @param primaryKey
     *            the primaryKey to set
     */
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * indicates if element is taken from an external schema.
     * @return true if identifier contains colon, for example geo:lat
     */
    public boolean isExternalSchema() {
        return StringUtils.contains(identifier, ":");
    }
    /**
     * returns external namespace prefix.
     * @return NS prefix. null if an internal namespace
     */
    public String getNameSpacePrefix() {
        return (isExternalSchema() ? StringUtils.substringBefore(identifier, ":") : null);
    }

    public String getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public boolean isAllConceptsValid() {
        return allConceptsValid;
    }

    public void setAllConceptsValid(boolean allConceptsValid) {
        this.allConceptsValid = allConceptsValid;
    }

}

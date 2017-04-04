package eionet.datadict.model;

import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import eionet.meta.dao.domain.DatasetRegStatus;


public abstract class DataElement implements SimpleAttributeOwner {
 
    public static enum ValueType {
        FIXED,
        QUANTITATIVE,
        VOCABULARY
    }
    
    public static enum Status {
        INCOMPLETE,
        CANDIDATE,
        RECORDED,
        QUALIFIED,
        RELEASED
    }
    
    @Id
    private Integer id;
    private String identifier;
    private String shortName;
    private Status status;
    private String workingUser;
    private boolean workingCopy;
    
    @ManyToOne
    private Namespace namespace;
    @OneToOne(mappedBy = "dataElement")
    private DatasetTableElement datasetTableElement;
    private Set<SimpleAttribute> simpleAttributes;
    @OneToMany(mappedBy = "owner")
    private Set<SimpleAttributeValues> simpleAttributesValues;

    public DataElement() {
        super();
    }
    
    public DataElement(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public DatasetTableElement getDatasetTableElement() {
        return datasetTableElement;
    }

    public void setDatasetTableElement(DatasetTableElement datasetTableElement) {
        this.datasetTableElement = datasetTableElement;
    }

    @Override
    public Set<SimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

    @Override
    public void setSimpleAttributes(Set<SimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

    @Override
    public Set<SimpleAttributeValues> getSimpleAttributesValues() {
        return simpleAttributesValues;
    }

    @Override
    public void setSimpleAttributesValues(Set<SimpleAttributeValues> simpleAttributeValues) {
        this.simpleAttributesValues = simpleAttributeValues;
    }
    
    public abstract ValueType getValueType();
    
    public abstract boolean supportsValueList();
    
    public abstract Iterable<? extends ValueListItem> getValueList();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof DataElement)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        DataElement other = (DataElement) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    


    
    public static enum DataElementType {
        
        CH1(2, "Data elements with fixed values (code list and elements from a vocabulary)"), // Data elements with fixed values (code list and elements from a vocabulary)
        CH2(1, "Data elements with quantitative values"), // Data elements with quantitative values
        CH3(2, "Data elements with fixed values (code list and elements from a vocabulary)"), // Data elements with fixed values (code list and elements from a vocabulary)
        ;
        
        private final int value;
        private final String label;
        
        private DataElementType(int value, String label) {
            this.value = value;
            this.label = label;
        }
        
        public static DataElementType getFromString(String string){
            for (DataElementType type : DataElementType.values()){
                if (type.getLabel().equals(string)){
                    return type;
                }
            }
            return null;
        }
        
        public int getValue() {
            return this.value;
        }
        
        public String getLabel() {
            return this.label;
        }
        
    }
    
    private DataElementType type;
    private DatasetRegStatus regStatus;
    private Integer version;
    private String user;
    private Integer date;
    private Namespace parentNS;
    private Namespace topNS;
    private Integer checkedOutCopyId;
    private Integer vocabularyId;
    private Boolean allConceptsLegal;
  

    public DataElementType getType() {
        return type;
    }

    public void setType(DataElementType type) {
        this.type = type;
    }

   

    public Boolean getWorkingCopy() {
        return workingCopy;
    }

    public void setWorkingCopy(Boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    public DatasetRegStatus getRegStatus() {
        return regStatus;
    }

    public void setRegStatus(DatasetRegStatus regStatus) {
        this.regStatus = regStatus;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }
    
    public Namespace getParentNS() {
        return parentNS;
    }

    public void setParentNS(Namespace parentNS) {
        this.parentNS = parentNS;
    }

    public Namespace getTopNS() {
        return topNS;
    }

    public void setTopNS(Namespace topNS) {
        this.topNS = topNS;
    }



    public Integer getCheckedOutCopyId() {
        return checkedOutCopyId;
    }

    public void setCheckedOutCopyId(Integer checkedOutCopyId) {
        this.checkedOutCopyId = checkedOutCopyId;
    }

    public Integer getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(Integer vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public Boolean getAllConceptsLegal() {
        return allConceptsLegal;
    }

    public void setAllConceptsLegal(Boolean allConceptsLegal) {
        this.allConceptsLegal = allConceptsLegal;
    }

}

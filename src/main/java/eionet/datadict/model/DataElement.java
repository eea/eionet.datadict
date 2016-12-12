package eionet.datadict.model;

import eionet.meta.dao.domain.DatasetRegStatus;

public class DataElement {
    
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
    private Integer id;
    private Namespace namespace;
    private String shortName;
    private String workingUser;
    private Boolean workingCopy;
    private DatasetRegStatus regStatus;
    private Integer version;
    private String user;
    private Integer date;
    private Namespace parentNS;
    private Namespace topNS;
    private String identifier;
    private Integer checkedOutCopyId;
    private Integer vocabularyId;
    private Boolean allConceptsLegal;
  

    public DataElementType getType() {
        return type;
    }

    public void setType(DataElementType type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getWorkingUser() {
        return workingUser;
    }

    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

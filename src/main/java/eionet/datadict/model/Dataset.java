package eionet.datadict.model;

import eionet.meta.dao.domain.DatasetRegStatus;

public class Dataset {

    private Integer id;
    private String shortName;
    private Integer version;
    private String visual;
    private String detailedVisual;
    private String workingUser;
    private Boolean workingCopy;
    private DatasetRegStatus regStatus;
    private Integer date;
    private String user;
    private Namespace correspondingNS;
    private String deleted;
    private String identifier;
    private Integer dispCreateLinks;
    private Integer checkedOutCopyId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public String getDetailedVisual() {
        return detailedVisual;
    }

    public void setDetailedVisual(String detailedVisual) {
        this.detailedVisual = detailedVisual;
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

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Namespace getCorrespondingNS() {
        return correspondingNS;
    }

    public void setCorrespondingNS(Namespace correspondingNS) {
        this.correspondingNS = correspondingNS;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getDispCreateLinks() {
        return dispCreateLinks;
    }

    public void setDispCreateLinks(Integer dispCreateLinks) {
        this.dispCreateLinks = dispCreateLinks;
    }

    public Integer getCheckedOutCopyId() {
        return checkedOutCopyId;
    }

    public void setCheckedOutCopyId(Integer checkedOutCopyId) {
        this.checkedOutCopyId = checkedOutCopyId;
    }
    
    
}

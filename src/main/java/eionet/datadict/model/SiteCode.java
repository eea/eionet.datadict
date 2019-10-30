package eionet.datadict.model;

import javax.persistence.Id;

/**
 * This class represents the T_SITE_CODE table
 *
 * @author nta@eworx.gr
 */
public class SiteCode {
    
    /* The primary key which is also a foreign key that references the id of VOCABULARY_CONCEPT table */
    @Id
    private String vocabularyConceptId;
    
    /* The site code */
    private String siteCode;
    
    /* The initial site name */
    private String initialSiteName;
    
    /* The site code nat */
    private String siteCodeNat;
    
    /* The site code's status */
    private String status;
    
    /* The ccIso2 */
    private String ccIso2;
    
    /* The parent iso */
    private String parentIso;
    
    /* The date the site code was created */
    private String dateCreated;
    
    /* The username of the user who created  the site code */
    private String userCreated;
    
    /* The date the site code was allocated */
    private String dateAllocated;
    
    /* The username of the user who allocated  the site code */
    private String userAllocated;
    
    /* The number of years that the site code has been deleted for.
        Used Object Integer instead of int because the value can be null*/
    private Integer yearsDeleted;
    
    /* The number of years that the site code has dissapeared for.
        Used Object Integer instead of int because the value can be null*/
    private Integer yearsDisappeared;
    
    /* The date the site code was deleted */
    private String dateDeleted;

    public SiteCode() {
    }
    
    public SiteCode(String vocabularyConceptId, String siteCode, String initialSiteName, String siteCodeNat,
            String status, String ccIso2, String parentIso, String dateCreated, String userCreated,
            String dateAllocated, String userAllocated, Integer yearsDeleted, Integer yearsDisappeared, String dateDeleted) {
        this.vocabularyConceptId = vocabularyConceptId;
        this.siteCode = siteCode;
        this.initialSiteName = initialSiteName;
        this.siteCodeNat = siteCodeNat;
        this.status = status;
        this.ccIso2 = ccIso2;
        this.parentIso = parentIso;
        this.dateCreated = dateCreated;
        this.userCreated = userCreated;
        this.dateAllocated = dateAllocated;
        this.userAllocated = userAllocated;
        this.yearsDeleted = yearsDeleted;
        this.yearsDisappeared = yearsDisappeared;
        this.dateDeleted = dateDeleted;
    }

    
    
    public String getVocabularyConceptId() {
        return vocabularyConceptId;
    }

    public void setVocabularyConceptId(String vocabularyConceptId) {
        this.vocabularyConceptId = vocabularyConceptId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getInitialSiteName() {
        return initialSiteName;
    }

    public void setInitialSiteName(String initialSiteName) {
        this.initialSiteName = initialSiteName;
    }

    public String getSiteCodeNat() {
        return siteCodeNat;
    }

    public void setSiteCodeNat(String siteCodeNat) {
        this.siteCodeNat = siteCodeNat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCcIso2() {
        return ccIso2;
    }

    public void setCcIso2(String ccIso2) {
        this.ccIso2 = ccIso2;
    }

    public String getParentIso() {
        return parentIso;
    }

    public void setParentIso(String parentIso) {
        this.parentIso = parentIso;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUserCreated() {
        return userCreated;
    }

    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }

    public String getDateAllocated() {
        return dateAllocated;
    }

    public void setDateAllocated(String dateAllocated) {
        this.dateAllocated = dateAllocated;
    }

    public String getUserAllocated() {
        return userAllocated;
    }

    public void setUserAllocated(String userAllocated) {
        this.userAllocated = userAllocated;
    }

    public Integer getYearsDeleted() {
        return yearsDeleted;
    }

    public void setYearsDeleted(Integer yearsDeleted) {
        this.yearsDeleted = yearsDeleted;
    }

    public Integer getYearsDisappeared() {
        return yearsDisappeared;
    }

    public void setYearsDisappeared(Integer yearsDisappeared) {
        this.yearsDisappeared = yearsDisappeared;
    }

    public String getDateDeleted() {
        return dateDeleted;
    }

    public void setDateDeleted(String dateDeleted) {
        this.dateDeleted = dateDeleted;
    }
    
    
}

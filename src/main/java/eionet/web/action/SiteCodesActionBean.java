/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.properties.SortOrderEnum;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;

import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.service.IEmailService;
import eionet.meta.service.ISiteCodeService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.SiteCodeServiceImpl;
import eionet.meta.service.data.AllocationResult;
import eionet.meta.service.data.CountryAllocations;
import eionet.meta.service.data.PagedRequest;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;

/**
 * Site codes controller.
 *
 * @author Juhan Voolaid
 * @author Jaanus Heinlaid
 */
@UrlBinding("/services/siteCodes/{$event}")
public class SiteCodesActionBean extends AbstractActionBean {

    /** Page. */
    private static final String VIEW_SITE_CODES_JSP = "/pages/services/siteCodes.jsp";

    /** Choice radio button values. */
    private static final String CHOICE_AMOUNT = "amount";
    private static final String CHOICE_LABEL = "label";

    /** Date-time format. */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Maximum amount site codes to allocate without name (1st method). */
    private static final int MAX_ALLOCATE_AMOUNT_WITHOUT_NAMES = Props.getIntProperty(PropsIF.SITE_CODES_MAX_ALLOCATE_WITHOUT_NAMES);

    /** Maximum amount site codes to allocate. */
    private static final int MAX_ALLOCATE_AMOUNT = Props.getIntProperty(PropsIF.SITE_CODES_MAX_ALLOCATE);

    /** Maximum amount site codes to allocate by ETC or EEA users. */
    private static final int MAX_ALLOCATE_AMOUNT_FOR_ETC = Props.getIntProperty(PropsIF.SITE_CODES_MAX_ALLOCATE_ETC_EEA);

    /** Maximum amount available site codes to reserve. */
    private static final int MAX_RESERVE_AMOUNT = Props.getIntProperty(PropsIF.SITE_CODES_MAX_RESERVE_AMOUNT);

    /** Site code service. */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** E-mail service. */
    @SpringBean
    private IEmailService emailService;

    /** Form fields. */
    private String country;
    /** Country inserted site names or other identifiers into textarea. The values are separated by new line. */
    private String labels;
    /** List of cleaned up (trimmed and nulls removed) site names inserted by country. */
    private String[] siteNames;
    private int amount;
    private String choice = CHOICE_AMOUNT;

    /** Site code countries. */
    private List<FixedValue> countries;

    /** Site code countries. */
    private List<FixedValue> userCountries;

    /** Site code search filter. */
    private SiteCodeFilter filter;

    /** Site codes search result. */
    private SiteCodeResult siteCodeResult;

    /** Concepts table page number. */
    private int page = 1;

    /** Sorting parameter. */
    private String sort;

    /** Sorting direction. */
    private String dir;

    /** Number of rows displayed on page. */
    private int pageSize = PagedRequest.DEFAULT_PAGE_SIZE;

    /** Amount of concepts to reserve. */
    private int reserveAmount;

    /** The starting identifier for reserving new site codes. */
    private int startIdentifier;

    /** The ending identifier for reserving new site codes. */
    private int endIdentifier;

    /** Site code folder id. */
    private int siteCodeFolderId;

    /** Number of available new unallocated site codes. */
    private int unallocatedSiteCodes;

    /** Allocations per user country. */
    private List<CountryAllocations> allocations;

    /** Filtering property for allocated user. */
    private String userAllocated;

    /** Filtering property for date of allocation. */
    private String dateAllocated;

    /** Make the enum value visible for jsp. */
    private String allocatedStatus = SiteCodeStatus.ALLOCATED.toString();

    /**
     * View site codes action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        unallocatedSiteCodes = siteCodeService.getFeeSiteCodeAmount();
        initFormData();
        return new ForwardResolution(VIEW_SITE_CODES_JSP);
    }

    /**
     * Search action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution search() throws ServiceException {
        initFormData();

        // detect if it is a export request, don't use paging in this case.
        String exportTypeStr =
                getContext().getRequest().getParameter((new ParamEncoder("siteCode").encodeParameterName(TableTagParameters.PARAMETER_EXPORTTYPE)));
        if (String.valueOf(MediaTypeEnum.CSV.getCode()).equals(exportTypeStr)
                || String.valueOf(MediaTypeEnum.EXCEL.getCode()).equals(exportTypeStr)) {
            filter.setUsePaging(false);
        }
        siteCodeResult = siteCodeService.searchSiteCodes(filter);
        unallocatedSiteCodes = siteCodeService.getFeeSiteCodeAmount();
        return new ForwardResolution(VIEW_SITE_CODES_JSP);
    }

    /**
     * Action that reserves new site codes (empty vocabulary concepts).
     *
     * @return
     * @throws ServiceException
     */
    public Resolution reserveNewSiteCodes() throws ServiceException {

        vocabularyService.reserveFreeSiteCodes(siteCodeFolderId, reserveAmount, startIdentifier, getUserName());

        String eventTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date());

        // Notify by email
        try {
            emailService.notifySiteCodeReservation(getUserName(), startIdentifier, reserveAmount);
        } catch (ServiceException e) {
            LOGGER.error("Failed to send notification", e);
            addWarningMessage("Failed to send notification: " + e.getMessage());
        }

        addSystemMessage(reserveAmount + " new site codes successfully created in range " + startIdentifier + " - "
                + endIdentifier + ". (" + eventTime + ")");
        return new RedirectResolution(SiteCodesActionBean.class);
    }

    /**
     * Allocate action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution allocate() throws ServiceException {
        AllocationResult allocationResult = null;
        if (CHOICE_LABEL.equals(choice)) {
            if (siteNames == null || siteNames.length == 0) {
                setCleanSiteNames();
            }
            allocationResult = siteCodeService.allocateSiteCodes(country, siteNames, getUserName());
        } else {
            allocationResult = siteCodeService.allocateSiteCodes(country, amount, getUserName());
        }

        userAllocated = getUserName();
        dateAllocated = new SimpleDateFormat(DATE_TIME_FORMAT).format(allocationResult.getAllocationTime());

        // Notify by email
        try {
            emailService.notifySiteCodeAllocation(country, allocationResult, isCreateRight());
        } catch (ServiceException e) {
            LOGGER.error("Failed to send notification", e);
            addWarningMessage("Failed to send notification: " + e.getMessage());
        }

        // Ping CR to reharvest site codes
        try {
            siteCodeFolderId = siteCodeService.getSiteCodeVocabularyFolderId();
            vocabularyService.pingCrToReharvestVocabulary(siteCodeFolderId);
        } catch (ServiceException e) {
            LOGGER.error("Failed to send ping request to Content Registry", e);
        }

        addSystemMessage(allocationResult.getAmount()
                + " site codes successfully allocated. For new allocated site codes, see the table below. (" + dateAllocated + ")");

        return new RedirectResolution(SiteCodesActionBean.class, "search").addParameter("userAllocated", userAllocated).addParameter("dateAllocated", dateAllocated).addParameter("filter.status", SiteCodeStatus.ALLOCATED).addParameter("filter.pageSize", allocationResult.getAmount());
    }

    /**
     * Validates allocation.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = { "allocate" })
    public void validateAllocate() throws ServiceException {

        if (!isAllocateRight()) {
            addGlobalValidationError("No privilege to allocate site codes");
        }

        if (StringUtils.isEmpty(choice)) {
            addGlobalValidationError("Number of site codes or site names must be specified");
        }

        if (CHOICE_AMOUNT.equals(choice)) {
            if (amount < 1) {
                addGlobalValidationError("Number of site codes must be positive number");
            }
        }
        // clean site names - remove empty values
        setCleanSiteNames();
        if (CHOICE_LABEL.equals(choice)) {
            if (StringUtils.isEmpty(labels) || siteNames == null || siteNames.length == 0) {
                addGlobalValidationError("The list of preliminary site names/identifiers must be valued");
            }
        }

        if (isUpdateRight()) {
            // validation for ETCs
            if ((CHOICE_AMOUNT.equals(choice) && amount > MAX_ALLOCATE_AMOUNT_FOR_ETC)
                    || (CHOICE_LABEL.equals(choice) && siteNames.length > MAX_ALLOCATE_AMOUNT_FOR_ETC)) {
                addGlobalValidationError("Number of allocated site codes cannot exceed " + MAX_ALLOCATE_AMOUNT_FOR_ETC
                        + " in one request.");
            }
        } else {
            // validation for countries
            int unusedCodesWithoutNames = getUnusedCodesForCountry(country, true);
            int unusedCodes = getUnusedCodesForCountry(country, false);
            if (CHOICE_AMOUNT.equals(choice)) {
                if ((amount + unusedCodesWithoutNames > MAX_ALLOCATE_AMOUNT_WITHOUT_NAMES)
                        || (amount + unusedCodes > MAX_ALLOCATE_AMOUNT)) {
                    addGlobalValidationError("Unable to allocate site codes! You already have or after the allocation"
                            + " you would have more than " + MAX_ALLOCATE_AMOUNT_WITHOUT_NAMES
                            + " site codes, yet unassigned, allocated. Try using the allocation method 2 by providing site names"
                            + " or IDs in the dedicated textarea. In this case you are able to allocate up to "
                            + MAX_ALLOCATE_AMOUNT
                            + " site codes. If you need more site codes allocated than this criteria allows, "
                            + "then please contact <a href=\"cdda.helpdesk@eionet.europa.eu\">cdda.helpdesk@eionet.europa.eu</a> "
                            + "with explanation of why you need more codes. "
                            + "If the explanation is valid the CDDA helpdesk will allocate the codes for your country.");
                }
            }
            if (CHOICE_LABEL.equals(choice)) {
                if (siteNames.length + unusedCodes > MAX_ALLOCATE_AMOUNT) {
                    addGlobalValidationError("Unable to allocate site codes! You already have or after the allocation"
                            + " you would have more than "
                            + MAX_ALLOCATE_AMOUNT
                            + " site codes, yet unassigned, allocated. If you need more site codes allocated than this criteria allows, "
                            + "then please contact <a href=\"cdda.helpdesk@eionet.europa.eu\">cdda.helpdesk@eionet.europa.eu</a> "
                            + "with explanation of why you need more codes. "
                            + "If the explanation is valid the CDDA helpdesk will allocate the codes for your country.");
                }
            }
        }

        if (isValidationErrors()) {
            initFormData();
            siteCodeResult = siteCodeService.searchSiteCodes(filter);
        }
    }

    /**
     * Validates reserve site codes.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = { "reserveNewSiteCodes" })
    public void validateReserveNewSiteCodes() throws ServiceException {
        if (!isCreateRight()) {
            addGlobalValidationError("No privilege to reserve new site codes");
        }

        if (startIdentifier < 1) {
            addGlobalValidationError("Range start must be a positive number");
        }

        if (endIdentifier < 1) {
            addGlobalValidationError("Range end must be a positive number");
        }

        if (endIdentifier < startIdentifier) {
            addGlobalValidationError("Range start should be lower than range end");
        }

        setReserveAmount(endIdentifier - startIdentifier + 1);

        if (reserveAmount > MAX_RESERVE_AMOUNT) {
            addGlobalValidationError("Amount cannot be bigger than " + MAX_RESERVE_AMOUNT);
        }

        List<Integer> unavailableIdentifiers =
                vocabularyService.checkAvailableIdentifiers(siteCodeFolderId, reserveAmount, startIdentifier);
        if (unavailableIdentifiers.size() > 0) {
            addGlobalValidationError("Identifers are unavailaible: " + StringUtils.join(unavailableIdentifiers, ", "));
        }

        if (isValidationErrors()) {
            initFormData();
            siteCodeResult = siteCodeService.searchSiteCodes(filter);
        }
    }

    /**
     * Initializes form beans.
     *
     * @throws ServiceException
     */
    private void initFormData() throws ServiceException {
        initFilter();
        initUserCountryData();
        countries = siteCodeService.getAllCountries();

        siteCodeFolderId = siteCodeService.getSiteCodeVocabularyFolderId();
        startIdentifier = vocabularyService.getNextIdentifierValue(siteCodeFolderId);
    }

    /**
     *
     * @throws ServiceException
     */
    private void initUserCountryData() throws ServiceException {
        if (getUser() != null) {
            userCountries = siteCodeService.getUserCountries(getUser());

            if (!isUpdateRight() && !isCreateRight()) {
                allocations = siteCodeService.getCountryAllocations(userCountries);
            }
        } else {
            userCountries = new ArrayList<FixedValue>();
        }
    }

    /**
     * Initializes search filter.
     */
    private void initFilter() {
        if (filter == null) {
            filter = new SiteCodeFilter();
        }
        filter.setUser(getUser());
        filter.setPageNumber(page);
        filter.setSortProperty(sort);
        if (StringUtils.isNotEmpty(dir)) {
            if (dir.equals("asc")) {
                filter.setSortOrder(SortOrderEnum.ASCENDING);
            } else {
                filter.setSortOrder(SortOrderEnum.DESCENDING);
            }
        }

        if (StringUtils.isNotEmpty(userAllocated)) {
            filter.setUserAllocated(userAllocated);
        }
        if (StringUtils.isNotEmpty(dateAllocated)) {
            try {
                filter.setDateAllocated(new SimpleDateFormat(DATE_TIME_FORMAT).parse(dateAllocated));
            } catch (ParseException e) {
                LOGGER.warn("Failed to parse date: " + dateAllocated, e);
            }
        }
    }

    /**
     * True, if user has site code update right.
     *
     * @return
     */
    public boolean isUpdateRight() {
        if (getUser() != null) {
            return getUser().hasPermission("/sitecodes", "u");
        }
        return false;
    }

    /**
     * True, if user has site code create right.
     *
     * @return
     */
    public boolean isCreateRight() {
        if (getUser() != null) {
            return getUser().hasPermission("/sitecodes", "c");
        }
        return false;
    }

    /**
     * True, if the user has allocation right.
     *
     * @return
     */
    public boolean isAllocateRight() throws ServiceException {
        return isUpdateRight() || isAllocateRightAsCountry();
    }

    /**
     * True, if the user has allocation right for country.
     *
     * @return
     */
    public boolean isAllocateRightAsCountry() throws ServiceException {
        if (getUser() != null) {
            List<String> countriesByRole = SecurityUtil.getUserCountriesFromRoles(getUser(), SiteCodeServiceImpl.getSiteCodeParentRoles());
            return countriesByRole != null && countriesByRole.size() > 0;
        }

        return false;
    }

    /**
     * Returns public filtering statuses.
     *
     * @return
     */
    public SiteCodeStatus[] getPublicStatuses() {
        SiteCodeStatus[] result = new SiteCodeStatus[2];
        result[0] = SiteCodeStatus.ALLOCATED;
        result[1] = SiteCodeStatus.ASSIGNED;
        return result;
    }

    /**
     *
     * @param countryCode
     * @return
     * @throws ServiceException
     */
    private int getUnusedCodesForCountry(String countryCode, boolean onlyWithoutNames) throws ServiceException {

        if (allocations == null) {
            initUserCountryData();
        }
        if (allocations != null) {
            for (CountryAllocations allocation : allocations) {
                if (countryCode.equals(allocation.getCountry().getValue())) {
                    if (onlyWithoutNames) {
                        return allocation.getUnusedCodesWithoutSiteNames();
                    } else {
                        return allocation.getUnusedCodes();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Remove empty labels from the list of inserted new line separated site names
     */
    private void setCleanSiteNames() {
        if (labels != null && labels.length() > 0) {
            String[] labelsArray = labels.split("\\n");
            List<String> siteNamesList = new ArrayList<String>();
            for (String label : labelsArray) {
                if (label.trim().length() > 0) {
                    siteNamesList.add(label.trim());
                }
            }
            siteNames = siteNamesList.toArray(new String[] {});
        }
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the labels
     */
    public String getLabels() {
        return labels;
    }

    /**
     * @param labels
     *            the labels to set
     */
    public void setLabels(String labels) {
        this.labels = labels;
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @param amount
     *            the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * @return the choice
     */
    public String getChoice() {
        return choice;
    }

    /**
     * @param choice
     *            the choice to set
     */
    public void setChoice(String choice) {
        this.choice = choice;
    }

    /**
     * @return the countries
     */
    public List<FixedValue> getCountries() {
        return countries;
    }

    /**
     * @return the filter
     */
    public SiteCodeFilter getFilter() {
        return filter;
    }

    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter(SiteCodeFilter filter) {
        this.filter = filter;
    }

    /**
     * @return the userCountries
     */
    public List<FixedValue> getUserCountries() {
        return userCountries;
    }

    /**
     * @return the siteCodeResult
     */
    public SiteCodeResult getSiteCodeResult() {
        return siteCodeResult;
    }

    /**
     * @param siteCodeResult
     *            the siteCodeResult to set
     */
    public void setSiteCodeResult(SiteCodeResult siteCodeResult) {
        this.siteCodeResult = siteCodeResult;
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     *            the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the reserveAmount
     */
    public int getReserveAmount() {
        return reserveAmount;
    }

    /**
     * @param reserveAmount
     *            the reserveAmount to set
     */
    public void setReserveAmount(int reserveAmount) {
        this.reserveAmount = reserveAmount;
    }

    /**
     * @return the startIdentifier
     */
    public int getStartIdentifier() {
        return startIdentifier;
    }

    /**
     * @param startIdentifier
     *            the startIdentifier to set
     */
    public void setStartIdentifier(int startIdentifier) {
        this.startIdentifier = startIdentifier;
    }

    /**
     * @return the siteCodeFolderId
     */
    public int getSiteCodeFolderId() {
        return siteCodeFolderId;
    }

    /**
     * @param siteCodeFolderId
     *            the siteCodeFolderId to set
     */
    public void setSiteCodeFolderId(int siteCodeFolderId) {
        this.siteCodeFolderId = siteCodeFolderId;
    }

    /**
     * @return the unallocatedSiteCodes
     */
    public int getUnallocatedSiteCodes() {
        return unallocatedSiteCodes;
    }

    /**
     * @return the allocations
     */
    public List<CountryAllocations> getAllocations() {
        return allocations;
    }

    /**
     * @return the userAllocated
     */
    public String getUserAllocated() {
        return userAllocated;
    }

    /**
     * @param userAllocated
     *            the userAllocated to set
     */
    public void setUserAllocated(String userAllocated) {
        this.userAllocated = userAllocated;
    }

    /**
     * @return the dateAllocated
     */
    public String getDateAllocated() {
        return dateAllocated;
    }

    /**
     * @param dateAllocated
     *            the dateAllocated to set
     */
    public void setDateAllocated(String dateAllocated) {
        this.dateAllocated = dateAllocated;
    }

    /**
     * @return the endIdentifier
     */
    public int getEndIdentifier() {
        return endIdentifier;
    }

    /**
     * @param endIdentifier
     *            the endIdentifier to set
     */
    public void setEndIdentifier(int endIdentifier) {
        this.endIdentifier = endIdentifier;
    }

    /**
     * @return the allocated
     */
    public String getAllocatedStatus() {
        return allocatedStatus;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize
     *            the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return the sort
     */
    public String getSort() {
        return sort;
    }

    /**
     * @param sort
     *            the sort to set
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return the dir
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dir
     *            the dir to set
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /** Returns the maximum amount of codes allowed to allocate to one country. */
    public int getMaxAllocateAmount() {
        return MAX_ALLOCATE_AMOUNT;
    }

    /** Returns the maximum amount of codes allowed to allocate without site names to one country. */
    public int getMaxAllocateAmountWithoutNames() {
        return MAX_ALLOCATE_AMOUNT_WITHOUT_NAMES;
    }
}

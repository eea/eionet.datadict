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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.service.ISiteCodeService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

/**
 * Site codes controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/services/siteCodes")
public class SiteCodesActionBean extends AbstractActionBean {

    /** Page. */
    private static final String VIEW_SITE_CODES_JSP = "/pages/services/siteCodes.jsp";

    /** Choice radio button values. */
    private static final String CHOICE_AMOUNT = "amount";
    private static final String CHOICE_LABELS = "labels";

    /** Maximum amount site codes to allocate. */
    private static final int MAX_AMOUNT = 10;

    /** Site code service. */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /** Form fields. */
    private String country;
    private String labels;
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

    /**
     * View site codes action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        initFormData();
        siteCodeResult = siteCodeService.searchSiteCodes(filter);
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
        siteCodeResult = siteCodeService.searchSiteCodes(filter);
        return new ForwardResolution(VIEW_SITE_CODES_JSP);
    }

    /**
     * Allocate action.
     *
     * @return
     * @throws ServiceException
     */
    public Resolution allocate() throws ServiceException {
        LOGGER.debug("Result: ");
        LOGGER.debug(country);
        LOGGER.debug(labels);
        LOGGER.debug(amount);
        LOGGER.debug(choice);

        if ("label".equals(choice)) {
            siteCodeService.allocateSiteCodes(country, labels.split("\\n"), getUserName());
        } else {
            siteCodeService.allocateSiteCodes(country, amount, getUserName());
        }

        addSystemMessage("Site codes successfully allocated");
        return new RedirectResolution(SiteCodesActionBean.class);
    }

    /**
     * Validates allocation.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = { "allocate" })
    public void validateAllocate() throws ServiceException {

        if (!isAllocationRight()) {
            addGlobalValidationError("No privilege to allocate site codes");
        }

        if (StringUtils.isEmpty(choice)) {
            addGlobalValidationError("Number of site codes or site code names must be specified");
        }

        if (CHOICE_AMOUNT.equals(choice)) {
            if (amount < 1) {
                addGlobalValidationError("Number of site codes must be positive number");
            }
            if (amount > MAX_AMOUNT) {
                addGlobalValidationError("Number of site codes cannot exceed more than " + MAX_AMOUNT);
            }
        }
        if (CHOICE_LABELS.equals(choice)) {
            if (StringUtils.isEmpty(labels)) {
                addGlobalValidationError("Site code names must be valued");
            }
        }

        if (isValidationErrors()) {
            initFormData();
        }
    }

    /**
     * Initializes form beans.
     *
     * @throws ServiceException
     */
    private void initFormData() throws ServiceException {
        if (filter == null) {
            filter = new SiteCodeFilter();
        }
        filter.setUser(getUser());

        if (getUser() != null) {
            userCountries = siteCodeService.getUserCountries(getUser());
        } else {
            userCountries = new ArrayList<FixedValue>();
        }
        countries = siteCodeService.getAllCountries();
    }

    /**
     * True, if the user has allocation right.
     *
     * @return
     */
    public boolean isAllocationRight()  throws ServiceException{
        if (getUser() != null) {
            userCountries = siteCodeService.getUserCountries(getUser());
            return getUser().hasPermission("sitecode", "u") || (userCountries != null && userCountries.size() > 0);
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

}

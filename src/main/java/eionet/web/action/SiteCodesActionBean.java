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
import eionet.meta.service.ISiteCodeService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;

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

    /** Vocabulary service. */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** Site code service. */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /** Form fields. */
    private String country;
    private String labels;
    private int amount;
    private String choice;

    /** Site code countries. */
    private List<FixedValue> countries;

    /**
     * View site codes action.
     *
     * @return
     * @throws ServiceException
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        countries = siteCodeService.getAllCountries();
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

        addSystemMessage("Site codes successfully allocated");
        return new RedirectResolution(SiteCodesActionBean.class);
    }

    /**
     * Validates allocation.
     *
     * @throws ServiceException
     */
    @ValidationMethod(on = {"allocate"})
    public void validateAllocate() throws ServiceException {

        if (getUser() == null) {
            addGlobalValidationError("User must be logged in");
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
            countries = siteCodeService.getAllCountries();
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

}

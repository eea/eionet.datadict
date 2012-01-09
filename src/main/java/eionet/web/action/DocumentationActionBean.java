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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.doc.DocumentationService;
import eionet.doc.dto.DocPageDTO;

/**
 *
 * @author Risto Alt
 *
 */
@UrlBinding("/documentation/{pageId}/{event}")
public class DocumentationActionBean extends AbstractActionBean {

    /** */
    private String pageId;
    private String event;

    private DocPageDTO pageObject;

    /**
     *
     * @return Resolution
     * @throws DAOException - if query fails
     */
    @DefaultHandler
    public Resolution view() throws Exception {

        String forward = "/pages/documentation.jsp";
        pageObject = DocumentationService.getInstance().view(pageId, event);
        if (pageObject != null && pageObject.getFis() != null) {
            return new StreamingResolution(pageObject.getContentType(), pageObject.getFis());
        }

        return new ForwardResolution(forward);
    }

    /**
     * Edit page
     *
     * @return Resolution
     * @throws Exception
     */
    public Resolution editContent() throws Exception {

        if (isUserLoggedIn()) {
            if (isPostRequest()) {
                DocumentationService.getInstance().editContent(pageObject, false);
                addSystemMessage("Successfully saved!");
            }
        } else {
            addWarningMessage("You are not logged in!");
        }
        return new RedirectResolution("/documentation/" + pageObject.getPid() + "/edit");
    }

    /**
     * Adds content into documentation table
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution addContent() throws Exception {
        if (isUserLoggedIn()) {
            if (isPostRequest()) {
                DocumentationService.getInstance().addContent(pageObject, false);
            }
        } else {
            addWarningMessage("You are not logged in!");
        }
        return new RedirectResolution("/documentation/" + pageObject.getPid() + "/edit");
    }

    @ValidationMethod(on = {"addContent"})
    public void validatePageId(ValidationErrors errors) throws Exception {
        // Expects that first parameter is named "pageObject" in this actionBean class
        // Does two validations:
        // - If pageObject.overwrite = false, then checks if page ID already exists
        // - If no file is chosen, then Page ID is mandatory
        errors = DocumentationService.getInstance().getStripesValidationErrors(pageObject, errors);
        if (errors != null && errors.size() > 0) {
            getContext().setValidationErrors(errors);
        }
        event = "add";
    }

    /**
     * Deletes content
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution delete() throws Exception {

        if (isUserLoggedIn()) {
            // The page title is not mandatory. If it is not filled in, then it takes the value of the page_id.
            if (pageObject != null && pageObject.getDocIds() != null &&  pageObject.getDocIds().size() > 0) {
                DocumentationService.getInstance().delete(pageObject);
            } else {
                addWarningMessage("No objects selected!");
            }
        } else {
            addWarningMessage("You are not logged in!");
        }

        return new RedirectResolution("/documentation/contents");
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public DocPageDTO getPageObject() {
        return pageObject;
    }

    public void setPageObject(DocPageDTO pageObject) {
        this.pageObject = pageObject;
    }

}

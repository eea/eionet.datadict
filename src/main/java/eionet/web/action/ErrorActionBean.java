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

import eionet.meta.service.ServiceException;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.RedirectResolution;

/**
 * Error page action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/error.action")
public class ErrorActionBean extends AbstractActionBean {

    /** Error type key identifier. */
    public static final String ERROR_TYPE_KEY = "eionet.web.action.ErrorActionBean.ErrorType";

    /** Possible Error Types. */
    public static enum ErrorType {
        UNKNOWN,
        INTERNAL_SERVER_ERROR,
        NOT_AUTHENTICATED_401,
        FORBIDDEN_403,
        NOT_FOUND_404, 
        INVALID_INPUT
    };
    
    /** Error message. */
    private String message;
    /** Error type. */
    private ErrorType type = ErrorType.UNKNOWN;

    @DefaultHandler
    public Resolution showError() throws ServiceException {
        switch (this.type) {
            case INTERNAL_SERVER_ERROR:
                return this.createHttpCodeBasedErrorResolution(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            case NOT_AUTHENTICATED_401:
                return this.createHttpCodeBasedErrorResolution(HttpServletResponse.SC_UNAUTHORIZED);
            case FORBIDDEN_403:
                return this.createHttpCodeBasedErrorResolution(HttpServletResponse.SC_FORBIDDEN);
            // not found 404 errors are handled in DDExceptionHandler, this line probably wont be reached
            case NOT_FOUND_404:
                return this.createHttpCodeBasedErrorResolution(HttpServletResponse.SC_NOT_FOUND);
            case UNKNOWN:
            default:
                return new ForwardResolution("/pages/error.jsp");
        }
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorTypeMsg() {
        switch (this.type) {
            case INTERNAL_SERVER_ERROR:
                return "500 Server error";
            case NOT_AUTHENTICATED_401:
                return "401 Unauthorized";
            case FORBIDDEN_403:
                return "403 Forbidden";
            // not found 404 errors are handled in DDExceptionHandler, this line probably wont be reached
            case NOT_FOUND_404:
                return "404 Not Found";
            case INVALID_INPUT:
                return "An error has occurred due to invalid input";
            case UNKNOWN:
            default:
                return "An unexpected system error has occurred:";
        }
    }

    public void setType(ErrorType type) {
        if (type != null) {
            this.type = type;
        } else {
            this.type = ErrorType.UNKNOWN;
        }
    }
    
    private Resolution createHttpCodeBasedErrorResolution(int httpCode) {
        return new ForwardResolution("/pages/error.jsp");
    }
    
}

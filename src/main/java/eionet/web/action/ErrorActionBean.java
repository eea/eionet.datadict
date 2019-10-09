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
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import javax.servlet.http.HttpServletResponse;

/**
 * Error page action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/error.action")
public class ErrorActionBean extends AbstractActionBean {

    /**
     * Error type key identifier.
     */
    public static final String ERROR_TYPE_KEY = "eionet.web.action.ErrorActionBean.ErrorType";

    /**
     * A return error event for API calls.
     */
    public static final String RETURN_ERROR_EVENT = "returnError";

    /**
     * A show error event for showing error on UI.
     */
    public static final String SHOW_ERROR_EVENT = "showError";

    /**
     * Possible Error Types.
     */
    public static enum ErrorType {
        UNKNOWN,
        INTERNAL_SERVER_ERROR,
        NOT_AUTHENTICATED_401,
        FORBIDDEN_403,
        NOT_FOUND_404,
        INVALID_INPUT,
        CONFLICT
    }

    /**
     * Error message.
     */
    private String message;
    /**
     * Error type.
     */
    private ErrorType type = ErrorType.UNKNOWN;

    @DefaultHandler
    public Resolution showError() throws ServiceException {
        return createHttpCodeBasedErrorResolution(getHttpCodeForErrorType(this.type));
    }

    @HandlesEvent(RETURN_ERROR_EVENT)
    public Resolution returnError() throws ServiceException {
        return createHttpCodeBasedErrorResolution(getHttpCodeForErrorType(this.type), this.message);
    }

    /**
     * Converts internal error type to http error code.
     *
     * @param type internal error type.
     * @return http error code.
     */
    public static int getHttpCodeForErrorType(ErrorType type) {
        int httpCode;
        switch (type) {
            case NOT_AUTHENTICATED_401:
                httpCode = HttpServletResponse.SC_UNAUTHORIZED;
                break;
            case FORBIDDEN_403:
                httpCode = HttpServletResponse.SC_FORBIDDEN;
                break;
            case NOT_FOUND_404:
                httpCode = HttpServletResponse.SC_NOT_FOUND;
                break;
            case INVALID_INPUT:
                httpCode = HttpServletResponse.SC_NOT_ACCEPTABLE;
                break;
            case CONFLICT:
                httpCode = HttpServletResponse.SC_CONFLICT;
                break;
            case INTERNAL_SERVER_ERROR:
            case UNKNOWN:
            default:
                httpCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                break;
        }
        return httpCode;
    } // end of method getHttpCodeForErrorType

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
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
            case NOT_FOUND_404:
                return "404 Not Found";
            case INVALID_INPUT:
                return "An error has occurred due to invalid input";
            case CONFLICT:
                return "409 Conflict";
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

    /**
     * Redirects error to internal page. Can be used for error handling for user interactions.
     *
     * @param httpCode error code.
     * @return internal error handling page.
     */
    private Resolution createHttpCodeBasedErrorResolution(int httpCode) {
        super.getContext().getResponse().setStatus(httpCode);
        return new ForwardResolution("/pages/error.jsp");
    }

    /**
     * Redirects http error resolution. Can be used for API calls.
     *
     * @param httpCode error code.
     * @param message  error message.
     * @return http error resolution.
     */
    private Resolution createHttpCodeBasedErrorResolution(int httpCode, String message) {
        super.getContext().getResponse().setStatus(httpCode);
        return new ErrorResolution(httpCode, message);
    }
}

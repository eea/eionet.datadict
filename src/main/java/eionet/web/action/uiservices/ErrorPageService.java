package eionet.web.action.uiservices;

import eionet.web.action.ErrorActionBean;
import net.sourceforge.stripes.action.Resolution;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ErrorPageService {

    Resolution createErrorResolution(ErrorActionBean.ErrorType errorType, String message);
    Resolution createErrorResolution(ErrorActionBean.ErrorType errorType, String message, String event);
}

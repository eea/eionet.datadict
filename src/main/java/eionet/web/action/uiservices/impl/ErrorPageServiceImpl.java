package eionet.web.action.uiservices.impl;

import eionet.web.action.ErrorActionBean;
import eionet.web.action.uiservices.ErrorPageService;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class ErrorPageServiceImpl implements ErrorPageService {

    @Override
    public Resolution createErrorResolution(ErrorActionBean.ErrorType errorType, String message) {
        return new RedirectResolution(ErrorActionBean.class).addParameter("type", errorType).addParameter("message", message);
    }

    @Override
    public Resolution createErrorResolution(ErrorActionBean.ErrorType errorType, String message, String event) {
        return new RedirectResolution(ErrorActionBean.class, event).addParameter("type", errorType).addParameter("message", message);
    }
}

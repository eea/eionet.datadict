package eionet.web.action;

import eionet.datadict.services.data.CleanupService;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;

@UrlBinding("/cleanup")
public class CleanupActionBean extends AbstractActionBean {

    public static final String CLEANUP_PAGE = "/pages/cleanup.jsp";

    @SpringBean
    private CleanupService cleanupService;

    public CleanupService getCleanupService() {
        return cleanupService;
    }

    public void setCleanupService(CleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Before(on = {"view", "cleanup"})
    private Resolution checkPermissionsInterceptor() {
        if (!isUserLoggedIn()) {
            return createErrorResolution(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "You have to login to access the cleanup page");
        }
        if (!hasAuthorizationPermission()) {
            return createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, "You are not authorized to access the cleanup page");
        }
        return null;
    }

    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(CLEANUP_PAGE);
    }

    public Resolution cleanup() {
        Thread.currentThread().setName("CLEANUP");
        ActionMethodUtils.setLogParameters(getContext());
        int deletedBrokenDatasetToTableRelations = this.cleanupService.deleteBrokenDatasetToTableRelations();
        int deletedOrphanTables = this.cleanupService.deleteOrphanTables();
        int deletedBrokenTableToElementRelations = this.cleanupService.deleteBrokenTableToElementRelations();
        int deletedOrphanNonCommonDataElements = this.cleanupService.deleteOrphanNonCommonDataElements();
        int deletedOrphanNamespaces = this.cleanupService.deleteOrphanNamespaces();
        int deletedOrphanAcls = this.cleanupService.deleteOrphanAcls();

        String message = "Deleted " +
                deletedBrokenDatasetToTableRelations + " DST2TBL relations, " +
                deletedOrphanTables + " tables with no parent dataset, " + 
                deletedBrokenTableToElementRelations + " TBL2ELEM relations, " +
                deletedOrphanNonCommonDataElements + " non-common elements with no parent table, " +
                deletedOrphanNamespaces + " NAMESPACE entries and " +
                deletedOrphanAcls + " object ACLs.";
        addSystemMessage(message.toString());
        
        return new RedirectResolution(this.getClass());
    }

    private boolean hasAuthorizationPermission() {
        if (getUser() != null) {
            return getUser().hasPermission("/cleanup", "x");
        }
        return false;
    }

}

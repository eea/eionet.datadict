package eionet.web.action;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.web.action.fixedvalues.DataElementFixedValuesViewModelBuilder;
import eionet.web.action.fixedvalues.FixedValuesViewModel;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@UrlBinding("/fixedvalues/elem/{ownerId}/{$event}/{fixedValueId}")
public class DataElementFixedValuesActionBean extends AbstractActionBean {
    
    public static final String PAGE_FIXED_VALUES_VIEW;
    public static final String PAGE_FIXED_VALUE_VIEW;
    public static final String PAGE_FIXED_VALUES_EDIT;
    public static final String PAGE_FIXED_VALUE_EDIT;
    
    static {
        PAGE_FIXED_VALUES_VIEW = "/pages/fixedValues/fixed_values.jsp";
        PAGE_FIXED_VALUE_VIEW = "/pages/fixedValues/fixed_value.jsp";
        PAGE_FIXED_VALUES_EDIT = "/pages/fixedValues/edit_fixed_values.jsp";
        PAGE_FIXED_VALUE_EDIT = "/pages/fixedValues/edit_fixed_value.jsp";
    }

    @SpringBean
    private DataElementFixedValuesController controller;
    
    @SpringBean
    private DataElementFixedValuesViewModelBuilder viewModelBuilder;
    
    private String ownerId;
    private String fixedValueId;
    
    private FixedValuesViewModel viewModel;
    
    public DataElementFixedValuesController getController() {
        return controller;
    }

    public void setController(DataElementFixedValuesController controller) {
        this.controller = controller;
    }

    public DataElementFixedValuesViewModelBuilder getViewModelBuilder() {
        return viewModelBuilder;
    }

    public void setViewModelBuilder(DataElementFixedValuesViewModelBuilder viewModelBuilder) {
        this.viewModelBuilder = viewModelBuilder;
    }
    
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFixedValueId() {
        return fixedValueId;
    }

    public void setFixedValueId(String fixedValueId) {
        this.fixedValueId = fixedValueId;
    }

    public FixedValuesViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(FixedValuesViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    @DefaultHandler
    @HandlesEvent("view")
    public Resolution view() {
        if (this.isSingleValueRequest()) {
            return this.viewSingle();
        }
        
        return this.viewAll();
    }
    
    @HandlesEvent("edit")
    public Resolution edit() {
        if (this.isSingleValueRequest()) {
            return this.editSingle();
        }
        
        return this.editAll();
    }
    
    @HandlesEvent("add")
    public Resolution add() {
        return new ActionHandler<DataElement>(this) {

            @Override
            protected DataElement executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                return controller.getEditableOwnerDataElement(getContextProvider(), ownerId);
            }

            @Override
            protected Resolution onActionComplete(DataElement actionResult) {
                viewModel = viewModelBuilder.buildFromOwner(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUE_EDIT);
            }
            
        }.invoke();
    }
    
    @HandlesEvent("save")
    public Resolution save() {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                FixedValue fxv = viewModel.getFixedValue();
                controller.saveFixedValue(getContextProvider(), ownerId, fxv);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    @HandlesEvent("delete")
    public Resolution delete() {
        if (this.isSingleValueRequest()) {
            return this.deleteSingle();
        }
        
        return this.deleteAll();
    }
    
    private Resolution viewSingle() {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                return controller.getSingleValueModel(ownerId, fixedValueId);
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                viewModel = viewModelBuilder.buildFromSingleValueModel(actionResult, false);
        
                return new ForwardResolution(PAGE_FIXED_VALUE_VIEW);
            }
        }.invoke();
    }
    
    private Resolution viewAll() {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                return controller.getAllValuesModel(ownerId);
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                viewModel = viewModelBuilder.buildFromAllValuesModel(actionResult, false);
        
                return new ForwardResolution(PAGE_FIXED_VALUES_VIEW);
            }
            
        }.invoke();
    }
    
    private Resolution editSingle() {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                return controller.getEditableSingleValueModel(getContextProvider(), ownerId, fixedValueId);
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                viewModel = viewModelBuilder.buildFromSingleValueModel(actionResult, true);
                
                return new ForwardResolution(PAGE_FIXED_VALUE_EDIT);
            }
        }.invoke();
    }
    
    private Resolution editAll() {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                return controller.getEditableAllValuesModel(getContextProvider(), ownerId);
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                viewModel = viewModelBuilder.buildFromAllValuesModel(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUES_EDIT);
            }
        }.invoke();
    }
    
    private Resolution deleteSingle() {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                controller.deleteFixedValue(getContextProvider(), ownerId, fixedValueId);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
        }.invoke();
    }
    
    private Resolution deleteAll() {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, 
                            UserAuthorizationException, DuplicateResourceException, EmptyParameterException {
                controller.deleteFixedValues(getContextProvider(), ownerId);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
        }.invoke();
    }
    
    private Resolution onAnonymousUser() {
        String msg = "You have to login to access fixed values";
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, msg);
    }
    
    private Resolution onMalformedDataElementId() {
        String msg = String.format("Malformed data element id: %s", this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onMalformedFixedValueId() {
        String msg = String.format("Malformed fixed value id: %s", this.fixedValueId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onResourceNotFound(String message) {
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, message);
    }
    
    private Resolution onUnauthorizedUser() {
        String msg = "You are not authorized to edit this data element";
        return super.createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, msg);
    }
    
    private Resolution onEmptyValue() {
        String msg = "Cannot use an empty value as a fixed value";
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onConflict(String message) {
        return super.createErrorResolution(ErrorActionBean.ErrorType.CONFLICT, message);
    }
    
    private Resolution redirectToEditValuesPage() {
        return new RedirectResolution(this.getClass(), "edit").addParameter("ownerId", this.ownerId);
    }
    
    private boolean isSingleValueRequest() {
        return !StringUtils.isBlank(this.fixedValueId);
    }
    
    private static abstract class ActionHandler<T> {
        
        private final DataElementFixedValuesActionBean actionBean;
        
        public ActionHandler(DataElementFixedValuesActionBean actionBean) {
            this.actionBean = actionBean;
        }
        
        public final Resolution invoke() {
            int ownerId;
            
            try {
                ownerId = Integer.parseInt(this.actionBean.ownerId);
            }
            catch (NumberFormatException ex) {
                return this.actionBean.onMalformedDataElementId();
            }
            
            Integer fixedValueId = null;
            
            if (this.actionBean.isSingleValueRequest()) {
                try {
                    fixedValueId = Integer.parseInt(this.actionBean.fixedValueId);
                }
                catch (NumberFormatException ex) {
                    return this.actionBean.onMalformedFixedValueId();
                }
            }
            
            T actionResult;
            
            try {
                actionResult = this.executeAction(ownerId, fixedValueId);
            }
            catch (UserAuthenticationException ex) {
                return this.actionBean.onAnonymousUser();
            }
            catch (ResourceNotFoundException ex) {
                return this.actionBean.onResourceNotFound(ex.getMessage());
            }
            catch (UserAuthorizationException ex) {
                return this.actionBean.onUnauthorizedUser();
            }
            catch (EmptyParameterException ex) {
                return this.actionBean.onEmptyValue();
            }
            catch (ConflictException ex) {
                return this.actionBean.onConflict(ex.getMessage());
            }
            
            return this.onActionComplete(actionResult);
        }
        
        protected abstract T executeAction(int ownerId, Integer fixedValueId) 
                throws UserAuthenticationException, ResourceNotFoundException, ConflictException,
                       UserAuthorizationException, DuplicateResourceException, EmptyParameterException;
        
        protected abstract Resolution onActionComplete(T actionResult);
    }
}

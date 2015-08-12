package eionet.web.action;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
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
@UrlBinding("/fixedvalues/elem/{ownerId}/{$event}/{fixedValue}")
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
    private String fixedValue;
    
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

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
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
            protected DataElement executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                           DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
                return controller.getOwnerDataElement(getContextProvider(), ownerId, true);
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
            protected Void executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, DuplicateResourceException, 
                           NotAFixedValueOwnerException, EmptyValueException {
                FixedValue fxv = viewModel.getFixedValue();
                controller.saveFixedValue(getContextProvider(), ownerId, fixedValue, fxv);
                
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
            protected CompoundDataObject executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           NotAFixedValueOwnerException, DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
                return controller.getSingleValueModel(getContextProvider(), ownerId, fixedValue, false);
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
            protected CompoundDataObject executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                           DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
                return controller.getAllValuesModel(getContextProvider(), ownerId, false);
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
            protected CompoundDataObject executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           NotAFixedValueOwnerException, DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
                return controller.getSingleValueModel(getContextProvider(), ownerId, fixedValue, true);
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
            protected CompoundDataObject executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                           DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
                return controller.getAllValuesModel(getContextProvider(), ownerId, true);
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
            protected Void executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           NotAFixedValueOwnerException, DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
                controller.deleteFixedValue(getContextProvider(), ownerId, fixedValue);
                
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
            protected Void executeAction(int ownerId) 
                    throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                           DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
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
    
    private Resolution onOwnerDataElementNotFound(int ownerId) {
        String msg = "Cannot find element with id: " + ownerId;
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, msg);
    }
    
    private Resolution onOwnerDataElementNotEditable() {
        String msg = String.format("Data element with id '%s' is not a working copy", this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, msg);
    }
    
    private Resolution onUnauthorizedUser() {
        String msg = "You are not authorized to edit this data element";
        return super.createErrorResolution(ErrorActionBean.ErrorType.FORBIDDEN_403, msg);
    }
    
    private Resolution onFixedValueNotFound(String value) {
        String msg = String.format("Cannot find value '%s' for owner data element with id %s", value, this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, msg);
    }
    
    private Resolution onFixedValueAlreadyExists(String value) {
        String msg = String.format("Value '%s' already exists for owner data element with id %s", value, this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR, msg);
    }
    
    private Resolution onEmptyValue() {
        String msg = "Cannot use an empty value as a fixed value";
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onNotAFixedValueOwner() {
        String msg = String.format("Data element with id %s is not a fixed values owner", this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR, msg);
    }
    
    private Resolution redirectToEditValuesPage() {
        return new RedirectResolution(this.getClass(), "edit").addParameter("ownerId", this.ownerId);
    }
    
    private boolean isSingleValueRequest() {
        return !StringUtils.isBlank(this.fixedValue);
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
            
            T actionResult;
            
            try {
                actionResult = this.executeAction(ownerId);
            }
            catch (UserAuthenticationException ex) {
                return this.actionBean.onAnonymousUser();
            }
            catch (FixedValueOwnerNotFoundException ex) {
                return this.actionBean.onOwnerDataElementNotFound(ex.getOwnerId());
            }
            catch (DataElementFixedValuesController.FixedValueOwnerNotEditableException ex) {
                return this.actionBean.onOwnerDataElementNotEditable();
            }
            catch (UserAuthorizationException ex) {
                return this.actionBean.onUnauthorizedUser();
            }
            catch (FixedValueNotFoundException ex) {
                return this.actionBean.onFixedValueNotFound(ex.getFixedValue());
            }
            catch (DuplicateResourceException ex) {
                return this.actionBean.onFixedValueAlreadyExists(ex.getResourceId().toString());
            }
            catch (EmptyValueException ex) {
                return this.actionBean.onEmptyValue();
            }
            catch (NotAFixedValueOwnerException ex) {
                return this.actionBean.onNotAFixedValueOwner();
            }
            
            return this.onActionComplete(actionResult);
        }
        
        protected abstract T executeAction(int ownerId) 
                throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                       DataElementFixedValuesController.FixedValueOwnerNotEditableException, NotAFixedValueOwnerException,
                       UserAuthorizationException, DuplicateResourceException, EmptyValueException;
        
        protected abstract Resolution onActionComplete(T actionResult);
    }
}

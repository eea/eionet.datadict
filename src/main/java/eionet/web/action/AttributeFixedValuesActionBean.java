package eionet.web.action;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.web.action.fixedvalues.AttributeFixedValuesViewModelBuilder;
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
@UrlBinding("/fixedvalues/attr/{ownerId}/{$event}/{fixedValueId}")
public class AttributeFixedValuesActionBean extends AbstractActionBean {
    
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
    private AttributeFixedValuesController controller;
    
    @SpringBean
    private AttributeFixedValuesViewModelBuilder viewModelBuilder;
    
    private String ownerId;
    private String fixedValueId;
    
    private FixedValuesViewModel viewModel;

    public AttributeFixedValuesController getController() {
        return controller;
    }

    public void setController(AttributeFixedValuesController controller) {
        this.controller = controller;
    }

    public AttributeFixedValuesViewModelBuilder getViewModelBuilder() {
        return viewModelBuilder;
    }

    public void setViewModelBuilder(AttributeFixedValuesViewModelBuilder viewModelBuilder) {
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
    
    @HandlesEvent("delete")
    public Resolution delete() {
        if (this.isSingleValueRequest()) {
            return this.deleteSingle();
        }
        
        return this.deleteAll();
    }
    
    @HandlesEvent("add")
    public Resolution add() {
        return new ActionHandler<SimpleAttribute>(this) {
            
            @Override
            protected SimpleAttribute executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
                return controller.getEditableOwnerAttribute(getContextProvider(), ownerId);
            }
            
            @Override
            protected Resolution onActionComplete(SimpleAttribute actionResult) {
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
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
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
    
    private Resolution viewSingle() {
        return this.getSingleValueViewResolution(false, PAGE_FIXED_VALUE_VIEW);
    }
    
    private Resolution viewAll() {
        return this.getAllValuesViewResolution(false, PAGE_FIXED_VALUES_VIEW);
    }
    
    private Resolution editSingle() {
        return this.getSingleValueViewResolution(true, PAGE_FIXED_VALUE_EDIT);
    }
    
    private Resolution editAll() {
        return this.getAllValuesViewResolution(true, PAGE_FIXED_VALUES_EDIT);
    }
    
    private Resolution deleteSingle() {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
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
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
                controller.deleteFixedValues(getContextProvider(), ownerId);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    private Resolution getSingleValueViewResolution(final boolean isEditSource, final String forwardTargetPage) {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
                if (isEditSource) {
                    return controller.getEditableSingleValueModel(getContextProvider(), ownerId, fixedValueId);
                }
                else {
                    return controller.getSingleValueModel(ownerId, fixedValueId);
                }
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                viewModel = viewModelBuilder.buildFromSingleValueModel(actionResult, isEditSource);
        
                return new ForwardResolution(forwardTargetPage);
            }
            
        }.invoke();
    }
    
    private Resolution getAllValuesViewResolution(final boolean isEditSource, final String forwardTargetPage) {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(int ownerId, Integer fixedValueId) 
                    throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
                if (isEditSource) {
                    return controller.getEditableAllValuesModel(getContextProvider(), ownerId);
                }
                else {
                    return controller.getAllValuesModel(ownerId);
                }
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                viewModel = viewModelBuilder.buildFromAllValuesModel(actionResult, isEditSource);
        
                return new ForwardResolution(forwardTargetPage);
            }
            
        }.invoke();
    }
    
    private Resolution onAnonymousUser() {
        String msg = "You have to login to access fixed values";
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, msg);
    }
    
    private Resolution onMalformedAttributeId() {
        String msg = String.format("Malformed attribute id: %s", this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onMalformedFixedValueId() {
        String msg = String.format("Malformed fixed value id: %s", this.fixedValueId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onResourceNotFound(String message) {
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, message);
    }
    
    private Resolution onFixedValueAlreadyExists(String message) {
        return super.createErrorResolution(ErrorActionBean.ErrorType.CONFLICT, message);
    }
    
    private Resolution onEmptyValue() {
        String msg = "Cannot use an empty value as a fixed value";
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution onNotAFixedValueOwner() {
        String msg = String.format("Attribute with id %s is not a fixed values owner", this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.CONFLICT, msg);
    }
    
    private Resolution redirectToEditValuesPage() {
        return new RedirectResolution(this.getClass(), "edit").addParameter("ownerId", this.ownerId);
    }
    
    private boolean isSingleValueRequest() {
        return !StringUtils.isBlank(this.fixedValueId);
    }
    
    private static abstract class ActionHandler<T> {
        
        private final AttributeFixedValuesActionBean actionBean;
        
        public ActionHandler(AttributeFixedValuesActionBean actionBean) {
            this.actionBean = actionBean;
        }
        
        public final Resolution invoke() {
            int ownerId;
            
            try {
                ownerId = Integer.parseInt(this.actionBean.ownerId);
            }
            catch (NumberFormatException ex) {
                return this.actionBean.onMalformedAttributeId();
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
            catch (DuplicateResourceException ex) {
                return this.actionBean.onFixedValueAlreadyExists(ex.getMessage());
            }
            catch (EmptyParameterException ex) {
                return this.actionBean.onEmptyValue();
            }
            catch (ConflictException ex) {
                return this.actionBean.onNotAFixedValueOwner();
            }
            
            return this.onActionComplete(actionResult);
        }
        
        protected abstract T executeAction(int ownerId, Integer fixedValueId) 
                throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException;
        
        protected abstract Resolution onActionComplete(T actionResult);
    }
}

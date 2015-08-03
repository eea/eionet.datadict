package eionet.web.action;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.ServiceException;
import eionet.web.action.fixedvalues.FixedValueCategory;
import eionet.web.action.fixedvalues.FixedValueOwnerDetails;
import eionet.web.action.fixedvalues.FixedValuesViewModel;
import java.util.Collection;
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
@UrlBinding("/fixedvalues/attr/{ownerId}/{$event}/{fixedValue}")
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
    
    private String ownerId;
    private String fixedValue;
    
    private FixedValuesViewModel viewModel;

    public AttributeFixedValuesController getController() {
        return controller;
    }

    public void setController(AttributeFixedValuesController contoller) {
        this.controller = contoller;
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
    public Resolution view() throws ServiceException {
        if (this.isSingleValueRequest()) {
            return this.viewSingle();
        }
        
        return this.viewAll();
    }
    
    @HandlesEvent("edit")
    public Resolution edit() throws ServiceException {
        if (this.isSingleValueRequest()) {
            return this.editSingle();
        }
        
        return this.editAll();
    }
    
    @HandlesEvent("delete")
    public Resolution delete() throws ServiceException {
        if (this.isSingleValueRequest()) {
            return this.deleteSingle();
        }
        
        return this.deleteAll();
    }
    
    @HandlesEvent("add")
    public Resolution add() throws ServiceException {
        return new ActionHandler<Attribute>(this) {
            
            @Override
            protected Attribute executeAction() throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
                return controller.getOwnerAttribute(getContextProvider(), ownerId);
            }
            
            @Override
            protected Resolution onActionComplete(Attribute actionResult) {
                applyResultToViewModel(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUE_EDIT);
            }
        }.invoke();
    }
    
    @HandlesEvent("save")
    public Resolution save() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction() throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                    FixedValueNotFoundException, EmptyValueException, DuplicateResourceException, ServiceException {
                FixedValue fxv = viewModel.getFixedValue();
                
                if (fxv != null) {
                    fixedValue = fxv.getValue();
                }

                controller.saveFixedValue(getContextProvider(), ownerId, fxv);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    private Resolution viewSingle() throws ServiceException {
        return this.getSingleValueViewResolution(false, PAGE_FIXED_VALUE_VIEW);
    }
    
    private Resolution viewAll() throws ServiceException {
        return this.getAllValuesViewResolution(false, PAGE_FIXED_VALUES_VIEW);
    }
    
    private Resolution editSingle() throws ServiceException {
        return this.getSingleValueViewResolution(true, PAGE_FIXED_VALUE_EDIT);
    }
    
    private Resolution editAll() throws ServiceException {
        return this.getAllValuesViewResolution(true, PAGE_FIXED_VALUES_EDIT);
    }
    
    private Resolution deleteSingle() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction() throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                    FixedValueNotFoundException, ServiceException {
                controller.deleteFixedValue(getContextProvider(), ownerId, fixedValue);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    private Resolution deleteAll() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction() throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                    FixedValueNotFoundException, ServiceException {
                controller.deleteFixedValues(getContextProvider(), ownerId);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(Void actionResult) {
                return redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    private Resolution getSingleValueViewResolution(final boolean isEditSource, final String forwardTargetPage) throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction() throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                    FixedValueNotFoundException, ServiceException {
                return controller.getSingleValueModel(getContextProvider(), ownerId, fixedValue);
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                applyResultToViewModel(actionResult, isEditSource);
        
                return new ForwardResolution(forwardTargetPage);
            }
            
        }.invoke();
    }
    
    private Resolution getAllValuesViewResolution(final boolean isEditSource, final String forwardTargetPage) throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction() throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
                return controller.getAllValuesModel(getContextProvider(), ownerId);
            }

            @Override
            protected Resolution onActionComplete(CompoundDataObject actionResult) {
                applyResultToViewModel(actionResult, isEditSource);
        
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
    
    private Resolution onOwnerAttributeNotFound() {
        String msg = "Cannot find attribute with id: " + this.ownerId;
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, msg);
    }
    
    private Resolution onFixedValueNotFound() {
        String msg = String.format("Cannot find value '%s' for owner attribute with id %s", this.fixedValue, this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, msg);
    }
    
    private Resolution onFixedValueAlreadyExists() {
        String msg = String.format("Value '%s' already exists for owner attribute with id %s", this.fixedValue, this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR, msg);
    }
    
    private Resolution onEmptyValue() {
        String msg = "Cannot use an empty value as a fixed value";
        return super.createErrorResolution(ErrorActionBean.ErrorType.INVALID_INPUT, msg);
    }
    
    private Resolution redirectToEditValuesPage() {
        return new RedirectResolution(this.getClass(), "edit").addParameter("ownerId", this.ownerId);
    }
    
    private boolean isSingleValueRequest() {
        return !StringUtils.isBlank(this.fixedValue);
    }
    
    private void applyResultToViewModel(CompoundDataObject result, boolean hasEditSource) {
        this.initViewModel();
        this.applyOwnerToViewModel(result, hasEditSource);
        this.applyFixedValuesToViewModel(result);
    }
    
    private void applyResultToViewModel(Attribute result, boolean hasEditSource) {
        this.initViewModel();
        this.applyOwnerToViewModel(result, hasEditSource);
    }
    
    private void initViewModel() {
        this.setViewModel(new FixedValuesViewModel());
        this.viewModel.setActionBeanName(this.getClass().getName());
        this.viewModel.setDefaultValueRequired(true);
    }
    
    private void applyOwnerToViewModel(CompoundDataObject result, boolean hasEditSource) {
        Attribute ownerAttribute = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        this.applyOwnerToViewModel(ownerAttribute, hasEditSource);
    }
    
    private void applyOwnerToViewModel(Attribute ownerAttribute, boolean hasEditSource) {
        FixedValueOwnerDetails owner = new FixedValueOwnerDetails();
        owner.setId(ownerAttribute.getId());
        owner.setCaption(ownerAttribute.getShortName());
        owner.setUri(this.composeOwnerUri(ownerAttribute, hasEditSource));
        owner.setEntityName("attribute");
        this.viewModel.setOwner(owner);
        this.viewModel.setFixedValueCategory(FixedValueCategory.ALLOWABLE);
    }
    
    private String composeOwnerUri(Attribute ownerAttribute, boolean hasEditSource) {
        String uri = String.format("/delem_attribute.jsp?type=SIMPLE&attr_id=%d", ownerAttribute.getId());
        
        if (hasEditSource) {
            uri += "&mode=edit";
        }
        
        return uri;
    }
    
    private void applyFixedValuesToViewModel(CompoundDataObject result) {
        if (result.containsKey(AttributeFixedValuesController.PROPERTY_FIXED_VALUES)) {
            Collection<FixedValue> fixedValues = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUES);
            viewModel.getFixedValues().addAll(fixedValues);
        }
        else {
            FixedValue fxv = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUE);
            viewModel.setFixedValue(fxv);
        }
    }
    
    private static abstract class ActionHandler<T> {
        
        private final AttributeFixedValuesActionBean actionBean;
        
        public ActionHandler(AttributeFixedValuesActionBean actionBean) {
            this.actionBean = actionBean;
        }
        
        public final Resolution invoke() throws ServiceException {
            T actionResult;
            
            try {
                actionResult = this.executeAction();
            }
            catch (UserAuthenticationException ex) {
                return this.actionBean.onAnonymousUser();
            }
            catch (MalformedIdentifierException ex) {
                return this.actionBean.onMalformedAttributeId();
            }
            catch (FixedValueOwnerNotFoundException ex) {
                return this.actionBean.onOwnerAttributeNotFound();
            }
            catch (FixedValueNotFoundException ex) {
                return this.actionBean.onFixedValueNotFound();
            }
            catch (DuplicateResourceException ex) {
                return this.actionBean.onFixedValueAlreadyExists();
            }
            catch (EmptyValueException ex) {
                return this.actionBean.onEmptyValue();
            }
            
            return this.onActionComplete(actionResult);
        }
        
        protected abstract T executeAction() 
                throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                       EmptyValueException, DuplicateResourceException, ServiceException;
        
        protected abstract Resolution onActionComplete(T actionResult);
    }
}

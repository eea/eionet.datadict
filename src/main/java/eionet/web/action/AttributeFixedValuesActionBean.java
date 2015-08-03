package eionet.web.action;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.controllers.CompoundDataObject;
import eionet.meta.controllers.ControllerFactory;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.IDataService;
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
    private IDataService dataService;
    
    @SpringBean
    private ControllerFactory controllerFactory;
    
    private String ownerId;
    private String fixedValue;
    
    private FixedValuesViewModel viewModel;

    public IDataService getDataService() {
        return dataService;
    }

    public void setDataService(IDataService dataService) {
        this.dataService = dataService;
    }

    public ControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    public void setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;
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
            protected Attribute executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
                return controller.getOwnerAttribute(ownerId);
            }
            
            @Override
            protected Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, Attribute actionResult) {
                actionBean.applyResultToViewModel(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUE_EDIT);
            }
        }.invoke();
    }
    
    @HandlesEvent("save")
    public Resolution save() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           EmptyValueException, DuplicateResourceException, ServiceException {
                FixedValue fxv = actionBean.viewModel.getFixedValue();
                
                if (fxv != null) {
                    actionBean.fixedValue = fxv.getValue();
                }

                controller.saveFixedValue(actionBean.ownerId, fxv);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, Void actionResult) {
                return actionBean.redirectToEditValuesPage();
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
            protected Void executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException {
                controller.deleteFixedValue(actionBean.ownerId, actionBean.fixedValue);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, Void actionResult) {
                return actionBean.redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    private Resolution deleteAll() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException {
                controller.deleteFixedValues(actionBean.ownerId);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, Void actionResult) {
                return actionBean.redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    private Resolution getSingleValueViewResolution(final boolean isEditSource, final String forwardTargetPage) throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException {
                return controller.getSingleValueModel(actionBean.ownerId, actionBean.fixedValue);
            }

            @Override
            protected Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, CompoundDataObject actionResult) {
                actionBean.applyResultToViewModel(actionResult, isEditSource);
        
                return new ForwardResolution(forwardTargetPage);
            }
            
        }.invoke();
    }
    
    private Resolution getAllValuesViewResolution(final boolean isEditSource, final String forwardTargetPage) throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
                return controller.getAllValuesModel(actionBean.ownerId);
            }

            @Override
            protected Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, CompoundDataObject actionResult) {
                actionBean.applyResultToViewModel(actionResult, isEditSource);
        
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
    
    private AttributeFixedValuesController createController() {
        return this.controllerFactory.createAttributeFixedValuesController(this.getContextProvider(), this.dataService);
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
            AttributeFixedValuesController controller = this.actionBean.createController();
            T actionResult;
            
            try {
                actionResult = this.executeAction(this.actionBean, controller);
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
            
            return this.onActionComplete(this.actionBean, actionResult);
        }
        
        protected abstract T executeAction(AttributeFixedValuesActionBean actionBean, AttributeFixedValuesController controller) 
                throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                       EmptyValueException, DuplicateResourceException, ServiceException;
        
        protected abstract Resolution onActionComplete(AttributeFixedValuesActionBean actionBean, T actionResult);
    }
}

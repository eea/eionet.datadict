package eionet.web.action;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.controllers.CompoundDataObject;
import eionet.meta.controllers.ControllerFactory;
import eionet.meta.controllers.ElementFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.DataElement;
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
@UrlBinding("/fixedvalues/elem/{ownerId}/{$event}/{fixedValue}")
public class ElementFixedValuesActionBean extends AbstractActionBean {
    
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
    
    @HandlesEvent("add")
    public Resolution add() throws ServiceException {
        return new ActionHandler<DataElement>(this) {

            @Override
            protected DataElement executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                return controller.getOwnerDataElement(actionBean.ownerId, true);
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, DataElement actionResult) {
                actionBean.applyResultToViewModel(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUE_EDIT);
            }
            
        }.invoke();
    }
    
    @HandlesEvent("save")
    public Resolution save() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, DuplicateResourceException, 
                           EmptyValueException, ServiceException {
                FixedValue fxv = actionBean.viewModel.getFixedValue();
                
                if (fxv != null) {
                    actionBean.fixedValue = fxv.getValue();
                }
                
                controller.saveFixedValue(actionBean.ownerId, fxv);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, Void actionResult) {
                return actionBean.redirectToEditValuesPage();
            }
            
        }.invoke();
    }
    
    @HandlesEvent("delete")
    public Resolution delete() throws ServiceException {
        if (this.isSingleValueRequest()) {
            return this.deleteSingle();
        }
        
        return this.deleteAll();
    }
    
    private Resolution viewSingle() throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                return controller.getSingleValueModel(actionBean.ownerId, actionBean.fixedValue, false);
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, CompoundDataObject actionResult) {
                actionBean.applyResultToViewModel(actionResult, false);
        
                return new ForwardResolution(PAGE_FIXED_VALUE_VIEW);
            }
        }.invoke();
    }
    
    private Resolution viewAll() throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                return controller.getAllValuesModel(actionBean.ownerId, false);
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, CompoundDataObject actionResult) {
                actionBean.applyResultToViewModel(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUES_VIEW);
            }
            
        }.invoke();
    }
    
    private Resolution editSingle() throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                return controller.getSingleValueModel(actionBean.ownerId, actionBean.fixedValue, true);
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, CompoundDataObject actionResult) {
                actionBean.applyResultToViewModel(actionResult, true);
                
                return new ForwardResolution(PAGE_FIXED_VALUE_EDIT);
            }
        }.invoke();
    }
    
    private Resolution editAll() throws ServiceException {
        return new ActionHandler<CompoundDataObject>(this) {

            @Override
            protected CompoundDataObject executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                return controller.getAllValuesModel(actionBean.ownerId, true);
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, CompoundDataObject actionResult) {
                actionBean.applyResultToViewModel(actionResult, true);
        
                return new ForwardResolution(PAGE_FIXED_VALUES_EDIT);
            }
        }.invoke();
    }
    
    private Resolution deleteSingle() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                controller.deleteFixedValue(actionBean.ownerId, actionBean.fixedValue);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, Void actionResult) {
                return actionBean.redirectToEditValuesPage();
            }
        }.invoke();
    }
    
    private Resolution deleteAll() throws ServiceException {
        return new ActionHandler<Void>(this) {

            @Override
            protected Void executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                    throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                           ElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
                controller.deleteFixedValues(actionBean.ownerId);
                
                return null;
            }

            @Override
            protected Resolution onActionComplete(ElementFixedValuesActionBean actionBean, Void actionResult) {
                return actionBean.redirectToEditValuesPage();
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
    
    private Resolution onOwnerDataElementNotFound() {
        String msg = "Cannot find element with id: " + this.ownerId;
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
    
    private Resolution onFixedValueNotFound() {
        String msg = String.format("Cannot find value '%s' for owner data element with id %s", this.fixedValue, this.ownerId);
        return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, msg);
    }
    
    private Resolution onFixedValueAlreadyExists() {
        String msg = String.format("Value '%s' already exists for owner data element with id %s", this.fixedValue, this.ownerId);
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
    
    private ElementFixedValuesController createController() {
        return this.controllerFactory.createElementFixedValuesController(this.getContextProvider(), this.dataService);
    }
    
    private void applyResultToViewModel(CompoundDataObject result, boolean hasEditSource) {
        this.initViewModel();
        this.applyOwnerToViewModel(result, hasEditSource);
        this.applyFixedValuesToViewModel(result);
    }
    
    private void applyResultToViewModel(DataElement result, boolean hasEditSource) {
        this.initViewModel();
        this.applyOwnerToViewModel(result, hasEditSource);
    }
    
    private void initViewModel() {
        this.setViewModel(new FixedValuesViewModel());
        this.viewModel.setActionBeanName(this.getClass().getName());
    }
    
    private void applyOwnerToViewModel(CompoundDataObject result, boolean hasEditSource) {
        DataElement ownerElement = result.get(ElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        this.applyOwnerToViewModel(ownerElement, hasEditSource);
    }
    
    private void applyOwnerToViewModel(DataElement ownerElement, boolean hasEditSource) {
        FixedValueOwnerDetails owner = new FixedValueOwnerDetails();
        owner.setId(ownerElement.getId());
        owner.setCaption(ownerElement.getShortName());
        owner.setUri(this.composeOwnerUri(ownerElement, hasEditSource));
        owner.setEntityName("element");
        this.viewModel.setOwner(owner);
        this.viewModel.setFixedValueCategory("CH2".equals(ownerElement.getType()) ? FixedValueCategory.SUGGESTED : FixedValueCategory.ALLOWABLE);
    }
    
    private String composeOwnerUri(DataElement ownerElement, boolean hasEditSource) {
        String uri = String.format("/dataelements/%d", ownerElement.getId());
        
        if (hasEditSource) {
            uri += "/edit";
        }
        
        return uri;
    }
    
    private void applyFixedValuesToViewModel(CompoundDataObject result) {
        if (result.containsKey(ElementFixedValuesController.PROPERTY_FIXED_VALUES)) {
            Collection<FixedValue> fixedValues = result.get(ElementFixedValuesController.PROPERTY_FIXED_VALUES);
            viewModel.getFixedValues().addAll(fixedValues);
        }
        else {
            FixedValue fxv = result.get(ElementFixedValuesController.PROPERTY_FIXED_VALUE);
            viewModel.setFixedValue(fxv);
        }
    }
    
    private static abstract class ActionHandler<T> {
        
        private final ElementFixedValuesActionBean actionBean;
        
        public ActionHandler(ElementFixedValuesActionBean actionBean) {
            this.actionBean = actionBean;
        }
        
        public final Resolution invoke() throws ServiceException {
            ElementFixedValuesController controller = this.actionBean.createController();
            T actionResult;
            
            try {
                actionResult = this.executeAction(this.actionBean, controller);
            }
            catch (UserAuthenticationException ex) {
                return this.actionBean.onAnonymousUser();
            } 
            catch (MalformedIdentifierException ex) {
                return this.actionBean.onMalformedDataElementId();
            } 
            catch (FixedValueOwnerNotFoundException ex) {
                return this.actionBean.onOwnerDataElementNotFound();
            }
            catch (ElementFixedValuesController.FixedValueOwnerNotEditableException ex) {
                return this.actionBean.onOwnerDataElementNotEditable();
            }
            catch (UserAuthorizationException ex) {
                return this.actionBean.onUnauthorizedUser();
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
        
        protected abstract T executeAction(ElementFixedValuesActionBean actionBean, ElementFixedValuesController controller) 
                throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                       FixedValueNotFoundException, ElementFixedValuesController.FixedValueOwnerNotEditableException, 
                       UserAuthorizationException, DuplicateResourceException, EmptyValueException, ServiceException;
        
        protected abstract Resolution onActionComplete(ElementFixedValuesActionBean actionBean, T actionResult);
    }
}

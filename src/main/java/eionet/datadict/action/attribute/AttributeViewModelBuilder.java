package eionet.datadict.action.attribute;

import eionet.datadict.action.AttributeActionBean;
import eionet.datadict.controllers.AttributeController;
import eionet.datadict.model.AttributeDefinition;
import eionet.util.CompoundDataObject;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Aliki Kopaneli
 */
@Component
public class AttributeViewModelBuilder {
    
    /**
     * Create a viewModel for the view page of attributes
     * 
     * @param model
     * @return 
     */
    public AttributeViewModel buildForView(CompoundDataObject model) {
        AttributeViewModel  viewModel = new AttributeViewModel();
        viewModel.setAttributeDefinition((AttributeDefinition)model.get(AttributeController.ATTRIBUTE_DEFINITION));
        viewModel.setDisplayForTypes((List)model.get(AttributeController.DISPLAY_FOR_TYPES));
        viewModel.setFixedValues((List)model.get(AttributeController.FIXED_VALUES));
        return viewModel;
    }
    
    /**
     * Create a viewModel for the edit page of attributes
     * 
     * @param model
     * @return 
     */
    public AttributeViewModel buildForEdit(CompoundDataObject model) {
        AttributeViewModel viewModel = new AttributeViewModel();
        viewModel.setSubmitActionBeanName(AttributeActionBean.class.getName());
        viewModel.setAttributeDefinition((AttributeDefinition)model.get(AttributeController.ATTRIBUTE_DEFINITION));
        viewModel.setNamespaces((List)model.get(AttributeController.NAMESPACES));
        viewModel.setDisplayOrder((String)model.get(AttributeController.DISPLAY_ORDER));
        viewModel.setDisplayForTypes((List)model.get(AttributeController.DISPLAY_FOR_TYPES));
        viewModel.setAllRdfNamespaces((List)model.get(AttributeController.RDF_NAMESPACES));
        if (viewModel.getAttributeDefinition().getRdfNamespace() != null) {
            viewModel.setRdfNamespaceId(String.valueOf(viewModel.getAttributeDefinition().getRdfNamespace().getId()));
        }
        return viewModel;
    }
    
    /**
     * Create a viewModel for the add page of attributes
     * @param model
     * @return 
     */
    public AttributeViewModel buildForAdd(CompoundDataObject model){
        AttributeViewModel viewModel = new AttributeViewModel();
        viewModel.setSubmitActionBeanName(AttributeActionBean.class.getName());
        viewModel.setAttributeDefinition(new AttributeDefinition());
        viewModel.setAllRdfNamespaces((List)model.get(AttributeController.RDF_NAMESPACES));
        return viewModel;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.action.attribute;

import eionet.datadict.action.AttributeActionBean;
import eionet.datadict.controllers.AttributeController;
import eionet.datadict.model.AttributeDefinition;
import eionet.util.CompoundDataObject;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author eworx-alk
 */
@Component
public class AttributeViewModelBuilder {
    
    public AttributeViewModel buildForView(CompoundDataObject model) {
        AttributeViewModel  viewModel = new AttributeViewModel();
        viewModel.setAttributeDefinition((AttributeDefinition)model.get(AttributeController.ATTRIBUTE_DEFINITION));
        viewModel.setDisplayForTypes((List)model.get(AttributeController.DISPLAY_FOR_TYPES));
        viewModel.setFixedValues((List)model.get(AttributeController.FIXED_VALUES));
        return viewModel;
    }
    
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
    
    public AttributeViewModel buildForAdd(CompoundDataObject model){
        AttributeViewModel viewModel = new AttributeViewModel();
        viewModel.setSubmitActionBeanName(AttributeActionBean.class.getName());
        viewModel.setAttributeDefinition(new AttributeDefinition());
        viewModel.setAllRdfNamespaces((List)model.get(AttributeController.RDF_NAMESPACES));
        return viewModel;
    }
    
    
}

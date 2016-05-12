/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.action.attribute;

import eionet.datadict.action.AttributeActionBean;
import eionet.datadict.model.AttributeDefinition;
import eionet.util.CompoundDataObject;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author eworx-alk
 */
@Component
public class AttributeViewModelBuilder {
    
    public AttributeViewModel buildForView(CompoundDataObject model) {
        AttributeViewModel  viewModel = new AttributeViewModel();
        viewModel.setAttributeDefinition((AttributeDefinition)model.get("attributeDefinition"));
        viewModel.setDisplayForTypes((Map)model.get("displayForTypes"));
        viewModel.setFixedValues((List)model.get("fixedValues"));
        return viewModel;
    }
    
    public AttributeViewModel buildForEdit(CompoundDataObject model) {
        AttributeViewModel viewModel = new AttributeViewModel();
        viewModel.setSubmitActionBeanName((AttributeActionBean.class).getName());
        viewModel.setAttributeDefinition((AttributeDefinition)model.get("attributeDefinition"));
        return viewModel;
    }
}

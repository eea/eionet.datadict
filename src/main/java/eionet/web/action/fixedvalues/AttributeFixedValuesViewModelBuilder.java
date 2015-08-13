package eionet.web.action.fixedvalues;

import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.util.CompoundDataObject;
import eionet.web.action.AttributeFixedValuesActionBean;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Component
public class AttributeFixedValuesViewModelBuilder {

    public FixedValuesViewModel buildFromOwner(SimpleAttribute ownerElement, boolean editView) {
        FixedValuesViewModel viewModel = this.createViewModel();
        this.attachOwnerDetails(ownerElement, editView, viewModel);
        
        return viewModel;
    }
    
    public FixedValuesViewModel buildFromSingleValueModel(CompoundDataObject model, boolean editView) {
        FixedValuesViewModel viewModel = this.createViewModel();
        this.attachOwnerDetails(model, editView, viewModel);
        this.attachFixedValue(model, viewModel);
        
        return viewModel;
    }
    
    public FixedValuesViewModel buildFromAllValuesModel(CompoundDataObject model, boolean editView) {
        FixedValuesViewModel viewModel = this.createViewModel();
        this.attachOwnerDetails(model, editView, viewModel);
        this.attachFixedValues(model, viewModel);
        
        return viewModel;
    }
    
    private FixedValuesViewModel createViewModel() {
        FixedValuesViewModel viewModel = new FixedValuesViewModel();
        viewModel.setActionBeanName(AttributeFixedValuesActionBean.class.getName());
        viewModel.setDefaultValueRequired(true);
        
        return viewModel;
    }
    
    private void attachOwnerDetails(CompoundDataObject model, boolean editView, FixedValuesViewModel viewModel) {
        SimpleAttribute ownerAttribute = model.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        this.attachOwnerDetails(ownerAttribute, editView, viewModel);
    }
    
    private void attachOwnerDetails(SimpleAttribute ownerAttribute, boolean editView, FixedValuesViewModel viewModel) {
        FixedValueOwnerDetails owner = new FixedValueOwnerDetails();
        owner.setId(ownerAttribute.getAttributeId());
        owner.setCaption(ownerAttribute.getIdentifier());
        owner.setUri(this.composeOwnerUri(ownerAttribute, editView));
        owner.setEntityName("attribute");
        viewModel.setOwner(owner);
        viewModel.setFixedValueCategory(FixedValueCategory.ALLOWABLE);
    }
    
    private String composeOwnerUri(SimpleAttribute ownerAttribute, boolean editView) {
        String uri = String.format("/delem_attribute.jsp?type=SIMPLE&attr_id=%d", ownerAttribute.getAttributeId());
        
        if (editView) {
            uri += "&mode=edit";
        }
        
        return uri;
    }
    
    private void attachFixedValues(CompoundDataObject model, FixedValuesViewModel viewModel) {
        Collection<FixedValue> values = model.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUES);
        viewModel.setFixedValues(values);
    }
    
    private void attachFixedValue(CompoundDataObject model, FixedValuesViewModel viewModel) {
        FixedValue value = model.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUE);
        viewModel.setFixedValue(value);
    }
}

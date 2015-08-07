package eionet.web.action.fixedvalues;

import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import eionet.web.action.AttributeFixedValuesActionBean;
import java.util.Collection;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class AttributeFixedValuesViewModelBuilder {

    public FixedValuesViewModel buildFromOwner(Attribute ownerElement, boolean editView) {
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
        Attribute ownerAttribute = model.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        this.attachOwnerDetails(ownerAttribute, editView, viewModel);
    }
    
    private void attachOwnerDetails(Attribute ownerAttribute, boolean editView, FixedValuesViewModel viewModel) {
        FixedValueOwnerDetails owner = new FixedValueOwnerDetails();
        owner.setId(ownerAttribute.getId());
        owner.setCaption(ownerAttribute.getShortName());
        owner.setUri(this.composeOwnerUri(ownerAttribute, editView));
        owner.setEntityName("attribute");
        viewModel.setOwner(owner);
        viewModel.setFixedValueCategory(FixedValueCategory.ALLOWABLE);
    }
    
    private String composeOwnerUri(Attribute ownerAttribute, boolean editView) {
        String uri = String.format("/delem_attribute.jsp?type=SIMPLE&attr_id=%d", ownerAttribute.getId());
        
        if (editView) {
            uri += "&mode=edit";
        }
        
        return uri;
    }
    
    private void attachFixedValues(CompoundDataObject model, FixedValuesViewModel viewModel) {
        Collection<FixedValue> values = model.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUES);
        viewModel.getFixedValues().addAll(values);
    }
    
    private void attachFixedValue(CompoundDataObject model, FixedValuesViewModel viewModel) {
        FixedValue value = model.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUE);
        viewModel.setFixedValue(value);
    }
}

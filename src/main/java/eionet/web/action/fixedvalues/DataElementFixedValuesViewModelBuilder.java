package eionet.web.action.fixedvalues;

import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import eionet.web.action.DataElementFixedValuesActionBean;
import java.util.Collection;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DataElementFixedValuesViewModelBuilder {

    public FixedValuesViewModel buildFromOwner(DataElement ownerElement, boolean editView) {
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
        viewModel.setActionBeanName(DataElementFixedValuesActionBean.class.getName());
        viewModel.setDefaultValueRequired(false);
        
        return viewModel;
    }
    
    private void attachOwnerDetails(CompoundDataObject model, boolean editView, FixedValuesViewModel viewModel) {
        DataElement ownerElement = model.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        this.attachOwnerDetails(ownerElement, editView, viewModel);
    }
    
    private void attachOwnerDetails(DataElement ownerElement, boolean editView, FixedValuesViewModel viewModel) {
        FixedValueOwnerDetails owner = new FixedValueOwnerDetails();
        owner.setId(ownerElement.getId());
        owner.setCaption(ownerElement.getIdentifier());
        owner.setUri(this.composeOwnerUri(ownerElement, editView));
        owner.setEntityName("element");
        viewModel.setOwner(owner);
        viewModel.setFixedValueCategory("CH2".equals(ownerElement.getType()) ? FixedValueCategory.SUGGESTED : FixedValueCategory.ALLOWABLE);
    }
    
    private String composeOwnerUri(DataElement ownerElement, boolean editView) {
        String uri = String.format("/dataelements/%d", ownerElement.getId());
        
        if (editView) {
            uri += "/edit";
        }
        
        return uri;
    }
    
    private void attachFixedValues(CompoundDataObject model, FixedValuesViewModel viewModel) {
        Collection<FixedValue> values = model.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUES);
        viewModel.getFixedValues().addAll(values);
    }
    
    private void attachFixedValue(CompoundDataObject model, FixedValuesViewModel viewModel) {
        FixedValue value = model.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUE);
        viewModel.setFixedValue(value);
    }
    
}

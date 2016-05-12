/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.controllers.impl;

import eionet.datadict.controllers.AttributeController;
import eionet.datadict.dal.AttributeDefinitionDAO;
import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.enums.Enumerations;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author exorx-alk
 */
@Controller
public class AttributeControllerImpl implements AttributeController{

    private final AttributeDefinitionDAO ddAttributeDefinitionDAOImpl;
    private final IFixedValueDAO fixedValueDAO;
    
    @Autowired
    public AttributeControllerImpl(AttributeDefinitionDAO attributeDefinitionDAOImpl, IFixedValueDAO fixedValueDAO) {
        this.ddAttributeDefinitionDAOImpl = attributeDefinitionDAOImpl;
        this.fixedValueDAO = fixedValueDAO;
    }
    
    @Override
    public CompoundDataObject getAttributeViewInfo(String attrId) {
        AttributeDefinition attrDef = this.getAttributeDefinition(Integer.parseInt(attrId));
        Map<String, String> displayForTypes = this.getDisplayForTypes(attrDef);
        String obligation = this.getObligation(attrDef);
        String displayType = this.getDisplayType(attrDef);
        List<FixedValue> fixedValues = this.getFixedValues(attrDef);
        return this.packageResults(attrDef, displayForTypes, obligation, displayType, fixedValues);
    }


    private AttributeDefinition getAttributeDefinition(int attrId) {
        return ddAttributeDefinitionDAOImpl.getAttributeDefinitionById(attrId);
    }

    private Map<String, String> getDisplayForTypes(AttributeDefinition attributeDefinition) {
       return Enumerations.DisplayForType.getDisplayForTypes(attributeDefinition.getDisplayWhen());
    }

    private String getObligation(AttributeDefinition attributeDefinition) {
        return attributeDefinition.getObligationLevel().getLabel();
    }

    private String getDisplayType(AttributeDefinition attributeDefinition) {
        return attributeDefinition.getDisplayType().getDisplayLabel();
    }

    private List<FixedValue> getFixedValues(AttributeDefinition attributeDefinition) {
       return fixedValueDAO.getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, attributeDefinition.getId());
    }
    
    private CompoundDataObject packageResults (
            AttributeDefinition attrDef, Map<String, String> displayForTypes, String obligation, String displayType, List<FixedValue> fixedValues) {
        CompoundDataObject object = new CompoundDataObject();
        object.put("attributeDefinition", attrDef);
        object.put("displayForTypes", displayForTypes);
        object.put("obligation", obligation);
        object.put("displayType", displayType);
        object.put("fixedValues", fixedValues);
        return object;
    }
    
}

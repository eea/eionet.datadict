/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.controllers.impl;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.datadict.controllers.AttributeController;
import eionet.datadict.dal.AttributeDefinitionDAO;
import eionet.datadict.dal.NamespaceDAO;
import eionet.datadict.dal.RdfNamespaceDAO;
import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.DisplayForType;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author exorx-alk
 */
@Controller
public class AttributeControllerImpl implements AttributeController {

    private final AttributeDefinitionDAO ddAttributeDefinitionDAOImpl;
    private final IFixedValueDAO fixedValueDAO;
    private final NamespaceDAO namespaceDAOImpl;
    private final RdfNamespaceDAO rdfNamespaceDAOImpl;

    @Autowired
    public AttributeControllerImpl(AttributeDefinitionDAO attributeDefinitionDAOImpl, IFixedValueDAO fixedValueDAO, NamespaceDAO namespaceDAOImpl, RdfNamespaceDAO rdfNamespaceDAOImpl) {
        this.ddAttributeDefinitionDAOImpl = attributeDefinitionDAOImpl;
        this.fixedValueDAO = fixedValueDAO;
        this.namespaceDAOImpl = namespaceDAOImpl;
        this.rdfNamespaceDAOImpl = rdfNamespaceDAOImpl;
    }

    @Override
    public CompoundDataObject getAttributeViewInfo(String attrId) throws ResourceNotFoundException {
        AttributeDefinition attrDef = this.getAttributeDefinition(Integer.parseInt(attrId));
        List<DisplayForType> displayForTypes = this.getDisplayForTypes(attrDef);
        List<FixedValue> fixedValues = this.getFixedValues(attrDef);
        return this.packageResults(attrDef, displayForTypes, fixedValues);
    }

    @Override
    public CompoundDataObject getAttributeEditInfo(String attrId) throws ResourceNotFoundException {
        AttributeDefinition attrDef = this.getAttributeDefinition(Integer.parseInt(attrId));
        List<Namespace> namespaces = this.getAttributeNamespaces();
        String displayOrder = this.getDisplayOrderToView(attrDef);
        List<DisplayForType> displayForTypes = this.getDisplayForTypes(attrDef);
        List<RdfNamespace> rdfNamespaces = this.getAllRdfNamespaces();
        return packageResults(attrDef, namespaces, displayOrder, displayForTypes, rdfNamespaces);
    }

    @Override
    public void saveAttribute(AttributeViewModel viewModel) {
        AttributeDefinition attrDef = viewModel.getAttributeDefinition();
        attrDef.setDisplayOrder(getDisplayOrderToSave(viewModel.getDisplayOrder()));
        attrDef.setDisplayWhen(getDisplayWhenToSave(viewModel.getDisplayForTypes()));
        attrDef.setRdfNameSpace(getRdfNamespaceToSave(viewModel.getRdfNamespaceId()));
        ddAttributeDefinitionDAOImpl.save(attrDef);
    }

    @Override
    public void deleteAttribute(String id) throws ResourceNotFoundException {
        try {
            ddAttributeDefinitionDAOImpl.delete(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    private int getDisplayWhenToSave(List<DisplayForType> displayForTypes) {
        int sum = 0;
        for (DisplayForType displayForType : displayForTypes) {
            sum = sum + displayForType.getValue();
        }
        return sum;
    }

    private List<RdfNamespace> getAllRdfNamespaces() {
        return this.rdfNamespaceDAOImpl.getRdfNamespaces();
    }

    private RdfNamespace getRdfNamespaceToSave(String rdfNamespaceId) {
        RdfNamespace rdfNamespace = new RdfNamespace();
        if (rdfNamespaceId != null) {
            rdfNamespace.setId(Integer.parseInt(rdfNamespaceId));
        } else {
            rdfNamespace = null;
        }
        return rdfNamespace;
    }

    private int getDisplayOrderToSave(String displayOrder) {
        if (displayOrder == null || displayOrder.equals("")) {
            return 999;
        } else {
            return Integer.parseInt(displayOrder);
        }
    }

    private String getDisplayOrderToView(AttributeDefinition attrDef) {
        int dispOrder = attrDef.getDisplayOrder();
        if (dispOrder == 999) {
            return "";
        }
        return String.valueOf(dispOrder);
    }

    private List<Namespace> getAttributeNamespaces() {
        return namespaceDAOImpl.getAttributeNamespaces();
    }

    private AttributeDefinition getAttributeDefinition(int attrId) throws ResourceNotFoundException {
        return ddAttributeDefinitionDAOImpl.getAttributeDefinitionById(attrId);
    }

    private List<DisplayForType> getDisplayForTypes(AttributeDefinition attributeDefinition) {
        return Enumerations.DisplayForType.getDisplayForTypes(attributeDefinition.getDisplayWhen());
    }

    private List<FixedValue> getFixedValues(AttributeDefinition attributeDefinition) {
        return fixedValueDAO.getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, attributeDefinition.getId());
    }

    private CompoundDataObject packageResults(
            AttributeDefinition attrDef, List<Namespace> namespaces, String displayOrder, List<DisplayForType> displayForTypes, List<RdfNamespace> rdfNamespaces) {
        CompoundDataObject object = new CompoundDataObject();
        object.put(ATTRIBUTE_DEFINITION, attrDef);
        object.put(NAMESPACES, namespaces);
        object.put(DISPLAY_ORDER, displayOrder);
        object.put(DISPLAY_FOR_TYPES, displayForTypes);
        object.put(RDF_NAMESPACES, rdfNamespaces);
        return object;
    }

    private CompoundDataObject packageResults(
            AttributeDefinition attrDef, List<DisplayForType> displayForTypes, List<FixedValue> fixedValues) {
        CompoundDataObject object = new CompoundDataObject();
        object.put(ATTRIBUTE_DEFINITION, attrDef);
        object.put(DISPLAY_FOR_TYPES, displayForTypes);
        object.put(FIXED_VALUES, fixedValues);
        return object;
    }

}

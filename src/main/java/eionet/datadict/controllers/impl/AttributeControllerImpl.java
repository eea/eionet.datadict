package eionet.datadict.controllers.impl;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.datadict.controllers.AttributeController;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.NamespaceDao;
import eionet.datadict.dal.RdfNamespaceDao;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.DisplayForType;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.errors.ResourceNotFoundException;
//import eionet.datadict.resources.AttributeDefinitionIdInfo;
import eionet.datadict.resources.ResourceType;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Aliki Kopaneli
 */
@Controller
public class AttributeControllerImpl implements AttributeController {

    private final AttributeDataService attributeDefinitionServiceImpl;
    private final AttributeDao ddAttributeDefinitionDAOImpl;
    private final IFixedValueDAO fixedValueDAO;
    private final NamespaceDao namespaceDAOImpl;
    private final RdfNamespaceDao rdfNamespaceDAOImpl;

    @Autowired
    public AttributeControllerImpl(AttributeDao ddAttributeDefinitionDAOImpl, AttributeDataService attributeDefinitionServiceImpl,
            IFixedValueDAO fixedValueDAO, NamespaceDao namespaceDAOImpl, RdfNamespaceDao rdfNamespaceDAOImpl) {
        this.attributeDefinitionServiceImpl = attributeDefinitionServiceImpl;
        this.ddAttributeDefinitionDAOImpl = ddAttributeDefinitionDAOImpl;
        this.fixedValueDAO = fixedValueDAO;
        this.namespaceDAOImpl = namespaceDAOImpl;
        this.rdfNamespaceDAOImpl = rdfNamespaceDAOImpl;
    }
    
    //Methods to create the object to be passed to the ViewModel (for GET actions)
    @Override
    public CompoundDataObject getAttributeViewInfo(String attrId) throws ResourceNotFoundException {
//        Attribute attrDef = this.getAttributeDefinition(Integer.parseInt(attrId));
//        List<DisplayForType> displayForTypes = this.getDisplayForTypesFromDisplayWhen(attrDef.getDisplayWhen());
//        List<FixedValue> fixedValues = this.getFixedValues(attrDef);
//        return this.packageResults(attrDef, displayForTypes, fixedValues);
        return null;
    }
    
    @Override
    public void saveNewVocabulary(String attrId, String vocId) {
        if (vocId == null) {
            ddAttributeDefinitionDAOImpl.deleteVocabularyBinding(Integer.valueOf(attrId));
        }
        else {
            ddAttributeDefinitionDAOImpl.updateVocabularyBinding(Integer.valueOf(attrId),
                Integer.valueOf(vocId));
        }
    }
    
    @Override
    public CompoundDataObject getAttributeEditInfo(String attrId) throws ResourceNotFoundException {
//        Attribute attrDef = this.getAttributeDefinition(Integer.parseInt(attrId));
//        List<Namespace> namespaces = this.getAttributeNamespaces();
//        String displayOrder = this.getDisplayOrderToView(attrDef);
//        List<DisplayForType> displayForTypes = this.getDisplayForTypesFromDisplayWhen(attrDef.getDisplayWhen());
//        List<RdfNamespace> rdfNamespaces = this.getAllRdfNamespaces();
//        return packageResults(attrDef, namespaces, displayOrder, displayForTypes, rdfNamespaces);
        return null;
    }

    @Override
    public CompoundDataObject getAttributeAddInfo() {
        List<RdfNamespace> rdfNamespaces = this.getAllRdfNamespaces();
        return packageResults(rdfNamespaces);
    }
    //----------------
    
    //Methods which update the database
    @Override
    public void saveAttribute(AttributeViewModel viewModel) {
        Attribute attrDef = viewModel.getAttributeDefinition();
        attrDef.setDisplayOrder(getDisplayOrderToSave(viewModel.getDisplayOrder()));
//        attrDef.setDisplayWhen(getDisplayWhenFromDisplayForTypes(viewModel.getDisplayForTypes()));
//        attrDef.setRdfNameSpace(getRdfNamespaceToSave(viewModel.getRdfNamespaceId()));
        ddAttributeDefinitionDAOImpl.update(attrDef);
    }

    @Override
    public int saveNewAttribute(AttributeViewModel viewModel) {
        Attribute attrDef = viewModel.getAttributeDefinition();
        if (attrDef.getDefinition()==null) attrDef.setDefinition("");
        attrDef.setDisplayOrder(getDisplayOrderToSave(viewModel.getDisplayOrder()));
//        attrDef.setDisplayWhen(getDisplayWhenFromDisplayForTypes(viewModel.getDisplayForTypes()));
//        attrDef.setDisplayWhen(getDisplayWhenFromDisplayForTypes(viewModel.getDisplayForTypes()));
//        attrDef.setRdfNameSpace(getRdfNamespaceToSave(viewModel.getRdfNamespaceId()));
        attrDef.setNamespace(getDefaultNamespace());
        return ddAttributeDefinitionDAOImpl.create(attrDef);
    }

    @Override
    public void deleteAttribute(String id) throws ResourceNotFoundException {
//        try {
//            ddAttributeDefinitionDAOImpl.delete(Integer.parseInt(id));
//        } catch (NumberFormatException e) {
//            throw new ResourceNotFoundException(ResourceType.ATTRIBUTE, new AttributeDefinitionIdInfo(id));
//        }
    }
    //----------------------
    
    //methods to create CompoundDataObjects
    private CompoundDataObject packageResults(List<RdfNamespace> rdfNamespaces) {
        CompoundDataObject object = new CompoundDataObject();
        object.put(RDF_NAMESPACES, rdfNamespaces);
        return object;
    }

    private CompoundDataObject packageResults(
            Attribute attrDef, List<Namespace> namespaces, String displayOrder, List<DisplayForType> displayForTypes, List<RdfNamespace> rdfNamespaces) {
        CompoundDataObject object = new CompoundDataObject();
        object.put(ATTRIBUTE_DEFINITION, attrDef);
        object.put(NAMESPACES, namespaces);
        object.put(DISPLAY_ORDER, displayOrder);
        object.put(DISPLAY_FOR_TYPES, displayForTypes);
        object.put(RDF_NAMESPACES, rdfNamespaces);
        return object;
    }

    private CompoundDataObject packageResults(
            Attribute attrDef, List<DisplayForType> displayForTypes, List<FixedValue> fixedValues) {
        CompoundDataObject object = new CompoundDataObject();
        object.put(ATTRIBUTE_DEFINITION, attrDef);
        object.put(DISPLAY_FOR_TYPES, displayForTypes);
        object.put(FIXED_VALUES, fixedValues);
        return object;
    }
    //-------------------------------

    /**
     * Returns the namespace to be saved for a new attribute
     * 
     * @return a namespace with the default ID for attribute
     */
    private Namespace getDefaultNamespace() {
        Namespace namespace = new Namespace();
//        namespace.setNamespaceID(NAMESPACE_ID_DEFAULT);
        return namespace;
    }
     
    //Transformations between displayForTypes and displayWhen
    private int getDisplayWhenFromDisplayForTypes(List<DisplayForType> displayForTypes) {
        if (displayForTypes == null) return 0;
        int displayWhen = 0;
        for (DisplayForType displayForType : displayForTypes) {
            displayWhen = displayWhen + displayForType.getValue();
        }
        return displayWhen;
    }
    
    private List<DisplayForType> getDisplayForTypesFromDisplayWhen(int displayWhen) {
        return Enumerations.DisplayForType.getDisplayForTypes(displayWhen);
    }
    //----------------------------
    
    //Handles the null case of rdfNamespace
    private RdfNamespace getRdfNamespaceToSave(String rdfNamespaceId) {
        RdfNamespace rdfNamespace = new RdfNamespace();
        if (rdfNamespaceId != null) {
            rdfNamespace.setId(Integer.parseInt(rdfNamespaceId));
        } else {
            rdfNamespace = null;
        }
        return rdfNamespace;
    }

    //Handle the 999 issue of displayOrder
    private int getDisplayOrderToSave(String displayOrder) {
        if (displayOrder == null || displayOrder.equals("")) {
            return 999;
        } else {
            return Integer.parseInt(displayOrder);
        }
    }
    
    private String getDisplayOrderToView(Attribute attrDef) {
        int dispOrder = attrDef.getDisplayOrder();
        if (dispOrder == 999) {
            return "";
        }
        return String.valueOf(dispOrder);
    }
    //-----------------
    
    
    private List<Namespace> getAttributeNamespaces() {
        return namespaceDAOImpl.getAttributeNamespaces();
    }

    private Attribute getAttributeDefinition(int attrId) throws ResourceNotFoundException {
        return attributeDefinitionServiceImpl.getAttribute(attrId);
    }

     private List<RdfNamespace> getAllRdfNamespaces() {
        return this.rdfNamespaceDAOImpl.getRdfNamespaces();
    }
     
    private List<FixedValue> getFixedValues(Attribute attributeDefinition) {
        return fixedValueDAO.getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, attributeDefinition.getId());
    }
}

package eionet.datadict.controllers;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.util.CompoundDataObject;

/**
 *
 * @author Aliki Kopaneli
 */
public interface AttributeController {
    
    /**
     * Fetch info for the view page
     * 
     * @param attrId the M_ATTRIBUTE id
     * @return an object containing all the display info
     * @throws eionet.meta.application.errors.ResourceNotFoundException
     */
    CompoundDataObject getAttributeViewInfo(String attrId) throws ResourceNotFoundException;
    
    /**
     * Fetch info for the edit page
     * 
     * @param attrId the M_ATTRIBUTE id
     * @return an object containing all the display info
     * @throws eionet.meta.application.errors.ResourceNotFoundException
     */
    CompoundDataObject getAttributeEditInfo(String attrId) throws ResourceNotFoundException;
   
    /**
     * Fetch info for the add page
     * 
     * @return an object containing the display info 
     */
    CompoundDataObject getAttributeAddInfo();
    
    /**
     * Save the Attribute described by the viewModel
     * 
     * @param viewModel the viewModel holding info from the Stripes form
     */
    void saveAttribute(AttributeViewModel viewModel);
    
    /**
     * Save the new Attribute described by the viewModel
     * 
     * @param viewModel viewModel the viewModel holding info from the Stripes form
     * @return the id of the newly inserted attribute
     */
    int saveNewAttribute (AttributeViewModel viewModel);
    
    /**
     * Update the vocabulary from which the attribute gets its values
     * 
     * @param attrId
     * @param vocId 
     */
    void saveNewVocabulary(String attrId, String vocId);
    
    
    /**
     * Delete the specified attribute
     * 
     * @param id the id of the attribute to be deleted
     * @throws eionet.meta.application.errors.ResourceNotFoundException
     */
    void deleteAttribute(String id) throws ResourceNotFoundException;
    
    /**
     * labels for exchanging data as key-value pairs through CompoundDataObject
     */
    
    //eionet.datadict.model.AttributeDefinition.java
    public static final String ATTRIBUTE_DEFINITION = "attributeDefinition";
    //List<eionet.datadict.model.enums.Enumerations$DisplayForTypes>
    public static final String DISPLAY_FOR_TYPES = "displayForTypes";
    //List<eionet.meta.dao.model.FixedValues>
    public static final String FIXED_VALUES = "fixedValues";
    //List<eionet.meta.dao.model.Namespace>
    public static final String NAMESPACES = "namespaces";
    //String
    public static final String DISPLAY_ORDER = "displayOrder";
    //List<eionet.datadict.model.RdfNamespace>
    public static final String RDF_NAMESPACES = "rdfNamespaces";
    
    public static final int DISPLAY_WIDTH_DEFAULT = 20;
    public static final int DISPLAY_HEIGHT_DEFAULT = 1;
    public static final int NAMESPACE_ID_DEFAULT = 3;
}

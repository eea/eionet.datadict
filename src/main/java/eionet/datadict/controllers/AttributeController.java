/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.controllers;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.datadict.model.AttributeDefinition;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.util.CompoundDataObject;

/**
 *
 * @author exorx-alk
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
     * Save the Attribute described by the viewModel
     * 
     * @param viewModel the viewModel holding info from the Stripes form
     */
    void saveAttribute(AttributeViewModel viewModel);
    
    /**
     * Delete the specified attribute
     * @param id the id of the attribute to be deleted
     * @throws eionet.meta.application.errors.ResourceNotFoundException
     */
    void deleteAttribute(String id) throws ResourceNotFoundException;
    
    /**
     * labels for exchanging data as key-value pairs
     */
    public static final String ATTRIBUTE_DEFINITION = "attributeDefinition";
    public static final String DISPLAY_FOR_TYPES = "displayForTypes";
    public static final String FIXED_VALUES = "fixedValues";
    public static final String NAMESPACES = "namespaces";
    public static final String DISPLAY_ORDER = "displayOrder";
    public static final String RDF_NAMESPACES = "rdfNamespaces";
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.controllers;

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
}

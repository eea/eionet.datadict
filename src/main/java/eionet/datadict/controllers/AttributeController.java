/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.controllers;

import eionet.util.CompoundDataObject;

/**
 *
 * @author exorx-alk
 */
public interface AttributeController {
    
    /**
     * Fetch all info for the view page
     * @param attrId the M_ATTRIBUTE id
     * @return an object containing all the display info
     */
    CompoundDataObject getAttributeViewInfo(String attrId);
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class FixedValueHandler extends CodeValueHandler{
    
    public FixedValueHandler( IDataElementDAO elementDAO ){
        super( elementDAO );
    }

    @Override
    List<CodeItem> getCodeItemList() {
        if ( this.element == null ){
            throw new UnsupportedOperationException("Data element is not set");
        }
        int elementID = this.element.getId();
        
        List<FixedValue> values = this.elementDAO.getFixedValues(elementID);
        
        List<CodeItem> items = new ArrayList<CodeItem>();
        
        for ( FixedValue value : values ){
            items.add( new CodeItem( value.getLabel(), value.getShortDescription(), value.getDefinition() ) );
        }
        
        return items;
    }

    @Override
    List<String> getRelationshipNames(){ return Collections.emptyList(); }
}

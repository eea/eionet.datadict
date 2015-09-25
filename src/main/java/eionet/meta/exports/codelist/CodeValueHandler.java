/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.DataElement;
import java.util.List;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public abstract class CodeValueHandler {
    
    final IDataElementDAO elementDAO;
    
    DataElement element;

    public CodeValueHandler( IDataElementDAO elementDAO ) {
        this.elementDAO = elementDAO;
    }
    
    void setDataElement( DataElement element ){
        this.element = element;
    }

    abstract List<CodeItem> getCodeItemList();

    abstract List<String> getRelationshipNames();
}

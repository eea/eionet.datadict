package eionet.meta.exports.codelist;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.exports.codelist.ExportStatics.ExportType;
import eionet.meta.exports.codelist.ExportStatics.ObjectType;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface CodelistExporter {
    
    byte[] exportCodelist(String ownerId, ObjectType objectType, ExportType exportType) 
            throws ResourceNotFoundException;
    
}

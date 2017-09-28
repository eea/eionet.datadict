package eionet.datadict.services.data;

import eionet.datadict.model.AttributeValue;
import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface AttributeValueDataService {

    public List<AttributeValue> getAllByDataSetId(Integer datasetId);

    public List<AttributeValue> getAllByDataSetTableId(Integer datasetTableId);

    public List<AttributeValue> getAllByAttributeAndDataSetId(Integer attributeId, Integer datasetId);
    
    public List<AttributeValue> getAllByAttributeAndDataSetTableId(Integer attributeId, Integer datasetTableId);

   public List<AttributeValue> getAllByDataElementId(Integer dataElementId);
}

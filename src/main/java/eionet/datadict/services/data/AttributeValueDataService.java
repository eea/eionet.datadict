package eionet.datadict.services.data;

import eionet.datadict.model.AttributeValue;
import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface AttributeValueDataService {

    public List<AttributeValue> getAllByDataSetId(Integer datasetId);

}

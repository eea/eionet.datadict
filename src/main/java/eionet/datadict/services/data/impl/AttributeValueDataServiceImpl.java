package eionet.datadict.services.data.impl;

import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.services.data.AttributeValueDataService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class AttributeValueDataServiceImpl implements AttributeValueDataService {

    private final AttributeValueDao attributeValueDao;

    @Autowired
    public AttributeValueDataServiceImpl(AttributeValueDao attributeValueDao) {
        this.attributeValueDao = attributeValueDao;
    }

    @Override
    public List<AttributeValue> getAllByDataSetId(Integer datasetId) {
        return attributeValueDao.getByOwner(new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
    }

}

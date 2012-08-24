package eionet.meta.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;

@Service
@Transactional
public class DataServiceImpl implements IDataService {

    /** Data set DAO. */
    @Autowired
    private IDataSetDAO dataSetDao;

    /** Attribute DAO. */
    @Autowired
    private IAttributeDAO attributeDao;

    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSet> getDataSets() throws ServiceException {
        try {
            return dataSetDao.getDataSets();
        } catch (Exception e) {
            throw new ServiceException("Failed to get data sets: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttributeByName(String shortName) throws ServiceException {
        try {
            return attributeDao.getAttributeByName(shortName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get the attribute for '" + shortName + "': " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElementsResult searchDataElements(DataElementsFilter filter) throws ServiceException {
        try {
            return dataElementDao.searchDataElements(filter);
        } catch (Exception e) {
            throw new ServiceException("Failed search data elements: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attribute> getDataElementAttributes() throws ServiceException {
        try {
            return dataElementDao.getDataElementAttributes();
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element attributes: " + e.getMessage(), e);
        }
    }

}

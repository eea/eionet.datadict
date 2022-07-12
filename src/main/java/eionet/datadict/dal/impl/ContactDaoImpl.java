package eionet.datadict.dal.impl;

import eionet.datadict.dal.ContactDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.model.ContactDetails;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.DataSet;
import eionet.meta.dao.IDataSetDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ContactDaoImpl extends JdbcDaoBase implements ContactDao {

    @Autowired
    public ContactDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Autowired
    private IDataSetDAO dataSetDAO;
    @Autowired
    private DatasetDao datasetD;
    @Autowired
    private DataElementDao dataElementDao;
    @Autowired
    private DatasetTableDao datasetTableDao;

    @Override
    public Set<ContactDetails> getAllByValue(String value) {
        String sql = "select M_ATTRIBUTE.NAME as M_ATTRIBUTE_NAME, ATTRIBUTE.*, DATAELEM.SHORT_NAME as DATAELEM_SHORT_NAME, DATAELEM.IDENTIFIER as DATAELEM_IDENTIFIER, \n" +
                "DATAELEM.REG_STATUS as DATAELEM_REG_STATUS, DATAELEM.TYPE as DATAELEM_TYPE, DATAELEM.PARENT_NS as DATAELEM_PARENT_NS, DATAELEM.TOP_NS as DATAELEM_TOP_NS, " +
                "TBL2ELEM.TABLE_ID as DATAELEM_TABLE_ID, DATASET.SHORT_NAME as DATASET_SHORT_NAME, DATASET.IDENTIFIER as DATASET_IDENTIFIER, DATASET.REG_STATUS as DATASET_REG_STATUS, " +
                "DATASET.WORKING_COPY AS DATASET_WORKING_COPY, DATAELEM.WORKING_COPY AS DATAELEM_WORKING_COPY "
                + "from ATTRIBUTE "
                + "left join M_ATTRIBUTE on ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID "
                + "left join DATAELEM on DATAELEM.DATAELEM_ID=ATTRIBUTE.DATAELEM_ID "
                + "left join DATASET on DATASET.DATASET_ID=ATTRIBUTE.DATAELEM_ID "
                + "left join TBL2ELEM on TBL2ELEM.DATAELEM_ID=ATTRIBUTE.DATAELEM_ID "
                + "WHERE ATTRIBUTE.VALUE = :value "
                + "ORDER BY ATTRIBUTE.DATAELEM_ID desc;";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("value", value);
        Set<ContactDetails> contactDetailsSet = new HashSet<>();
        Set<ContactDetails> contactDetailsTempSet = new HashSet<>();
        try {
            List<ContactDetails> contactDetailsList = this.getNamedParameterJdbcTemplate().query(sql, params, new ContactDaoImpl.ContactRowMapper());
            contactDetailsList.forEach(entry -> contactDetailsTempSet.add(entry));
            for (ContactDetails contactDetails : contactDetailsTempSet) {
                if (contactDetails.getParentType().equals("Dataset") && contactDetails.getDatasetIdentifier() != null
                        && contactDetails.getDatasetWorkingCopy()!=null && contactDetails.getDatasetWorkingCopy().equals("N")) {
                    Integer latestDatasetId = dataSetDAO.getLatestDatasetId(contactDetails.getDatasetIdentifier());
                    if (contactDetails.getDataElemId().equals(latestDatasetId)) {
                        contactDetailsSet.add(contactDetails);
                    }
                } else if (contactDetails.getParentType().equals("DataElement") && contactDetails.getDataElementIdentifier() != null) {
                    if (contactDetails.getDataElemParentNs() != null && !contactDetails.getDataElemParentNs().equals(0)) {
                        //non common elements
                        Integer parentDatasetId = datasetTableDao.getParentDatasetId(contactDetails.getDataElemTableId());
                        DataSet parentDataset = datasetD.getById(parentDatasetId);
                        Integer latestDatasetId = dataSetDAO.getLatestDatasetId(parentDataset.getIdentifier());
                        if (latestDatasetId.equals(parentDatasetId) && parentDataset.getWorkingCopy()!=null && !parentDataset.getWorkingCopy()) {
                            contactDetails.setDataElementDatasetId(parentDatasetId);
                            DatasetTable table = datasetTableDao.getById(contactDetails.getDataElemTableId());
                            contactDetails.setDataElemTableIdentifier(table.getIdentifier());
                            contactDetailsSet.add(contactDetails);
                        }
                    } else {
                        //common elements
                        Integer latestDataElementId = dataElementDao.getLatestDataElementId(contactDetails.getDataElementIdentifier());
                        if (contactDetails.getDataElemId().equals(latestDataElementId) && contactDetails.getDataElemWorkingCopy()!=null && contactDetails.getDataElemWorkingCopy().equals("N")) {
                           boolean exists = contactDetailsSet.stream().anyMatch(entry -> entry.getDataElemId().equals(contactDetails.getDataElemId()));
                           if (!exists) {
                               contactDetailsSet.add(contactDetails);
                           }
                        }
                    }
                }
            }
            return contactDetailsSet;
        } catch (EmptyResultDataAccessException ex) {
            return Collections.EMPTY_SET;
        }
    }

    public static class ContactRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            ContactDetails contactDetails = new ContactDetails();
            contactDetails.setmAttributeId(rs.getInt("M_ATTRIBUTE_ID"));
            contactDetails.setmAttributeName(rs.getString("M_ATTRIBUTE_NAME"));
            contactDetails.setDataElemId(rs.getInt("DATAELEM_ID"));
            String parentType = rs.getString("PARENT_TYPE");

            switch (DataDictEntity.Entity.getFromString(parentType)) {

                case DS:
                    contactDetails.setParentType("Dataset");
                    break;
                case E:
                    contactDetails.setParentType("DataElement");
                    break;
            }

            contactDetails.setDatasetShortName(rs.getString("DATASET_SHORT_NAME"));
            contactDetails.setDatasetIdentifier(rs.getString("DATASET_IDENTIFIER"));
            contactDetails.setDataElementShortName(rs.getString("DATAELEM_SHORT_NAME"));
            contactDetails.setDataElementIdentifier(rs.getString("DATAELEM_IDENTIFIER"));
            contactDetails.setDataElemRegStatus(rs.getString("DATAELEM_REG_STATUS"));
            contactDetails.setDataElemTableId(rs.getInt("DATAELEM_TABLE_ID"));
            contactDetails.setDataElemType(rs.getString("DATAELEM_TYPE"));
            contactDetails.setDataElemParentNs(rs.getInt("DATAELEM_PARENT_NS"));
            contactDetails.setDataElemTopNs(rs.getInt("DATAELEM_TOP_NS"));
            contactDetails.setDatasetRegStatus(rs.getString("DATASET_REG_STATUS"));
            contactDetails.setDatasetWorkingCopy(rs.getString("DATASET_WORKING_COPY"));
            contactDetails.setDataElemWorkingCopy(rs.getString("DATAELEM_WORKING_COPY"));
            contactDetails.setValue(rs.getString("VALUE"));
            return contactDetails;
        }

    }
}

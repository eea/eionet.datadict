package eionet.datadict.dal.impl;

import eionet.datadict.dal.ContactDao;
import eionet.datadict.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ContactDaoImpl extends JdbcDaoBase implements ContactDao {

    @Autowired
    public ContactDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<ContactDetails> getAllByValue(String value) {
        String sql = "select M_ATTRIBUTE.NAME as M_ATTRIBUTE_NAME, ATTRIBUTE.*, DATAELEM.SHORT_NAME as DATAELEM_SHORT_NAME, DATAELEM.IDENTIFIER as DATAELEM_IDENTIFIER, \n" +
                "DATASET.SHORT_NAME as DATASET_SHORT_NAME, DATASET.IDENTIFIER as DATASET_IDENTIFIER from ATTRIBUTE "
                + "left outer join M_ATTRIBUTE on ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID "
                + "left outer join DATAELEM on DATAELEM.DATAELEM_ID=ATTRIBUTE.DATAELEM_ID "
                + "left outer join DATASET on DATASET.DATASET_ID=ATTRIBUTE.DATAELEM_ID "
                + "WHERE VALUE = :value";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("value", value);
        try {
            return this.getNamedParameterJdbcTemplate().query(sql, params, new ContactDaoImpl.ContactRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return Collections.EMPTY_LIST;
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
            return contactDetails;
        }

    }
}

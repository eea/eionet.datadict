package eionet.datadict.dal.impl;

import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataElementWithFixedValues;
import eionet.datadict.model.DataElementWithQuantitativeValues;
import eionet.datadict.model.FixedValue;
import eionet.datadict.model.FixedValuesOwner;
import eionet.datadict.model.FixedValuesOwnerType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Repository
public class FixedValuesDaoImpl extends JdbcDaoBase implements FixedValuesDao {

    @Autowired
    public FixedValuesDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<FixedValue> getValueListCodesOfDataElementsInTable(int tableId) {
        String sql = "SELECT FXV.*, DATAELEM.*\n"
                + "FROM FXV\n"
                + " LEFT JOIN DATAELEM ON FXV.OWNER_ID = DATAELEM.DATAELEM_ID WHERE FXV.OWNER_ID=:tableId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tableId", tableId);
        params.put("ownerType", FixedValuesOwnerType.Type.elem);
        try {
            return this.getNamedParameterJdbcTemplate().query(sql, params, new FixedValuesDaoImpl.FixedValueRowMapper());
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }

    }

    @Override
    public List<FixedValue> getFixedValues(int dataElementId) {
          String sql = "select * from FXV where OWNER_ID=:ownerId and OWNER_TYPE=:ownerType order by FXV_ID";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", dataElementId);
        params.put("ownerType", "elem");

        List<FixedValue> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<FixedValue>() {
            @Override
            public FixedValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                FixedValue fv = new FixedValue();
                fv.setId(rs.getInt("FXV_ID"));
                fv.setValue(rs.getString("VALUE"));
                fv.setDefinition(rs.getString("DEFINITION"));
                fv.setShortDescription(rs.getString("SHORT_DESC"));
                return fv;
            }
        });

        return result;
    }

    public static class FixedValueRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            FixedValue fixedValue = new FixedValue();
            fixedValue.setId(rs.getInt("FXV.FXV_ID"));
            fixedValue.setValue(rs.getString("FXV.VALUE"));
            Integer ownerId = rs.getInt("FXV.OWNER_ID");
            fixedValue.setDefinition(rs.getString("FXV.DEFINITION"));
            fixedValue.setShortDescription(rs.getString("FXV.SHORT_DESC"));
            String dataElementOwnerType = rs.getString("DATAELEM.TYPE");
            switch (DataElement.DataElementType.resolveTypeFromName(dataElementOwnerType)) {

                case CH1:
                    fixedValue.setOwner(new DataElementWithFixedValues(ownerId));
                    break;
                case CH2:
                    fixedValue.setOwner(new DataElementWithQuantitativeValues(ownerId));
                    break;

            }

            return fixedValue;
        }

    }
}

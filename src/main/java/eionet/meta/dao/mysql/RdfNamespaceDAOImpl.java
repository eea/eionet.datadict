package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.DataElement;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.IRdfNamespaceDAO;
import eionet.meta.dao.domain.RdfNamespace;

/**
 * IRDFNamespaceDAO implementation in mysql.
 *
 * @author Kaido Laine
 */
@Repository
public class RdfNamespaceDAOImpl extends GeneralDAOImpl implements IRdfNamespaceDAO {

    @Override
    public boolean namespaceExists(String namespaceId) throws DAOException {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from T_RDF_NAMESPACE where NAME_PREFIX = :nsPrefix ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("nsPrefix", namespaceId.toLowerCase());

        List<String> resultList = getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("NAME_PREFIX");
            }
        });

        return resultList.size() != 0;
    }

    @Override
    public RdfNamespace getNamespace(String namespaceId) throws DAOException {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from T_RDF_NAMESPACE where NAME_PREFIX = :nsPrefix ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("nsPrefix", namespaceId);

        List<RdfNamespace> resultList =
                getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowMapper<RdfNamespace>() {
                    @Override
                    public RdfNamespace mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RdfNamespace ns = new RdfNamespace();
                        ns.setId(Integer.parseInt(rs.getString("id")));
                        ns.setPrefix(rs.getString("NAME_PREFIX"));
                        ns.setUri(rs.getString("URI"));

                        return ns;

                    }
                });

        return resultList.size() > 0 ? resultList.get(0) : null;
    }

    @Override
    public List<RdfNamespace> getElementExternalNamespaces(List<DataElement> elements) throws DAOException {
        ArrayList<RdfNamespace> nameSpaces = new ArrayList<RdfNamespace>();

        for (DataElement elem : elements) {
            if (elem.isExternalSchema()) {
                RdfNamespace ns = getNamespace(elem.getNameSpacePrefix());
                if (!nameSpaces.contains(ns)) {
                    nameSpaces.add(ns);
                }
            }
        }
        return nameSpaces;
    }

    @Override
    public List<RdfNamespace> getRdfNamespaces() throws DAOException {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from T_RDF_NAMESPACE order by NAME_PREFIX");

        List<RdfNamespace> resultList =
                getNamedParameterJdbcTemplate().query(sql.toString(), new HashMap<String, Object>(),
                        new RowMapper<RdfNamespace>() {
                            @Override
                            public RdfNamespace mapRow(ResultSet rs, int rowNum) throws SQLException {
                                RdfNamespace rns = new RdfNamespace();
                                rns.setId(rs.getInt("ID"));
                                rns.setUri(rs.getString("URI"));
                                rns.setPrefix(rs.getString("NAME_PREFIX"));
                                return rns;
                            }
                        });

        return resultList;
    }

}

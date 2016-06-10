package eionet.datadict.dal.impl;

import eionet.datadict.dal.impl.RdfNamespaceDaoImpl.RdfNamespaceRowMapper;
import eionet.datadict.model.RdfNamespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

public class RdfNamespaceDaoImplTest {
    @Mock
    private ResultSet resultSet;
    @Spy
    private RdfNamespaceRowMapper rowMapper;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testNamespaceRowMapper() throws SQLException {
        when(resultSet.getString(anyString())).thenReturn("");
        when(resultSet.getInt(anyString())).thenReturn(1);
        
        RdfNamespace rdfNamespace = rowMapper.mapRow(resultSet, 0);
        
        assertNotNull(rdfNamespace);
        assertEquals(1, rdfNamespace.getId().intValue());
        assertEquals("", rdfNamespace.getUri());
        assertEquals("", rdfNamespace.getPrefix());
    }
}

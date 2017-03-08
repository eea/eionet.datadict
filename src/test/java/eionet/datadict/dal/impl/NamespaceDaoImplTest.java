package eionet.datadict.dal.impl;

import eionet.datadict.dal.impl.NamespaceDaoImpl.NamespaceRowMapper;
import eionet.datadict.model.Namespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;
import org.mockito.Spy;

public class NamespaceDaoImplTest {
    
    @Mock
    private ResultSet resultSet;
    @Spy
    private NamespaceRowMapper rowMapper;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testNamespaceRowMapper() throws SQLException {
        when(resultSet.getString(anyString())).thenReturn("");
        when(resultSet.getInt(anyString())).thenReturn(1);
        
        Namespace namespace = rowMapper.mapRow(resultSet, 0);
        
        assertNotNull(namespace);
        assertEquals(1, namespace.getId().intValue());
        assertEquals("", namespace.getShortName());
        assertEquals("", namespace.getFullName());
        assertEquals("", namespace.getDefinition());
        assertEquals("", namespace.getWorkingUser());
    }
}

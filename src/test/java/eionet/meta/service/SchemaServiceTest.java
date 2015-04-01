/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service;

import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import org.displaytag.properties.SortOrderEnum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 * JUnit integration test with Unitils for schema service.
 * 
 * @author dev-nn
 */
@SpringApplicationContext("spring-context.xml")
public class SchemaServiceTest extends UnitilsJUnit4 {
    
    private static final String SEED_FILE_ATTRIBUTE = "seed-searchSchemaSets-attribute.xml";
    private static final String SEED_FILE_SCHEMA_SETS = "seed-searchSchemaSets-schemaSets.xml";
    private static final String SEED_FILE_SCHEMAS = "seed-searchSchemaSets-schemas.xml";
    
    @SpringBeanByType
    private ISchemaService service;
    
    public SchemaServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        DBUnitHelper.loadData(SEED_FILE_ATTRIBUTE);
        DBUnitHelper.loadData(SEED_FILE_SCHEMA_SETS);
        DBUnitHelper.loadData(SEED_FILE_SCHEMAS);
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        DBUnitHelper.deleteData(SEED_FILE_SCHEMAS);
        DBUnitHelper.deleteData(SEED_FILE_SCHEMA_SETS);
        DBUnitHelper.deleteData(SEED_FILE_ATTRIBUTE);
    }
    
    @Test 
    public void testReleasedSchemaSets() throws ServiceException {
        SchemaSetFilter filterDummy = new SchemaSetFilter();
        
        SchemaSetFilter filterReleased = new SchemaSetFilter();
        filterReleased.setRegStatus(RegStatus.RELEASED.toString());
        
        SchemaSetsResult allSchemaSets = this.service.searchSchemaSets(filterDummy);
        SchemaSetsResult releasedSchemaSets = this.service.searchSchemaSets(filterReleased);
        
        assertEquals(2, allSchemaSets.getTotalItems());
        assertEquals(1, releasedSchemaSets.getTotalItems());
    }
}

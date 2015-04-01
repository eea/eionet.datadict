/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service;

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
    public void testDateModifiedEnhancement() throws ServiceException {
        SchemaSetFilter filterByDateModified = new SchemaSetFilter();
        filterByDateModified.setSortProperty("DATE_MODIFIED");
        filterByDateModified.setSortOrder(SortOrderEnum.ASCENDING);
        
        SchemaSetFilter filterByEnhancedDateModified = new SchemaSetFilter();
        filterByEnhancedDateModified.setDateModifiedEnhanced(true);
        filterByEnhancedDateModified.setSortProperty("DATE_MODIFIED_ENHANCED");
        filterByEnhancedDateModified.setSortOrder(SortOrderEnum.ASCENDING);
        
        SchemaSetsResult result1 = this.service.searchSchemaSets(filterByDateModified);
        SchemaSetsResult result2 = this.service.searchSchemaSets(filterByEnhancedDateModified);
        
        assertEquals(2, result1.getTotalItems());
        assertEquals("Sort order should not affect result set size", result1.getTotalItems(), result2.getTotalItems());
        
        assertEquals(261, result1.getList().get(0).getId());
        assertEquals(310, result2.getList().get(0).getId());
    }
}

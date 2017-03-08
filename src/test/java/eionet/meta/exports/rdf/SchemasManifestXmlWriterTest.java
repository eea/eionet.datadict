/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.rdf;

import eionet.meta.ActionBeanUtils;
import eionet.meta.service.DBUnitHelper;
import eionet.meta.service.ISchemaService;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Nikolaos Nakas
 */
@SpringApplicationContext("mock-spring-context.xml")
public class SchemasManifestXmlWriterTest extends UnitilsJUnit4 {
    
    private static enum XmlWriterType {
        SCHEMA,
        SCHEMA_SET
    }
    
    private static final String SEED_FILE_ATTRIBUTE = "seed-searchSchemaSets-attribute.xml";
    private static final String SEED_FILE_SCHEMA_SETS = "seed-searchSchemaSets-schemaSets.xml";
    private static final String SEED_FILE_SCHEMAS = "seed-searchSchemaSets-schemas.xml";
    
    private static final int SEED_SCHEMA_SETS_COUNT = 2;
    private static final int SEED_SCHEMAS_COUNT = 12;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        ActionBeanUtils.getServletContext();
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

    @SpringBeanByType
    private ISchemaService service;
    
    @Test
    public void testSchemaSetsWriteManifestXml() throws Exception {
        String schemaSetsRdf = this.generateManifestXml(XmlWriterType.SCHEMA_SET);
        
        int schemaSetCount = this.countOccurences(schemaSetsRdf, String.format("</%s:SchemaSet>", SchemasBaseManifestXmlWriter.DD_NS_PREFIX));
        assertEquals(SEED_SCHEMA_SETS_COUNT, schemaSetCount);
        
        int labelCount = this.countOccurences(schemaSetsRdf, String.format("</%s:label>", SchemasBaseManifestXmlWriter.RDFS_NS_PREFIX));
        assertEquals(SEED_SCHEMA_SETS_COUNT, labelCount);
        
        int hasSchemaCount = this.countOccurences(schemaSetsRdf, String.format("</%s:hasSchema>", SchemasBaseManifestXmlWriter.DD_NS_PREFIX));
        assertEquals(SEED_SCHEMAS_COUNT, hasSchemaCount);
    }
    
    @Test
    public void testSchemasWriteManifestXml() throws Exception {
        String schemasRdf = this.generateManifestXml(XmlWriterType.SCHEMA);
        
        int schemaCount = this.countOccurences(schemasRdf, String.format("</%s:XMLSchema>", SchemasBaseManifestXmlWriter.CR_NS_PREFIX));
        assertEquals(SEED_SCHEMAS_COUNT, schemaCount);
        
        int labelCount = this.countOccurences(schemasRdf, String.format("</%s:label>", SchemasBaseManifestXmlWriter.RDFS_NS_PREFIX));
        assertEquals(SEED_SCHEMAS_COUNT, labelCount);
    }
    
    private String generateManifestXml(XmlWriterType type) throws Exception {
        ByteArrayOutputStream out = null;
        
        try {
            out = new ByteArrayOutputStream();
            SchemasBaseManifestXmlWriter xmlWriter = this.createByType(type, out);
            xmlWriter.writeManifestXml();
            
            return new String(out.toByteArray(), SchemasBaseManifestXmlWriter.ENCODING);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    private SchemasBaseManifestXmlWriter createByType(XmlWriterType type, OutputStream out) {
        switch (type) {
            case SCHEMA:
                return new SchemasManifestXmlWriter(out, this.service);
            case SCHEMA_SET:
                return new SchemaSetsManifestXmlWriter(out, this.service);
            default:
                throw new IllegalArgumentException();
        }
    }
    
    private int countOccurences(String text, String search) {
        int fromIndex = 0;
        int foundIndex;
        int count = 0;
        
        while ((foundIndex = text.indexOf(search, fromIndex)) > -1) {
            count++;
            fromIndex = foundIndex + search.length();
        }
        
        return count;
    }
}

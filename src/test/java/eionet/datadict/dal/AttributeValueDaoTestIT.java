package eionet.datadict.dal;

import eionet.config.ApplicationTestContext;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.meta.service.DBUnitHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class
    })

public class AttributeValueDaoTestIT {

    @Autowired
    AttributeValueDao attributeValueDao;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-attributevaluesIT.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-attributevaluesIT.xml");
    }
    @Test
    public void testGetByAttributeAndOwner() {
        List<AttributeValue> actualAttrValues = attributeValueDao.getByAttributeAndOwner(38, new DataDictEntity(1301, DataDictEntity.Entity.DS));
        assertEquals(1, actualAttrValues.size());
    }

    @Test
    public void testDeleteAttributeValue() {
        attributeValueDao.deleteAttributeValue(38, new DataDictEntity(1301, DataDictEntity.Entity.DS), "Nature conservation and biodiversity");
        List<AttributeValue> actualAttrValues = attributeValueDao.getByAttributeAndOwner(38, new DataDictEntity(1301, DataDictEntity.Entity.DS));
        assertTrue(actualAttrValues.isEmpty());
    }

    @Test
    public void testDeleteAttributeValues() throws InterruptedException {
        attributeValueDao.deleteAllAttributeValues(41, new DataDictEntity(1694, DataDictEntity.Entity.T));
        List<AttributeValue> actualAttrValues = attributeValueDao.getByAttributeAndOwner(41, new DataDictEntity(1694, DataDictEntity.Entity.T));
        assertTrue(actualAttrValues.isEmpty());
    }

    @Test
    public void testAddAttributeValues() {
        attributeValueDao.addAttributeValues(543, new DataDictEntity(1700, DataDictEntity.Entity.T), Arrays.asList("value1", "value2"));
        List<AttributeValue> actualAttrValues = attributeValueDao.getByAttributeAndOwner(543, new DataDictEntity(1700, DataDictEntity.Entity.T));
        assertEquals(2, actualAttrValues.size());
    }

    @Test
    public void testGetByOwner() {
        List<AttributeValue> actualAttrValues = attributeValueDao.getByOwner(new DataDictEntity(2002, DataDictEntity.Entity.T));
        assertEquals(2, actualAttrValues.size());
    }

    @Test
    public void testAttributeValueRowMapperMapRow() {
        List<AttributeValue> actualAttrValues = attributeValueDao.getByAttributeAndOwner(41, new DataDictEntity(1729, DataDictEntity.Entity.T));
        AttributeValue attrValueToTest = actualAttrValues.get(0);
        assertEquals("KoizumiJunichiro1.jpg", attrValueToTest.getValue());
        assertThat(41,is(equalTo(attrValueToTest.getAttribute().getId())));
    }
}

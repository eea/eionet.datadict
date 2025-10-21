package eionet.datadict.dal;

import eionet.config.ApplicationTestContext;
import eionet.datadict.model.FixedValue;
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

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class
    })

public class FixedValuesDaoTestIT {

    @Autowired
    FixedValuesDao fixedValuesDao;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-fixedValues.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-fixedValues.xml");
    }
    @Test
    public void testGetValueListCodesOfDataElementsInTable() {
        List<FixedValue> values = this.fixedValuesDao.getValueListCodesOfDataElementsInTable(8880);
        assertEquals(2, values.size());
    }
}

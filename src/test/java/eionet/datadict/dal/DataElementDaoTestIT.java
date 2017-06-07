package eionet.datadict.dal;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import eionet.config.ApplicationTestContext;
import eionet.datadict.model.DataElement;
import eionet.meta.dao.domain.DatasetRegStatus;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT,
        value = "classpath:seed-dataelementIT.xml")
public class DataElementDaoTestIT {

    @Autowired
    DataElementDao dataElementDao;

    @Test
    public void testGetByID() {
        DataElement actualDataElement = dataElementDao.getById(31880);
        assertNotNull(actualDataElement);
        assertThat(actualDataElement.getId(), is(equalTo(31880)));
        assertThat(actualDataElement.getType(), is(equalTo(DataElement.DataElementType.CH2)));
        assertThat(actualDataElement.getNamespace().getId(), is(equalTo(1)));
        assertThat(actualDataElement.getShortName(), is(equalTo("HCO3")));
        assertThat(actualDataElement.getWorkingCopy(), is(equalTo(false)));
        assertThat(actualDataElement.getRegStatus(), is(equalTo(DatasetRegStatus.RELEASED)));
        assertThat(actualDataElement.getUser(), is(equalTo("staromar")));
        assertThat(actualDataElement.getVersion(), is(equalTo(1)));
        assertThat(actualDataElement.getDate(), is(equalTo(1193839)));
        assertThat(actualDataElement.getIdentifier(), is(equalTo("HCO3")));
    }

    @Test
    public void testGetParentTableId() {
        Integer actualParentTableId = dataElementDao.getParentTableId(31882);
        assertThat(actualParentTableId, is(equalTo(4582)));
    }

    @Test
    public void testGetDataElementsOfDatasetTable() {
        List<DataElement> expectedDataElements = dataElementDao.getDataElementsOfDatasetTable(4582);

        assertEquals(expectedDataElements.size(), 2);
        List<Integer> dataElementIDs = Arrays.asList(31880, 31882);

        for (DataElement expectedDataElement : expectedDataElements) {
            assertTrue(dataElementIDs.contains(expectedDataElement.getId()));
        }
    }
    
    @Test
    public void testDataElementRowMapperMapRow(){
    DataElement actualDataElement = dataElementDao.getById(31884);
        assertNotNull(actualDataElement);
        assertThat(actualDataElement.getId(), is(equalTo(31884)));
        assertThat(actualDataElement.getType(), is(equalTo(DataElement.DataElementType.CH2)));
        assertThat(actualDataElement.getNamespace().getId(), is(equalTo(1)));
        assertThat(actualDataElement.getShortName(), is(equalTo("HCO3")));
        assertThat(actualDataElement.getWorkingCopy(), is(equalTo(false)));
        assertThat(actualDataElement.getWorkingUser(),is(equalTo("kleinlau")));
        assertThat(actualDataElement.getRegStatus(), is(equalTo(DatasetRegStatus.RELEASED)));
        assertThat(actualDataElement.getUser(), is(equalTo("staromar")));
        assertThat(actualDataElement.getVersion(), is(equalTo(1)));
        assertThat(actualDataElement.getDate(), is(equalTo(1193839)));
        assertThat(actualDataElement.getIdentifier(), is(equalTo("HCO3")));
        assertThat(actualDataElement.getCheckedOutCopyId(),is(equalTo(36619)));
        assertThat(actualDataElement.getNamespace().getId(),is(equalTo(1)));
        assertThat(actualDataElement.getParentNS().getId(),is(equalTo(52)));
        assertThat(actualDataElement.getTopNS().getId(),is(equalTo(59)));

    }

}

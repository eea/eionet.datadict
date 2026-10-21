package eionet.datadict.services.data.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyInt;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;

@SpringApplicationContext("mock-spring-context.xml")
public class AttributeDataServiceTestIT extends UnitilsJUnit4 {

    @Mock
    Attribute attribute;
    
    @Mock
    AttributeDao attributeDao;
    
    @Mock
    AttributeValueDao attributeValueDao;
    
    @Mock
    IFixedValueDAO fixedValueDao;
    
    @Spy
    @InjectMocks
    AttributeDataServiceImpl attributeDataService;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGetAttribute() throws ResourceNotFoundException {
        Mockito.doReturn(attribute).when(attributeDao).getById(anyInt());
        assertNotNull(attributeDataService.getAttribute(0));
        Mockito.verify(attributeDao, times(1)).getById(0);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testGetAttributeNotFound() throws ResourceNotFoundException{
        Mockito.doReturn(null).when(attributeDao).getById(0);
        attributeDataService.getAttribute(0);
    }

    @Test
    public void testExists() {
        Mockito.doReturn(Boolean.TRUE).when(attributeDao).exists(anyInt());
        attributeDataService.existsAttribute(0);
        Mockito.verify(attributeDao, times(1)).exists(0);
    }

    @Test
    public void testGetAllAttributes() {
        attributeDataService.getAllAttributes();
        Mockito.verify(attributeDao, times(1)).getAll();
    }

    @Test
    public void testCreateAttribute() {
        Mockito.doReturn(1).when(attributeDao).create(attribute);
        attributeDataService.createAttribute(attribute);
        Mockito.verify(attributeDao, times(1)).create(attribute);
    }
    
    @Test
    public void testUpdateAttribute() {
        Mockito.doNothing().when(attributeDao).update(attribute);
        attributeDataService.updateAttribute(attribute);
        Mockito.verify(attributeDao, times(1)).update(attribute);
    }
    
    @Test
    public void testDeleteAttributeById() {
        Mockito.doNothing().when(attributeDao).deleteValues(0);
        Mockito.doNothing().when(attributeDao).delete(0);
        Mockito.doNothing().when(fixedValueDao).deleteAll(FixedValue.OwnerType.ATTRIBUTE, 0);
        attributeDataService.deleteAttributeById(0);
        Mockito.verify(attributeDao, times(1)).deleteValues(0);
        Mockito.verify(attributeDao, times(1)).delete(0);
        Mockito.verify(fixedValueDao, times(1)).deleteAll(FixedValue.OwnerType.ATTRIBUTE, 0);
    }
    
    @Test
    public void testCountAttributeValues() {
        Mockito.doReturn(2).when(attributeDao).countAttributeValues(0);
        attributeDataService.countAttributeValues(0);
        Mockito.verify(attributeDao, times(1)).countAttributeValues(0);
    }
    
    @Test
    public void testDeleteRelatedFixedValues() {
        Mockito.doNothing().when(fixedValueDao).deleteAll(FixedValue.OwnerType.ATTRIBUTE, 0);
        attributeDataService.deleteRelatedFixedValues(0);
        Mockito.verify(fixedValueDao).deleteAll(FixedValue.OwnerType.ATTRIBUTE, 0);
    }
    
    @Test
    public void testFixedValues() {
        Mockito.doReturn(new ArrayList<FixedValue>()).when(fixedValueDao).getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, 0);
        attributeDataService.getFixedValues(0);
        Mockito.verify(fixedValueDao, times(1)).getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, 0);
    }

}

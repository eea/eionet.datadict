package eionet.datadict.services.impl.data;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class AttributeDataServiceTest {

    @Mock
    VocabularyFolder vocabulary;
    
    @Mock
    Attribute attribute;
    
    @Mock
    AttributeDao attributeDao;
    
    @Mock
    VocabularyDao vocabularyDao;
    
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
    public void testGetAttributeWithNoVocabulary() throws ResourceNotFoundException {
        Mockito.doReturn(attribute).when(attributeDao).getById(anyInt());
        Mockito.doReturn(null).when(attribute).getVocabulary();
        assertNotNull(attributeDataService.getAttribute(0));
        Mockito.verify(attributeDao, times(1)).getById(0);
    }
    
    @Test
    public void testGetAttributeWithVocabulary() throws ResourceNotFoundException {
        Mockito.doReturn(attribute).when(attributeDao).getById(anyInt());
        Mockito.doReturn(vocabulary).when(attribute).getVocabulary();
        Mockito.doReturn(vocabulary).when(vocabularyDao).getPlainVocabularyById(anyInt());
        Mockito.doNothing().when(attribute).setVocabulary(any(VocabularyFolder.class));
        assertNotNull(attributeDataService.getAttribute(0));
        Mockito.verify(attributeDao).getById(0);
        Mockito.verify(vocabularyDao).getPlainVocabularyById(0);
        Mockito.verify(attribute).setVocabulary(vocabulary);
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
    public void testCreateAttribute() {
        Mockito.doReturn(1).when(attributeDao).create(attribute);
        attributeDataService.createAttribute(attribute);
        Mockito.verify(attributeDao, times(1)).create(attribute);
    }
    
    @Test
    public void testUpdateAttributeWithNullVocabulary() {
        Mockito.doNothing().when(attributeDao).update(attribute);
        Mockito.doReturn(null).when(attribute).getVocabulary();
        Mockito.doNothing().when(attributeDao).deleteVocabularyBinding(anyInt());
        Mockito.doReturn(0).when(attribute).getId();
        attributeDataService.updateAttribute(attribute);
        Mockito.verify(attributeDao, times(1)).update(attribute);
        Mockito.verify(attributeDao, times(1)).deleteVocabularyBinding(0);
    }
    
    @Test
    public void testUpdateAttributeWithVocabulary() {
        Mockito.doNothing().when(attributeDao).update(attribute);
        Mockito.doReturn(vocabulary).when(attribute).getVocabulary();
        Mockito.doNothing().when(attributeDao).updateVocabularyBinding(anyInt(), anyInt());
        Mockito.doReturn(0).when(attribute).getId();
        Mockito.doReturn(0).when(vocabulary).getId();
        
        attributeDataService.updateAttribute(attribute);
        
        Mockito.verify(attributeDao, times(1)).update(attribute);
        Mockito.verify(attributeDao, times(1)).updateVocabularyBinding(0, 0);
    }
    
    @Test
    public void testDeleteAttributeById() {
        Mockito.doNothing().when(attributeDao).deleteValues(0);
        Mockito.doNothing().when(attributeDao).deleteVocabularyBinding(0);
        Mockito.doNothing().when(attributeDao).delete(0);
        Mockito.doNothing().when(fixedValueDao).deleteAll(FixedValue.OwnerType.ATTRIBUTE, 0);
        attributeDataService.deleteAttributeById(0);
        Mockito.verify(attributeDao, times(1)).deleteValues(0);
        Mockito.verify(attributeDao, times(1)).deleteVocabularyBinding(0);
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
    public void testSetNewVocabularyToAttributeObject() throws ResourceNotFoundException {
        Mockito.doNothing().when(attribute).setVocabulary(any(VocabularyFolder.class));
        Mockito.doReturn(vocabulary).when(vocabularyDao).getPlainVocabularyById(0);
        attributeDataService.setNewVocabularyToAttributeObject(attribute, 0);
        Mockito.verify(attribute).setVocabulary(vocabulary);
    }
    
    @Test
    public void testDeleteVocabularyBInding() {
        Mockito.doNothing().when(attributeDao).deleteVocabularyBinding(0);
        attributeDataService.deleteVocabularyBinding(0);
        Mockito.verify(attributeDao, times(1)).deleteVocabularyBinding(0);
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

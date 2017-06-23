package eionet.datadict.orm;

import eionet.datadict.orm.testmodel.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isIn;
import org.junit.Test;

public class OrmUtilsTest {

    @Test
    public void testSortById() {
        List<ParentEntityWithReferenceToChildren> entities = new ArrayList<ParentEntityWithReferenceToChildren>();
        long[] ids = new long[] { 5, 4, 3, 2, 1 };
        
        for (long id : ids) {
            entities.add(new ParentEntityWithReferenceToChildren(id));
        }
        
        Arrays.sort(ids);
        OrmUtils.sortById(entities);
        
        for (int i = 0; i < ids.length; i++) {
            long id = ids[i];
            ParentEntityWithReferenceToChildren entity = entities.get(i);
            assertThat(entity.getId(), is(equalTo(id)));
        }
    }
    
    @Test
    public void testLinkOneToMany() {
        Map<Long, Set<Long>> ids = new HashMap<Long, Set<Long>>();
        ids.put(5L, new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 4L, 5L)));
        ids.put(4L, new HashSet<Long>(Arrays.asList(6L, 7L)));
        ids.put(3L, new HashSet<Long>());
        ids.put(2L, new HashSet<Long>(Arrays.asList(8L, 9L, 10L)));
        ids.put(1L, new HashSet<Long>(Arrays.asList(11L, 12L, 13L, 14L)));
        
        List<ParentEntityWithReferenceToChildren> parentEntities1 = new ArrayList<ParentEntityWithReferenceToChildren>();
        List<ChildEntityReferencedByParent> childEntities1 = new ArrayList<ChildEntityReferencedByParent>();
        this.generateLinkInputOneToManyWithReference(ids, parentEntities1, childEntities1);
        OrmUtils.link(parentEntities1, childEntities1, "parent");
        this.assertOneToManyWithReferenceLinking(ids, parentEntities1);
        
        List<ParentEntityWithoutReferenceToChildren> parentEntities2 = new ArrayList<ParentEntityWithoutReferenceToChildren>();
        List<ChildEntityUnreferencedByParent> childEntities2 = new ArrayList<ChildEntityUnreferencedByParent>();
        this.generateLinkInputOneToManyWithoutReference(ids, parentEntities2, childEntities2);
        OrmUtils.link(parentEntities2, childEntities2, "parent");
        this.assertOneToManyWithoutReferenceLinking(ids, parentEntities2, childEntities2);
    }
    
    @Test
    public void testLinkOneToOne() {
        Map<Long, Long> ids = new HashMap<Long, Long>();
        ids.put(1L, 15L);
        ids.put(2L, 14L);
        ids.put(3L, 13L);
        ids.put(4L, 12L);
        ids.put(5L, 11L);
        
        List<ParentEntityWithReferenceToChildren> parentEntities1 = new ArrayList<ParentEntityWithReferenceToChildren>();
        List<ParentExtentionEntityReferencedByParent> childEntities1 = new ArrayList<ParentExtentionEntityReferencedByParent>();
        this.generateLinkInputOneToOneWithReference(ids, parentEntities1, childEntities1);
        OrmUtils.link(parentEntities1, childEntities1, "parent");
        this.assertOneToOneWithReferenceLinking(ids, parentEntities1);
        
        List<ParentEntityWithoutReferenceToChildren> parentEntities2 = new ArrayList<ParentEntityWithoutReferenceToChildren>();
        List<ParentExtentionEntityUnreferencedByParent> childEntities2 = new ArrayList<ParentExtentionEntityUnreferencedByParent>();
        this.generateLinkInputOneToOneWithoutReference(ids, parentEntities2, childEntities2);
        OrmUtils.link(parentEntities2, childEntities2, "parent");
        this.assertOneToOneWithoutReferenceLinking(ids, parentEntities2, childEntities2);
    }
    
    @Test
    public void testLinkOneToManyPerformance() {
        this.testLinkOneToManyPerformance(100, 10);
        this.testLinkOneToManyPerformance(1000, 10);
        this.testLinkOneToManyPerformance(10000, 10);
        this.testLinkOneToManyPerformance(100000, 10);
    }
    
    private void generateLinkInputOneToManyWithReference(Map<Long, Set<Long>> ids, 
            List<ParentEntityWithReferenceToChildren> parentEntities, List<ChildEntityReferencedByParent> childEntities) {
        for (Long parentId : ids.keySet()) {
            parentEntities.add(new ParentEntityWithReferenceToChildren(parentId));
            ParentEntityWithReferenceToChildren parentOfChild = new ParentEntityWithReferenceToChildren(parentId);
            
            for (Long childId : ids.get(parentId)) {
                childEntities.add(new ChildEntityReferencedByParent(childId, parentOfChild));
            }
        }
    }
    
    private void generateLinkInputOneToManyWithoutReference(Map<Long, Set<Long>> ids, 
            List<ParentEntityWithoutReferenceToChildren> parentEntities, List<ChildEntityUnreferencedByParent> childEntities) {
        for (Long parentId : ids.keySet()) {
            parentEntities.add(new ParentEntityWithoutReferenceToChildren(parentId));
            ParentEntityWithoutReferenceToChildren parentOfChild = new ParentEntityWithoutReferenceToChildren(parentId);
            
            for (Long childId : ids.get(parentId)) {
                childEntities.add(new ChildEntityUnreferencedByParent(childId, parentOfChild));
            }
        }
    }
    
    private void generateLinkInputOneToOneWithReference(Map<Long, Long> ids,
            List<ParentEntityWithReferenceToChildren> parentEntities, List<ParentExtentionEntityReferencedByParent> childEntities) {
        for (Long parentId : ids.keySet()) {
            parentEntities.add(new ParentEntityWithReferenceToChildren(parentId));
            ParentEntityWithReferenceToChildren parentOfChild = new ParentEntityWithReferenceToChildren(parentId);
            childEntities.add(new ParentExtentionEntityReferencedByParent(ids.get(parentId), parentOfChild));
        }
    }
    
    private void generateLinkInputOneToOneWithoutReference(Map<Long, Long> ids,
            List<ParentEntityWithoutReferenceToChildren> parentEntities, List<ParentExtentionEntityUnreferencedByParent> childEntities) {
        for (Long parentId : ids.keySet()) {
            parentEntities.add(new ParentEntityWithoutReferenceToChildren(parentId));
            ParentEntityWithoutReferenceToChildren parentOfChild = new ParentEntityWithoutReferenceToChildren(parentId);
            childEntities.add(new ParentExtentionEntityUnreferencedByParent(ids.get(parentId), parentOfChild));
        }
    }
    
    private void assertOneToManyWithReferenceLinking(Map<Long, Set<Long>> ids, List<ParentEntityWithReferenceToChildren> parentEntities) {
        for (ParentEntityWithReferenceToChildren parent : parentEntities) {
            Set<Long> childIds = ids.get(parent.getId());
            
            if (childIds.isEmpty()) {
                assertThat(parent.getChildren(), is(anyOf(nullValue(), empty())));
                continue;
            }
            
            assertThat(String.format("Null child collection for parent with id %d", parent.getId()), 
                    parent.getChildren(), is(notNullValue()));
            assertThat(String.format("Child count mismatch for parent with id", parent.getId()), 
                    parent.getChildren().size(), is(equalTo(childIds.size())));
            
            for (ChildEntityReferencedByParent child : parent.getChildren()) {
                assertThat(String.format("Child with id %d should not be linked to parent with id %d.", child.getId(), parent.getId()), 
                        child.getId(), isIn(childIds));
                assertThat(String.format("Parent entity is not identical to the linked on in child with id %d", child.getId()),
                        child.getParent(), is(sameInstance(parent)));
            }
        }
    }
    
    private void assertOneToManyWithoutReferenceLinking(Map<Long, Set<Long>> ids, 
            List<ParentEntityWithoutReferenceToChildren> parentEntities, List<ChildEntityUnreferencedByParent> childEntities) {
        Map<Long, ParentEntityWithoutReferenceToChildren> parents = new HashMap<Long, ParentEntityWithoutReferenceToChildren>();
        
        for (ParentEntityWithoutReferenceToChildren parent : parentEntities) {
            parents.put(parent.getId(), parent);
        }
        
        for (ChildEntityUnreferencedByParent child : childEntities) {
            assertThat(String.format("Parent of child with id %d should have been set.", child.getId()), 
                    child.getParent(), is(notNullValue()));
            Set<Long> childIds = ids.get(child.getParent().getId());
            assertThat(String.format("Child with id %d is not child of parent with id %d", child.getId(), child.getParent().getId()), 
                    childIds, hasItem(child.getId()));
            assertThat(String.format("Parent entity is not identical to the linked on in child with id %d", child.getId()),
                    child.getParent(), is(sameInstance(parents.get(child.getParent().getId()))));
        }
    }
    
    private void assertOneToOneWithReferenceLinking(Map<Long, Long> ids, List<ParentEntityWithReferenceToChildren> parentEntities) {
        for (ParentEntityWithReferenceToChildren parent : parentEntities) {
            Long childId = ids.get(parent.getId());
            assertThat(String.format("Null extension for parent with id %d.", parent.getId()), 
                    parent.getExtension(), is(notNullValue()));
            assertThat(String.format("Child with id %d should not be linked to parent with id %d.", parent.getExtension().getId(), parent.getId()),
                    parent.getExtension().getId(), is(equalTo(childId)));
            assertThat(String.format("Parent entity is not identical to the linked on in child with id %d", parent.getExtension().getId()),
                    parent.getExtension().getParent(), is(sameInstance(parent)));
        }
    }
    
    private void assertOneToOneWithoutReferenceLinking(Map<Long, Long> ids, 
            List<ParentEntityWithoutReferenceToChildren> parentEntities, List<ParentExtentionEntityUnreferencedByParent> childEntities) {
        Map<Long, ParentEntityWithoutReferenceToChildren> parents = new HashMap<Long, ParentEntityWithoutReferenceToChildren>();
        
        for (ParentEntityWithoutReferenceToChildren parent : parentEntities) {
            parents.put(parent.getId(), parent);
        }
        
        for (ParentExtentionEntityUnreferencedByParent child : childEntities) {
            assertThat(String.format("Null parent for extension with id %d.", child.getId()), 
                    child.getParent(), is(notNullValue()));
            assertThat(String.format("Child with id %d should not be linked to parent with id %d.", child.getId(), child.getParent().getId()),
                    child.getId(), is(equalTo(ids.get(child.getParent().getId()))));
            assertThat(String.format("Parent entity is not identical to the linked on in child with id %d", child.getId()),
                    child.getParent(), is(sameInstance(parents.get(child.getParent().getId()))));
        }
    }
    
    private void testLinkOneToManyPerformance(int parentSize, int childSize) {
        System.out.printf("Testing %d parent entities with %d child entities each.%n", parentSize, childSize);
        
        Map<Long, Set<Long>> ids = this.generateLinkInputMap(parentSize, childSize);
        List<ParentEntityWithReferenceToChildren> parentEntities = new ArrayList<ParentEntityWithReferenceToChildren>();
        List<ChildEntityReferencedByParent> childEntities = new ArrayList<ChildEntityReferencedByParent>();
        this.generateLinkInputOneToManyWithReference(ids, parentEntities, childEntities);
        
        long timeStart = System.currentTimeMillis();
        OrmUtils.link(parentEntities, childEntities, "parent");
        long timeEnd = System.currentTimeMillis();
        
        System.out.printf("link one to many duration: %d ms.%n", timeEnd - timeStart);
        
        this.assertOneToManyWithReferenceLinking(ids, parentEntities);
    }
    
    private Map<Long, Set<Long>> generateLinkInputMap(int parentSize, int childSize) {
        Map<Long, Set<Long>> ids = new HashMap<Long, Set<Long>>();
        long childId = 0;
        
        for (long parentId = parentSize - 1; parentId >= 0; parentId--) {
            Set<Long> childIds = new HashSet<Long>(childSize);
            
            for (int j = 0; j < childSize; j++) {
                childIds.add(childId++);
            }
            
            ids.put(parentId, childIds);
        }
        
        return ids;
    }
    
}

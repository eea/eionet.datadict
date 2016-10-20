package eionet.datadict.orm;

import eionet.datadict.orm.testmodel.*;
import java.lang.reflect.Field;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

public class OrmReflectionUtilsTest {
    
    @Test
    public void testGetIdField() {
        Field f = OrmReflectionUtils.getIdField(ParentEntityWithReferenceToChildren.class);
        assertThat(f, is(notNullValue()));
        assertThat(f.getName(), is(equalTo("id")));
        
        try {
            OrmReflectionUtils.getIdField(EntityWithoutId.class);
            fail("Should have thrown NoSuchPropertyException");
        }
        catch (NoSuchPropertyException ex) { }
        
        try {
            OrmReflectionUtils.getIdField(EntityWithMultipleIds.class);
            fail("Should have thrown AmbiguousPropertyMatchException");
        }
        catch(AmbiguousMatchException ex) { }
    }
    
    @Test
    public void testGetRelationInfo() {
        RelationInfo info1 = OrmReflectionUtils.getParentChildRelationInfo(ParentEntityWithReferenceToChildren.class, ChildEntityReferencedByParent.class, "parent");
        assertThat(info1, is(notNullValue()));
        assertThat(info1.getParentEndpoint(), is(notNullValue()));
        assertThat(info1.getChildEndpoint(), is(notNullValue()));
        assertThat(info1.getParentEndpoint().getName(), is(equalTo("children")));
        assertThat(info1.getChildEndpoint().getName(), is(equalTo("parent")));
        
        try {
            OrmReflectionUtils.getParentChildRelationInfo(ParentEntityWithReferenceToChildren.class, ChildEntityReferencedByParent.class, "parentttt");
            fail("Should have thrown NoSuchPropertyException");
        }
        catch (NoSuchPropertyException ex) { }
        
        RelationInfo info2 = OrmReflectionUtils.getParentChildRelationInfo(ParentEntityWithoutReferenceToChildren.class, ChildEntityUnreferencedByParent.class, "parent");
        assertThat(info2, is(notNullValue()));
        assertThat(info2.getParentEndpoint(), is(nullValue()));
        assertThat(info2.getChildEndpoint(), is(notNullValue()));
        assertThat(info2.getChildEndpoint().getName(), is(equalTo("parent")));
        
        RelationInfo info3 = OrmReflectionUtils.getParentChildRelationInfo(ParentEntityWithReferenceToChildren.class, ParentExtentionEntityReferencedByParent.class, "parent");
        assertThat(info3, is(notNullValue()));
        assertThat(info3.getParentEndpoint(), is(notNullValue()));
        assertThat(info3.getChildEndpoint(), is(notNullValue()));
        assertThat(info3.getParentEndpoint().getName(), is(equalTo("extension")));
        assertThat(info3.getChildEndpoint().getName(), is(equalTo("parent")));
        
        RelationInfo info4 = OrmReflectionUtils.getParentChildRelationInfo(ParentEntityWithoutReferenceToChildren.class, ParentExtentionEntityUnreferencedByParent.class, "parent");
        assertThat(info4, is(notNullValue()));
        assertThat(info4.getParentEndpoint(), is(nullValue()));
        assertThat(info4.getChildEndpoint(), is(notNullValue()));
        assertThat(info4.getChildEndpoint().getName(), is(equalTo("parent")));
    }
    
    @Test
    public void testInferRelationInfo() {
        RelationInfo info1 = OrmReflectionUtils.inferParentChildRelationInfo(ParentEntityWithReferenceToChildren.class, ChildEntityReferencedByParent.class);
        assertThat(info1, is(notNullValue()));
        assertThat(info1.getParentEndpoint(), is(notNullValue()));
        assertThat(info1.getChildEndpoint(), is(notNullValue()));
        assertThat(info1.getParentEndpoint().getName(), is(equalTo("children")));
        assertThat(info1.getChildEndpoint().getName(), is(equalTo("parent")));
        
        try {
            OrmReflectionUtils.inferParentChildRelationInfo(ParentEntityWithReferenceToChildren.class, ChildEntityUnreferencedByParent.class);
            fail("Should have thrown InvalidRelationException");
        }
        catch (InvalidRelationException ex) { }
        
        try {
            OrmReflectionUtils.inferParentChildRelationInfo(EntityWithSiblings.class, EntityWithSiblings.class);
            fail("Should have thrown AmbiguousMatchException");
        }
        catch (AmbiguousMatchException ex) { }
        
        RelationInfo info2 = OrmReflectionUtils.inferParentChildRelationInfo(ParentEntityWithoutReferenceToChildren.class, ChildEntityUnreferencedByParent.class);
        assertThat(info2, is(notNullValue()));
        assertThat(info2.getParentEndpoint(), is(nullValue()));
        assertThat(info2.getChildEndpoint(), is(notNullValue()));
        assertThat(info2.getChildEndpoint().getName(), is(equalTo("parent")));
        
        RelationInfo info3 = OrmReflectionUtils.inferParentChildRelationInfo(ParentEntityWithReferenceToChildren.class, ParentExtentionEntityReferencedByParent.class);
        assertThat(info3, is(notNullValue()));
        assertThat(info3.getParentEndpoint(), is(notNullValue()));
        assertThat(info3.getChildEndpoint(), is(notNullValue()));
        assertThat(info3.getParentEndpoint().getName(), is(equalTo("extension")));
        assertThat(info3.getChildEndpoint().getName(), is(equalTo("parent")));
        
        RelationInfo info4 = OrmReflectionUtils.inferParentChildRelationInfo(ParentEntityWithoutReferenceToChildren.class, ParentExtentionEntityUnreferencedByParent.class);
        assertThat(info4, is(notNullValue()));
        assertThat(info4.getParentEndpoint(), is(nullValue()));
        assertThat(info4.getChildEndpoint(), is(notNullValue()));
        assertThat(info4.getChildEndpoint().getName(), is(equalTo("parent")));
    }
    
}

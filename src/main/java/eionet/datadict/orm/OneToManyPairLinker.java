package eionet.datadict.orm;

import java.util.Collection;

class OneToManyPairLinker<T, S> implements OneToAnyPairLinker<T, S> {

    private Collection<S> childCollection;
    
    @Override
    public void onPairMatch(T parent, S child, RelationInfo relationInfo) {
        if (relationInfo.isBidirectional() && childCollection == null) {
            childCollection = OrmCollectionUtils.createChildCollection((Class<S>) child.getClass());
            OrmReflectionUtils.writeField(relationInfo.getParentEndpoint(), parent, childCollection);
        }

        if (relationInfo.isBidirectional()) {
            childCollection.add(child);
        }
        
        OrmReflectionUtils.writeField(relationInfo.getChildEndpoint(), child, parent);
    }
    
    @Override
    public void onPairMismatch() {
        this.childCollection = null;
    }
    
}

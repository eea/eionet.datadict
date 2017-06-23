package eionet.datadict.orm;

class OneToOnePairLinker<T, S> implements OneToAnyPairLinker<T, S> {

    @Override
    public void onPairMatch(T parent, S child, RelationInfo relationInfo) {
        if (relationInfo.isBidirectional()) {
            OrmReflectionUtils.writeField(relationInfo.getParentEndpoint(), parent, child);
        }
        
        OrmReflectionUtils.writeField(relationInfo.getChildEndpoint(), child, parent);
    }

    @Override
    public void onPairMismatch() {
        
    }
    
}

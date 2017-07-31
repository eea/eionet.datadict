package eionet.datadict.orm;

interface OneToAnyPairLinker<T, S> {

    void onPairMatch(T parent, S child, RelationInfo relationInfo);
    
    void onPairMismatch();
    
}

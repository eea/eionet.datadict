package eionet.datadict.resources;

public class ResourceDbIdInfo implements ResourceIdInfo {

    private Number id;
    
    public ResourceDbIdInfo(Number id) {
        this.id = id;
    }
    
    @Override
    public String getIdDescription() {
        return "id: " + id.toString();
    }
    
}

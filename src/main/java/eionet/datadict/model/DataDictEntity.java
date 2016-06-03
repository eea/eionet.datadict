
package eionet.datadict.model;

public class DataDictEntity {
    
    public static enum Entity  {
        E("Element"),
        T("Table"),
        DS("Dataset"),
        SCH("Schema"),
        SCS("Schema set"),
        VCF("Vocabulary");
        
        private final String label;
        
        private Entity(String label) {
            this.label = label;
        }
        
        public String getLabel(){
            return this.label;
        }
    }
    
    private Integer id;
    private Entity type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Entity getType() {
        return type;
    }

    public void setType(Entity type) {
        this.type = type;
    }
}

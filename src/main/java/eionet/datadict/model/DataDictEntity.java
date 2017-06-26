
package eionet.datadict.model;

import org.apache.commons.lang.StringUtils;

public class DataDictEntity {
    
    public DataDictEntity(Integer id, Entity type) {
        this.id = id;
        this.type = type;
    }
    
    public static enum Entity  {
        E("DataElement"),
        T("Table"),
        DS("Dataset"),
        SCH("Schema"),
        SCS("Schemaset"),
        VCF("Vocabulary");
        
        private final String label;
        
        private Entity(String label) {
            this.label = label;
        }
        
        public String getLabel(){
            return this.label;
        }
        
        public static Entity getFromString(String stringLabel) {
            for (Entity entity : Entity.values()){
                if (StringUtils.equalsIgnoreCase(entity.name(), stringLabel)) {
                    return entity;
                } 
                if (StringUtils.equalsIgnoreCase(entity.getLabel(), stringLabel)) {
                    return entity;
                }
            }
            return null;
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

package eionet.datadict.model;

import java.util.Set;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public abstract class SimpleAttribute {

    public static enum ObligationType {
        MANDATORY,
        OPTIONAL,
        CONDITIONAL
    }
    
    public static enum DisplayType {
        NONE,
        TEXT_BOX,
        TEXT_AREA,
        IMAGE,
        DROPDOWN_FIXED_VALUES
    }
    
    public static enum InheritanceMode {
        NONE,
        INHERIT_ADD,
        INHERIT_OVERRIDE
    }
    
    @Id
    private Integer id;
    private String shortName;
    private String name;
    private String definition;
    private ObligationType obligation;
    private boolean displayMultiple;
    private InheritanceMode inheritanceMode;
    private Set<SimpleAttributeOwnerCategory> targetEntities;
    private Integer displayOrder;
    private Integer displayWidth;
    
    @ManyToOne
    private Namespace namespace;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public ObligationType getObligation() {
        return obligation;
    }

    public void setObligation(ObligationType obligation) {
        this.obligation = obligation;
    }

    public boolean isDisplayMultiple() {
        return displayMultiple;
    }

    public void setDisplayMultiple(boolean displayMultiple) {
        this.displayMultiple = displayMultiple;
    }

    public InheritanceMode getInheritanceMode() {
        return inheritanceMode;
    }

    public void setInheritanceMode(InheritanceMode inheritanceMode) {
        this.inheritanceMode = inheritanceMode;
    }

    public Set<SimpleAttributeOwnerCategory> getTargetEntities() {
        return targetEntities;
    }

    public void setTargetEntities(Set<SimpleAttributeOwnerCategory> targetEntities) {
        this.targetEntities = targetEntities;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(Integer displayWidth) {
        this.displayWidth = displayWidth;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }
    
    public abstract DisplayType getDisplayType();
    
    public abstract boolean supportsValueList();
    
    public abstract Iterable<? extends ValueListItem> getValueList();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof SimpleAttribute)) {
            return false;
        }
        
        if (this.id == null) {
            return false;
        }
        
        SimpleAttribute other = (SimpleAttribute) obj;
        
        return this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.id == null ?  super.hashCode() : this.id.hashCode();
    }
    
}

package eionet.datadict.action.attribute;

import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.model.enums.Enumerations.DisplayForType;
import eionet.datadict.model.enums.Enumerations.Inherit;
import eionet.meta.dao.domain.FixedValue;
import java.util.List;

/**
 *
 * @author eworx-alk
 */
public final class AttributeViewModel {
    
    private final List<DisplayForType> allDisplayForTypes = DisplayForType.getAllEnums();
    private final List<Inherit> allInherits = Inherit.getAllEnums();

    public List<DisplayForType> getAllDisplayForTypes() {
        return allDisplayForTypes;
    }

    public List<Inherit> getAllInherits() {
        return allInherits;
    }
    
    private List<RdfNamespace> allRdfNamespaces;
    private String submitActionBeanName;
    private String displayOrder;
    private String rdfNamespaceId;
    private AttributeDefinition attributeDefinition;
    private List<DisplayForType> displayForTypes;
    private List<FixedValue> fixedValues; 
    private List<Namespace> namespaces;
    
    
    public String getRdfNamespaceId() {
        return rdfNamespaceId;
    }

    public void setRdfNamespaceId(String rdfNamespaceId) {
        this.rdfNamespaceId = rdfNamespaceId;
    }
    
    public List<RdfNamespace> getAllRdfNamespaces() {
        return allRdfNamespaces;
    }

    public void setAllRdfNamespaces(List<RdfNamespace> allRdfNamespaces) {
        this.allRdfNamespaces = allRdfNamespaces;
    }
    
    public String getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(String displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public String getSubmitActionBeanName() {
        return submitActionBeanName;
    }

    public void setSubmitActionBeanName(String submitActionBean) {
        this.submitActionBeanName = submitActionBean;
    }
    
    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public List<DisplayForType> getDisplayForTypes() {
        return displayForTypes;
    }

    public void setDisplayForTypes(List<DisplayForType> displayForTypes) {
        this.displayForTypes = displayForTypes;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }
    
}

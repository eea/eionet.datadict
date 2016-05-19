package eionet.datadict.action.attribute;

import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.model.enums.Enumerations.AttributeDisplayType;
import eionet.datadict.model.enums.Enumerations.DisplayForType;
import eionet.datadict.model.enums.Enumerations.Inherit;
import eionet.datadict.model.enums.Enumerations.Obligation;
import eionet.meta.dao.domain.FixedValue;
import java.util.List;

/**
 *
 * @author Aliki Kopaneli
 */
public final class AttributeViewModel {
   
    private AttributeDefinition attributeDefinition;
    
    /**
     * The list of all the existing RDF Namespaces
     */
    private List<RdfNamespace> allRdfNamespaces;
    
    /**
     * The list of all the namespaces related to Attributes
     */
    private List<Namespace> namespaces;
    
    /**
     * The actionBean to handle the submit form
     */
    private String submitActionBeanName;
    
    private String displayOrder;
    private String rdfNamespaceId;
    
    private List<DisplayForType> displayForTypes;
    private List<FixedValue> fixedValues; 
    
    //Hold info about the enumerations
    private final List<DisplayForType> allDisplayForTypes = DisplayForType.getAllEnums();
    private final List<Inherit> allInherits = Inherit.getAllEnums();
    private final String obligationClass = Obligation.M.getClass().getName();
    private final String displayTypeClass = AttributeDisplayType.SELECT.getClass().getName();
    
    
    public String getObligationClass() {
        return obligationClass;
    }

    public String getDisplayTypeClass() {
        return displayTypeClass;
    }

    public List<DisplayForType> getAllDisplayForTypes() {
        return allDisplayForTypes;
    }

    public List<Inherit> getAllInherits() {
        return allInherits;
    }

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

package eionet.meta.service.data;

import java.util.List;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;

/**
 *
 * Interface for objects that are related with dynamic attributes. The interface provides methods for setting and getting
 * attributes.
 *
 * @author Enriko Käsper
 */
public interface IObjectWithDynamicAttrs {

    /**
     * @return the attributes
     */
    public List<Attribute> getAttributes();

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(List<Attribute> attributes);

    /**
     * @return the complex attributes
     */
    public List<ComplexAttribute> getComplexAttributes();

    /**
     * @param attributes the complex attributes to set
     */
    public void setComplexAttributes(List<ComplexAttribute> complexAttributes);
}

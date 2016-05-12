package eionet.datadict.dal;

import eionet.datadict.model.AttributeDefinition;
import java.util.List;

/**
 *
 * @author eworx-alk
 */
public interface AttributeDefinitionDAO {

    public List<AttributeDefinition> getAttributes();

    public AttributeDefinition getAttributeDefinitionById(int id);
}

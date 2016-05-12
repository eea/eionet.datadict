package eionet.datadict.dal;

import eionet.datadict.model.Attribute;
import java.util.List;



/**
 *
 * @author eworx-alk
 */
public interface AttributeDAO {
    public List<Attribute> getAttributeByDeclarationId(int id);
}

package eionet.meta.dao;

import eionet.meta.DElemAttribute;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public interface IAttributeDAO {

    /**
     * 
     * @param parentId
     * @param parentType
     * @param newParentId
     */
    void copySimpleAttributes(int parentId, String parentType, int newParentId);

    /**
     * 
     * @param replacedId
     * @param substituteId
     * @param parentType
     */
    void replaceParentId(int replacedId, int substituteId, DElemAttribute.ParentType parentType);
}

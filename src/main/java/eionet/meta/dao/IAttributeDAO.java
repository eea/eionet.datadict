package eionet.meta.dao;

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
}

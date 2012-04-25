package eionet.meta.dao;

import java.util.List;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.domain.Attribute;

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
     * Deletes all attributes of given parent ids.
     *
     * @param parentIds
     * @param parentType
     */
    void deleteAttributes(List<Integer> parentIds, String parentType);

    /**
     *
     * @param replacedId
     * @param substituteId
     * @param parentType
     */
    void replaceParentId(int replacedId, int substituteId, DElemAttribute.ParentType parentType);

    /**
     * Wraps the DDSearchEngine functionality for getting attributes meta data.
     *
     * @param parentType
     * @param elementType
     * @return
     * @throws DAOException
     */
    List<Attribute> getAttributes(DElemAttribute.ParentType parentType, String elementType) throws DAOException;
}

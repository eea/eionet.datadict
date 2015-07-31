/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Raptis Dimos
 */

package eionet.meta.dao;

import eionet.meta.dao.domain.FixedValue;


/**
 *  Interface for FixedValue DAO
 */
public interface IFixedValueDAO {
    
    /**
     * Creates a new fixed value
     * @param fixedValue
     */
    public void create(FixedValue fixedValue);
    
    /**
     * Deletes an existing fixed value
     * @param fixedValue
     */
    public void delete(FixedValue fixedValue);
    
    /**
     * Deletes all fixed values of a specific owner.
     * 
     * @param ownerType the type of the owner
     * @param ownerId the id of the owner
     */
    void deleteAll(FixedValue.OwnerType ownerType, int ownerId);
    
    /**
     * Updates an existing fixed value
     * @param fixedValue
     */
    public void update(FixedValue fixedValue);
    
    /**
     * Returns fixed value with specific id
     * @param id
     * @return fixed value
     */
    public FixedValue getById(int id);
    
    /**
     * Gets a fixed value by value and based on the owner specified.
     * 
     * @param ownerType the type of the owner
     * @param ownerId the id of the owner
     * @param value the value attribute of the fixed value
     * @return a fixed value matching the specified criteria; null otherwise
     */
    public FixedValue getByValue(FixedValue.OwnerType ownerType, int ownerId, String value);
    
    /**
     * Checks existence of specific fixed value
     * @param id
     * @return fixed value
     */
    public boolean exists(int id);
    
    /**
     * Checks if fixed value exists with same owner, name
     * 
     * @param ownerType the type of the owner
     * @param ownerId the id of the owner
     * @param value the value attribute of the fixed value
     * @return true if found; false otherwise
     */
    public boolean existsWithSameNameOwner(FixedValue.OwnerType ownerType, int ownerId, String value);
}

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
 *        Juhan Voolaid
 */

package eionet.meta.service;

import java.util.List;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.service.data.TableFilter;

/**
 * Table service interface.
 *
 * @author Juhan Voolaid
 */
public interface ITableService {

    /**
     * Searches tables.
     *
     * @param tableFilter
     * @return
     * @throws ServiceException
     */
    List<DataSetTable> searchTables(TableFilter tableFilter) throws ServiceException;

    /**
     * Lists table attributes.
     *
     * @return
     * @throws ServiceException
     */
    List<Attribute> getTableAttributes() throws ServiceException;

    /**
     * Returns the name attribute(s) of the table.
     *
     * @param tableId
     * @return
     * @throws ServiceException
     */
    List<String> getNameAttribute(int tableId) throws ServiceException;

    List<DataSetTable> getTablesForObligation(String obligationId, boolean releasedOnly) throws ServiceException;
}

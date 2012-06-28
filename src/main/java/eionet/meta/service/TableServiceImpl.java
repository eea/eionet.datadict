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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.ITableDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.service.data.TableFilter;

/**
 * Table service.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class TableServiceImpl implements ITableService {

    /** Table DAO. */
    @Autowired
    private ITableDAO tableDAO;

    /** The DAO for operations with attributes */
    @Autowired
    private IAttributeDAO attributeDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSetTable> searchTables(TableFilter tableFilter) throws ServiceException {
        try {
            return tableDAO.searchTables(tableFilter);
        } catch (Exception e) {
            throw new ServiceException("Failed to search tables: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attribute> getTableAttributes() throws ServiceException {
        try {
            return attributeDAO.getAttributes(DElemAttribute.ParentType.TABLE, DElemAttribute.TYPE_SIMPLE);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema set attributes: " + e.getMessage(), e);
        }
    }

}

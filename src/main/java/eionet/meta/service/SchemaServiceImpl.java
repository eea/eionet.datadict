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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.service.data.PagedRequest;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * Schema service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
public class SchemaServiceImpl implements ISchemaService {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SchemaServiceImpl.class);

    /** SchemaSet DAO. */
    @Autowired
    private ISchemaSetDAO schemaSetDAO;

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    public SchemaSetsResult getSchemaSets(PagedRequest pagedRequest) throws ServiceException {
        try {
            return schemaSetDAO.getSchemaSets(pagedRequest);
        } catch (Exception e) {
            throw new ServiceException("Failed to get schema sets", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteSchemaSets(List<Integer> ids) throws ServiceException {
        try {
            schemaSetDAO.deleteAttributes(ids);
            schemaSetDAO.deleteSchemaSets(ids);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schema sets", e);
        }

    }

    /**
     * @param schemaSetDAO
     *            the schemaSetDAO to set
     */
    public void setSchemaSetDAO(ISchemaSetDAO schemaSetDAO) {
        this.schemaSetDAO = schemaSetDAO;
    }

}

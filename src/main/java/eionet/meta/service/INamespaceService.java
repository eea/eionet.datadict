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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */
package eionet.meta.service;

import java.util.List;

import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.service.data.NamespaceFilter;
import eionet.meta.service.data.NamespaceResult;

/**
 * Namespace service definition.
 *
 * @author enver
 */
public interface INamespaceService {
    /**
     * Returns all namespaces.
     *
     * @param filter
     *            any search criteria or filtering.
     * @return list of namespaces as a result set.
     * @throws ServiceException
     *             when an error occurs
     */
    NamespaceResult getNamespaces(NamespaceFilter filter) throws ServiceException;

    /**
     * Returns all rdf namespaces.
     *
     * @return container of RDF Namespace objects
     * @throws ServiceException
     *             if query fails
     */
    List<RdfNamespace> getRdfNamespaces() throws ServiceException;

} // end of interface INamespaceService

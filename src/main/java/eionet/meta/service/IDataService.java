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
import java.util.Map;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;

/**
 * Service for getting data objects.
 *
 * @author Juhan Voolaid
 */
public interface IDataService {

    /**
     * Returns all available data element attributes.
     *
     * @return
     * @throws ServiceException
     */
    List<Attribute> getDataElementAttributes() throws ServiceException;

    /**
     * Lists all the latest versions of data sets.
     *
     * @return
     * @throws ServiceException
     */
    List<DataSet> getDataSets() throws ServiceException;

    /**
     * Returns attribute by shortName.
     *
     * @param shortName
     * @return
     */
    Attribute getAttributeByName(String shortName) throws ServiceException;

    /**
     * Returns data element by id.
     *
     * @param id
     * @return
     */
    DataElement getDataElement(int id) throws ServiceException;

    /**
     * Search data elements.
     *
     * @param filter
     * @return
     * @throws ServiceException
     */
    DataElementsResult searchDataElements(DataElementsFilter filter) throws ServiceException;

    /**
     * Returns data element's fixed values.
     *
     * @return
     * @throws ServiceException
     */
    List<FixedValue> getFixedValues(int dataElementId) throws ServiceException;

    /**
     * Returns data elements that have code list values.
     *
     * @return
     * @throws ServiceException
     */
    List<DataElement> getDataElementsWithFixedValues() throws ServiceException;

    /**
     * Returns data element's data type.
     *
     * @param dataElementId
     * @return
     * @throws ServiceException
     */
    String getDataElementDataType(int dataElementId) throws ServiceException;

    /**
     * Returns attribute values of the given data element.
     * @param dataElementId element ID
     * @return List of attributes
     * @throws ServiceException if query fails
     */
    Map<String, List<String>> getDataElementSimpleAttributeValues(int dataElementId) throws ServiceException;

    /**
     * Returns list of common data elements that are released.
     * @return list of data elements
     * @throws ServiceException if query fails
     */
    List<DataElement> getReleasedCommonDataElements() throws ServiceException;

    /**
     * returns ID of a common data element in DATAELEM table.
     * @param identifier common element identifier
     * @return ID
     * @throws ServiceException if query fails
     */
    int getCommonElementIdByIdentifier(String identifier) throws ServiceException;
}

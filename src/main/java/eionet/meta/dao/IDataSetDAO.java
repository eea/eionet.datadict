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

package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.data.DatasetFilter;

/**
 * Data set DAO.
 *
 * @author Juhan Voolaid
 */
public interface IDataSetDAO {

    /**
     * Lists all the latest versions of data sets.
     *
     * @return
     * @throws ServiceException
     */
    List<DataSet> getDataSets();

    /**
     * Search datasets by search criteria defined in DatasetFilter. The methods searches datasets by short name, identifier,
     * registrations statuses and attributes.
     * @param datasetFilter DatasetFilter object defining search criteria
     * @return List of DataSet objects.
     */
    List<DataSet> searchDatasets(DatasetFilter datasetFilter);

    /**
     * Searches for recently released datasets.
     *
     * @param limit
     *            maximum number of objects/
     * @return List of DataSet objects.
     */
    List<DataSet> getRecentlyReleasedDatasets(int limit);

    String getIdentifierById(int id);

    List<DataSet> getWorkingCopiesOf(String userName);

    Integer getLatestDatasetId(String shortname);

}

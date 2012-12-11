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
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.service.data.TableFilter;

/**
 * Table DAO interface.
 *
 * @author Juhan Voolaid
 */
public interface ITableDAO {

    /**
     * Search dataset tables by search criteria defined in DatasetFilter. The methods searches dataset tables by short name,
     * identifier and simple attributes
     * @param tableFilter TableFilter object defining search criteria
     * @return List of DataSetTable objects.
     */
    List<DataSetTable> searchTables(TableFilter tableFilter);

    /**
     * Returns list of DataSetTable objects included in specified datasets.
     * @param datasets List of DataSet objects
     * @return
     */
    List<DataSetTable> listForDatasets(List<DataSet> datasets);
}

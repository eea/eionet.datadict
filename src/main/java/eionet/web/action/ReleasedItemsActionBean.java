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
 * Agency. Portions created by TripleDev or Zero TechnoLOGGERies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */
package eionet.web.action;

import java.util.List;

import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemasResult;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;
import org.displaytag.properties.SortOrderEnum;

/**
 * Released items action bean.
 *
 * @author enver
 */
@UrlBinding("/releasedItems.action")
public class ReleasedItemsActionBean extends AbstractActionBean {

    /**
     * Data service.
     */
    @SpringBean
    private IDataService dataService;
    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;
    /**
     * List of recently released data sets.
     */
    private List<DataSet> dataSets;
    /**
     * List of recently released vocabularies.
     */
    private List<VocabularyFolder> vocabularies;
    /**
     * List of recently released schemas.
     */
    private List<Schema> schemas;
    /**
     * Schema service.
     */
    @SpringBean
    private ISchemaService schemaService;

    /**
     * Default handler for released items.
     *
     * @return resolution to page displaying recently released item
     * @throws ServiceException
     *             if operation fails
     */
    @DefaultHandler
    public Resolution view() throws ServiceException {
        this.dataSets = this.dataService.getRecentlyReleasedDatasets(7);
        this.vocabularies = this.vocabularyService.getRecentlyReleasedVocabularyFolders(7);
        SchemaFilter filter = new SchemaFilter();
        filter.setRegStatus(RegStatus.RELEASED.toString());
        filter.setUsePaging(true);
        filter.setPageNumber(1);
        filter.setPageSize(7);
        filter.setSortProperty("S.DATE_MODIFIED");
        filter.setSortOrder(SortOrderEnum.DESCENDING);
        SchemasResult schemasResult = this.schemaService.searchSchemas(filter);
        this.schemas = schemasResult.getList();
        return new ForwardResolution("/pages/releasedItems.jsp");
    }

    public String getTitle() {
        return "Header from action bean";
    }

    public List<DataSet> getDataSets() {
        return dataSets;
    }

    public List<VocabularyFolder> getVocabularies() {
        return vocabularies;
    }

    public List<Schema> getSchemas() {
        return schemas;
    }
} // end of class ReleasedItemsActionBean

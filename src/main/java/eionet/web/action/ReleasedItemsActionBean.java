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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.displaytag.properties.SortOrderEnum;

import eionet.meta.RecentlyReleased;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.RegStatus;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IDataService;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Released items action bean.
 *
 * @author enver
 */
@UrlBinding("/releasedItems.action")
public class ReleasedItemsActionBean extends AbstractActionBean {
    /**
     * Default view jsp.
     */
    private static final String RELEASED_ITEMS_JSP = "/pages/releasedItems.jsp";
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
     * List of combined results.
     */
    private List<RecentlyReleased> results;
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
        List<DataSet> dataSets =
                this.dataService.getRecentlyReleasedDatasets(Props.getIntProperty(PropsIF.DD_RECENTLY_RELEASED_DATASETS_KEY));
        List<VocabularyFolder> vocabularies =
                this.vocabularyService.getRecentlyReleasedVocabularyFolders(Props
                        .getIntProperty(PropsIF.DD_RECENTLY_RELEASED_VOCABULARIES_KEY));
        SchemaSetFilter filter = new SchemaSetFilter();
        filter.setRegStatus(RegStatus.RELEASED.toString());
        filter.setUsePaging(true);
        filter.setPageNumber(1);
        filter.setPageSize(Props.getIntProperty(PropsIF.DD_RECENTLY_RELEASED_SCHEMAS_KEY));
        // TODO sort property can be added to filter and be queried from there
        filter.setSortProperty("DATE_MODIFIED");
        filter.setSortOrder(SortOrderEnum.DESCENDING);
        SchemaSetsResult schemaSetsResult = this.schemaService.searchSchemaSets(filter);
        List<SchemaSet> schemaSets = schemaSetsResult.getList();
        convertAndSort(dataSets, vocabularies, schemaSets);
        
        return new ForwardResolution(RELEASED_ITEMS_JSP);
    } // end of default handler - view

    /**
     * Merges and sorts all recently released item into a single list of RecentlyReleased objects.
     *
     * @param dataSets
     *            list of data sets.
     * @param vocabularies
     *            list of vocabularies.
     * @param schemaSets
     *            list of schema-sets.
     */
    private void convertAndSort(List<DataSet> dataSets, List<VocabularyFolder> vocabularies, List<SchemaSet> schemaSets) {
        // create empty list
        this.results = new ArrayList<RecentlyReleased>();
        RecentlyReleased rr;

        // add datasets to list
        if (dataSets != null) {
            for (DataSet ds : dataSets) {
                rr = new RecentlyReleased(ds.getName(), ds.getAdjustedDate(), RecentlyReleased.Type.DATASET);
                rr.addParameter("datasetId", ds.getId());
                this.results.add(rr);
            }
        }

        // add vocabularies to list
        if (vocabularies != null) {
            for (VocabularyFolder vf : vocabularies) {
                rr = new RecentlyReleased(vf.getLabel(), vf.getDateModified(), RecentlyReleased.Type.VOCABULARY);
                rr.addParameter("folderName", vf.getFolderName());
                rr.addParameter("identifier", vf.getIdentifier());
                this.results.add(rr);
            }
        }

        // add schemas to list
        if (schemaSets != null) {
            for (SchemaSet s : schemaSets) {
                rr = new RecentlyReleased(s.getNameAttribute(), s.getDateModified(), RecentlyReleased.Type.SCHEMA_SET);
                rr.addParameter("schemaSetIdentifier", s.getIdentifier());
                //rr.addParameter("fileName", s.getFileName());
                this.results.add(rr);
            }
        }

        // sort list of objects
        Collections.sort(this.results, Collections.reverseOrder());
    } // end of method convertAndSort

    public String getTitle() {
        return "Header from action bean";
    }

    public List<RecentlyReleased> getResults() {
        return results;
    }
} // end of class ReleasedItemsActionBean

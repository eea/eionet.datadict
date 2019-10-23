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
 *        Enriko Käsper
 */

package eionet.web.action;

import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.rdf.VocabularyXmlWriter;
import eionet.meta.service.ISiteCodeService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.util.Props;
import eionet.util.PropsIF;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Folder action bean. Folder contains collection of vocabularies.
 *
 * @author Enriko Käsper
 */
@UrlBinding("/vocabularyfolder/{folder.identifier}/{$event}")
public class FolderActionBean extends AbstractActionBean {

    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;

    /**
     * Site code service.
     */
    @SpringBean
    private ISiteCodeService siteCodeService;

    /**
     * Folder containing vocabularies.
     */
    private Folder folder;

    /**
     * Action, that returns RDF output of the folder's vocabularies.
     *
     * @return StreamingResolution
     */
    @DefaultHandler
    public Resolution rdf() {
        try {
            folder = vocabularyService.getFolderByIdentifier(folder.getIdentifier());
            int folderId = folder.getId();
            final List<VocabularyFolder> vocabularyFolders = vocabularyService.getReleasedVocabularyFolders(folderId);

            StreamingResolution result = new StreamingResolution("application/rdf+xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyXmlWriter xmlWriter = new VocabularyXmlWriter(response.getOutputStream());

                    String folderContextRoot =
                            Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + folder.getIdentifier() + "/";
                    String commonElemsUri = Props.getRequiredProperty(PropsIF.DD_URL) + "/property/";
                    List<RdfNamespace> nameSpaces = vocabularyService.getVocabularyNamespaces(vocabularyFolders);
                    xmlWriter.writeXmlStart(true, commonElemsUri, folderContextRoot, nameSpaces);
                    xmlWriter.writeFolderXml(folderContextRoot, folder, vocabularyFolders);

                    for (VocabularyFolder vocabularyFolder : vocabularyFolders) {
                        List<? extends VocabularyConcept> concepts = null;
                        concepts = vocabularyService.getAllConceptsWithAttributes(vocabularyFolder.getId());

                        final List<? extends VocabularyConcept> finalConcepts = concepts;

                        String vocabularyContextRoot = VocabularyFolder.getBaseUri(vocabularyFolder);

                        xmlWriter.writeVocabularyFolderXml(folderContextRoot, vocabularyContextRoot, vocabularyFolder,
                                finalConcepts);
                    }

                    xmlWriter.writeXmlEnd();
                }
            };

            result.setFilename(folder.getIdentifier() + ".rdf");

            return result;

        } catch (Exception e) {
            LOGGER.error("Failed to output folder RDF data", e);
            ErrorResolution error = new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

    /**
     * @return the folder
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(Folder folder) {
        this.folder = folder;
    }

}

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
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */
package eionet.meta.service;

import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyImportService.MissingConceptsAction;
import eionet.meta.service.IVocabularyImportService.UploadAction;
import eionet.meta.service.IVocabularyImportService.UploadActionBefore;

import java.io.Reader;
import java.util.List;

/**
 * This interface contains methods to import csv contents to bulk edit a
 * vocabulary or create a Vocabulary From RDF.
 *
 * @author enver
 */
public interface IRDFVocabularyImportService {

    
     /**
     * Transactional method to create a folder and a vocabulary from a Rest Call using a mix
     * of Request Params and RDF Payload
     *
     * @param contents Reader object to read file content 
     * @param folder 
     * @param vocabularyFolder 
     * @param username the username of the creator of the vocabulary 
     */
    List<String> createFolderAndVocabularyFromRDF(Reader contents,Folder folder,  VocabularyFolder vocabularyFolder, String username) throws ServiceException;
    
    
    
    /**
     * Transactional method to create a vocabulary from a Rest Call using a mix
     * of Request Params and RDF Payload
     *
     * @param contents Reader object to read file content 
     * @param vocabularyFolder 
     * @param username the username of the creator of the vocabulary 
     */
    List<String> createVocabularyFromRDF(Reader contents, VocabularyFolder vocabularyFolder, String username) throws ServiceException;

    /**
     * A Transactional method to import RDF file contents into a vocabulary
     * folder. User can request purging data first and then inserting from
     * scratch.
     *
     * @param contents Reader object to read file content.
     * @param vocabularyFolder Vocabulary folder under bulk edit mode.
     * @param uploadActionBefore Action before for this upload operation.
     * @param uploadAction Action for this upload operation.
     * @param missingConceptsAction Missing concepts action for this upload
     * operation.
     * @return List of log messages
     * @throws ServiceException Error if input is not valid
     */
    List<String> importRdfIntoVocabulary(Reader contents,
            VocabularyFolder vocabularyFolder,
            UploadActionBefore uploadActionBefore,
            UploadAction uploadAction,
            MissingConceptsAction missingConceptsAction) throws ServiceException;

    /**
     * A support method for legacy calls (calls importRdfIntoVocabulary
     * internally).
     *
     * @param contents Reader object to read file content
     * @param vocabularyFolder Vocabulary folder under bulk edit mode
     * @param purgeVocabularyData Purge all vocabulary concepts of folder
     * @param purgePredicateBasis Purge bound elements per predicate basis
     * @return List of log messages
     * @throws ServiceException Error if input is not valid
     */
    List<String> importRdfIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder, boolean purgeVocabularyData,
            boolean purgePredicateBasis) throws ServiceException;
    
    /**
     * A support method for legacy calls (calls importRdfIntoVocabulary
     * internally).
     *
     * @param contents Reader object to read file content
     * @param vocabularyFolder Vocabulary folder under bulk edit mode
     * @param deleteVocabularyData Deletes the imported concepts
     * @param purgeVocabularyData Purge all vocabulary concepts of folder
     * @param purgePredicateBasis Purge bound elements per predicate basis
     * @param missingConceptsAction Missing concepts action for this upload
     * @return List of log messages
     * @throws ServiceException Error if input is not valid
     */
    List<String> importRdfIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder, boolean deleteVocabularyData, boolean purgeVocabularyData,
            boolean purgePredicateBasis, MissingConceptsAction missingConceptsAction) throws ServiceException;

    /**
     * A method to get all supported action before elements, depending on upload
     * operation is called from API, or from UI.
     *
     * @param isApiCall is api call.
     * @return list of supported values.
     */
    List<UploadActionBefore> getSupportedActionBefore(boolean isApiCall);

    /**
     * A method to get default action before element, depending on upload
     * operation is called from API, or from UI.
     *
     * @param isApiCall is api call.
     * @return default value.
     */
    UploadActionBefore getDefaultActionBefore(boolean isApiCall);

    /**
     * A method to get all supported action elements, depending on upload
     * operation is called from API, or from UI.
     *
     * @param isApiCall is api call.
     * @return list of supported values.
     */
    List<UploadAction> getSupportedAction(boolean isApiCall);

    /**
     * A method to get default action element, depending on upload operation is
     * called from API, or from UI.
     *
     * @param isApiCall is api call.
     * @return default value.
     */
    UploadAction getDefaultAction(boolean isApiCall);

    /**
     * A method to get all supported missing concepts action elements, depending
     * on upload operation is called from API, or from UI.
     *
     * @param isApiCall is api call.
     * @return list of supported values.
     */
    List<MissingConceptsAction> getSupportedMissingConceptsAction(boolean isApiCall);

    /**
     * A method to get default missing concepts action element, depending on
     * upload operation is called from API, or from UI.
     *
     * @param isApiCall is api call.
     * @return default value.
     */
    MissingConceptsAction getDefaultMissingConceptsAction(boolean isApiCall);

} // end of interface IRDFVocabularyImportService

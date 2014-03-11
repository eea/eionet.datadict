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
package eionet.meta.imp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;

/**
 * Base abstract class used for vocabulary import handling from different sources (RDF or CSV).
 *
 * @author enver
 */
public abstract class VocabularyImportBaseHandler {

    /**
     * log message list.
     */
    protected List<String> logMessages = null;

    /**
     * Vocabulary service.
     */
    @Autowired
    protected IVocabularyService vocabularyService;

    /**
     * Data elements service.
     */
    @Autowired
    protected IDataService dataService;

    /**
     * Default constructor. An object can only be instantiated from this package.
     */
    protected VocabularyImportBaseHandler() {
        this.logMessages = new ArrayList<String>();
    }

    public List<String> getLogMessages() {
        return this.logMessages;
    }

} // end of class VocabularyImportBaseHandler

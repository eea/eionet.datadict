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
 *        TripleDev
 */

package eionet.meta.scheduled;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled Job class to match references in vocabulary.
 *
 * @author enver
 */
public class VocabularyReferenceMatchJob extends AbstractScheduledJob {
    /**
     * Static definition of name of job. Visible to all parties.
     */
    public static final String NAME = "vocabularyReferenceMatch";
    /**
     * Logging instance.
     */
    private static final Logger LOGGER = Logger.getLogger(VocabularyReferenceMatchJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("Starting to match references.");

        LOGGER.info("References matched.");
    }

    @Override
    protected String getName() {
        return VocabularyReferenceMatchJob.NAME;
    } // end of method getName
} // end of class VocabularyReferenceMatchJob

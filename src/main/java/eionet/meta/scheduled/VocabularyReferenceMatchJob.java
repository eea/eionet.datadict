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

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import eionet.meta.service.IVocabularyReferenceMatchService;
import eionet.meta.service.ServiceException;

/**
 * Scheduled Job class to match references in vocabulary.
 *
 * @author enver
 */
@Configurable
@DisallowConcurrentExecution
public class VocabularyReferenceMatchJob extends AbstractScheduledJob {
    /**
     * Static definition of name of job. Visible to all parties.
     */
    public static final String NAME = "vocabularyReferenceMatch";
    /**
     * Logging instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyReferenceMatchJob.class);
    /**
     * Elements key.
     */
    private static final String ELEMENTS_KEY = NAME + "ElementsKey";

    /**
     * Match service.
     */
    @Autowired
    protected IVocabularyReferenceMatchService service;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        long start = System.currentTimeMillis();
        LOGGER.info("Starting to match references.");
        List<String> strings = null;
        try {
            strings = service.matchReferences((String[]) jobExecutionContext.getJobDetail().getJobDataMap().get(ELEMENTS_KEY));
        } catch (ServiceException e) {
            LOGGER.error("Match operation failed: " + e.getMessage());
            throw new JobExecutionException(e);
        }
        for (String s : strings) {
            LOGGER.info(s);
        }
        LOGGER.info("References matched in " + (System.currentTimeMillis() - start) + " msecs.");
    } // end of method execute

    @Override
    protected String getName() {
        return VocabularyReferenceMatchJob.NAME;
    } // end of method getName

    @Override
    protected void parseJobData(JobDetail jobDetails, String jobData) {
        jobDetails.getJobDataMap().put(ELEMENTS_KEY, jobData.split("[,]"));
    } // end of method parseJobData
} // end of class VocabularyReferenceMatchJob

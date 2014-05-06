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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;

import eionet.util.Props;

/**
 * Abstract class for scheduled job.
 *
 * @author Kaido Laine
 */
public abstract class AbstractScheduledJob implements ServletContextListener, Job {

    /**
     * Schedule property suffix used for each job properties in resource file.
     */
    public static final String SCHEDULE_PROPERTY_SUFFIX = ".job.schedule";
    /**
     * Data property suffix used for each job properties in resource file.
     */
    public static final String DATA_PROPERTY_SUFFIX = ".job.data";

    /**
     * local logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractScheduledJob.class);

    /**
     * Job Name.
     *
     * @return current job name
     */
    protected abstract String getName();

    /**
     * Schedule property name.
     *
     * @return A string to query property for job.
     */
    protected String getSchedulePropName() {
        return getName() + SCHEDULE_PROPERTY_SUFFIX;
    } // end of method getSchedulePropName

    /**
     * Data property name.
     *
     * @return A string to query property for job.
     */
    protected String getDataPropName() {
        return getName() + DATA_PROPERTY_SUFFIX;
    } // end of method getDataPropName

    /**
     * Parses job data. It does not have a default implementation yet. It can be overwritten by subclasses for specific needs.
     *
     * @param jobDetails
     *            cron object for job details
     * @param jobData
     *            String job data from properties file
     */
    protected void parseJobData(JobDetail jobDetails, String jobData) {
        throw new UnsupportedOperationException("Base method is not implemented");
    } // end of method parseJobData

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Class<? extends AbstractScheduledJob> clazz = this.getClass();
        JobBuilder jobBuilder = JobBuilder.newJob(clazz);
        jobBuilder.withIdentity(clazz.getSimpleName(), clazz.getName());
        JobDetail jobDetails = jobBuilder.build();
        String jobData = Props.getProperty(getDataPropName());
        jobData = StringUtils.trimToEmpty(jobData);
        if (StringUtils.isNotBlank(jobData)) {
            parseJobData(jobDetails, jobData);
        }
        String propName = getSchedulePropName();
        String intervalPrpValue = Props.getProperty(propName);
        try {
            if (CronExpression.isValidExpression(intervalPrpValue)) {
                DDJobScheduler.scheduleCronJob(intervalPrpValue, jobDetails);
                LOGGER.info(getName() + " job started crontab " + intervalPrpValue);
            } else {
                long interval = Props.getTimePropertyMilliseconds(propName, DDJobScheduler.DEFAULT_SCHEDULE_INTERVAL);
                DDJobScheduler.scheduleIntervalJob(interval, jobDetails);
                LOGGER.info(getName() + " job started with interval " + interval + " ms");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize job: " + getName() + ": \n" + e);
        }
    } // end of method contextInitialized

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.debug(getName() + " context destroyed.");
    } // end of method contextDestroyed

} // end of class AbstractScheduledJob

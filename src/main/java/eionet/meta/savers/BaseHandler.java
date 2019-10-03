package eionet.meta.savers;

import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import eionet.meta.DDUser;
import eionet.meta.MrProper;
import eionet.meta.service.IDataService;
import eionet.meta.spring.SpringApplicationContext;
import eionet.util.Util;
import eionet.util.sql.SQLTransaction;

/**
 * @author Jaanus Heinlaid
 */
public abstract class BaseHandler {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);

    /** */
    protected Connection conn = null;
    protected Parameters req = null;
    protected ServletContext ctx = null;
    protected HttpServletRequest httpServletRequest = null;

    /** */
    protected DDUser user = null;

    /** Lazily initialized. */
    private IDataService dataService;

    /**
     *
     * @throws Exception
     */
    public void execute() throws Exception {

        SQLTransaction tx = null;
        try {
            tx = SQLTransaction.begin(conn);
            execute_();
            tx.commit();
        } catch (Exception e) {
            SQLTransaction.rollback(tx);
            throw e;
        } finally {
            SQLTransaction.end(tx);
        }
    }

    /**
     *
     * @param pars
     * @throws Exception
     */
    public void execute(Parameters pars) throws Exception {
        this.req = pars;
        execute();
    }

    /**
     *
     * @param pars
     * @throws Exception
     */
    public void execute(HttpServletRequest req) throws Exception {
        this.httpServletRequest = req;
        execute();
    }

    /**
     *
     */
    protected void cleanVisuals() {

        String vp = ctx == null ? null : ctx.getInitParameter("visuals-path");
        if (Util.isEmpty(vp)) {
            LOGGER.error("cleanVisuals() failed to find visuals path!");
        }

        MrProper mrProper = new MrProper(conn);
        mrProper.setUser(user);

        Parameters pars = new Parameters();
        pars.addParameterValue(MrProper.FUNCTIONS_PAR, MrProper.CLN_VISUALS);
        pars.addParameterValue(MrProper.VISUALS_PATH, vp);

        mrProper.execute(pars);
        LOGGER.debug(mrProper.getResponse().toString());
    }

    /**
     *
     * @throws Exception
     */
    public abstract void execute_() throws Exception;

    /**
     * Lazily initialize and return the data service instance.
     *
     * @return the dataService
     */
    protected IDataService getDataService() {

        if (dataService == null) {

            ApplicationContext appContext = SpringApplicationContext.getContext();
            dataService = appContext.getBean(IDataService.class);
        }

        return dataService;
    }
}

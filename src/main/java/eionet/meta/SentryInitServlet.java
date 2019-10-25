package eionet.meta;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class SentryInitServlet extends HttpServlet {

    /**
     * Sentry initialization
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        final Logger LOGGER = LoggerFactory.getLogger(ServletConfig.class);

        super.init(config);

        SentryClient client;

        try {
            client =  Sentry.init();
        } catch (Exception e) {
            LOGGER.error("No sentry service available due to: " + e.getMessage());
        }
    }
}
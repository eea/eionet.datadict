package eionet.web.util;

import eionet.meta.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolverImpl;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import io.sentry.Sentry;
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
        ConfigurationPropertyResolver propertyResolver = SpringApplicationContext.getBean(ConfigurationPropertyResolverImpl.class);

        String dsn = null;

        try {
            dsn = propertyResolver.resolveValue("config.sentry.dsn");
        } catch (UnresolvedPropertyException e) {
            LOGGER.error(e.getMessage());
        } catch (CircularReferenceException e) {
            LOGGER.error(e.getMessage());
        }
        if (dsn != null && dsn.length() > 2) {
            Sentry.init(dsn);
        } else {
            Sentry.init();
        }

    }
}

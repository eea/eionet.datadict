package eionet.meta;

import io.sentry.Sentry;
import io.sentry.SentryClient;

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
        super.init(config);
        //ConfigurationPropertyResolver propertyResolver = SpringApplicationContext.getBean("configurationPropertyResolver");

        String dsn = null;


            dsn = "https://7f20b266a47b4307808d8f3da301ea0c@sentry.eea.europa.eu/43";
       // if (dsn != null && dsn.length() > 2) {
        SentryClient client =     Sentry.init(dsn);
        client.setRelease("2019-10-21T1827");
       // } else {
          //  Sentry.init();
       // }

    }
}
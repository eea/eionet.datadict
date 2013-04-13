package eionet.meta.filters;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import edu.yale.its.tp.cas.client.filter.CASFilter;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class EionetCASFilter extends CASFilter {

    /*
     * (non-Javadoc)
     * @see edu.yale.its.tp.cas.client.filter.CASFilter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {

        CASFilterConfig.init(config);
        super.init(CASFilterConfig.getInstance());
    }
}

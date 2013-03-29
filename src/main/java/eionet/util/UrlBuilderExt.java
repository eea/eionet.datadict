package eionet.util;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.util.StringUtil;
import net.sourceforge.stripes.util.UrlBuilder;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class UrlBuilderExt extends UrlBuilder {

    /**
     * 
     * @param actionBeanContext
     * @param isForPage
     */
    public UrlBuilderExt(ActionBeanContext actionBeanContext, boolean isForPage) {
        super(actionBeanContext.getLocale(), isForPage);
    }

    /**
     * 
     * @param actionBeanContext
     * @param url
     * @param isForPage
     */
    public UrlBuilderExt(ActionBeanContext actionBeanContext, String url, boolean isForPage) {
        super(actionBeanContext.getLocale(), url, isForPage);
    }

    /**
     * 
     * @param actionBeanContext
     * @param beanType
     * @param isForPage
     */
    public UrlBuilderExt(ActionBeanContext actionBeanContext, Class<? extends ActionBean> beanType, boolean isForPage) {
        super(actionBeanContext.getLocale(), beanType, isForPage);
    }

    /**
     * @see net.sourceforge.stripes.util.UrlBuilder#toString()
     */
    public String toString() {

        String url = build();
        String anchor = getAnchor();
        if (!StringUtils.isBlank(anchor)) {
            return new StringBuilder(url).append("#").append(StringUtil.uriFragmentEncode(anchor)).toString();
        } else {
            return url;
        }
    }
}

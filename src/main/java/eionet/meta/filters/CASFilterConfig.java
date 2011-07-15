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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.meta.filters;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import eionet.util.Props;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class CASFilterConfig extends Hashtable<String,String> implements FilterConfig {

    /** */
    private static CASFilterConfig instance;
    private static Object lock = new Object();

    /** */
    private String filterName;
    private ServletContext servletContext;

    /**
     *
     * @param defaultConfig
     */
    private CASFilterConfig(FilterConfig defaultConfig) {

        super();

        if (defaultConfig!=null) {

            // load default configuration supplied by CAS
            for (Enumeration names=defaultConfig.getInitParameterNames(); names.hasMoreElements();) {

                String name = names.nextElement().toString();
                put(name, defaultConfig.getInitParameter(name));
            }

            // set filter name and servlet context as they came from default config
            filterName = defaultConfig.getFilterName();
            servletContext = defaultConfig.getServletContext();
        }

        // overwrite with DD's own values
        for (CASInitParam casInitParam : CASInitParam.values()) {

            String name = casInitParam.toString();
            put(name, Props.getRequiredProperty(name));
        }
    }

    /**
     *
     * @param defaultConfig
     */
    public static void init(FilterConfig defaultConfig) {

        if (instance==null) {

            synchronized (lock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (instance==null) {
                    instance = new CASFilterConfig(defaultConfig);
                }
            }
        }
    }

    /**
     *
     * @param defaultConfig
     * @return
     */
    public static CASFilterConfig getInstance() {

        if (instance==null) {
            throw new IllegalStateException(
                    CASFilterConfig.class.getSimpleName() + " not yet initialized");
        }
        else {
            return instance;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.FilterConfig#getFilterName()
     */
    public String getFilterName() {

        return filterName;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String paramName) {

        return get(paramName);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.FilterConfig#getInitParameterNames()
     */
    public Enumeration<String> getInitParameterNames() {

        return keys();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.FilterConfig#getServletContext()
     */
    public ServletContext getServletContext() {

        return servletContext;
    }
}

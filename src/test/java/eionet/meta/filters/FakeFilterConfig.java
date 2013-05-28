package eionet.meta.filters;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 *
 * This is fake object. It is implementing only default methods and fill in test data.
 *
 * @author Rait Väli
 */
public class FakeFilterConfig extends Hashtable<String, String> implements FilterConfig {

    /**  */
    private static final long serialVersionUID = 1L;
    String filterName = "testFilterName";
    FakeServletContext servletContext = new FakeServletContext();
    Vector<String> v = new Vector<String>();
    Enumeration<?> testInitParameters;

    Map<String, String> paramsMap = createParamsMap();

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return paramsMap.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(paramsMap.keySet());
    }

    /**
     * Construct map of init parameters
     *
     * @return
     */
    private static Map<String, String> createParamsMap() {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("param1", "value1");
        map.put("param2", "value2");

        return Collections.unmodifiableMap(map);
    }
}

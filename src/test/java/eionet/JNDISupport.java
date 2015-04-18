package eionet;

import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Support functions to set up JNDI.
 */
public class JNDISupport {

    static final String tomcatContextLocation = "java:comp/env/";
    private static boolean isSetupCore = false;
    private static InitialContext context;
    private static ArrayList<String> addedProps = new ArrayList<String>();
    private static ArrayList<String> addedSubCtxs = new ArrayList<String>();

    static {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        try {
            context = new InitialContext();
        } catch (NamingException e) {
            throw new UnsupportedOperationException("Unable to create context");
        }
    }

    /**
     * Constructor.
     */
    private JNDISupport() {
    }

    /**
     * Clean up.
     */
    public static void cleanUp() throws Exception {
        if (!isSetupCore) {
            return;
        }
        for (String name : addedProps) {
            context.unbind(name);
        }
        addedProps.clear();
        for (String name : addedSubCtxs) {
            context.destroySubcontext(name);
        }
        addedSubCtxs.clear();
        isSetupCore = false;
    }

    public static void setUpCore() throws Exception {
        if (isSetupCore) {
            return;
        }
        String[] subCtxs = {"java:", "java:comp", "java:comp/env"};
        for (String subCtx : subCtxs) {
            addSubcontext(subCtx);
        }
        isSetupCore = true;
    }

    /**
     * Add a property to Tomcat's context.
     */
    public static void addPropToTomcat(String name, Object value) throws Exception {
        context.bind(tomcatContextLocation + name,  value);
        addedProps.add(tomcatContextLocation + name);
    }

    /**
     * Add a subContext to Tomcat's context.
     */
    public static void addSubCtxToTomcat(String name) throws Exception {
        context.createSubcontext(tomcatContextLocation + name);
        addedSubCtxs.add(0, tomcatContextLocation + name);
    }

    static void addSubcontext(String subCtx) throws Exception {
        context.createSubcontext(subCtx);
        addedSubCtxs.add(0, subCtx);
    }
}

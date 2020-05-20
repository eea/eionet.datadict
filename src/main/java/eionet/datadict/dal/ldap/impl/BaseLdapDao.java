package eionet.datadict.dal.ldap.impl;

import eionet.util.Props;
import eionet.util.PropsIF;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import java.io.IOException;
import java.util.Hashtable;

@Component
public class BaseLdapDao {

    public static final int PAGE_SIZE = 10000;

    protected static String baseDn = Props.getProperty(PropsIF.LDAP_CONTEXT);

    protected DirContext getDirContext() throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, Props.getProperty(PropsIF.LDAP_URL));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, Props.getProperty(PropsIF.LDAP_PRINCIPAL));
        env.put(Context.SECURITY_CREDENTIALS, Props.getProperty(PropsIF.LDAP_PASSWORD));
        DirContext ctx = new InitialDirContext(env);
        return ctx;
    }

    protected LdapContext getPagedLdapContext() throws NamingException, IOException {
        Hashtable env = new Hashtable();
        env.put(LdapContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(LdapContext.PROVIDER_URL, Props.getProperty(PropsIF.LDAP_URL));
        env.put(LdapContext.SECURITY_AUTHENTICATION, "simple");
        env.put(LdapContext.SECURITY_PRINCIPAL, Props.getProperty(PropsIF.LDAP_PRINCIPAL));
        env.put(LdapContext.SECURITY_CREDENTIALS, Props.getProperty(PropsIF.LDAP_PASSWORD));
        LdapContext ctx = new InitialLdapContext(env, null);
        ctx.setRequestControls(new Control[]{
                new PagedResultsControl(PAGE_SIZE, Control.CRITICAL)
        });
        return ctx;
    }

    protected void closeContext(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                // do nothing
            }
        }
    }
}

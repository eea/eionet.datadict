package eionet.datadict.dal.ldap.impl;

import eionet.util.Props;
import eionet.util.PropsIF;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@Component
public class BaseLdapDao {

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

    protected void closeContext(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }
}

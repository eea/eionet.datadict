package eionet.meta;

import org.apache.log4j.Logger;

import com.tee.uit.security.AuthMechanism;
import com.tee.uit.security.SignOnException;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDCASUser extends DDUser {

    /** */
    private static final Logger LOGGER = Logger.getLogger(DDCASUser.class);

    /**
     *
     */
    public DDCASUser() {
        super();
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.DDuser#authenticate(java.lang.String, java.lang.String)
     */
    @Override
    public boolean authenticate(String userName, String userPws) {

        invalidate();

        try {
            fullName = AuthMechanism.getFullName(userName);
            LOGGER.debug("User " + userName + " logged in through CAS.");
        } catch (SignOnException e) {
            LOGGER.error("Fatal error: can not get full name for authaticated user", e);
        }
        //
        authented = true;
        username = userName;
        password = userPws;

        return authented;
    }

    /**
     *
     * @return
     */
    public static DDCASUser create(String userName) {
        DDCASUser user = new DDCASUser();
        user.authenticate(userName, null);
        return user;
    }
}

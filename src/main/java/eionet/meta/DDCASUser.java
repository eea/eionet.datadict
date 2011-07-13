package eionet.meta;

import com.tee.uit.security.AuthMechanism;
import com.tee.uit.security.SignOnException;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDCASUser extends DDUser {
	
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
	public boolean authenticate(String userName, String userPws) {
		
		invalidate();

		try {
			fullName = AuthMechanism.getFullName(userName);
		}
		catch (SignOnException e) {
			logger.error("Fatal error: can not get full name for authaticated user", e);
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
	public static DDCASUser create(String userName){
		DDCASUser user = new DDCASUser();
		user.authenticate(userName, null);
		return user;
	}
}

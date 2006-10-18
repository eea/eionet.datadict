package eionet.meta;

import com.tee.xmlserver.*;

import com.tee.uit.security.*;

public class DDCASuser extends DDuser {
	
	public DDCASuser() {
		   super();
	   }
	
	
	public boolean authenticate(String userName, String userPws) {
		invalidate();

		// LOG
		if (Logger.enable(5))
			Logger.log("Create DD user '" + userName + "'");
		try {
			fullName = AuthMechanism.getFullName(userName);
		} catch (SignOnException e) {
			Logger.log("Fatal error: can not get full name for authaticated user", e);
		}
		//
		authented = true;
		user = userName;
		password = userPws;

		return authented;
	}

}

package eionet.meta;

import java.sql.*;
import com.tee.xmlserver.*;

public class TestUser implements AppUserIF {
    
    private boolean authented = false;
    private String user = null;
    private String password = null;
    private String fullName = null;
    private String[] _roles = null;
	private DBPoolIF dbPool = null;

    /**
    *
    */
    public TestUser() {
		dbPool = XDBApplication.getDBPool();
    }

	public TestUser(boolean noPool) {
	}
   
    /**
    *
    */
    public boolean authenticate(String userName, String userPws) {
        
        invalidate();
        authented = true;
        user = userName;
        password = userPws;
        
        return authented;
    }
    
    /**
    *
    */
    public boolean isAuthentic() {
        return authented;
    }
    
    /**
    *
    */
    public boolean isUserInRole(String role) {
        
        boolean b = false;

        if (_roles == null)
            getUserRoles();
                    
        for (int i =0; i< _roles.length; i++)
            if ( _roles[i].equals(role))
                b = true;
                      
        return b;
   }

/**
* FullName
*/
   public String getFullName() {
      return fullName;
   }

/**
 *
 */
   public String getUserName() {
      return user;
   }
/**
 *
 */
	public Connection getConnection() {
		return dbPool.getConnection();
	}
/**
 * Returns a string array of roles the user is linked to.
 * Note that the method returns newly constructed array, leaving internal role list unrevealed.
 */
   public String[] getUserRoles() {
    
        return null;
   }
/**
 *
 */
   public void invalidate() {
      authented = false;
      user = null;
      password = null;
   }
/** 
 *
 */
   public String toString() {
      return (user == null ? "" : user );
   }

}

package eionet.meta;

import com.tee.xmlserver.*;
import java.sql.*;
import java.util.Vector;

import eionet.directory.DirectoryService;

public class TestUser implements AppUserIF {
    
    private boolean authented = false;
    private String user = null;
    private String password = null;
    private DBPoolIF dbPool = null;
    private String fullName = null;
       
    private String[] _roles = null;
       
    public TestUser() {
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
      //return dbPool.getConnection(user, password);
      return dbPool.getConnection();
   }
/**
 * Returns a string array of roles the user is linked to.
 * Note that the method returns newly constructed array, leaving internal role list unrevealed.
 */
   public String[] getUserRoles() {
      //String[] roles;
      if (_roles == null) {
        try {
          
          Vector v = DirectoryService.getRoles(user);
          String[] roles = new String[v.size()];
          for ( int i=0; i< v.size(); i++)
              _roles[i] = (String)v.elementAt(i);
          
          } catch ( Exception e ) {
            //return empty String, no need for roles
            _roles = new String[]{};
          }
       }
     
      //return new String[]{};
      return _roles;
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
      //return user;
   }

}
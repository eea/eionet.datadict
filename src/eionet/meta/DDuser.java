
package eionet.meta;

import com.caucho.sql.*;
import java.util.*;
import java.sql.*;

public class DDuser {
    
    private boolean authented = false;
    
    private String username = null;
    private String password = null;
    private Vector roles = null;
    
    private DBPool dbPool = null;
   
    public DDuser(){
    }
    
    public DDuser(com.caucho.sql.DBPool dbPool) {
        this.dbPool = dbPool;
    }
    
    public boolean authenticate(String userName, String userPws) {
        
        invalidate();
      
        try {
            Connection conn = dbPool.getConnection(userName, userPws);
            if (conn == null)
                return false;
         
            authented = true;
            this.username = username;
            this.password = password;
         
            try {
                conn.close();
            } catch (SQLException se) {}
         
        } catch (Exception e) {}
        
        return authented;
    }

    public boolean isAuthentic() {
        return authented;
    }
    
    public boolean isUserInRole(String role) {
        if (roles == null) // no associated roles
            return false;
         
        for (int i = 0; i < roles.size(); i++) {
            String tmpRole = (String)roles.elementAt(i);
            if (role.toLowerCase().equals(tmpRole.toLowerCase()))
                return true;
        }
      
        return false;
    }

    public String getUsername() {
        return username;
    }

    public Connection getConnection() throws SQLException {
        return dbPool.getConnection(username, password);
    }
    
    public String[] getUserRoles() {
        if (roles == null)
            return new String[]{};
        else {
            int numRoles = roles.size();
            String[] roleArray = new String[numRoles];
            
            for (int i = 0; i < numRoles; i++) {
                roleArray[i] = (String)roles.elementAt(i);
            }
            
            return roleArray;
        }
    }

    public void invalidate() {
        authented = false;
        username = null;
        password = null;
        roles = null;
    }

    public String toString() {
        return username;
    }
}
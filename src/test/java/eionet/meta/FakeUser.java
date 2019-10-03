package eionet.meta;

import java.sql.Connection;

import org.apache.commons.lang.StringUtils;

import eionet.util.sql.ConnectionUtil;

public class FakeUser extends DDUser {

    /**
     *
     */
    public FakeUser() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.DDuser#authenticate(java.lang.String, java.lang.String)
     */
    public boolean authenticate(String userName, String userPws) {

        username = userName;
        fullName = userName;
        password = userPws;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.DDuser#isAuthentic()
     */
    public boolean isAuthentic() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.DDUser#getConnection()
     */
    public Connection getConnection() {

        try {
            return ConnectionUtil.getConnection();
        } catch (Exception e) {
            throw new DDRuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.DDuser#getUserRoles()
     */
    public String[] getUserRoles() {
        String[] ss = {};
        return ss;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.DDuser#invalidate()
     */
    public void invalidate() {
    }

    /**
     * Gives every permission to testUser
     *
     */
    @Override
    public boolean hasPermission(String aclPath, String permission) {
        if (StringUtils.equals("testUser", username)) {
            return true;
        }
        return super.hasPermission(aclPath, permission);
    }
}

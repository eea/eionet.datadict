package eionet.meta;

import eionet.meta.dao.domain.RegStatus;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public enum AttributeParentType {

    /** */
    DRAFT("Draft"), RELEASED("Released");

    /** */
    String s;

    /**
     *
     * @param s
     */
    AttributeParentType(String s) {
        this.s = s;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    public String toString() {
        return s;
    }

    /**
     *
     * @param s
     * @return
     */
    public static RegStatus fromString(String s) {
        for (RegStatus regStatus : RegStatus.values()) {
            if (regStatus.toString().equals(s)) {
                return regStatus;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public static RegStatus getDefault() {
        return RegStatus.DRAFT;
    }


}

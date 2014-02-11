/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */
package eionet.util;

import java.io.Serializable;

/**
 *
 * @author Enver
 *
 * @param <L>
 * @param <C>
 * @param <R>
 */
public class Triple<L, C, R> implements Serializable {

    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 2L;

    /** */
    private L left;
    private C central;
    private R right;

    /**
     * @param left
     * @param right
     */
    public Triple(L left, C central, R right) {        
        this.left = left;
        this.central = central;
        this.right = right;
    }

    /**
     * @return the left
     */
    public L getLeft() {
        return left;
    }

    /**
     * @param left
     *            the left to set
     */
    public void setLeft(L id) {
        this.left = id;
    }

    /**
     * @return the central
     */
    public C getCentral() {
        return central;
    }

    /**
     * @param central the central to set
     */
    public void setCentral(C central) {
        this.central = central;
    }

    /**
     * @return the right
     */
    public R getRight() {
        return right;
    }

    /**
     * @param right
     *            the right to set
     */
    public void setRight(R value) {
        this.right = value;
    }
}

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
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */
package eionet.util;

import java.io.Serializable;

/**
 * @param <L>
 * @param <C>
 * @param <R>
 * @author Enver
 */
public class Triple<L, C, R> implements Serializable {

    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Left Value.
     */
    private L left;
    /**
     * Central Value.
     */
    private C central;
    /**
     * Right Value.
     */
    private R right;

    /**
     * Class constructor.
     *
     * @param left    left
     * @param central central
     * @param right   right
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
     * @param left the left to set
     */
    public void setLeft(L left) {
        this.left = left;
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
     * @param right the right to set
     */
    public void setRight(R right) {
        this.right = right;
    }
}

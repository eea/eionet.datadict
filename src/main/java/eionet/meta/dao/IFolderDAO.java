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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.Folder;

/**
 * Folder DAO interface.
 *
 * @author Juhan Voolaid
 */
public interface IFolderDAO {

    /**
     * Returns folder.
     *
     * @param folderId
     * @return
     */
    Folder getFolder(int folderId);

    /**
     * Creates new folder.
     *
     * @param folder
     * @return
     */
    int createFolder(Folder folder);

    /**
     * Returns all folders.
     *
     * @return
     */
    List<Folder> getFolders();

    /**
     * Checks, if the folder identifier is unique.
     *
     * @param identifier
     * @param excludedId
     * @return
     */
    boolean isFolderUnique(String identifier, int excludedId);

    /**
     * True, when in the folder with given id, are vocabulary folders included.
     *
     * @param folderId
     * @return
     */
    boolean isFolderEmpty(int folderId);

    /**
     * Deletes folder.
     *
     * @param folderId
     */
    void deleteFolder(int folderId);

    /**
     * Updates folder.
     *
     * @param folder
     */
    void updateFolder(Folder folder);
}

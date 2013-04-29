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

package eionet.meta.service;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Helper methods for using DBUnit.
 *
 * @author Juhan Voolaid
 */
public class DBUnitHelper {

    /**
     * Inserts data from the xml file.
     *
     * @param xmlFileName
     * @throws Exception
     */
    public static void loadData(String xmlFileName) throws Exception {
        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection jdbcConnection =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));
        IDatabaseConnection con = new DatabaseConnection(jdbcConnection);

        InputStream is = VocabularyServiceTest.class.getClassLoader().getResourceAsStream(xmlFileName);
        IDataSet dataSet = new FlatXmlDataSetBuilder().build(is);
        DatabaseOperation.CLEAN_INSERT.execute(con, dataSet);

        con.close();
    }

    /**
     * Deletes data from xml file.
     *
     * @param xmlFileName
     * @throws Exception
     */
    public static void deleteData(String xmlFileName) throws Exception {
        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection jdbcConnection =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));
        IDatabaseConnection con = new DatabaseConnection(jdbcConnection);

        InputStream is = VocabularyServiceTest.class.getClassLoader().getResourceAsStream(xmlFileName);
        IDataSet dataSet = new FlatXmlDataSetBuilder().build(is);
        DatabaseOperation.DELETE_ALL.execute(con, dataSet);

        con.close();
    }
}

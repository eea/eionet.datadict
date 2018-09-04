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

import eionet.util.Props;
import eionet.util.PropsIF;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

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
        Properties properties = new Properties();
        properties.setProperty("http://www.dbunit.org/properties/datatypeFactory", "org.dbunit.ext.mysql.MySqlDataTypeFactory");

        Connection jdbcConnection =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));
        IDatabaseConnection con = new DatabaseConnection(jdbcConnection);
        con.getConfig().setPropertiesByString(properties);

        InputStream is = DBUnitHelper.class.getClassLoader().getResourceAsStream(xmlFileName);
        FlatXmlDataSet dataSet = new FlatXmlDataSetBuilder().build(is);

        ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
        replacementDataSet.addReplacementObject("[NULL]", null);

        DatabaseOperation.CLEAN_INSERT.execute(con, replacementDataSet);

        con.close();
    }

    /**
     * Deletes data from xml file.
     *
     * @param xmlFileName
     * @throws Exception
     */
    public static void deleteData(String xmlFileName) throws Exception {
        Connection jdbcConnection =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));
        IDatabaseConnection con = new DatabaseConnection(jdbcConnection);

        InputStream is = VocabularyServiceTestIT.class.getClassLoader().getResourceAsStream(xmlFileName);
        IDataSet dataSet = new FlatXmlDataSetBuilder().build(is);
        DatabaseOperation.DELETE_ALL.execute(con, dataSet);

        con.close();
    }

}

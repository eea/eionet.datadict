// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta.savers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.ITable;

import eionet.DDDatabaseTestCase;
import eionet.meta.FakeUser;

/**
 * This unittest tests the attributes.
 *
 * See www.easymock.org and http://www.evolutionnext.com/blog/2006/01/27.html
 */
public class AttributeHandlerTestIT extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-attributes.xml";
    }

    /**
     * Tests that a simple attribute with M_ATTRIBUTE_ID=37 is deleted.
     */
    public void testDeleteSimpleAttr() throws Exception {
        String attribute_to_delete = "37";

        FakeUser masterUser = new FakeUser();
        masterUser.authenticate("master", "master");

        QueryDataSet queryDataSet = new QueryDataSet(getConnection());

        queryDataSet.addTable("ATTRIBUTE", "SELECT count(*) as C FROM ATTRIBUTE");
        queryDataSet.addTable("M_ATTRIBUTE", "SELECT count(*) as C FROM M_ATTRIBUTE");
        queryDataSet.addTable("FXV", "SELECT count(*) as C FROM FXV");
        // Verify that there are the expected number of rows in the ATTRIBUTE table
        ITable tmpTable = queryDataSet.getTable("ATTRIBUTE");

        TestCase.assertEquals("757", tmpTable.getValue(0, "C").toString());
        // Verify that there are the expected number of rows in the M_ATTRIBUTE table
        tmpTable = queryDataSet.getTable("M_ATTRIBUTE");
        TestCase.assertEquals("2", tmpTable.getValue(0, "C").toString());
        tmpTable = queryDataSet.getTable("FXV");
        TestCase.assertEquals("20", tmpTable.getValue(0, "C").toString());

        // Create the mock objects
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletConfig servletConfig = createMock(ServletConfig.class);
        ServletContext servletContext = createMock(ServletContext.class);

        Connection jdbcConn = getConnection().getConnection();

        // This is what we expect for the servletContext object
        expect(servletContext.getInitParameter("visuals-path")).andReturn("HERE-IS-VISUALS-PATH");
        expect(servletContext.getInitParameter("versioning")).andReturn("HERE-IS-VERSIONING");

        // This is what we expect for the request object
        expect(request.getRequestedSessionId()).andReturn("92834kejwh89");
        expect(request.getParameter("mode")).andReturn("delete");
        expect(request.getParameter("attr_id")).andReturn(attribute_to_delete);
        expect(request.getParameter("name")).andReturn("Keywords");
        expect(request.getParameter("short_name")).andReturn("Keyword");
        expect(request.getParameter("definition")).andReturn("One or more significant words.");
        expect(request.getParameter("obligation")).andReturn("M");
        expect(request.getParameter("ns")).andReturn("basens");

        String[] vs = new String[1];
        vs[0] = attribute_to_delete;
        expect(request.getParameterValues("simple_attr_id")).andReturn(vs);

        // start the replay for all mock objects
        replay(request);
        replay(response);
        replay(servletConfig);
        replay(servletContext);

        // Create and execute the instance
        AttributeHandler instance = new AttributeHandler(jdbcConn, request, servletContext);
        instance.setUser(masterUser);
        instance.execute_();

        // verify the responses
        verify(request);
        verify(response);
        verify(servletConfig);
        verify(servletContext);

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("ATTRIBUTE", "SELECT count(*) as C FROM ATTRIBUTE");
        queryDataSet.addTable("M_ATTRIBUTE", "SELECT count(*) as C FROM M_ATTRIBUTE");
        queryDataSet.addTable("FXV", "SELECT count(*) as C FROM FXV");

        // Verify that there are the expected number of rows in the ATTRIBUTE table
        tmpTable = queryDataSet.getTable("ATTRIBUTE");
        TestCase.assertEquals("142", tmpTable.getValue(0, "C").toString());
        // Verify that there are the expected number of rows in the M_ATTRIBUTE table
        tmpTable = queryDataSet.getTable("M_ATTRIBUTE");
        TestCase.assertEquals("1", tmpTable.getValue(0, "C").toString());
        tmpTable = queryDataSet.getTable("FXV");
        TestCase.assertEquals("2", tmpTable.getValue(0, "C").toString());
    }

}

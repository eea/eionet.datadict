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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.meta.service;

import eionet.meta.ActionBeanUtils;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.Namespace;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.service.data.NamespaceFilter;
import eionet.meta.service.data.NamespaceResult;

/**
 * JUnit integration test with Unitils for namespace service.
 *
 * @author enver
 */
@SpringApplicationContext("mock-spring-context.xml")
public class NamespaceServiceTestIT extends UnitilsJUnit4 {

    private static final String SEED_FILE = "seed-namespaces.xml";

    /** Logger. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(NamespaceServiceTestIT.class);

    @SpringBeanByType
    private INamespaceService namespaceService;

    @BeforeClass
    public static void loadData() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData(SEED_FILE);
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData(SEED_FILE);
    }

    @Test
    public void testGetAllRDFNamespaces() throws ServiceException {
        List<RdfNamespace> rdfNamespaces = this.namespaceService.getRdfNamespaces();
        Assert.assertEquals("RDF Namespace list size does not match", 3, rdfNamespaces.size());
        String namePrefix = "ns";
        String uriPrefix = "http://namespace";
        String uriSuffix = ".somewhere.com";
        for (int i = 0, rowNum = 1; i < rdfNamespaces.size(); i++, rowNum++) {
            RdfNamespace ns = rdfNamespaces.get(i);
            Assert.assertEquals("ID does not match", rowNum, ns.getId());
            Assert.assertEquals("Prefix does not match", namePrefix + rowNum, ns.getPrefix());
            Assert.assertEquals("URI does not match", uriPrefix + rowNum + uriSuffix, ns.getUri());
        }
    } // end of step testAllRDFNamespaces

    @Test
    public void testGetAllNamespaces() throws ServiceException {
        NamespaceFilter filter = new NamespaceFilter();
        filter.setUsePaging(false);
        filter.setPageNumber(5);

        NamespaceResult namespaceResult = this.namespaceService.getNamespaces(filter);
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getFullListSize());
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getTotalItems());
        List<Namespace> namespaces = namespaceResult.getList();
        Assert.assertEquals("Namespace paged result list size does not match", 9, namespaces.size());

        String shortNamePrefix = "short_name_";
        String fullNamePrefix = "Full Name ";
        String namePrefix = "dd";
        for (int i = 0, rowNum = 1; i < namespaces.size(); i++, rowNum++) {
            Namespace ns = namespaces.get(i);
            Assert.assertEquals("ID does not match", String.valueOf(rowNum), ns.getID());
            Assert.assertEquals("Short name does not match", shortNamePrefix + rowNum, ns.getShortName());
            Assert.assertEquals("Full name  does not match", fullNamePrefix + rowNum, ns.getFullName());
            Assert.assertEquals("Prefix does not match", namePrefix + rowNum, ns.getPrefix());
        }
    } // end of step testAllRDFNamespaces

    @Test
    public void testGetAllNamespacesWithDefaultValues() throws ServiceException {
        NamespaceFilter filter = new NamespaceFilter(); //when default values are used, pagesize is 20 so all should return

        NamespaceResult namespaceResult = this.namespaceService.getNamespaces(filter);
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getFullListSize());
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getTotalItems());
        List<Namespace> namespaces = namespaceResult.getList();
        Assert.assertEquals("Namespace paged result list size does not match", 9, namespaces.size());

        String shortNamePrefix = "short_name_";
        String fullNamePrefix = "Full Name ";
        String namePrefix = "dd";
        for (int i = 0, rowNum = 1; i < namespaces.size(); i++, rowNum++) {
            Namespace ns = namespaces.get(i);
            Assert.assertEquals("ID does not match", String.valueOf(rowNum), ns.getID());
            Assert.assertEquals("Short name does not match", shortNamePrefix + rowNum, ns.getShortName());
            Assert.assertEquals("Full name  does not match", fullNamePrefix + rowNum, ns.getFullName());
            Assert.assertEquals("Prefix does not match", namePrefix + rowNum, ns.getPrefix());
        }
    } // end of step testGetAllNamespacesWithDefaultValues

    @Test
    public void testGetAllNamespacesWithPageSize() throws ServiceException {
        NamespaceFilter filter = new NamespaceFilter();
        filter.setPageSize(5); //first 5 results should return

        NamespaceResult namespaceResult = this.namespaceService.getNamespaces(filter);
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getFullListSize());
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getTotalItems());
        List<Namespace> namespaces = namespaceResult.getList();
        Assert.assertEquals("Namespace paged result list size does not match", 9, namespaces.size());

        String shortNamePrefix = "short_name_";
        String fullNamePrefix = "Full Name ";
        String namePrefix = "dd";
        for (int i = 0, rowNum = 1; i < namespaces.size(); i++, rowNum++) {
            Namespace ns = namespaces.get(i);
            Assert.assertEquals("ID does not match", String.valueOf(rowNum), ns.getID());
            Assert.assertEquals("Short name does not match", shortNamePrefix + rowNum, ns.getShortName());
            Assert.assertEquals("Full name  does not match", fullNamePrefix + rowNum, ns.getFullName());
            Assert.assertEquals("Prefix does not match", namePrefix + rowNum, ns.getPrefix());
        }
    } // end of step testGetAllNamespacesWithPageSize

    @Test
    public void testGetAllNamespacesWithPageSizeAndPageNumber() throws ServiceException {
        NamespaceFilter filter = new NamespaceFilter();
        filter.setPageNumber(1);
        filter.setPageSize(5); //last 4 results should return

        NamespaceResult namespaceResult = this.namespaceService.getNamespaces(filter);
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getFullListSize());
        Assert.assertEquals("Namespace list size does not match", 9, namespaceResult.getTotalItems());
        
        List<Namespace> namespaces = namespaceResult.getList();

        Assert.assertEquals("Namespace paged result list size does not match", 9, namespaces.size());

        String shortNamePrefix = "short_name_";
        String fullNamePrefix = "Full Name ";
        String namePrefix = "dd";
        for (int i = 0, rowNum = 1; i < namespaces.size(); i++, rowNum++) {
            Namespace ns = namespaces.get(i);
            Assert.assertEquals("ID does not match", String.valueOf(rowNum), ns.getID());
            Assert.assertEquals("Short name does not match", shortNamePrefix + rowNum, ns.getShortName());
            Assert.assertEquals("Full name  does not match", fullNamePrefix + rowNum, ns.getFullName());
            Assert.assertEquals("Prefix does not match", namePrefix + rowNum, ns.getPrefix());
        }
    } // end of step testGetAllNamespacesWithPageSize


} // end of class NamespaceServiceTest

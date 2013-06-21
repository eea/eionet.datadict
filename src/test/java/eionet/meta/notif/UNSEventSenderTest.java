package eionet.meta.notif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.Namespace;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Tests for the {@link UNSEventSender}.
 *
 * @author jaanus
 */
public class UNSEventSenderTest {

    /**
     * @throws InterruptedException
     *
     */
    @Test
    public void testCommonElementDefinitionChanged() throws InterruptedException {

        DataElement elm = mockCommonDataElement();
        String eventType = Subscriber.COMMON_ELEMENT_CHANGED_EVENT;
        String user = "obama";

        HashSet<String> expectedPredicates = new HashSet<String>();
        expectedPredicates.add(Props.getProperty(Subscriber.PROP_UNS_COMMONELEM_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_DEFINITION_URL_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_DEFINITION_STATUS_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_USER_PREDICATE));
        expectedPredicates.add(Props.getProperty(Subscriber.PROP_UNS_EVENTTYPE_PREDICATE));
        expectedPredicates.add(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE));
        expectedPredicates.add(Props.getProperty(PropsIF.PREDICATE_RDF_TYPE));

        UNSEventSenderMock unsEventSender = new UNSEventSenderMock();
        unsEventSender.definitionChanged(elm, eventType, user);
        postCallAssertions(expectedPredicates, null, unsEventSender);
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void testNonCommonElementDefinitionChanged() throws InterruptedException {

        DataElement elm = mockNonCommonDataElement();
        String eventType = Subscriber.COMMON_ELEMENT_CHANGED_EVENT;
        String user = "obama";

        HashSet<String> expectedPredicates = new HashSet<String>();
        expectedPredicates.add(Props.getProperty(Subscriber.PROP_UNS_COMMONELEM_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_DEFINITION_URL_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_USER_PREDICATE));
        expectedPredicates.add(Props.getProperty(Subscriber.PROP_UNS_EVENTTYPE_PREDICATE));
        expectedPredicates.add(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE));
        expectedPredicates.add(Props.getProperty(PropsIF.PREDICATE_RDF_TYPE));

        HashSet<String> unexpectedPredicates = new HashSet<String>();
        unexpectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_DEFINITION_STATUS_PREDICATE));

        UNSEventSenderMock unsEventSender = new UNSEventSenderMock();
        unsEventSender.definitionChanged(elm, eventType, user);
        postCallAssertions(expectedPredicates, unexpectedPredicates, unsEventSender);
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void testDatasetDefinitionChanged() throws InterruptedException {

        Dataset dst = mockDataset();
        String eventType = Subscriber.COMMON_ELEMENT_CHANGED_EVENT;
        String user = "obama";

        HashSet<String> expectedPredicates = new HashSet<String>();
        expectedPredicates.add(Props.getProperty(Subscriber.PROP_UNS_DATASET_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_DEFINITION_URL_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_DEFINITION_STATUS_PREDICATE));
        expectedPredicates.add(Props.getProperty(UNSEventSender.PROP_UNS_USER_PREDICATE));
        expectedPredicates.add(Props.getProperty(Subscriber.PROP_UNS_EVENTTYPE_PREDICATE));
        expectedPredicates.add(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE));
        expectedPredicates.add(Props.getProperty(PropsIF.PREDICATE_RDF_TYPE));

        UNSEventSenderMock unsEventSender = new UNSEventSenderMock();
        unsEventSender.definitionChanged(dst, eventType, user);
        postCallAssertions(expectedPredicates, null, unsEventSender);
    }

    /**
     *
     */
    @Test
    public void testSiteCodesAdded() {

        SiteCodeAddedNotification notif = new SiteCodeAddedNotification();
        UNSEventSenderMock unsEventSender = new UNSEventSenderMock();
        unsEventSender.siteCodesAdded(notif, "obama");

        // Assert that there was NO XmlRpcClient created.
        // TODO change this after unsEventSender.siteCodesAdded(...) has been properly implemented.
        XmlRpcClientMock client = unsEventSender.getRpcClientMock();
        assertNull("XmlRpcClient null-check", client);
    }

    /**
     *
     */
    @Test
    public void testSiteCodesAllocated() {

        SiteCodeAllocationNotification notif = new SiteCodeAllocationNotification();
        UNSEventSenderMock unsEventSender = new UNSEventSenderMock();
        unsEventSender.siteCodesAllocated(notif, "obama");

        // Assert that there was NO XmlRpcClient created.
        // TODO change this after unsEventSender.siteCodesAllocated(...) has been properly implemented.
        XmlRpcClientMock client = unsEventSender.getRpcClientMock();
        assertNull("XmlRpcClient null-check", client);
    }

    /**
     *
     * @return
     */
    private DataElement mockNonCommonDataElement() {

        DataElement elm = new DataElement("1", "shortName", "CH1");
        elm.setIdentifier("identifier");
        elm.setNamespace(new Namespace("11", "shortName", "fullName", "url", "description"));
        elm.setDatasetID("2");
        elm.setDstIdentifier("dstIdentifier");
        elm.setTableID("3");
        elm.setTblIdentifier("tblIdentifier");
        return elm;
    }

    /**
     *
     * @return
     */
    private DataElement mockCommonDataElement() {

        DataElement elm = new DataElement("1", "shortName", "CH1");
        elm.setIdentifier("identifier");
        elm.setStatus("Released");
        return elm;
    }

    /**
     *
     * @return
     */
    private Dataset mockDataset() {

        Dataset dst = new Dataset("1", "shortName", "1");
        dst.setIdentifier("identifier");
        dst.setStatus("Released");
        return dst;
    }

    /**
     *
     * @param expectedPredicates
     * @param unsEventSender
     * @throws InterruptedException
     */
    private void postCallAssertions(HashSet<String> expectedPredicates, HashSet<String> unexpectedPredicates,
            UNSEventSenderMock unsEventSender)
            throws InterruptedException {

        // Assert that there was an XmlRpcClient created.
        XmlRpcClientMock client = unsEventSender.getRpcClientMock();
        assertNotNull("XmlRpcClient null-check", client);

        // Assert various properties of the created XmlRpcClient
        String serverUrl = client.getURL() == null ? null : client.getURL().toString();
        assertEquals("XmlRpcClient server URL", Props.getProperty(Subscriber.PROP_UNS_XMLRPC_SERVER_URL), serverUrl);

        assertEquals("XmlRpcClient user-name", Props.getProperty(Subscriber.PROP_UNS_USERNAME), client.getUser());
        assertEquals("XmlRpcClient password", Props.getProperty(Subscriber.PROP_UNS_PASSWORD), client.getPassword());

        // Wait just in case, because client call will be issued by another thread.
        Thread.sleep(2000);

        // Assert that the XmlRpcClient was executed, with the expected method.
        assertTrue("XmlRpcClient.execute called", client.isExecuteCalled());
        assertEquals("XmlRpcClient method", Props.getProperty(Subscriber.PROP_UNS_SEND_NOTIFICATION_FUNC),
                client.getLastCalledMethod());

        // Assert that an expected set of parameters were sent by the XmlRpcClient.
        Vector params = client.getLastCalledParams();
        assertNotNull("RPC call params null-check", params);
        assertEquals("RPC call params size-check", 2, params.size());

        assertEquals("Notification channel:", Props.getProperty(Subscriber.PROP_UNS_CHANNEL_NAME), params.get(0));
        assertTrue("Notification triples type-check", params.get(1) instanceof Vector);

        assertTriplesPredicates(expectedPredicates, unexpectedPredicates, (Vector) params.get(1));
    }

    /**
     *
     * @param expectedPredicates
     */
    private void assertTriplesPredicates(Collection<String> expectedPredicates, HashSet<String> unexpectedPredicates,
            Vector notificationTriples) {

        HashSet<String> actualPredicates = new HashSet<String>();
        for (Object notificationTriple : notificationTriples) {
            assertTrue("Notification triple type-check", notificationTriple instanceof Vector);
            Vector tripleVector = (Vector) notificationTriple;
            assertEquals("Notification triple size", 3, tripleVector.size());
            String actualPredicate = tripleVector.get(1).toString();
            assertTrue("Notification triple blank-check", StringUtils.isNotBlank(actualPredicate));
            actualPredicates.add(actualPredicate);
        }

        for (String expectedPredicate : expectedPredicates) {
            assertTrue("existence of " + expectedPredicate, actualPredicates.contains(expectedPredicate));
        }

        if (unexpectedPredicates != null) {
            for (String unexpectedPredicate : unexpectedPredicates) {
                assertTrue("non-existence of " + unexpectedPredicate, !actualPredicates.contains(unexpectedPredicate));
            }
        }
    }
}

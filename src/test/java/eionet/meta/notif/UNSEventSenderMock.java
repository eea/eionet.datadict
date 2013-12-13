package eionet.meta.notif;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcClient;

/**
 * A mock of {@link UNSEventSender} that uses the "Subclass and Override Methods" principle to insert desired dependencies.
 *
 * @author jaanus
 */
public class UNSEventSenderMock extends UNSEventSender {

    /** */
    private XmlRpcClientMock rpcClientMock;

    /** */
    private boolean dontCallActually = false;

    /**
     * Default constructor.
     */
    public UNSEventSenderMock() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see eionet.meta.notif.UNSEventSender#newXmlRpcClient(java.lang.String)
     */
    @Override
    protected XmlRpcClient newXmlRpcClient(String serverURL) throws MalformedURLException {

        rpcClientMock = new XmlRpcClientMock(serverURL);
        return rpcClientMock;
    }

    /*
     * (non-Javadoc)
     * @see eionet.meta.notif.UNSEventSender#dontCallActually()
     */
    @Override
    protected boolean isSendingDisabled() {
        return dontCallActually;
    }

    /**
     * @return the rpcClientMock
     */
    public XmlRpcClientMock getRpcClientMock() {
        return rpcClientMock;
    }

    /**
     * @param dontCallActually the dontCallActually to set
     */
    public void setDontCallActually(boolean dontCallActually) {
        this.dontCallActually = dontCallActually;
    }
}

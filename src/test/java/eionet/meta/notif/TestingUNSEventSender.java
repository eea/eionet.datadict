package eionet.meta.notif;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcClient;

/**
 *
 * Type definition ...
 *
 * @author jaanus
 */
public class TestingUNSEventSender extends UNSEventSender {

    /** */
    private XmlRpcClientMock rpcClientMock;

    /** */
    private boolean dontCallActually = false;

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
    protected boolean dontCallActually() {
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

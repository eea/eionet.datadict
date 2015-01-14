package eionet.meta.inservices;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import eionet.rpcclient.ServiceClientIF;
import eionet.rpcclient.ServiceClients;

import eionet.util.Props;
import eionet.util.Util;

public abstract class InServiceClient implements InServiceClientIF{

    protected String serviceName = null;
    protected String serviceUrl  = null;
    protected String serviceUsr  = null;
    protected String servicePsw  = null;
    protected ServiceClientIF client = null;

    protected void load() throws Exception {

        if (Util.isEmpty(serviceName) || Util.isEmpty(serviceUrl))
            throw new Exception("serviceName or serviceUrl is missing!");

        client = ServiceClients.getServiceClient(serviceName, serviceUrl);
        if (!Util.isEmpty(serviceUsr) && !Util.isEmpty(serviceUsr))
            client.setCredentials(serviceUsr, servicePsw);
    }

    protected void getProps(String clientName) {

        String prefix = Props.INSERV_PREFIX + clientName;
        serviceName = Props.getProperty(prefix + Props.INSERV_NAME);
        serviceUrl  = Props.getProperty(prefix + Props.INSERV_URL);
        serviceUsr  = Props.getProperty(prefix + Props.INSERV_USR);
        servicePsw  = Props.getProperty(prefix + Props.INSERV_PSW);
    }

    protected Object execute(String method, Vector params) throws Exception {

        if (client == null) load();
        return client.getValue(method, params);
    }

    public abstract void execute(HttpServletRequest req) throws Exception;
}

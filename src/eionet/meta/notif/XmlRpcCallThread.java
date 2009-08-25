package eionet.meta.notif;

import java.io.IOException;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class XmlRpcCallThread extends Thread{
	
	/** */
	private XmlRpcClient client;
	private String methodName;
	private Vector params;
	
	/**
	 * 
	 * @param client
	 * @param methodName
	 * @param params
	 */
	public XmlRpcCallThread(XmlRpcClient client, String methodName, Vector params){
		
		this.client = client;
		this.methodName = methodName;
		this.params = params;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		
		try{
			client.execute(methodName, params);
		}
		catch (XmlRpcException e){
			e.printStackTrace(System.out);
		}
		catch (IOException e){
			e.printStackTrace(System.out);
		}
	}
}
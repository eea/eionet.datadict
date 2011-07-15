package eionet.meta.inservices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.util.Util;

public class InServicesServlet extends HttpServlet {

    private Hashtable clients = new Hashtable();

    public void service(HttpServletRequest req, HttpServletResponse res)
                                            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        try {
            act(req, res);
        }
        catch (Exception e) {
            handleError(e, req, res);
            return;
        }

        dispatch(req,res);
    }

    private void act(HttpServletRequest req, HttpServletResponse res) throws Exception {

        String clientName = req.getParameter(Params.CLIENT);
        if (Util.voidStr(clientName))
            throw new Exception(Params.CLIENT + " is missing!");

        InServiceClientIF client = (InServiceClientIF)clients.get(clientName);
        if (client == null) {
            if (clientName.equals(WebrodClient.NAME))
                client = new WebrodClient();
            else
                throw new Exception("Unknown client " + clientName);
            clients.put(clientName, client);
        }

        client.execute(req);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse res)
                                                    throws ServletException,IOException {

        String errHandler = (String)req.getAttribute(Attrs.ERR_HANDLER);
        if (Util.voidStr(errHandler)) errHandler = "error.jsp";

        req.getRequestDispatcher(errHandler).forward(req,res);
    }

    private void handleError(Exception e, HttpServletRequest req, HttpServletResponse res)
                                                    throws ServletException,IOException {

        req.setAttribute("DD_ERR_MSG", e.toString());
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bytesOut));
        req.setAttribute("DD_ERR_TRC", bytesOut.toString());
        handleError(req, res);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse res)
                                                throws ServletException, IOException  {

        String dispatcher = (String)req.getAttribute(Attrs.DISPATCHER);
        req.getRequestDispatcher(dispatcher).forward(req,res);
    }
}

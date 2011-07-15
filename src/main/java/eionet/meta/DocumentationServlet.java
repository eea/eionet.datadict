package eionet.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tee.uit.help.Area;
import com.tee.uit.help.HelpException;
import com.tee.uit.help.Helps;
import com.tee.uit.help.Screen;

import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/**
 * 
 * @author heinljab
 */
public class DocumentationServlet extends HttpServlet{
    
    public static final String FORWARD_JSP = "forward-jsp";
    
    /** */
    public static final String DOC_STRING = "doc-string";
    public static final String DOC_HEADING = "doc-heading";
    public static final String DOCS_LIST = "docs-list";
    public static final String DISPATCHER_PATH = "dispatcher-path";
    
    /** */
    public static final String UNTITLED = "Untitled documentation";
    
    /** */
    public static final String DEFAULT_SCREEN_NAME = "documentation";
    
    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        this.doGet(req, res);
    }

    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        
        String pathInfo = req.getPathInfo();
        try{
            if (pathInfo==null || pathInfo.trim().length()==0)
                req.setAttribute(DOCS_LIST, listDocs());
            else{
                List l = slicePathInfo(pathInfo);
                if (l.size()==0)
                    throw new Exception("No path info in request object");
                else
                    getDocStringAndHeading((String)l.get(0), req);
            }
        }
        catch (Exception e){
            req.setAttribute("DD_ERR_MSG", Util.getStack(e));
            req.getRequestDispatcher("error.jsp").forward(req, res);
        }
        
        String servletPath = req.getServletPath();
        if (servletPath==null || servletPath.equals("/"))
            servletPath = "";
        else if (servletPath.length()>1)
            servletPath = servletPath.substring(1);
        
        req.setAttribute(DISPATCHER_PATH, servletPath);
        getServletContext().getRequestDispatcher("/" + getInitParameter(FORWARD_JSP)).forward(req, res);
    }
    
    /**
     * 
     * @return
     * @throws HelpException 
     */
    public static List listDocs() throws HelpException{
        
        List result = new ArrayList();
        Hashtable screens = Helps.getHelps();
        if(screens != null){
            Screen screen = (Screen)screens.get(getScreenName());
            if(screen != null){
                Hashtable areas = screen.getAreas();
                if(areas != null && areas.size()>0){
                    for(Iterator iter = areas.values().iterator(); iter.hasNext();){
                        Area area = (Area)iter.next();
                        Properties props = new Properties();
                        props.setProperty("id", area.getID());
                        props.setProperty("heading", getDocHeading(area));
                        result.add(props);
                    }
                }
            }
        }

        Collections.sort(result, new DocPropertiesComparator());
        return result;
    }
    
    /**
     * 
     * @param docID
     * @return
     * @throws HelpException 
     */
    private void getDocStringAndHeading(String docID, HttpServletRequest request) throws HelpException{
        
        Hashtable screens = Helps.getHelps();
        if(screens != null){
            Screen screen = (Screen)screens.get(getScreenName());
            if(screen != null){
                Area area = screen.getArea(docID);
                if (area!=null){
                    request.setAttribute(DOC_HEADING, getDocHeading(area));
                    request.setAttribute(DOC_STRING, area.getHTML(null));
                }
            }
        }
    }
    
    /**
     * 
     * @param area
     * @return
     */
    private static String getDocHeading(Area area){
        String docHeading = area.getDescription();
        if (docHeading==null || docHeading.trim().length()==0)
            docHeading = getDocHeading(area.getHTML(null));
        return docHeading;
    }
    
    /**
     * 
     * @param pathInfo
     * @return
     */
    private List slicePathInfo(String pathInfo){
        
        List result = new ArrayList();
        for (StringTokenizer st = new StringTokenizer(pathInfo, "/"); st.hasMoreTokens(); result.add(st.nextToken()));
        return result;
    }
    
    /**
     * 
     * @param docString
     * @return
     */
    private static String getDocHeading(String docString){
        
        int i;
        int index = -1;
        String docStringUpperCase = docString.toUpperCase();
        for (i=1; i<=5; i++){
            index = docStringUpperCase.indexOf("<H" + i + ">");
            if (index>=0)
                break;
        }
        
        String result = UNTITLED;
        if (index>=0){
            int j = docStringUpperCase.indexOf("</H" + i + ">", index);
            if (j>0)
                result = docString.substring(index+4, j).trim();
        }
        
        return result;
    }
    
    /**
     * 
     * @return
     */
    public static String getScreenName(){
        String screenName = Props.getProperty(PropsIF.SCREEN_NAME);
        if (screenName==null)
            screenName = DEFAULT_SCREEN_NAME;
        return screenName;
    }
    
    /**
     * 
     * @author heinljab
     *
     */
    private static class DocPropertiesComparator implements Comparator{
        
        /*
         * 
         */
        public int compare(Object o1, Object o2){
            return (((Properties)o1).getProperty("id")).compareTo(((Properties)o2).getProperty("id"));
        }
    }
}

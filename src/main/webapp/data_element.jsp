<%@page import="eionet.meta.dao.domain.VocabularyConcept"%>
<%@page import="eionet.datadict.model.DataDictEntity"%>
<%@page import="eionet.meta.dao.mysql.DataElementDAOImpl"%>
<%@page import="eionet.meta.dao.domain.InferenceRule"%>
<%@page import="eionet.meta.notif.Subscriber"%>
<%@page contentType="text/html;charset=UTF-8" import="java.net.URLEncoder,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.meta.dao.domain.VocabularyFolder,eionet.util.*,eionet.util.sql.ConnectionUtil,java.io.*,javax.servlet.http.HttpUtils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final int MAX_CELL_LEN = 40;%>
<%!private static final int MAX_ATTR_LEN = 500;%>
<%!private static final int MAX_DISP_VALUES = 30;%>

<%!// servlet-scope helper functions
    //////////////////////////////////

    /**
     *
     */
    private DElemAttribute getAttributeByName(String name, Vector mAttributes) {

        for (int i = 0; mAttributes != null && i < mAttributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute) mAttributes.get(i);
            //if (attr.getName().equalsIgnoreCase(name))
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr;
        }

        return null;
    }

    /**
     *
     */
    private String getAttributeIdByName(String name, Vector mAttributes) {

        for (int i = 0; mAttributes != null && i < mAttributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute) mAttributes.get(i);
            //if (attr.getName().equalsIgnoreCase(name))
            if (attr.getShortName().equalsIgnoreCase(name))
                return attr.getID();
        }

        return null;
    }

    /**
     *
     */
    private String getValue(String id, String mode, DataElement dataElement,
            DataElement newDataElement) {
        return getValue(id, 0, mode, dataElement, newDataElement);
    }

    /**
     *  int val indicates which type of value is requested. the default is 0
     *  0 - display value (if original value is null, then show inherited value)
     *  1 - original value
     *  2 - inherited value
     */
    private String getValue(String id, int val, String mode,
            DataElement dataElement, DataElement newDataElement) {

        if (id == null)
            return null;
        DElemAttribute attr = null;

        if (mode.equals("add")) {
            if (val < 2)
                return null;
            else {
                if (newDataElement == null)
                    return null;
                attr = newDataElement.getAttributeById(id);
            }
        } else {
            if (dataElement == null)
                return null;
            attr = dataElement.getAttributeById(id);
        }

        if (attr == null)
            return null;
        if (val == 1)
            return attr.getOriginalValue();
        else if (val == 2)
            return attr.getInheritedValue();
        else
            return attr.getValue();
    }

    /**
     *
     */
    private Vector getValues(String id, String mode, DataElement dataElement,
            DataElement newDataElement) {
        return getValues(id, 0, mode, dataElement, newDataElement);
    }

    /**
     *  int val indicates which group of values is requested. the default is 0
     *  0 - all
     *  1 - original
     *  2 - inherited
     */
    private Vector getValues(String id, int val, String mode,
            DataElement dataElement, DataElement newDataElement) {
        if (id == null)
            return null;
        DElemAttribute attr = null;

        if (mode.equals("add")) {
            if (val < 2)
                return null;
            else {
                if (newDataElement == null)
                    return null;
                attr = newDataElement.getAttributeById(id);
            }
        } else {
            if (dataElement == null)
                return null;
            attr = dataElement.getAttributeById(id);
        }

        if (attr == null)
            return null;
        if (val == 1)
            return attr.getOriginalValues();
        else if (val == 2)
            return attr.getInheritedValues();
        else
            return attr.getValues();
    }

    /**
     *
     */
    private String getAttributeObligationById(String id, Vector mAttributes) {

        for (int i = 0; mAttributes != null && i < mAttributes.size(); i++) {
            DElemAttribute attr = (DElemAttribute) mAttributes.get(i);
            if (attr.getID().equalsIgnoreCase(id))
                return attr.getObligation();
        }

        return null;
    }

    /**
     *
     */
    private String legalizeAlert(String in) {

        in = (in != null ? in : "");
        StringBuffer ret = new StringBuffer();

        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'')
                ret.append("\\'");
            else if (c == '\\')
                ret.append("\\\\");
            else
                ret.append(c);
        }

        return ret.toString();
    }%>

<%
    // implementation of the servlet's service method
    //////////////////////////////////////////////////

    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    String mode = null;
    Vector mAttributes = null;
    DataElement dataElement = null;
    DataElement newDataElement = null;
    Vector complexAttrs = null;
    Vector fixedValues = null;
    String feedbackValue = null;
    ArrayList<InferenceRule> dataElementRules = null;

    ServletContext ctx = getServletContext();
    DDUser user = SecurityUtil.getUser(request);

     // Feedback messages
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("checkout")) {
        feedbackValue = "Working copy successfully created!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("checkin")) {
        feedbackValue = "Check-in successful!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("undo_checkout")) {
        feedbackValue = "Working copy successfully discarded!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("switch_type")) {
        feedbackValue = "Type switch successful!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("delete")) {
        feedbackValue = "Deletion successful!";
    }
    if (request.getParameter("feedback") != null && request.getParameter("feedback").equals("subscribe")) {
        feedbackValue = "Subscription successful!";
    }

    // POST request not allowed for anybody who hasn't logged in
    if (request.getMethod().equals("POST") && user == null) {
        request.setAttribute("DD_ERR_MSG",
                "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp").forward(request,
                response);
        return;
    }
%>
    <%@ include file="history.jsp" %>
    <%
        // init the flag indicating if this is a common element
        boolean elmCommon = request.getParameter("common") != null;

        // get values of several request parameters:
        // - mode
        // - delem_id
        // - element_idf
        // - copy_elem_id
        // - ds_id
        // - table_id
        // - type (indicates whether element is fixed values or quantitative)
        mode = request.getParameter("mode");
        String delem_id = request.getParameter("delem_id");
        String delem_name = request.getParameter("delem_name");
        String copy_elem_id = request.getParameter("copy_elem_id");
        String dsID = request.getParameter("ds_id");
        String tableID = request.getParameter("table_id");

        // indicates whether element is fixed values or quantitative
        String type = request.getParameter("type");
        if (type != null && type.length() == 0) {
            type = null;
        }

        // for historic reasons reference URL uses "element_idf" while as internally DD pages are used to "idfier"
        String idfier = "";
        String delemIdf = request.getParameter("element_idf");
        if (delemIdf != null && delemIdf.length() > 0){
            idfier = delemIdf;
        }
        else if (request.getParameter("idfier") != null){
            idfier = request.getParameter("idfier");
        }

        String tableIdf = request.getParameter("table_idf");
        String datasetIdf = request.getParameter("dataset_idf");

        // check missing request parameters
        if (mode == null || mode.trim().length() == 0) {
            mode = "view";
        }

        // security for add common element
        if (mode.equals("add") && elmCommon) {
            if (user == null
                    || !SecurityUtil.hasPerm(user.getUserName(),
                            "/elements", "i")) {
                request.setAttribute("DD_ERR_MSG",
                        "You have no permission to create new common element!");
                request.getRequestDispatcher("error.jsp").forward(request,
                        response);
                return;
            }
        }

        if (mode.equals("add") && !elmCommon) {
            if (Util.isEmpty(tableID)) {
                request.setAttribute("DD_ERR_MSG",
                        "Missing request parameter: table_id");
                request.getRequestDispatcher("error.jsp").forward(request,
                        response);
                return;
            }
        } else if (mode.equals("view")) {
            if (Util.isEmpty(delem_id) && Util.isEmpty(delemIdf)) {
                request.setAttribute("DD_ERR_MSG",
                        "Missing request parameter: delem_id or element_idf");
                request.getRequestDispatcher("error.jsp").forward(request,
                        response);
                return;
            }
        } else if (mode.equals("edit")) {
            if (Util.isEmpty(delem_id)) {
                request.setAttribute("DD_ERR_MSG",
                        "Missing request parameter: delem_id");
                request.getRequestDispatcher("error.jsp").forward(request,
                        response);
                return;
            }
        } else if (mode.equals("copy")) {
            if (Util.isEmpty(copy_elem_id)) {
                request.setAttribute("DD_ERR_MSG",
                        "Missing request parameter: copy_elem_id");
                request.getRequestDispatcher("error.jsp").forward(request,
                        response);
                return;
            }
        }

        // as of Sept 2006,  parameter "action" is a helper to add some extra context to parameter "mode"
        String action = request.getParameter("action");
        if (action != null && action.trim().length() == 0)
            action = null;

        //// handle the POST request //////////////////////
        //////////////////////////////////////////////////
        if (request.getMethod().equals("POST")) {

            DataElementHandler handler = null;
            Connection userConn = null;
            try {
                userConn = user.getConnection();
                handler = new DataElementHandler(userConn, request, ctx);
                handler.setUser(user);
                try {
                    handler.execute();
                } catch (Exception e) {
                    handler.cleanup();
                    String msg = e.getMessage();
                    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                    e.printStackTrace(new PrintStream(bytesOut));
                    String trace = bytesOut.toString(response
                            .getCharacterEncoding());
                    request.setAttribute("DD_ERR_MSG", msg);
                    request.setAttribute("DD_ERR_TRC", trace);
                    String backLink = request.getParameter("submitter_url");
                    if (backLink == null || backLink.length() == 0)
                        backLink = history.getBackUrl();
                    request.setAttribute("DD_ERR_BACK_LINK", backLink);
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }
            } finally {
                try {
                    if (userConn != null)
                        userConn.close();
                } catch (SQLException e) {
                }
            }

            // disptach the POST request
            ////////////////////////////
            String redirUrl = null;
            if (mode.equals("add") || mode.equals("copy")) {

                String id = handler.getLastInsertID();
                if (id != null && id.length() > 0){
                    redirUrl = request.getContextPath() + "/dataelements/" + id;
                }
            }
            else if (mode.equals("edit")) {

                boolean isSaveClose = request.getParameter("saveclose")!=null && request.getParameter("saveclose").equalsIgnoreCase("true");
                boolean isCheckIn = request.getParameter("check_in")!=null && request.getParameter("check_in").equalsIgnoreCase("true");
                boolean isSwitchType = request.getParameter("switch_type")!=null && request.getParameter("switch_type").equalsIgnoreCase("true");

                String id = isCheckIn ? handler.getCheckedInCopyID() : delem_id;
                redirUrl = request.getContextPath() + "/dataelements/" + id;
                if (!isSaveClose && !isCheckIn && !isSwitchType){
                    redirUrl = redirUrl + "/edit";
                }
                if (isCheckIn){
                    redirUrl = redirUrl + "/?feedback=checkin";
                }
                else if (isSwitchType){
                    redirUrl = redirUrl + "/?feedback=switch_type";
                }

            } else if (mode.equals("delete")) {

                if (elmCommon){
                    String checkedoutCopyID = request.getParameter("checkedout_copy_id");
                    String wasWorkingCopy = request.getParameter("is_working_copy");
                    if (checkedoutCopyID != null && !checkedoutCopyID.isEmpty()) {
                        redirUrl = request.getContextPath() + "/dataelements/" + checkedoutCopyID + "/?feedback=undo_checkout";
                    }
                    else if (wasWorkingCopy != null && wasWorkingCopy.equals("true")){
                        redirUrl = request.getContextPath() + "/search.jsp?feedback=undo_checkout";
                    }
                    else {
                        redirUrl = request.getContextPath() + "/search.jsp?feedback=delete";
                    }
                }
                else if (tableID!=null && !tableID.isEmpty()){
                    redirUrl = request.getContextPath() + "/tables/" + tableID;
                }
            }

            response.sendRedirect(redirUrl);
            return;
        }
        //// end of handle the POST request //////////////////////
        //////////////////////////////////////////////////////////

        // if requested by alphanumeric identifier and not by auto-generated id,
        // then it means the common element's latest version is requested
        boolean isLatestRequested = mode.equals("view")
                && !Util.isEmpty(delemIdf) && Util.isEmpty(delem_id);

        Dataset dataset = null;
        DsTable dsTable = null;
        VocabularyFolder vocabulary = null;
        String dstWorkingUser = null;
        String elmWorkingUser = null;
        String elmRegStatus = null;
        VersionManager verMan = null;
        Vector refTables = null;
        Vector otherVersions = null;
        String latestID = null;
        String vocabularyId = null;
        //if true all concepts are valid, no release date is checked
        boolean allowAllConcepts = true;

        // security flag for non-common elements only
        boolean editDstPrm = false;

        // security flags for common elements only
        boolean editPrm = false;
        boolean editReleasedPrm = false;
        boolean canCheckout = false;
        boolean canNewVersion = false;
        boolean isMyWorkingCopy = false;
        boolean isLatestElm = false;

        Connection conn = null;

        // the whole page's try block
        try {

            // get db connection, init search engine object
            conn = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
            searchEngine.setUser(user);

            // get metadata of attributes
            mAttributes = searchEngine.getDElemAttributes(null,
                    DElemAttribute.TYPE_SIMPLE,
                    DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);

            // if not in add mode, get the element object, set some parameters based on it
            if (!mode.equals("add")) {

                if (isLatestRequested) {

                    Vector v = new Vector();
                    if (user == null) {
                        v.add("Recorded");
                        v.add("Released");
                    }
                    dataElement = searchEngine.getLatestElm(delemIdf, tableIdf, datasetIdf, v);
                } else {
                    dataElement = searchEngine.getDataElement(delem_id);
                }

                if (dataElement == null) {
                    if (user != null) {
                        request.setAttribute("DD_ERR_MSG",
                                "Could not find a data element of this id or identifier in any status!");
                    }
                    else{
                        request.setAttribute("DD_ERR_MSG",
                            "Could not find a data element of this id or identifier in 'Recorded' or 'Released' status! " +
                            "As an anonymous user, you are not allowed to see definitions in any other status.");
                    }
                    session.setAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME, SecurityUtil.buildAfterLoginURL(request));
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }

                if(mode.equals("view")){
                    DataElementDAOImpl dao = searchEngine.getSpringContext().getBean(DataElementDAOImpl.class);
                    eionet.meta.dao.domain.DataElement element = dao.getDataElement(Integer.parseInt(dataElement.getID()));
                    dataElementRules = (ArrayList<InferenceRule>)dao.getInferenceRules(element);
                }

                // set parameters regardless of common or non-common elements
                elmCommon = dataElement.getNamespace() == null
                        || dataElement.getNamespace().getID() == null;
                delem_id = dataElement.getID();
                delem_name = dataElement.getShortName();
                delemIdf = dataElement.getIdentifier();
                idfier = dataElement.getIdentifier();
                type = dataElement.getType();
                if (type != null && type.length() == 0)
                    type = null;

                vocabularyId = dataElement.getVocabularyId();
                allowAllConcepts = dataElement.isAllConceptsValid();


                complexAttrs = searchEngine.getComplexAttributes(delem_id,
                        "E", null, tableID, dsID);
                if (complexAttrs == null)
                    complexAttrs = new Vector();

                // set parameters specific to NON-COMMON elements
                if (!elmCommon) {
                    tableID = dataElement.getTableID();
                    if (tableID == null || tableID.length() == 0) {
                        request.setAttribute("DD_ERR_MSG",
                                "Missing table id number in the non-common element object");
                        request.getRequestDispatcher("error.jsp").forward(
                                request, response);
                        return;
                    }
                }
                // set parameters and security flags specific to COMMON elements
                else {
                    elmWorkingUser = dataElement.getWorkingUser();
                    elmRegStatus = dataElement.getStatus();
                    refTables = searchEngine.getReferringTables(delem_id);

                    Vector v = new Vector();
                    if (user == null) {
                        v.add("Recorded");
                        v.add("Released");
                    }
                    latestID = searchEngine.getLatestElmID(delemIdf, tableIdf, datasetIdf, v);
                    isLatestElm = latestID != null
                            && delem_id.equals(latestID);

                    editPrm = user != null
                            && SecurityUtil.hasPerm(user.getUserName(),
                                    "/elements/" + delemIdf, "u");
                    editReleasedPrm = user != null
                            && SecurityUtil.hasPerm(user.getUserName(),
                                    "/elements/" + delemIdf, "er");

                    canNewVersion = !dataElement.isWorkingCopy()
                            && elmWorkingUser == null
                            && elmRegStatus != null && user != null
                            && isLatestElm;
                    if (canNewVersion) {
                        canNewVersion = elmRegStatus.equals("Released")
                                || elmRegStatus.equals("Recorded");
                        if (canNewVersion)
                            canNewVersion = editPrm || editReleasedPrm;
                    }

                    canCheckout = !dataElement.isWorkingCopy()
                            && elmWorkingUser == null
                            && elmRegStatus != null && user != null
                            && isLatestElm;
                    if (canCheckout) {
                        if (elmRegStatus.equals("Released"))
                            // || elmRegStatus.equals("Recorded")
                            canCheckout = editReleasedPrm;
                        else
                            canCheckout = editPrm || editReleasedPrm;
                    }

                    isMyWorkingCopy = elmCommon
                            && dataElement.isWorkingCopy()
                            && elmWorkingUser != null && user != null
                            && elmWorkingUser.equals(user.getUserName());

                    // get the element's other versions (does not include working copies)
                    if (mode.equals("view"))
                        otherVersions = searchEngine.getElmOtherVersions(
                                dataElement.getIdentifier(),
                                dataElement.getID());
                }
            }

            // if non-common element, get the table object (by this point the table id must not be null if non-common element)
            if (!elmCommon) {

                dsTable = searchEngine.getDatasetTable(tableID);
                if (dsTable == null) {
                    request.setAttribute("DD_ERR_MSG",
                            "No table found with this id number: "
                                    + tableID);
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }

                // overwrite the dataset id number parameter with the value from table object
                dsID = dsTable.getDatasetID();
                if (dsID == null || dsID.length() == 0) {
                    request.setAttribute("DD_ERR_MSG",
                            "Missing dataset id number in the table object");
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }

                dataset = searchEngine.getDataset(dsID);
                if (dataset == null) {
                    request.setAttribute("DD_ERR_MSG",
                            "No dataset found with this id number: " + dsID);
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }

                // set some parameters based on the table & dataset objects
                dstWorkingUser = dataset.getWorkingUser();
                editDstPrm = user != null && dataset.isWorkingCopy()
                        && dstWorkingUser != null
                        && dstWorkingUser.equals(user.getUserName());

                // security checks
                if (!mode.equals("view") && editDstPrm == false) {
                    request.setAttribute("DD_ERR_MSG",
                            "You have no permission to do modifications in this dataset!");
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }
                // anonymous users should not be allowed to see elements from a dataset working copy
                if (mode.equals("view") && user == null
                        && dataset.isWorkingCopy()) {
                    request.setAttribute("DD_ERR_MSG",
                            "Anonymous users are not allowed to view elements from a dataset working copy");
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }
                // anonymous users should not be allowed to see elements from datasets that are NOT in Recorded or Released status
                if (mode.equals("view") && user == null
                        && dataset.getStatus() != null
                        && !dataset.getStatus().equals("Recorded")
                        && !dataset.getStatus().equals("Released")) {
                    request.setAttribute(
                            "DD_ERR_MSG",
                            "Elements from datasets NOT in Recorded or Released status are inaccessible for anonymous users.");
                    request.getRequestDispatcher("error.jsp").forward(
                            request, response);
                    return;
                }
                // if add mode, populate the helper newDataElement object with attribute values inherited from table & dataset
                if (mode.equals("add")) {
                    newDataElement = new DataElement();
                    newDataElement.setDatasetID(dsID);
                    newDataElement.setTableID(tableID);
                    newDataElement.setAttributes(searchEngine
                            .getSimpleAttributes(null, "E", tableID, dsID));
                }
            }

                if (type != null && type.equals("CH3") && vocabularyId != null) {
                    vocabulary = searchEngine.getVocabulary(Integer.valueOf(vocabularyId));
                }
            // FOR COMMON ELEMENTS ONLY - security checks, checkin/checkout operations, dispatching of the GET request
            if (elmCommon) {


                verMan = new VersionManager(conn, searchEngine, user);
                if (mode.equals("edit")) {
                    if (!dataElement.isWorkingCopy() || user == null || (elmWorkingUser != null && !elmWorkingUser.equals(user.getUserName()))) {
                        request.setAttribute("DD_ERR_MSG",
                                "You have no permission to edit this common element!");
                        request.getRequestDispatcher("error.jsp").forward(
                                request, response);
                        return;
                    }
                }
                else if (mode.equals("view") && action!=null && action.equals("subscribe") && dataElement!=null && elmCommon){
                    Subscriber.subscribeToElement(Collections.singleton(user.getUserName()), dataElement.getIdentifier());
                    response.sendRedirect(request.getContextPath() + "/dataelements/" + dataElement.getID() + "/?feedback=subscribe");
                }
                else if (mode.equals("view") && action != null && (action.equals("checkout") || action.equals("newversion"))) {

                    if (action.equals("checkout") && !canCheckout) {
                        request.setAttribute("DD_ERR_MSG",
                                "You have no permission to check out this common element!");
                        request.getRequestDispatcher("error.jsp").forward(
                                request, response);
                        return;
                    }
                    if (action.equals("newversion") && !canNewVersion) {
                        request.setAttribute("DD_ERR_MSG",
                                "You have no permission to create new version of this common element!");
                        request.getRequestDispatcher("error.jsp").forward(
                                request, response);
                        return;
                    }

                    // if creating new version, let VersionManager know about this
                    if (action.equals("newversion")) {
                        eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
                        pars.addParameterValue("resetVersionAndStatus", "resetVersionAndStatus");
                        verMan.setServlRequestParams(pars);
                    }

                    // check out the element
                    String copyID = verMan.checkOut(delem_id, "elm");
                    if (!delem_id.equals(copyID)) {
                        // send to copy if created successfully, remove previous url (edit original) from history
                        history.remove(history.getCurrentIndex());
                        response.sendRedirect(request.getContextPath() + "/dataelements/" + copyID + "/?feedback=checkout");
                    }
                } else if (mode.equals("view")) {
                    // anonymous users should not be allowed to see anybody's working copy
                    if (dataElement.isWorkingCopy() && user == null) {
                        request.setAttribute("DD_ERR_MSG",
                                "Anonymous users are not allowed to view a working copy!");
                        request.getRequestDispatcher("error.jsp").forward(
                                request, response);
                        return;
                    }
                    // anonymous users should not be allowed to see definitions that are NOT in Recorded or Released status
                    if (user == null && elmRegStatus != null
                            && !elmRegStatus.equals("Recorded")
                            && !elmRegStatus.equals("Released")) {
                        request.setAttribute(
                                "DD_ERR_MSG",
                                "Definitions NOT in Recorded or Released status are inaccessible for anonymous users.");
                        request.getRequestDispatcher("error.jsp").forward(
                                request, response);
                        return;
                    }
                    // redircet user to his working copy of this element (if such exists)
                    String workingCopyID = verMan.getWorkingCopyID(dataElement);
                    if (workingCopyID!=null && workingCopyID.length()>0){
                        response.sendRedirect(request.getContextPath() + "/dataelements/" + workingCopyID);
                    }
                }
            }

            // prepare the page's HTML title, shown in browser title bar
            StringBuffer pageTitle = new StringBuffer();
            if (mode.equals("edit")) {
                if (elmCommon)
                    pageTitle.append("Edit common element");
                else
                    pageTitle.append("Edit element");
            } else {
                if (elmCommon)
                    pageTitle.append("Common element");
                else
                    pageTitle.append("Element");
            }
            if (dataElement != null && dataElement.getShortName() != null)
                pageTitle.append(" - ").append(dataElement.getShortName());
            if (dsTable != null && dataset != null) {
                if (dsTable.getShortName() != null
                        && dataset.getShortName() != null)
                    pageTitle.append("/").append(dsTable.getShortName())
                            .append("/").append(dataset.getShortName());
            }
    %>

<%
    // start HTML //////////////////////////////////////////////////////////////
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title><%=pageTitle.toString()%></title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/querystring.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/modal_dialog.js"></script>
    <script type="text/javascript">
        // <![CDATA[

        function forceAttrMaxLen(){
            var i = 0;
            var elms = document.forms["form1"].elements;
            for (i=0; elms!=null && i<elms.length; i++){
                var elmName = elms[i].name;
                if (startsWith(elmName, "attr_")){
                    if (elms[i].value.length > <%=MAX_ATTR_LEN%>){
                        alert("Maximum length of attribute values for data element definitions is <%=MAX_ATTR_LEN%>!");
                        return;
                    }
                }
            }
        }

        function checkIn(){
            submitCheckIn();
        }

        function switchType(){
            document.forms["form1"].elements["switch_type"].value = "true";
            document.forms["form1"].elements["mode"].value = "edit";
            document.forms["form1"].submit();
        }

        function submitCheckIn(){
            <%if (elmRegStatus != null && elmRegStatus.equals("Released")) {%>
                var b = confirm("You are checking in with Released status! This will automatically release your changes into public view. " +
                                "If you want to continue, click OK. Otherwise click Cancel.");
                if (b==false) return;
                <%}%>
            document.forms["form1"].elements["check_in"].value = "true";
            document.forms["form1"].elements["mode"].value = "edit";
            document.forms["form1"].submit();
        }

        function goTo(mode, id){
            if (mode == "edit"){
                document.location.assign("<%=request.getContextPath()%>/dataelements/" + id + "/edit");
            }
            else if (mode=="checkout"){
                document.location.assign("<%=request.getContextPath()%>/dataelements/" + id + "/checkout");
            }
            else if (mode=="newversion"){
                document.location.assign("<%=request.getContextPath()%>/dataelements/" + id + "/newversion");
            }
            else if (mode=="view"){
                document.location.assign("<%=request.getContextPath()%>/dataelements/" + id);
            }
        }

        function submitForm(mode){

            // detect element type, make sure it has been selected
            var elmTypeSelectedValue = "";
            var typeRadioButtons = document.forms["form1"].elements["type"];
            if (typeRadioButtons.length==undefined){
                elmTypeSelectedValue = typeRadioButtons.value;
            }
            else{
                for (var i=0; i<typeRadioButtons.length; i++){
                    if (typeRadioButtons[i].checked){
                        elmTypeSelectedValue = typeRadioButtons[i].value;
                    }
                }
            }
            if (elmTypeSelectedValue==null || elmTypeSelectedValue==""){
                alert('Element type not specified!');
                return false;
            }

            if (mode == "delete"){
                <%String confirmationText = "Are you sure you want to delete this element? Click OK, if yes. Otherwise click Cancel.";
                if (dataElement != null && elmCommon) {
                    if (dataElement.isWorkingCopy())
                        confirmationText = "This working copy will be deleted! Click OK, if you want to continue. Otherwise click Cancel.";
                    else if (elmRegStatus != null
                            && !dataElement.isWorkingCopy()
                            && elmRegStatus.equals("Released"))
                        confirmationText = "You are about to delete a Released common element! Are you sure you want to do this? Click OK, if yes. Otherwise click Cancel.";
                }%>

                var b = confirm("<%=confirmationText%>");
                if (b==false) return;

                document.forms["form1"].elements["mode"].value = "delete";
                document.forms["form1"].submit();
                return;
            }

            <%if (mode.equals("edit")) {
                    String par_dt = request.getParameter("elm_datatype");
                    if (par_dt != null && par_dt.length() > 0) {
                        String obj_dt = dataElement
                                .getAttributeValueByShortName("Datatype");
                        if (obj_dt != null && !obj_dt.equals(par_dt)) {%>
                        document.forms["form1"].elements["datatype_conversion"].value = "<%=obj_dt%>-<%=par_dt%>";
                        <%if (!Util.isAllowedFxvDatatypeConversion(obj_dt,
                                    par_dt)) {
                                StringBuffer text = new StringBuffer(
                                        "You have chosen to change the Datatype from '");
                                text.append(obj_dt)
                                        .append("' to '")
                                        .append(par_dt)
                                        .append("'. DD will remove this element's ");
                                text.append(
                                        dataElement.getType().equals("CH1") ? "fixed"
                                                : dataElement.getType().equals("CH2") ? "suggested" : "vocabulary").append(
                                        " values, ");
                                text.append("because they are not compatible with the new Datatype! Are you sure you want to continue?");%>
                            if (mode=="edit" || mode=="editclose"){
                                var bb = confirm("<%=text%>");
                                if (bb==false){
                                    document.forms["form1"].elements["remove_values"].value = "false"
                                    return;
                                }
                                else
                                    document.forms["form1"].elements["remove_values"].value = "true";
                            }<%}
                        }
                    }
                }%>

            // if not delete mode, do validation of inputs
            if (mode != "delete"){
                var isCommon = '<%=elmCommon%>';
                var strVocabularyId = '<%=vocabularyId%>';
                var strType = '<%=type%>';

                forceAttrMaxLen();
                if (!checkObligations()){
                    alert("You have not specified one of the mandatory attributes!");
                    return false;
                }

                if (mode != "add") {
                    if (strType == 'CH3' && strVocabularyId == 'null') {
                      alert("Vocabulary is not selected.");
                      return false;
                    }
                }

                if (hasWhiteSpace("idfier")){
                    alert("Identifier cannot contain any white space!");
                    return false;
                }
                if (!validForXMLTag(document.forms["form1"].elements["idfier"].value, isCommon)){

                    alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!" +
                          "Only common elements may have namespace prefix.");
                    return false;
                }
            }

            //slctAllValues();

            if (mode=="editclose"){
                mode = "edit";
                document.forms["form1"].elements["saveclose"].value = "true";
            }

            document.forms["form1"].elements["mode"].value = mode;
            document.forms["form1"].submit();
            return true;
        }

        function delDialogReturn(){
            var v = dialogWin.returnValue;
            if (v==null || v=="" || v=="cancel") return;

            document.forms["form1"].elements["upd_version"].value = v;
            deleteReady();
        }

        function deleteReady(){
            document.forms["form1"].elements["mode"].value = "delete";
            document.forms["form1"].submit();
        }

        function checkObligations(){

            var o = document.forms["form1"].delem_name;
            if (o!=null && o.value.length == 0) return false;

            o = document.forms["form1"].idfier;
            if (o!=null && o.value.length == 0) return false;

            var elems = document.forms["form1"].elements;
            for (var i=0; elems!=null && i<elems.length; i++){
                var elem = elems[i];
                var elemName = elem.name;
                var elemValue = elem.value;
                if (startsWith(elemName, "attr_")){
                    var o = document.forms["form1"].elements[i+1];
                    if (o == null) return false;
                    if (!startsWith(o.name, "oblig_"))
                        continue;
                    if (o.value == "M" && (elemValue==null || elemValue.length==0)){
                        return false;
                    }
                }
            }

            return true;
        }

        function hasWhiteSpace(input_name){

            var elems = document.forms["form1"].elements;
            if (elems == null) return false;
            for (var i=0; i<elems.length; i++){
                var elem = elems[i];
                if (elem.name == input_name){
                    var val = elem.value;
                    if (val.indexOf(" ") != -1) return true;
                }
            }

            return false;
        }

        function startsWith(str, pattern){
            var i = str.indexOf(pattern,0);
            if (i!=-1 && i==0)
                return true;
            else
                return false;
        }

        function endsWith(str, pattern){
            var i = str.indexOf(pattern, str.length-pattern.length);
            if (i!=-1)
                return true;
            else
                return false;
        }

        function fixType(radioButton){

            if (!radioButton){
                return;
            }

            var strType = radioButton.value;
            if (strType == null || strType.length==0){
                return;
            }
            var datatypeElemAttrID = <%=getAttributeIdByName("Datatype", mAttributes)%>

            var requestQS = new Querystring();
            var arr = new Array();
            arr[0] = strType;
            requestQS.setValues_("type", arr);


            slctAllValues();
            var s = visibleInputsToQueryString("form1");
            var inputsQS = new Querystring(s);
            inputsQS.remove("typeSelect");

            requestQS.removeAll(inputsQS);

            // The reason we want to exclude "reg_status" is that IE will change occurance
            // of "&reg_status" substring to "®_status". So we will add reg_status explicitly
            // right to the start of the query string, so that in the final URL it appears right after the question mark.
            requestQS.remove("reg_status");
            requestQS.remove("mode");
            inputsQS.remove("reg_status");
            inputsQS.remove("mode");
            //remove dataelem if CH3. Actually
            var datatypeElemID = "attr_<%=getAttributeIdByName("Datatype", mAttributes)%>";
            var eType = strType

            if (eType == "CH3") {
                inputsQS.remove(datatypeElemID);
            }

            var newLocation = "<%=request.getContextPath()%>/dataelements/add/?";
            if (document.forms["form1"].reg_status){
                newLocation = newLocation + "reg_status=" + escape(document.forms["form1"].reg_status.value) + "&";
            }

            newLocation = newLocation + requestQS.toString() + "&" + inputsQS.toString();
            window.location.assign(newLocation);
        }

        function onBodyLoad(){

            var formName = "form1";
            var inputName;
            var popValues;
            <%
            if (request.getQueryString()!=null && !request.getQueryString().isEmpty()){
                Hashtable qryStrHash1 = HttpUtils.parseQueryString(request.getQueryString());
                if (qryStrHash1 != null && qryStrHash1.size() > 0) {
                    Enumeration keys = qryStrHash1.keys();
                    while (keys != null && keys.hasMoreElements()) {
                        String name = (String) keys.nextElement();
                        String[] values = (String[]) qryStrHash1.get(name);
                        if (values != null && values.length > 0) {%>
                        inputName = "<%=name%>";
                        popValues = new Array();
                        <%for (int i = 0; i < values.length; i++) {
                                String value = values[i];
                                // value can contain line breaks which need to make to "\n" strings for the below Javascript that puts the values into form inputs
                                StringTokenizer valueLines = new StringTokenizer(
                                        value, "\r\n");
                                StringBuffer valueProcessed = new StringBuffer();
                                while (valueLines.hasMoreTokens()) {
                                    if (valueProcessed.length() > 0)
                                        valueProcessed.append("\\n");
                                    valueProcessed.append(valueLines
                                            .nextToken());
                                }%>
                            popValues[<%=i%>] = '<%=valueProcessed.toString().replace(
                                        '\'', '"')%>';
                            <%}%>
                        populateInput(formName, inputName, popValues);
                        <%}
                    }
                }
            }%>
        }

        function changeDatatype(){

            <%String datatypeID = getAttributeIdByName("Datatype",
                        mAttributes);
                if (datatypeID != null && datatypeID.length() > 0) {
                    datatypeID = "attr_" + datatypeID;%>
                var elmDataType = document.forms["form1"].<%=datatypeID%>.value;
                if (elmDataType == null || elmDataType.length==0)
                    return;

                var arr = new Array();
                arr[0] = elmDataType;

                var requestQS = new Querystring();
                requestQS.setValues_("elm_datatype", arr);
                requestQS.remove("<%=datatypeID%>");

                slctAllValues();

                var s = visibleInputsToQueryString("form1");
                var inputsQS = new Querystring(s);
                inputsQS.remove("<%=datatypeID%>");

                requestQS.removeAll(inputsQS);

                // The reason we want to exclude "reg_status" is that IE will change occurance
                // of "&reg_status" substring to "®_status". So we will add reg_status explicitly
                // right to the start of the query string, so that in the final URL it appears right after the question mark.
                requestQS.remove("reg_status");
                requestQS.remove("mode");
                inputsQS.remove("reg_status");
                inputsQS.remove("mode");

                var newLocation = "<%=request.getContextPath()%>/dataelements/<%=Util.isEmpty(delem_id) ? mode : delem_id%>/<%=Util.isEmpty(delem_id) ? "" : mode%>?";
                if (document.forms["form1"].reg_status){
                    newLocation = newLocation + "reg_status=" + escape(document.forms["form1"].reg_status.value) + "&";
                }
                newLocation = newLocation + requestQS.toString() + "&" + inputsQS.toString();
                window.location.assign(newLocation);
                <%}%>
        }

        function slctAllValues(){

            var elems = document.forms["form1"].elements;
            if (elems == null) return true;

            for (var j=0; j<elems.length; j++){
                var elem = elems[j];
                var elemName = elem.name;
                if (startsWith(elemName, "attr_mult_")){
                    var slct = document.forms["form1"].elements[elemName];
                    if (slct.options && slct.length){
                        if (slct.length==1 && slct.options[0].value=="" && slct.options[0].text==""){
                            slct.remove(0);
                            slct.length = 0;
                        }
                        for (var i=0; i<slct.length; i++){
                            slct.options[i].selected = "true";
                        }
                    }
                }
            }
        }

        function copyElem(){
            var isCommon = '<%=elmCommon%>';
            if (hasWhiteSpace("idfier")){
                alert("Identifier cannot contain any white space!");
                return;
            }

            if (!validForXMLTag(document.forms["form1"].elements["idfier"].value, isCommon)){
                alert("Identifier not valid for usage as an XML tag! " +
                          "In the first character only underscore or latin characters are allowed! " +
                          "In the rest of characters only underscore or hyphen or dot or 0-9 or latin characters are allowed!");
                return;
            }

            <%if (!elmCommon) {%>
                var ds = document.forms["form1"].elements["ds_id"].value;
                if (ds==null || ds==""){
                    alert('Dataset not specified!');
                    return;
                }

                var tbl = document.forms["form1"].elements["table_id"].value;
                if (tbl==null || tbl==""){
                    alert('Table not specified!');
                    return;
                }<%}%>

            var url="<%=request.getContextPath()%>/search.jsp?ctx=popup";
            wAdd = window.open(url,"Search","height=800,width=1200,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
            if (window.focus){
                wAdd.focus();
            }
        }

        function pickElem(id){

            document.forms["form1"].elements["copy_elem_id"].value=id;
            document.forms["form1"].elements["mode"].value = "copy";
            document.forms["form1"].submit();
            return true;
        }

        function validForXMLTag(str, isCommon){
            var colonCount = 0;
            // if empty string not allowed for XML tag
            if (str==null || str.length==0){
                return false;
            }

            // check the first character (only underscore or A-Z or a-z allowed)
            var ch = str.charCodeAt(0);
            if (!(ch==95 || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))){
                return false;
            }

            // check the rest of characters ((only underscore or hyphen or dot or 0-9 or A-Z or a-z allowed))
            if (str.length==1) return true;
            for (var i=1; i<str.length; i++){
                ch = str.charCodeAt(i);
                if (ch == 58) {
                  colonCount = colonCount + 1;
                }
                //only common elements may have colon
                if (!((ch == 58 && isCommon == "true") || ch==95 || ch==45 || ch==46 || (ch>=48 && ch<=57) || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))){
                    return false;
                }

                if (colonCount > 1) {
                  return false;
                }
            }

            //xml element name can contain only one colon:


            return true;
        }

    // ]]>
    </script>
</head>

<%
    String hlpScreen = "element";
        if (mode.equals("view")) {
            if (elmCommon && !dataElement.isWorkingCopy())
                hlpScreen = "common_element";
            else if (elmCommon && dataElement.isWorkingCopy())
                hlpScreen = "common_element_working_copy";
        }

        if (mode.equals("edit") && !elmCommon)
            hlpScreen = "element_edit";
        else if (mode.equals("edit") && elmCommon)
            hlpScreen = "common_element_edit";
        else if (mode.equals("add") && elmCommon)
            hlpScreen = "common_element_add";
        else if (mode.equals("add") && !elmCommon)
            hlpScreen = "element_add";

        // start HTML body ///////////////////////////////////////
        boolean popup = request.getParameter("popup") != null;
        if (popup) {
%>
    <body class="popup" onload="onBodyLoad()">
    <div id="pagehead">
        <a href="/"><img src="<%=request.getContextPath()%>/images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
        <div id="networktitle">Eionet</div>
        <div id="sitetitle"><%=application.getInitParameter("appDispName")%></div>
        <div id="sitetagline">This service is part of Reportnet</div>
    </div> <!-- pagehead -->
    <div id="workarea"><%
        } else {
    %>
    <body onload="onBodyLoad()">
    <div id="container">
        <jsp:include page="nlocation.jsp" flush="true">
            <jsp:param name="name" value="Data element"/>
            <jsp:param name="helpscreen" value="<%=hlpScreen%>"/>
        </jsp:include>
        <c:set var="currentSection" value="dataElements" />
        <%@ include file="/pages/common/navigation.jsp" %>
        <div id="workarea">
<%
    } // end if popup

            if (feedbackValue != null) {
            %>
                <div class="system-msg">
                    <strong><%= feedbackValue %></strong>
                </div>
            <%
            }

                String verb = "View";
                    if (mode.equals("add"))
                        verb = "Add";
                    else if (mode.equals("edit"))
                        verb = "Edit";
                    String strCommon = elmCommon ? "common" : "";
                %>
                <h1><%=verb%> <%=strCommon%> element definition</h1>
                <%
                    // set up fixed values
                    fixedValues = mode.equals("add") ? null : searchEngine.getFixedValuesOrderedByValue(delem_id, "elem");

                    // set up foreign key relations (if non-common element)
                    Vector fKeys = null;
                    if (!mode.equals("add") && !elmCommon && dataset != null) {
                        fKeys = searchEngine.getFKRelationsElm(delem_id, dataset.getID());
                    }
                    if (mode.equals("view")) {
                        Vector quicklinks = new Vector();

                        if (fixedValues != null && fixedValues.size() > 0) {
                            String s = type.equals("CH1") ? "Allowable values" : "Suggested values";
                            quicklinks.add(s + " | values");
                        }
                        if (fKeys != null && fKeys.size() > 0) {
                            quicklinks.add("Foreign key relations | fkeys");
                        }
                        if (complexAttrs != null && complexAttrs.size() > 0) {
                            quicklinks.add("Complex attributes | cattrs");
                        }
                        request.setAttribute("quicklinks", quicklinks);
                %>
                    <jsp:include page="quicklinks.jsp" flush="true" />
                <%
                    }
                    if (mode.equals("view") && user!=null) {
                    %>
                        <div id="drop-operations">
                            <ul>
                            <%
                            if (popup) {
                            %>
                                    <li class="help"><a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=<%=hlpScreen%>&amp;area=pagehelp">Page help</a></li><%
                            }
                            if (elmCommon && canNewVersion) {
                            %>
                                    <li class="newVersion"><a href="<%=request.getContextPath()%>/dataelements/<%=delem_id%>/newversion">New version</a></li><%
                            }
                            if (mode.equals("view") && elmCommon
                                    && !dataElement.isWorkingCopy()) {
                                if (user != null || (user == null && !isLatestRequested)) {
                                    if (latestID != null
                                            && !latestID.equals(dataElement.getID())) {
                            %>
                                    <li class="newest"><a href="<%=request.getContextPath()%>/dataelements/<%=latestID%>">Go to newest</a></li><%
                                    }
                                }
                            }
                            if (mode.equals("view")) {
                                //The buttons displayed in view mode
                                    if (!elmCommon && editDstPrm) {
                                %>
                                    <li class="edit"><a href="<%=request.getContextPath()%>/dataelements/<%=delem_id%>/edit">Edit</a></li>
                                    <% request.setAttribute("includeSwitchTypeDialog", "true"); %>
                                    <li class="switch"><a href="#" id="switchTypeLink">Switch type</a></li>
                                <%
                                    }
                                    if (elmCommon && canCheckout) {
                                %>
                                    <li class="checkout"><a href="<%=request.getContextPath()%>/dataelements/<%=delem_id%>/checkout">Check out</a></li>
                                <%
                                    }
                                    if ((elmCommon && canCheckout)
                                                    || (!elmCommon && editDstPrm)) {
                                %>
                                    <li class="delete"><a href="javascript:submitForm('delete')">Delete</a></li>
                                <%
                                    }
                                    if (!elmCommon && editDstPrm){
                                        %><li class="add"><a href="<%=request.getContextPath()%>/dataelements/add/?table_id=<%=tableID%>&ds_id=<%=dsID%>">Add new element to table</a></li><%
                                    }
                               }
                            if (mode.equals("view") && isMyWorkingCopy) {
                                 // view case
                                %>
                                    <li class="edit"><a href="<%=request.getContextPath()%>/dataelements/<%=delem_id%>/edit">Edit</a></li>
                                    <% request.setAttribute("includeSwitchTypeDialog", "true"); %>
                                    <li class="switch"><a href="#" id="switchTypeLink">Switch type</a></li>
                                    <li class="checkin"><a href="javascript:checkIn()">Check in</a></li>
                                    <li class="undo"><a href="javascript:submitForm('delete')">Undo checkout</a></li>
                                <%
                              }
                            if (mode.equals("view") && user != null && dataElement != null
                                    && elmCommon && dataElement.getIdentifier() != null && !dataElement.isWorkingCopy()) {
                            %>
                                    <li class="subscribe"><a href="<%=request.getContextPath()%>/dataelements/<%=delem_id%>/subscribe">Subscribe</a></li>
                            <%
                            }
                            %>
                            </ul>
                        </div>
                    <%
                    }

                if (mode.equals("add")) {
            %>
                <p>
                    You have 2 options here:
                </p>
                <ul>
                    <li>Copy an existing element by filling the input for Identifier (others will be ignored) and clicking <em>Copy</em>.</li>
                    <li>Create new element by filling at least the mandatory inputs and clicking <em>Add.</em></li>
                </ul>
                <%
                    }
                %>

            <form id="form1" method="post" action="<%=request.getContextPath()%>/dataelements">
                <div style="display:none">
                    <%
                        if (!mode.equals("add")) {
                    %>
                        <input id="txtElemId" type="hidden" name="delem_id" value="<%=delem_id%>"/>
                        <input id="txtVocabularyId" type="hidden" name="vocabulary_id" value="<%=vocabularyId%>"/><%
                            } else {
                        %>
                        <input type="hidden" name="dummy"/><%
                            }
                        %>
                </div>

                        <!-- main table body -->


                                <!-- quick links -->

                                <%
                                    // schema && codelist links
                                    // display schema link only in view mode and only for users that have a right to edit a dataset
                                        if (mode.equals("view")) {
                                            boolean dispOutputs = elmCommon;
                                            if (dispOutputs == false)
                                                dispOutputs = dataset != null
                                                        && dataset.displayCreateLink("XMLSCHEMA");
                                            if (!popup && dispOutputs) {
                                %>
                                        <script type="text/javascript">
                                            $(function() {
                                                applyExportOptionsToggle();
                                            });
                                        </script>
                                        <div id="createbox">
                                            <ul>
                                                <li>
                                                    <a rel="nofollow" href="<%=request.getContextPath()%>/GetSchema?id=ELM<%=delem_id%>" class="xsd">
                                                        Create an XML Schema for this element
                                                    </a>
                                                </li>
                                                <%
                                                    if (fixedValues != null && fixedValues.size() > 0) {
                                                %>
                                                    <li>
                                                        <stripes:link rel="nofollow" beanclass="eionet.web.action.CodelistDownloadActionBean" class="csv">
                                                            <stripes:param name="ownerType" value="dataelements"/>
                                                            <stripes:param name="ownerId" value="<%=dataElement.getID()%>"/>
                                                            <stripes:param name="format" value="csv"/>
                                                            Get the comma-separated codelist of this element
                                                        </stripes:link>
                                                    </li>
                                                    <li>
                                                        <stripes:link rel="nofollow" beanclass="eionet.web.action.CodelistDownloadActionBean" class="xml">
                                                            <stripes:param name="ownerType" value="dataelements"/>
                                                            <stripes:param name="ownerId" value="<%=dataElement.getID()%>"/>
                                                            <stripes:param name="format" value="xml"/>
                                                            Get the codelist of this element in XML format
                                                        </stripes:link>
                                                    </li><%
                                                        }
                                                    %>
                                            </ul>
                                        </div>
                                        <%
                                            }
                                                }
                                        %>

                                <!-- type -->
                                    <%
                                    if (mode.equals("add") && (type == null || type.length() == 0)) {
                                    %>
                                        <div>
                                            <b>Type:</b>
                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=element&amp;area=type"></a>
                                            <br/><input type="radio" name="type" value="CH2" onclick="javascript:fixType(this)" checked="checked">Data element with quantitative values (e.g. measurements)</input>
                                            <br/><input type="radio" name="type" value="CH1" onclick="javascript:fixType(this)">Data element with fixed values (codes)</input>
                                            <br/><input type="radio" name="type" value="CH3" onclick="javascript:fixType(this)">Data element with values from a vocabulary</input>
                                        </div><%
                                    }
                                    else { %>
                                    <h2>
                                    <%
                                        if (type.equals("CH1")) {
                                            %>
                                            DATA ELEMENT WITH FIXED VALUES<%
                                        }
                                        else if (type.equals("CH2")) {
                                            %>
                                            DATA ELEMENT WITH QUANTITATIVE VALUES<%
                                        }
                                        else if (type.equals("CH3")) {
                                            %>
                                            DATA ELEMENT RELATED TO A VOCABULARY<%
                                        }
                                        else {
                                            %>
                                            DATA ELEMENT WITH QUANTITATIVE VALUES<%
                                        }
                                        %>
                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=element&amp;area=type"></a>
                                    </h2>
                                    <%
                                    }
                                    %>

                                <!-- start dotted -->
                                <div id="outerframe">
                                        <!-- attributes -->
                                        <%
                                            int displayed = 1;
                                            String isOdd = Util.isOdd(displayed);
                                        %>

                                        <table class="datatable results">

                                            <!-- Identifier -->
                                            <tr class="<%=isOdd%>">
                                                <th scope="row" class="scope-row simple_attr_title">
                                                    Identifier
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=identifier"></a>
                                                </th>
                                                <%
                                                    if (!mode.equals("view")) {
                                                %>
                                                    <td class="simple_attr_help">
                                                        <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                    </td><%
                                                        }
                                                    %>
                                                <td class="simple_attr_value">
                                                    <%
                                                        if (!mode.equals("add")) {
                                                    %>
                                                        <b><%=Util.processForDisplay(idfier)%></b>
                                                        <input type="hidden" name="idfier" value="<%=Util.processForDisplay(delemIdf, true)%>"/><%
                                                            } else {
                                                        %>
                                                        <input class="smalltext" type="text" size="30" name="idfier" onchange="form_changed('form1')" value="<%=idfier%>"/><%
                                                            }
                                                        %>
                                                </td>
                                                <%
                                                    isOdd = Util.isOdd(++displayed);
                                                %>
                                            </tr>

                                              <!-- short name -->
                                            <tr id="short_name_row" class="<%=isOdd%>">
                                                <th scope="row" class="scope-row short_name">
                                                    Short name
                                                    <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=short_name"></a>
                                                </th>
                                                <%
                                                    if (!mode.equals("view")) {
                                                %>
                                                    <td class="short_name simple_attr_help">
                                                        <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                    </td><%
                                                        }
                                                    %>
                                                <td class="short_name_value">
                                                    <%
                                                        if (mode.equals("view")) {
                                                    %>
                                                        <%=Util.processForDisplay(dataElement.getShortName())%>
                                                        <input type="hidden" name="delem_name" value="<%=Util.processForDisplay(dataElement.getShortName(), true)%>"/><%
                                                            } else if (mode.equals("add")) {
                                                        %>
                                                        <input class="smalltext" type="text" size="30" name="delem_name"/><%
                                                            } else {
                                                        %>
                                                        <input class="smalltext" type="text" size="30" name="delem_name" value="<%=Util.processForDisplay(dataElement.getShortName())%>"/><%
                                                            }
                                                        %>
                                                </td>
                                                <%
                                                    isOdd = Util.isOdd(++displayed);
                                                %>
                                            </tr>

                                            <!-- dataset & table part, relevant for non-common elements only -->
                                            <%
                                                if (!elmCommon) {

                                                        // dataset
                                            %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Dataset
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=table&amp;area=dataset"></a>
                                                    </th>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <td class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                        </td><%
                                                            }
                                                        %>
                                                    <td class="simple_attr_value">
                                                        <em>
                                                            <a href="<%=request.getContextPath()%>/datasets/<%=dsID%>">
                                                                <b><%=Util.processForDisplay(dataset.getShortName())%></b>
                                                            </a>
                                                        </em>
                                                        <%
                                                            if (mode.equals("view") && dataset.isWorkingCopy()) {
                                                        %>
                                                            <span class="caution">(Working copy)</span><%
                                                                }
                                                            %>
                                                        <input type="hidden" name="ds_id" value="<%=dsID%>"/>
                                                    </td>
                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr>
                                                <%
                                                    // table
                                                %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Table
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=element&amp;area=table"></a>
                                                    </th>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <td class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                        </td><%
                                                            }
                                                        %>
                                                    <td class="simple_attr_value">
                                                        <em>
                                                            <a href="<%=request.getContextPath()%>/tables/<%=dsTable.getID()%>">
                                                                <%=Util.processForDisplay(dsTable.getShortName())%>
                                                            </a>
                                                        </em>
                                                        <input type="hidden" name="table_id" value="<%=dsTable.getID()%>"/>
                                                    </td>
                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr><%
                                                    } // end of dataset & table part (relevant only for non-common elements)
                                                %>

                                            <!-- RegistrationStatus, relevant for common elements only -->
                                            <%
                                                if (elmCommon) {
                                            %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        RegistrationStatus
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=regstatus"></a>
                                                    </th>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <td class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                        </td><%
                                                            }
                                                        %>
                                                    <td class="simple_attr_value">
                                                        <%
                                                        if (mode.equals("view")){ %>
                                                            <%=Util.processForDisplay(elmRegStatus)%>
                                                            <%
                                                            long timestamp = dataElement.getDate()==null ? 0 : Long.parseLong(dataElement.getDate());
                                                            String dateString = timestamp==0 ? "" : eionet.util.Util.releasedDate(timestamp);
                                                            String dateTimeString = timestamp==0 ? "" : dateString + " " + eionet.util.Util.hoursMinutesSeconds(timestamp);

                                                            if (elmWorkingUser != null) {
                                                                if (dataElement.isWorkingCopy() && user != null && elmWorkingUser.equals(user.getUserName())){
                                                                     %>
                                                                    <span class="caution" title="Checked out on <%=dateTimeString%>">(Working copy)</span><%
                                                                }
                                                                else if (user!=null){
                                                                    %>
                                                                    <span class="caution">(checked out by <em><%=elmWorkingUser%></em>)</span><%
                                                                }
                                                            }
                                                            else if (dateString.length()>0 && (elmRegStatus.equalsIgnoreCase("RELEASED") || user!=null)){
                                                                if (user==null){
                                                                    %><span><%=dateString%></span><%
                                                                }
                                                                else{
                                                                    %><span style="color:#A8A8A8;font-size:0.8em">(checked in <%=dateTimeString%> by <%=dataElement.getUser()%>)</span><%
                                                                }
                                                            }
                                                        }
                                                        else {
                                                        %>
                                                            <select name="reg_status" onchange="form_changed('form1')"> <%
                                                                    Vector regStatuses = "add".equals(mode) ? verMan.getSettableRegStatuses() : verMan.getRegStatuses();
																	for (int i = 0; i < regStatuses.size(); i++) {
																	    String status = (String) regStatuses.get(i);
																	    String selected = status.equals(elmRegStatus) ? "selected=\"selected\"" : "";
																	    String disabled = verMan.getSettableRegStatuses().contains(status) ? "" : "disabled=\"disabled\"";
		                                                                String title = disabled.length() > 0 ? "title=\"This status not allowed any more when adding/saving.\"" : "";
		                                                                String style = disabled.length() > 0 ? "style=\"background-color: #F2F2F2;\"" : "";
																	    %>
                                                                        <option <%=style%> <%=selected%> <%=disabled%> <%=title%> value="<%=Util.processForDisplay(status)%>"><%=Util.processForDisplay(status)%></option><%
                                                                    }
                                                                %>
                                                            </select><%
                                                                }
                                                            %>
                                                    </td>

                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr><%
                                                    }
                                                %>

                                            <!-- Reference URL -->
                                            <%
                                            String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
                                            if (mode.equals("view") && jspUrlPrefix != null) {
                                                String refUrl = dataElement.getReferenceURL();
                                                %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Reference URL
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=refurl"></a>
                                                    </th>
                                                    <td class="simple_attr_value">
                                                        <small><a href="<%=refUrl%>"><%=refUrl%></a></small>
                                                    </td>
                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr><%
                                                    }

                                                        String elmDataType = type == null || !type.equals("CH3") ? "string" : "reference";
                                                        if (mode.equals("add") || mode.equals("edit")) {
                                                            String _elmDataType = request.getParameter("elm_datatype");
                                                            if (_elmDataType != null && _elmDataType.length() > 0)
                                                                elmDataType = _elmDataType;
                                                            else if (mode.equals("edit")) {
                                                                _elmDataType = dataElement == null ? null : dataElement
                                                                        .getAttributeValueByShortName("Datatype");
                                                                if (_elmDataType != null && _elmDataType.length() > 0)
                                                                    elmDataType = _elmDataType;
                                                            }
                                                        } else {
                                                            String _elmDataType = dataElement == null ? null
                                                                    : dataElement
                                                                            .getAttributeValueByShortName("Datatype");
                                                            if (_elmDataType != null && _elmDataType.length() > 0)
                                                                elmDataType = _elmDataType;
                                                        }
                                                %>

                                            <!-- dynamic attributes -->
                                            <%
                                                String attrID = null;
                                                    String attrValue = null;
                                                    DElemAttribute attribute = null;
                                                    boolean isBoolean = false;
                                                    boolean imagesQuicklinkSet = false;
                                                    for (int i = 0; mAttributes != null && i < mAttributes.size(); i++) {

                                                        attribute = (DElemAttribute) mAttributes.get(i);
                                                        String dispType = attribute.getDisplayType();
                                                        if (dispType == null)
                                                            continue;
                                                        
                                                        if (dispType.equals("vocabulary") && mode.equals("add")) {
                                                            continue;
                                                        }

                                                        boolean dispFor = type == null ? attribute
                                                                .displayFor("CH2") : attribute.displayFor(type);

                                                        if (!dispFor)
                                                            continue;

                                                        if (Util.skipAttributeByDatatype(attribute.getShortName(),
                                                                elmDataType))
                                                            continue;

                                                        attrID = attribute.getID();

                                                        if (attribute.getShortName().equalsIgnoreCase("Datatype"))
                                                            attrValue = elmDataType;
                                                        else
                                                            attrValue = getValue(attrID, mode, dataElement,
                                                                    newDataElement);

                                                        String attrOblig = attribute.getObligation();
                                                        String obligImg = "optional.gif";
                                                        if (attrOblig.equalsIgnoreCase("M"))
                                                            obligImg = "mandatory.gif";
                                                        else if (attrOblig.equalsIgnoreCase("C"))
                                                            obligImg = "conditional.gif";

                                                        // set isBoolean if the element is of boolean datatype
                                                        if (attribute.getShortName().equalsIgnoreCase("Datatype")) {
                                                            if (attrValue != null
                                                                    && attrValue.equalsIgnoreCase("boolean"))
                                                                isBoolean = true;
                                                        }

                                                        // if element is of CH1 type, don't display MinSize and MaxSize
                                                        if (attribute.getShortName().equalsIgnoreCase("MaxSize")
                                                                || attribute.getShortName().equalsIgnoreCase(
                                                                        "MinSize"))
                                                            if (type != null && (type.equalsIgnoreCase("CH1") || type.equalsIgnoreCase("CH3") ))
                                                                continue;

                                                        if (mode.equals("view")
                                                                && (attrValue == null || attrValue.length() == 0)) 
                                                            continue;

                                                        // if image attribute, but not the case to display it, then skip
                                                        if (dispType.equals("image")) {
                                                            if (mode.equals("add")
                                                                    || (mode.equals("edit") && user == null)
                                                                    || (mode.equals("view") && Util.isEmpty(attrValue))) {
                                                                continue;
                                                            }
                                                        }

                                                        //displayed++; - done below

                                                        String width = attribute.getDisplayWidth();
                                                        String height = attribute.getDisplayHeight();

                                                        String disabled = user == null ? "disabled" : "";
                                                        boolean dispMultiple = attribute.getDisplayMultiple()
                                                                .equals("1") ? true : false;
                                                        boolean inherit = attribute.getInheritable().equals("0")
                                                                || elmCommon ? false : true;

                                                        Vector multiValues = null;
                                                        String inheritedValue = null;

                                                        if (!mode.equals("view")) {
                                                            if (inherit) {
                                                                inheritedValue = getValue(attrID, 2, mode,
                                                                        dataElement, newDataElement);
                                                            }

                                                            if (mode.equals("edit")) {
                                                                if (dispMultiple) {
                                                                    if (inherit) {
                                                                        multiValues = getValues(attrID, 1, mode,
                                                                                dataElement, newDataElement); //original values only
                                                                    } else {
                                                                        multiValues = getValues(attrID, 0, mode,
                                                                                dataElement, newDataElement); //all values
                                                                    }
                                                                } else {
                                                                    if (inherit)
                                                                        attrValue = getValue(attrID, 1, mode,
                                                                                dataElement, newDataElement); //get original value
                                                                }
                                                            }
                                                        }
                                            %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        <%=Util.processForDisplay(attribute.getName())%>
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                                                    </th>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <td class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/<%=Util.processForDisplay(obligImg)%>" width="16" height="16" alt=""/>
                                                        </td><%
                                                            }
                                                        %>

                                                    <!-- dynamic attribute value display -->
                                                    <td class="simple_attr_value"><%
                                                                if(dispType.equals("vocabulary")) {
                                                                    DataDictEntity ddEntity = new DataDictEntity(Integer.parseInt(delem_id), DataDictEntity.Entity.E);
                                                                    if(mode.equals("view")){
                                                                        List<VocabularyConcept> concepts = searchEngine.getAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity, attribute.getInheritable());
                                                                        if(concepts!=null){ %>
                                                                            <c:forEach var="concept" items="<%=concepts%>" varStatus="count">
                                                                                <c:out value="${concept.label}"/><c:if test="${!count.last}">, </c:if>
                                                                            </c:forEach>
                                                                        <%}                                                                      
                                                                    } else if (mode.equals("edit")) {
                                                                        if (inherit){
                                                                            List<VocabularyConcept> inheritedValues = searchEngine.getInheritedAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity);
                                                                            if(inheritedValues!=null && !inheritedValues.isEmpty()){
                                                                            %>
                                                                                <c:set var="inheritable" value="<%=attribute.getInheritable()%>" />
                                                                                <c:choose>
                                                                                    <c:when test="${inheritable eq '2'}">
                                                                                        <c:out value="Overriding parent level value: "/>
                                                                                    </c:when>
                                                                                    <c:when test="${inheritable eq '1'}">
                                                                                        <c:out value="Inherited from parent level: "/>
                                                                                    </c:when>
                                                                                </c:choose>
                                                                                <c:forEach var="value" items="<%=inheritedValues%>" varStatus="count">
                                                                                    <c:out value="${value.label}"/><c:if test="${!count.last}">, </c:if>
                                                                                </c:forEach>
                                                                                </br>
                                                                          <%}
                                                                        }
                                                                        List<VocabularyConcept> originalValues = searchEngine.getOriginalAttributeVocabularyConcepts(Integer.parseInt(attrID), ddEntity);
                                                                          %>
                                                                        <c:forEach var="concept" items="<%=originalValues%>">
                                                                            <input type="hidden" name="attr_mult_<%=attrID%>" value="${concept.identifier}"/>
                                                                        </c:forEach>
                                                                        <%if (searchEngine.existsVocabularyBinding(Integer.parseInt(attrID))){%>
                                                                            <a href="<%=request.getContextPath()%>/vocabularyvalues/attribute/<%=attrID%>/dataelement/<%=dataElement.getID()%>">[Manage links to the vocabulary]</a>
                                                                        <%} else {%>
                                                                            [Manage links to the vocabulary]
                                                                        <%}
                                                                  }%>
                                                              <%}
                                                        // handle image attribute first
                                                                else if (dispType.equals("image")) {

                                                                    if (!imagesQuicklinkSet) {
                                                    %>
                                                                <a id="images"></a><%
                                                                    imagesQuicklinkSet = true;
                                                                                }

                                                                                // thumbnail
                                                                                if (mode.equals("view") && !Util.isEmpty(attrValue)) {
                                                                %>
                                                                <div class="figure-plus-container">
                                                                    <div class="figure-plus">
                                                                        <div class="figure-image">
                                                                            <a href="<%=request.getContextPath()%>/visuals/<%=Util.processForDisplay(attrValue)%>">
                                                                                <img src="<%=request.getContextPath()%>/visuals/<%=Util.processForDisplay(attrValue)%>" alt="Image file could not be found on the server" class="scaled2 poponmouseclick"/>
                                                                            </a>
                                                                        </div>
                                                                    </div>
                                                                </div><%
                                                                    }

                                                                                // link to image edit page
                                                                                if (mode.equals("edit") && user != null) {
                                                                                    String actionText = Util.isEmpty(attrValue) ? "add image"
                                                                                            : "manage this image";
                                                                %>
                                                                <div>
                                                                    <a href="<%=request.getContextPath()%>/imgattr.jsp?obj_id=<%=delem_id%>&amp;obj_type=E&amp;attr_id=<%=attribute.getID()%>&amp;obj_name=<%=Util.processForDisplay(dataElement
                                    .getShortName())%>&amp;attr_name=<%=Util.processForDisplay(attribute.getShortName())%>">[Click to <%=Util.processForDisplay(actionText)%>]</a>
                                                                </div><%
                                                                    }
                                                                            }
                                                                            // if view mode or Datatype in edit mode, display simple text
                                                                            else if (mode.equals("view")) {
                                                                %>
                                                            <%=Util.processForDisplay(attrValue)%><%
                                                                }
                                                                        // if non-view mode, display input
                                                                        else {

                                                                            // inherited value(s)
                                                                            if (inherit && inheritedValue != null) {
                                                                                String sInhText = (((dispMultiple && multiValues != null) || (!dispMultiple && attrValue != null)) && attribute
                                                                                        .getInheritable().equals("2")) ? "Overriding parent level value: "
                                                                                        : "Inherited from parent level: ";

                                                                                if (sInhText.startsWith("Inherited")) {
                                                            %>
                                                                        <%=Util.processForDisplay(sInhText)%><%=Util.processForDisplay(inheritedValue)%><br/><%
                                                                            }
                                                                                        }

                                                                                        // mutliple display
                                                                                        if (dispMultiple && !dispType.equals("image")) {

                                                                                            Vector allPossibleValues = null;
                                                                                            if (dispType.equals("select"))
                                                                                                allPossibleValues = searchEngine
                                                                                                        .getFixedValues(attrID, "attr");
                                                                                            else if (dispType.equals("text"))
                                                                                                allPossibleValues = searchEngine
                                                                                                        .getSimpleAttributeValues(attrID);

                                                                                            String divHeight = "7.5em";
                                                                                            String textName = "other_value_attr_" + attrID;
                                                                                            String divID = "multiselect_div_attr_" + attrID;
                                                                                            String checkboxName = "attr_mult_" + attrID;
                                                                                            Vector displayValues = new Vector();
                                                                                            if (multiValues != null && multiValues.size() > 0)
                                                                                                displayValues.addAll(multiValues);
                                                                                            if (allPossibleValues != null
                                                                                                    && allPossibleValues.size() > 0)
                                                                                                displayValues.addAll(allPossibleValues);
                                                                        %>
                                                                <input type="text" name="<%=textName%>" value="insert other value" style="font-size:0.9em" onfocus="this.value=''"/>
                                                                <input type="button" value="-&gt;" style="font-size:0.8em;" onclick="addMultiSelectRow(document.forms['form1'].elements['<%=textName%>'].value, '<%=checkboxName%>','<%=divID%>')"/>
                                                                <div id="<%=divID%>" class="multiselect" style="height:<%=divHeight%>;width:25em;">
                                                                    <%
                                                                        HashSet displayedSet = new HashSet();
                                                                                        for (int k = 0; displayValues != null
                                                                                                && k < displayValues.size(); k++) {

                                                                                            Object valueObject = displayValues.get(k);
                                                                                            attrValue = (valueObject instanceof FixedValue) ? ((FixedValue) valueObject)
                                                                                                    .getValue() : valueObject.toString();
                                                                                            if (displayedSet.contains(attrValue))
                                                                                                continue;

                                                                                            String strChecked = "";
                                                                                            if (multiValues != null
                                                                                                    && multiValues.contains(attrValue))
                                                                                                strChecked = "checked=\"checked\"";
                                                                    %>
                                                                        <label style="display:block">
                                                                            <input type="checkbox" name="<%=checkboxName%>" value="<%=attrValue%>" <%=strChecked%> style="margin-right:5px"/><%=attrValue%>
                                                                        </label>
                                                                        <%
                                                                            displayedSet.add(attrValue);
                                                                                            }
                                                                        %>
                                                                </div><%
                                                                    }

                                                                                // no multiple display
                                                                                else {
                                                                                    if (dispType.equals("text")) {
                                                                                        if (attrValue != null) {
                                                                %>
                                                                        <input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%if (dispMultiple)%> mult_<%;%><%=attrID%>" value="<%=Util.processForDisplay(attrValue)%>" onchange="form_changed('form1')"/>
                                                                        <%
                                                                            } else {
                                                                        %>
                                                                        <input <%=disabled%> class="smalltext" type="text" size="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"/>
                                                                        <%
                                                                            }
                                                                                            } else if (dispType.equals("textarea")) {
                                                                                                if (attrValue != null) {
                                                                        %>
                                                                        <textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"><%=Util.processForDisplay(attrValue, true,
                                            true)%></textarea>
                                                                        <%
                                                                            } else {
                                                                        %>
                                                                        <textarea <%=disabled%> class="small" rows="<%=height%>" cols="<%=width%>" name="attr_<%=attrID%>" onchange="form_changed('form1')"></textarea>
                                                                        <%
                                                                            }
                                                                                            } else if (dispType.equals("select")) {

                                                                                                String onchange = "";
                                                                                                if (attribute.getShortName().equalsIgnoreCase(
                                                                                                        "Datatype"))
                                                                                                    onchange = " onchange=\"changeDatatype()\"";
                                                                                                else
                                                                                                    onchange = " onchange=\"form_changed('form1')\"";
                                                                        %>
                                                                    <select <%=disabled%> class="small" name="attr_<%=attrID%>"<%=onchange%>>
                                                                        <%
                                                                            Vector fxValues = searchEngine.getFixedValues(
                                                                                                        attrID, "attr");
                                                                                                if (fxValues == null || fxValues.size() == 0) {
                                                                        %>
                                                                            <option selected="selected" value=""></option> <%
     } else {
                             boolean selectedByValue = false;
                             for (int g = 0; g < fxValues.size(); g++) {
                                 FixedValue fxValue = (FixedValue) fxValues
                                         .get(g);

                                 String isSelected = (fxValue
                                         .getDefault() && !selectedByValue) ? "selected=\"selected\""
                                         : "";

                                 if (attribute.getShortName()
                                         .equalsIgnoreCase("Datatype")) {
                                     if (type != null
                                             && type.equals("CH2")
                                             && fxValue.getValue()
                                                     .equalsIgnoreCase(
                                                             "boolean"))
                                         continue;
                                 }

                                 if (attrValue != null
                                         && attrValue.equals(fxValue
                                                 .getValue())) {
                                     isSelected = "selected=\"selected\"";
                                     selectedByValue = true;
                                 }
 %>
                                                                                <option <%=isSelected%> value="<%=Util.processForDisplay(fxValue
                                                .getValue())%>"><%=Util.processForDisplay(fxValue
                                                .getValue())%></option> <%
     }
                         }
 %>
                                                                    </select>
                                                                    <a class="helpButton" href="<%=request.getContextPath()%>/fixedvalues/attr/<%=attrID%>"></a>
                                                                    <%
                                                                        } else if (dispType.equals("vocabulary")){
                                                                            if (searchEngine.existsVocabularyBinding(Integer.parseInt(attrID))){%>
                                                                                <a href="<%=request.getContextPath()%>/vocabularyvalues/attribute/<%=attrID%>/dataelement/<%=dataElement.getID()%>">[Manage links to the vocabulary]</a>
                                                                            <%} else {%>
                                                                                [Manage links to the vocabulary]
                                                                            <%}
                                                                    }else {%>
                                                                    Unknown display type!<%
                                                                        }
                                                                                    }
                                                                                } // end display input
                                                                    %>
                                                <input type="hidden" name="oblig_<%=attrID%>" value="<%=Util.processForDisplay(attribute.getObligation(), true)%>"/>
                                                    </td>
                                                    <!-- end dynamic attribute value display -->

                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr>
                                                <%
                                                    }
                                                %>
                                            <!-- end dynamic attributes -->

                                            <!-- version (or the so-called CheckInNo), relevant for common elements only -->
                                            <%
                                                if (verMan == null)
                                                        verMan = new VersionManager();

                                                    if (elmCommon && !mode.equals("add")) {
                                                        String elmVersion = dataElement.getVersion();
                                            %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        CheckInNo
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=check_in_no"></a>
                                                    </th>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <td class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                                                        </td><%
                                                            }
                                                        %>
                                                    <td class="simple_attr_value">
                                                        <%=elmVersion%>
                                                    </td>
                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr>
                                                <%
                                                    }
                                                %>

                                            <%
                                                if (vocabulary != null) {
                                            %>
                                                <tr class="<%=isOdd%>">
                                                    <th scope="row" class="scope-row simple_attr_title">
                                                        Vocabulary
                                                        <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=vocabulary_link"></a>
                                                    </th>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                    <td class="simple_attr_help">
                                                        <img style="border:0" src="<%=request.getContextPath()%>/images/optional.gif" width="16" height="16" alt=""/>
                                                    </td>
                                                    <%
                                                        }
                                                    %>
                                                    <td class="simple_attr_value">
                                                        <%
                                                            String vocabularyUri = request.getContextPath() + "/vocabulary/" + vocabulary.getFolderName() + "/" + vocabulary.getIdentifier();
                                                            boolean vocabularyEditing = "edit".equals(mode);

                                                            if (vocabularyEditing) {
                                                        %>
                                                        <input type="radio" name="all_concepts_legal" value="1" <% if (allowAllConcepts) { %> checked="checked" <%}%>>
                                                        <%
                                                            }
                                                            else {
                                                        %>
                                                        <div style="display: <%=allowAllConcepts ? "inherit" : "none"%>;">
                                                        <%
                                                            }
                                                        %>
                                                            All accepted concepts from the vocabulary
                                                            <a href="<%=vocabularyUri%>"><%=vocabulary.getLabel()%></a>
                                                            in the <em><%=vocabulary.getFolderName()%></em> set <br/> (Code = Notation)
                                                        <%
                                                            if (vocabularyEditing) {
                                                        %>
                                                        <br/>
                                                        <input type="radio" name="all_concepts_legal" value="0" <% if (!allowAllConcepts) { %> checked="checked" <%}%>>
                                                        <%
                                                            }
                                                            else {
                                                        %>
                                                        </div>
                                                        <div style="display: <%=!allowAllConcepts ? "inherit" : "none"%>;">
                                                        <%
                                                            }
                                                        %>
                                                            Only concepts accepted before the release of the data element are used from the vocabulary
                                                            <a href="<%=vocabularyUri%>"><%=vocabulary.getLabel()%></a>
                                                            in the <em><%=vocabulary.getFolderName()%></em> set
                                                        <%
                                                            if (!vocabularyEditing) {
                                                        %>
                                                        </div>
                                                        <%
                                                            }
                                                        %>
                                                    </td>
                                                    <%
                                                        isOdd = Util.isOdd(++displayed);
                                                    %>
                                                </tr>
                                            <%
                                                }
                                            %>

                                            <%
                                            pageContext.setAttribute("dataElement", dataElement);
                                            pageContext.setAttribute("mode", mode);
                                            /* if (mode.equals("edit") || mode.equals("add")) {
                                                pageContext.setAttribute("rdfNamespaces", searchEngine.getRdfNamespaces());
                                            } */
                                            %>
                                            <!-- add, save, check-in, undo check-out buttons -->
                                            <%
                                                // add case
                                                if (mode.equals("add")) {
                                            %>
                                                <tr>
                                                    <th></th>
                                                    <td colspan="3">
                                                        <input type="button" class="mediumbuttonb" value="Add" onclick="submitForm('add')"/>
                                                        <input type="button" class="mediumbuttonb" value="Copy"
                                                            onclick="copyElem()"
                                                            title="Opens an element search window, and from the search results you can select an element to copy."/>
                                                    </td>
                                                </tr>
                                            <%
                                                }// edit case
                                                else if (mode.equals("edit")) {
                                            %>
                                                <tr>
                                                    <th></th>
                                                    <td colspan="3">
                                                        <input type="button" class="mediumbuttonb" value="Save" onclick="submitForm('edit')"/>&nbsp;
                                                        <input type="button" class="mediumbuttonb" value="Save &amp; close" onclick="submitForm('editclose')"/>&nbsp;
                                                        <input type="button" class="mediumbuttonb" value="Cancel" onclick="goTo('view', '<%=delem_id%>')"/>
                                                    </td>
                                                </tr>
                                                    <%
                                                    }
                                                    %>
                                        </table>
                                        <!-- end of attributes -->

                                        <!-- allowable/suggested values -->
                                        <%
                                            boolean key = (mode.equals("edit") && user != null)
                                                        || (mode.equals("view") && fixedValues != null && fixedValues
                                                                .size() > 0);
                                                if (type != null && key) {
                                                    String title = "";
                                                    if (type.equals("CH1")) {
                                                      title =  "Allowable values";
                                                    } else if (type.equals("CH2")) {
                                                      title = "Suggested values";
                                                    } else if (type.equals("CH3")) {
                                                        title = "Vocabulary";
                                                    }
/*                                                     String title = (type.equals("CH1") || type.equals("CH3")) ? "Allowable values"
                                                            : "Suggested values";

*/

String helpAreaName = "";
                                              if (type.equals("CH1")) {
                                                  helpAreaName =  "allowable_values_link";
                                              } else if (type.equals("CH2")) {
                                                  helpAreaName =  "suggested_values_link";
                                              } else if (type.equals("CH3")) {
                                                  helpAreaName =  "vocabulary_link";
                                              }

                                        %>


                                                <!-- title & link part -->


                                                <h2>
                                                        <%=title%><a id="values"></a>

                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <span class="simple_attr_help">
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=element&amp;area=<%=Util.processForDisplay(helpAreaName)%>"></a>
                                                        </span>
                                                        <span class="simple_attr_help">
                                                            <% if (type.equals("CH3")) { %>
                                                                <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt="optional"/>
                                                            <%} else { %>
                                                                <img style="border:0" src="<%=request.getContextPath()%>/images/optional.gif" width="16" height="16" alt="optional"/>
                                                            <%} %>

                                                        </span><%
                                                            }

                                                                    // the link
                                                                    String valuesLink = "";
                                                                    if (!type.equals("CH3")) {
                                                                        String valuesLinkAction = "view".equals(mode) ? "view" : "edit";
                                                                        valuesLink = request.getContextPath() + "/fixedvalues/elem/" + delem_id + "/" + valuesLinkAction;
                                                                    } else {
                                                                        if (vocabulary != null) {
                                                                            valuesLink = request.getContextPath() + "/vocabulary/" + vocabulary.getFolderName()
                                                                                + "/" + vocabulary.getIdentifier() + "/view";
                                                                        }
                                                                    }

                                                                    if (mode.equals("edit") && user != null) {
                                                        %>
                                                        <span class="barfont_bordered">
                                                            <% if (type.equals("CH3")) { %>
                                                                <a id="selectVocabularyLnk" href="#">[Click to select vocabulary for values of this element]</a>
                                                            <%} else { %>
                                                                <a href="<%=valuesLink%>">[Click to manage <%=Util.processForDisplay(title.toLowerCase())%> of this element]</a>
                                                            <%} %>
                                                        </span><%
                                                            }
                                                        %>
                                                </h2>

                                                <!-- table part -->
                                                <%
                                                    if (mode.equals("view") && fixedValues != null
                                                                    && fixedValues.size() > 0) {
                                                %>
                                                            <table class="datatable results">
                                                                <col style="width:20%"/>
                                                                <col style="width:40%"/>
                                                                <col style="width:40%"/>
                                                                <thead>
                                                                    <tr>
                                                                        <th>Code</th>
                                                                        <th>Label</th>
                                                                        <th>Definition</th>
                                                                    </tr>
                                                                </thead>
                                                                <%
                                                                    // rows
                                                                                for (int i = 0; i < fixedValues.size()
                                                                                        && i < MAX_DISP_VALUES + 1; i++) {

                                                                                    FixedValue fxv = null;
                                                                                    String value = "";
                                                                                    String defin = "";
                                                                                    String shortDesc = "";
                                                                                    String valueLink = "";

                                                                                    if (i == MAX_DISP_VALUES) {
                                                                                        value = ". . .";
                                                                                        defin = ". . .";
                                                                                        shortDesc = ". . .";
                                                                                        valueLink = valuesLink;
                                                                                    } else {
                                                                                        fxv = (FixedValue) fixedValues.get(i);
                                                                                        value = fxv.getValue();
                                                                                        defin = fxv.getDefinition();
                                                                                        shortDesc = fxv.getShortDesc();
                                                                                        if (type.equals("CH3")) {
                                                                                            //build concept link if CH3
                                                                                            valueLink = request.getContextPath() + "/vocabularyconcept/"
                                                                                                    + vocabulary.getFolderName() + "/" + vocabulary.getIdentifier()
                                                                                                    + "/" + fxv.getCsID() + "/view";
                                                                                        }
                                                                                        else {
                                                                                            valueLink = request.getContextPath() + "/fixedvalues/elem/" + delem_id + "/view/" + java.net.URLEncoder.encode(fxv.getID(), "UTF-8");
                                                                                        }
                                                                                    }

                                                                                    defin = defin == null ? "" : defin;
                                                                                    String dispDefin = defin.length() > MAX_CELL_LEN ? defin
                                                                                            .substring(0, MAX_CELL_LEN) + "..."
                                                                                            : defin;

                                                                                    shortDesc = shortDesc == null ? "" : shortDesc;
                                                                                    String dispShortDesc = shortDesc.length() > MAX_CELL_LEN ? shortDesc
                                                                                            .substring(0, MAX_CELL_LEN) + "..."
                                                                                            : shortDesc;
                                                                %>
                                                                    <tr>
                                                                        <td>
                                                                            <%
                                                                                if (valueLink.length() > 0) {

                                                                            %>
                                                                            <a href="<%=valueLink%>">
                                                                            <%
                                                                                }
                                                                            %>
                                                                                <%=Util.processForDisplay(value)%>
                                                                            <%
                                                                                if (valueLink.length() > 0) {

                                                                            %>
                                                                            </a>
                                                                            <%
                                                                                }
                                                                            %>
                                                                        </td>
                                                                        <td title="<%=Util.processForDisplay(shortDesc, true)%>">
                                                                            <%=Util.processForDisplay(dispShortDesc)%>
                                                                        </td>
                                                                        <td title="<%=Util.processForDisplay(defin, true)%>">
                                                                            <%=Util.processForDisplay(dispDefin)%>
                                                                        </td>
                                                                    </tr><%
                                                                        }
                                                                    %>
                                                            </table>
                                                    <%
                                                        }
                                                            }
                                                    %>


                                        <!-- foreign key relations, relevant for non-common elements only -->

                                        <%
                                            if (!elmCommon
                                                        && (mode.equals("edit") || (mode.equals("view")
                                                                && fKeys != null && fKeys.size() > 0))) {
                                        %>
                                                <!-- title & link part -->
                                                <h2>
                                                    Foreign key relations<a id="fkeys"></a>
                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <span class="simple_attr_help">
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=element&amp;area=fks_link"></a>
                                                        </span>
                                                        <span class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/optional.gif" width="16" height="16" alt="optional"/>
                                                        </span><%
                                                            }
                                                                    // the link
                                                                    if (mode.equals("edit")) {
                                                        %>
                                                        <span class="barfont_bordered">
                                                            <a href="<%=request.getContextPath()%>/foreign_keys.jsp?delem_id=<%=delem_id%>&amp;delem_name=<%=Util.processForDisplay(delem_name)%>&amp;ds_id=<%=dsID%>&amp;table_id=<%=tableID%>">[Click to manage foreign keys of this element]</a>
                                                        </span><%
                                                            }
                                                        %>
                                                </h2>

                                                <!-- table part -->
                                                <%
                                                    if (mode.equals("view") && fKeys != null
                                                                    && fKeys.size() > 0) {
                                                %>
                                                            <table class="datatable" style="width:68%">
                                                                <tr>
                                                                    <th style="width:50%">Element</th>
                                                                    <th style="width:50%">Table</th>
                                                                </tr>
                                                                <%
                                                                    // rows
                                                                                for (int i = 0; i < fKeys.size(); i++) {

                                                                                    Hashtable fkRel = (Hashtable) fKeys.get(i);
                                                                                    String fkElmID = (String) fkRel.get("elm_id");
                                                                                    String fkElmName = (String) fkRel.get("elm_name");
                                                                                    String fkTblName = (String) fkRel.get("tbl_name");
                                                                                    String fkRelID = (String) fkRel.get("rel_id");

                                                                                    if (fkElmID == null || fkElmID.length() == 0)
                                                                                        continue;
                                                                %>
                                                                    <tr>
                                                                        <td style="width:50%">
                                                                            <a href="<%=request.getContextPath()%>/dataelements/<%=fkElmID%>">
                                                                                <%=Util.processForDisplay(fkElmName)%>
                                                                            </a>
                                                                        </td>
                                                                        <td style="width:50%">
                                                                            <%=Util.processForDisplay(fkTblName)%>
                                                                        </td>
                                                                    </tr><%
                                                                        }
                                                                    %>
                                                            </table>
                                                    <%
                                                        }
                                                            }
                                                    %>

                                        <!-- referring tables , relevant for common elements only -->

                                        <%
                                            if (elmCommon && mode.equals("view") && refTables != null
                                                        && refTables.size() > 0) {
                                        %>

                                                <!-- title part -->
                                                <h2>
                                                        Tables using this common element<a id="fkeys"></a>
                                                </h2>

                                                <!-- table part -->
                                                        <table class="datatable results">
                                                            <col style="width: 43%"/>
                                                            <col style="width: 43%"/>
                                                            <col style="width: 14%"/>
                                                            <thead>
                                                                <tr>
                                                                    <th>Table</th>
                                                                    <th>Dataset</th>
                                                                    <th>Owner</th>
                                                                </tr>
                                                            </thead>
                                                            <%
                                                                // rows
                                                                        for (int i = 0; i < refTables.size(); i++) {

                                                                            DsTable tbl = (DsTable) refTables.get(i);
                                                                            String tblLink = "";
                                                                            String dstLink = "";
                                                                            if (isLatestRequested) {
                                                                                dstLink = request.getContextPath() + "/datasets/latest/" + tbl.getDstIdentifier();
                                                                                tblLink = dstLink + "/tables/" + tbl.getIdentifier();
                                                                            } else {
                                                                                tblLink = request.getContextPath() + "/tables/" + tbl.getID();
                                                                                dstLink = request.getContextPath() + "/datasets/" + tbl.getDatasetID();
                                                                            }

                                                                            String owner = tbl.getOwner();
                                                                            owner = owner == null ? "Not specified" : owner;
                                                            %>
                                                                <tr>
                                                                    <td>
                                                                        <a href="<%=tblLink%>">
                                                                            <%=Util.processForDisplay(tbl.getShortName())%>
                                                                        </a>
                                                                    </td>
                                                                    <td>
                                                                        <a href="<%=dstLink%>">
                                                                            <%=Util.processForDisplay(tbl.getDatasetName())%>
                                                                        </a>
                                                                    </td>
                                                                    <td>
                                                                        <%=Util.processForDisplay(owner)%>
                                                                    </td>
                                                                </tr><%
                                                                    }
                                                                %>
                                                        </table>
                                            <%
                                                }
                                            %>

                                        <!-- complex attributes -->

                                        <%
                                            if ((mode.equals("edit") && user != null)
                                                        || (mode.equals("view") && complexAttrs != null && complexAttrs
                                                                .size() > 0)) {
                                        %>


                                                <h2>
                                                        Complex attributes<a id="cattrs"></a>

                                                    <%
                                                        if (!mode.equals("view")) {
                                                    %>
                                                        <span class="simple_attr_help">
                                                            <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=complex_attrs_link"></a>
                                                        </span>
                                                        <span class="simple_attr_help">
                                                            <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt="mandatory"/>
                                                        </span><%
                                                            }

                                                                    // the link
                                                                    if (mode.equals("edit") && user != null) {
                                                        %>
                                                        <span class="barfont_bordered">
                                                            <a href="<%=request.getContextPath()%>/complex_attrs.jsp?parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=Util.processForDisplay(delem_name)%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>">[Click to manage complex attributes of this element]</a>
                                                        </span><%
                                                            }
                                                        %>
                                                </h2>

                                                <%
                                                    // the table
                                                            if (mode.equals("view") && complexAttrs != null
                                                                    && complexAttrs.size() > 0) {
                                                %>
                                                            <table class="datatable results" id="dataset-attributes">
                                                                <col style="width: 30%"/>
                                                                <col style="width: 70%"/>
                                                                <%
                                                                    displayed = 1;
                                                                                isOdd = Util.isOdd(displayed);
                                                                                for (int i = 0; i < complexAttrs.size(); i++) {

                                                                                    DElemAttribute attr = (DElemAttribute) complexAttrs
                                                                                            .get(i);
                                                                                    attrID = attr.getID();
                                                                                    String attrName = attr.getName();
                                                                                    Vector attrFields = searchEngine.getAttrFields(
                                                                                            attrID, DElemAttribute.FIELD_PRIORITY_HIGH);
                                                                %>

                                                                    <tr class="<%=isOdd%>">
                                                                        <td>
                                                                            <a href="<%=request.getContextPath()%>/complex_attr.jsp?attr_id=<%=attrID%>&amp;parent_id=<%=delem_id%>&amp;parent_type=E&amp;parent_name=<%=Util.processForDisplay(delem_name)%>&amp;table_id=<%=tableID%>&amp;dataset_id=<%=dsID%>" title="Click here to view all the fields">
                                                                                <%=Util.processForDisplay(attrName)%>
                                                                                <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?attrid=<%=attrID%>&amp;attrtype=COMPLEX"></a>
                                                                            </a>
                                                                        </td>
                                                                        <td>
                                                                            <%
                                                                                StringBuffer rowValue = null;
                                                                                                Vector rows = attr.getRows();
                                                                                                for (int j = 0; rows != null && j < rows.size(); j++) {

                                                                                                    if (j > 0) {
                                                                            %>---<br/><%
                                                                                }

                                                                                                    Hashtable rowHash = (Hashtable) rows.get(j);
                                                                                                    rowValue = new StringBuffer();

                                                                                                    for (int t = 0; t < attrFields.size(); t++) {
                                                                                                        Hashtable hash = (Hashtable) attrFields
                                                                                                                .get(t);
                                                                                                        String fieldID = (String) hash.get("id");
                                                                                                        String fieldValue = fieldID == null ? null
                                                                                                                : (String) rowHash.get(fieldID);
                                                                                                        if (fieldValue == null)
                                                                                                            fieldValue = "";
                                                                                                        if (fieldValue.trim().equals(""))
                                                                                                            continue;

                                                                                                        if (t > 0 && fieldValue.length() > 0
                                                                                                                && rowValue.toString().length() > 0)
                                                                                                            rowValue.append(", ");

                                                                                                        rowValue.append(Util.processForDisplay(fieldValue));
                                                                            %>
                                                                                    <%=Util.processForDisplay(fieldValue)%><br/><%
                                                                                        }
                                                                                                        }
                                                                                    %>
                                                                        </td>

                                                                        <%
                                                                            isOdd = Util.isOdd(++displayed);
                                                                        %>
                                                                    </tr><%
                                                                        }
                                                                    %>
                                                            </table>
                                                    <%
                                                        }
                                                    %>
                                            <%
                                            }
                                            %>
                                        <!-- end complex attributes -->

                                        <!-- Inference Rules -->
                                        <% if(mode.equals("edit")){ %>
                                        <h2>Rules
                                            <!-- Eionet styling -->
                                            <span class="inference_rules_help">
                                                <a class="helpButton" href="<%=request.getContextPath()%>/help.jsp?screen=dataset&amp;area=complex_attrs_link"></a>
                                            </span>
                                            <span class="inference_rules_help">
                                                  <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt="mandatory"/>
                                            </span>
                                            <span class="barfont_bordered">
                                                <a href="<%=request.getContextPath()%>/inference_rules/<%=delem_id%>">[Click to manage rules of this element]</a>
                                            </span>
                                        </h2>
                                        <% } %>

                                        <% if( mode.equals("view") ){ %>
                                            <% if( dataElementRules.size() > 0 ){ %>
                                            <h2>Rules</h2>
                                            <table class="datatable results" id="element-rules">
                                                <col style="width: 27%"/>
                                                <col style="width: 63%"/>
                                                <thead>
                                                    <tr>
                                                        <th>Inference Rule</th>
                                                        <th>Element</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <%
                                                        eionet.meta.dao.domain.DataElement target;
                                                        for(InferenceRule rule : dataElementRules){
                                                            target = rule.getTargetDElement();
                                                        %>
                                                            <tr>
                                                                <td><%=rule.getTypeName()%></td>
                                                                <td>
                                                                    <a href="<%=request.getContextPath()%>/dataelements/<%=target.getId()%>"><%=target.getIdentifier()%></a>
                                                                    <% if (target.isWorkingCopy()) { %>
                                                                        <span class="checkedout" title="<%=target.getWorkingUser()%>">*</span>
                                                                    <% } %>
                                                                </td>
                                                            </tr>
                                                        <%}
                                                    %>
                                                </tbody>
                                            </table>
                                            <% } %>
                                        <% } %>
                                        <!-- end Inference Rules -->

                                        <%
                                            // other versions
                                                if (mode.equals("view") && elmCommon && otherVersions != null
                                                        && otherVersions.size() > 0) {
                                        %>
                                            <h2>
                                                Other versions<a id="versions"></a>
                                            </h2>
                                            <table class="datatable results">
                                                <col style="width:25%"/>
                                                <col style="width:25%"/>
                                                <col style="width:25%"/>
                                                <col style="width:25%"/>
                                                <thead>
                                                    <tr>
                                                        <th>Element number</th>
                                                        <th>Status</th>
                                                        <th>Release date</th>
                                                        <th></th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                <%
                                                DataElement otherVer;
                                                for (int i = 0; i < otherVersions.size(); i++) {

                                                    otherVer = (DataElement) otherVersions.get(i);
                                                    String status = otherVer.getStatus();
                                                    String releaseDate = null;
                                                    String releaseDateHint = null;

                                                    if (status.equals("Released")){
                                                        releaseDate = otherVer.getDate();
                                                    }
                                                    if (releaseDate != null){
                                                        long timestamp = Long.parseLong(releaseDate);
                                                        releaseDate = eionet.util.Util.releasedDate(timestamp);
                                                        releaseDateHint = releaseDate + " " + eionet.util.Util.hoursMinutesSeconds(timestamp);
                                                    }
                                                    else{
                                                        releaseDate = "";
                                                        releaseDateHint = "";
                                                    }
                                                    String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";
                                                %>
                                                    <tr class="<%=zebraClass%>">
                                                        <td><%=otherVer.getID()%></td>
                                                        <td><%=status%></td>
                                                        <td><%=releaseDate%></td>
                                                        <td>
                                                            <%
                                                                if (searchEngine.skipByRegStatus(otherVer.getStatus())) {
                                                            %>
                                                                &nbsp;<%
                                                                    } else {
                                                                %>
                                                                [<a href="<%=request.getContextPath()%>/dataelements/<%=otherVer.getID()%>">view</a>]<%
                                                                    }
                                                                %>
                                                        </td>
                                                    </tr>
                                                    <%
                                                        }
                                                    %>
                                                </tbody>
                                            </table>
                                            <%
                                                }
                                            %>

                                <!-- end dotted -->

                        </div>

                    <!-- end main table body -->

                <!-- end main table -->
                <div style="display:none">
                    <input type="hidden" name="mode" value="<%=mode%>"/>
                    <input type="hidden" name="check_in" value="false"/>
                    <input type="hidden" name="switch_type" value="false"/>
                    <input type="hidden" name="copy_elem_id" value=""/>
                    <input type="hidden" name="changed" value="0"/>
                    <input type="hidden" name="saveclose" value="false"/>
                    <input type="hidden" name="remove_values" value="false"/>
                    <input type="hidden" name="datatype_conversion" value=""/>

                    <%
                        if (type != null) {
                    %>
                        <input type="hidden" name="type" value="<%=type%>"/><%
                            }

                                if (elmCommon) {
                                    String checkedoutCopyID = dataElement == null ? null
                                            : dataElement.getCheckedoutCopyID();
                                    if (checkedoutCopyID != null) {
                        %>
                            <input type="hidden" name="checkedout_copy_id" value="<%=checkedoutCopyID%>"/><%
                                }
                                    if (dataElement != null && dataElement.isWorkingCopy()){
                                        %>
                                        <input type="hidden" name="is_working_copy" value="true"/><%
                                    }
                                        if (dataElement != null) {
                                            String checkInNo = dataElement.getVersion();
                                            if (checkInNo.equals("1")) {
                            %>
                                <input type="hidden" name="upd_version" value="true"/><%
                                    }
                                            }
                                %>
                        <input type="hidden" name="common" value="true"/><%
                            } else {
                                    String dstNamespaceID = dataset.getNamespaceID();
                                    if (dstNamespaceID != null && dstNamespaceID.length() > 0) {
                        %>
                            <input type="hidden" name="dst_namespace_id" value="<%=dstNamespaceID%>"/><%
                                }
                                        String tblNamespaceID = dsTable.getNamespace();
                                        if (tblNamespaceID != null && tblNamespaceID.length() > 0) {
                            %>
                            <input type="hidden" name="tbl_namespace_id" value="<%=tblNamespaceID%>"/><%
                                }

                                    }
                                    // submitter url, might be used by POST handler who might want to send back to POST submitter
                                    String submitterUrl = Util
                                            .getServletPathWithQueryString(request);
                                    if (submitterUrl != null) {
                                        submitterUrl = Util.processForDisplay(submitterUrl);
                            %>
                        <input type="hidden" name="submitter_url" value="<%=submitterUrl%>"/><%
                            }
                        %>
                </div>
            </form>
            <%@ include file="bindVocabularyInc.jsp" %>

            <%
            if (request.getAttribute("includeSwitchTypeDialog") != null) {
                %>
                <jsp:include page="switchDataElemType.jsp" flush="true">
                    <jsp:param name="elemId" value="<%= (dataElement == null) ? null : dataElement.getID()%>"/>
                    <jsp:param name="curType" value="<%= (dataElement == null) ? null : dataElement.getType()%>"/>
                </jsp:include>
                <%
            }
            %>
            </div> <!-- workarea -->

            <%
                if (!popup) {
            %>
                </div> <!-- container -->
                <%@ include file="footer.jsp" %>
                <%
                    }
                %>
<script type="text/javascript">
    var contextPath = "<%=request.getContextPath()%>";
</script>
<script type="text/javascript" src="<%=request.getContextPath()%>/popbox.js"></script>
<script type="text/javascript">
// <![CDATA[
        var popclickpop = {
         'onclick' : function() { Pop(this,-50,'PopBoxImageLarge'); },
         'pbShowPopBar' : false,
         'pbShowPopImage' : false,
         'pbShowPopText' : false,
         'pbShowRevertImage': true,
         'pbShowPopCaption' : true
        };
        PopRegisterClass('poponmouseclick', popclickpop);
        popBoxShowPopBar = false;
// ]]>
</script>
</body>
</html>

<%
    // end the whole page try block
    } catch (Exception e) {
        if (response.isCommitted())
            e.printStackTrace(System.out);
        else {
            String msg = e.getMessage();
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bytesOut));
            String trace = bytesOut.toString(response
                    .getCharacterEncoding());
            String backLink = history.getBackUrl();
            request.setAttribute("DD_ERR_MSG", msg);
            request.setAttribute("DD_ERR_TRC", trace);
            request.setAttribute("DD_ERR_BACK_LINK", backLink);
            request.getRequestDispatcher("error.jsp").forward(request,
                    response);
            return;
        }
    } finally {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
        }
    }
%>

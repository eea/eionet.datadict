package eionet.meta;

import java.sql.*;
import javax.servlet.*;
import java.util.*;

import com.tee.util.*;
import com.tee.xmlserver.AppUserIF;

public class DDSearchEngine {
    
    private final static String SEQUENCE_TYPE = "seq";
    private final static String CHOICE_TYPE   = "chc";
    private final static String ELEMENT_TYPE  = "elm";
    
    public final static String ORDER_BY_M_ATTR_NAME  = "SHORT_NAME";
    public final static String ORDER_BY_M_ATTR_DISP_ORDER  = "DISP_ORDER";
    
    private Connection conn = null;
    private ServletContext ctx = null;
    private String sessionID = "";
    
    private AppUserIF user = null;
    
    public DDSearchEngine(Connection conn){
        this.conn = conn;
    }
    
    public DDSearchEngine(Connection conn, String sessionID){
        this(conn);
        this.sessionID = sessionID;
    }

    public DDSearchEngine(Connection conn, String sessionID, ServletContext ctx){
        this(conn, sessionID);
        this.ctx = ctx;
    }
    
    public void setUser(AppUserIF user){
        this.user = user;
    }
    
    public AppUserIF getUser(){
        return this.user;
    }

    /**
    *
    */
    public Vector getDataElements() throws SQLException {
        return getDataElements(null, null, null, null);
    }
    
    /**
    *
    */
    public Vector getDataElements(Vector params,
                                  String type,
                                  String namespace,
                                  String short_name) throws SQLException {
        return getDataElements(params, type, namespace, short_name, null);
    }
    
    /**
    * Get data elements by table id.
    * 5 inputs
    */
    public Vector getDataElements(Vector params,
                                  String type,
                                  String namespace,
                                  String short_name,
                                  String tableID) throws SQLException {
        return getDataElements(params, type, namespace, short_name, tableID, null);
    }
    
    /**
    * Get data elements by table id and dataset id
    * 6 inputs
    */
    public Vector getDataElements(Vector params,
                                  String type,
                                  String namespace,
                                  String short_name,
                                  String tableID,
                                  String datasetID) throws SQLException {

        return getDataElements(params, type, namespace, short_name,
                                    tableID, datasetID, false);
    }

    /**
    * Get data elements with control over working copies
    * 7 inputs
    */
    public Vector getDataElements(Vector params,
                                  String type,
                                  String namespace,
                                  String short_name,
                                  String tableID,
                                  String datasetID,
                                  boolean wrkCopies) throws SQLException {

        return getDataElements(params, type, namespace, short_name,
                                    tableID, datasetID, wrkCopies, null);
    }
    
    /**
    * Get data elements, control over working copies & params oper
    * 8 inputs
    */
    public Vector getDataElements(Vector params,
                                  String type,
                                  String namespace,
                                  String short_name,
                                  String tableID,
                                  String datasetID,
                                  boolean wrkCopies,
                                  String oper) throws SQLException {

        boolean bAttributes=false;
        PreparedStatement prepStmt=null;

        if (oper==null) oper=" like ";

        StringBuffer tables = new StringBuffer();
        tables.append("DATAELEM left outer join NAMESPACE on ");
        tables.append("DATAELEM.PARENT_NS=NAMESPACE.NAMESPACE_ID ");
        tables.append("left outer join CONTENT on DATAELEM.DATAELEM_ID=CONTENT.PARENT_ID");
        tables.append(" left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID");
        tables.append(" left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID");

        StringBuffer constraints = new StringBuffer();

        if (type!=null && type.length()!=0){
            constraints.append("DATAELEM.TYPE='");
            constraints.append(type);
            constraints.append("'");
        }

        if (namespace!=null && namespace.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DATAELEM.PARENT_NS=");
            constraints.append(namespace);
        }

        if (short_name!=null && short_name.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DATAELEM.SHORT_NAME");
            if (oper.trim().equalsIgnoreCase("match")) oper=" like "; //short_name is not fulltext index
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like"))
                constraints.append("'%" + short_name + "%'");
            else
                constraints.append("'" + short_name + "'");
        }

        if (tableID!=null && tableID.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            //constraints.append("DATAELEM.TABLE_ID=");
            constraints.append("TBL2ELEM.TABLE_ID=");
            constraints.append(tableID);
            bAttributes=true;
        }
        if (datasetID!=null && datasetID.length()!=0){
            
            // JH140803
            // there's now a many-to-many relation btw DS_TABLE & DATASET
            tables.append(" left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID");
            tables.append(" left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID");
            
            if (constraints.length()!=0) constraints.append(" and ");
            if (datasetID.equals("-1"))
                constraints.append("DATASET.DATASET_ID IS NULL");
            else{
                constraints.append("DATASET.DATASET_ID=");
                constraints.append(datasetID);
            }
        }

        for (int i=0; params!=null && i<params.size(); i++){ // if params==null, we ask for all

            String index = String.valueOf(i+1);
            DDSearchParameter param = (DDSearchParameter)params.get(i);

            String attrID  = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();

            tables.append(", ATTRIBUTE as ATTR" + index);
            
            if (constraints.length()!=0) constraints.append(" and ");
            
            constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + attrID);
            constraints.append(" and ");
            
            if (attrValues!=null && attrValues.size()!=0){
                constraints.append("(");
                for (int j=0; j<attrValues.size(); j++){
                    if (j>0) constraints.append(" or ");
                    if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH"))
                       constraints.append("match(ATTR" + index + ".VALUE) against(" + attrValues.get(j) +")");
                    else
                       constraints.append("ATTR" + index + ".VALUE" + valueOper + attrValues.get(j));
                }
                constraints.append(")");
            }
            
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DATAELEM.DATAELEM_ID");

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='E'");
        }

		// prune out the working copies, unless specifically requested
		// otherwise. this we can do, because (the business logic at edit
		// view will lead the user eventually to the working copy anyway)
		// if looking for table elements, we want to see both types of copies
		if (tableID==null){
			if (wrkCopies && (user==null || !user.isAuthentic()))
				wrkCopies = false;
			if (constraints.length()!=0)
				constraints.append(" and ");
			if (!wrkCopies)
				constraints.append("DATAELEM.WORKING_COPY='N'");
			else
				constraints.append("DATAELEM.WORKING_COPY='Y' and " +
								   "DATAELEM.WORKING_USER='" +
								   user.getUserName() + "'");
		}

                
        // build the monster query
        
        StringBuffer buf = new StringBuffer("select DATAELEM.*, NAMESPACE.*, DS_TABLE.VERSION, ");
        buf.append("CONTENT.CHILD_ID,CONTENT.CHILD_TYPE, TBL2ELEM.TABLE_ID, TBL2ELEM.POSITION from ");
        buf.append(tables.toString());
        
        if (constraints.length()!=0){
            buf.append(" where ");
            buf.append(constraints.toString());
        }
        
         if (tableID!=null && tableID.length()!=0){
             buf.append(" order by TBL2ELEM.POSITION, " +
                        "DATAELEM.SHORT_NAME, DATAELEM.PARENT_NS");
         }
         else{
             buf.append(" order by DATAELEM.SHORT_NAME,DATAELEM.PARENT_NS");
         }
        
        log(buf.toString());
        
        // finally, execute the monster query
        
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            if (bAttributes){
                String prepQry =
                "select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE " +
                "where " +
                "ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ATTRIBUTE.PARENT_TYPE='E' and " +
                "ATTRIBUTE.DATAELEM_ID=?";

                prepStmt = conn.prepareStatement(prepQry);
            }
            
            int prvID = -1;
            String prvTblVersion = null;
            DataElement dataElement = null;
            
            while (rs.next()){
                
                // JH300803 - for now we don't support aggregates
                if (rs.getString("DATAELEM.TYPE").equals("AGG"))
                    continue;
                
                int id = rs.getInt("DATAELEM.DATAELEM_ID");
                String tblVersion = rs.getString("DS_TABLE.VERSION");
                String tblID = rs.getString("TBL2ELEM.TABLE_ID");
                
                if (id == prvID){
                    // JH150303
                    // same data element, but probably in another table.
                    // see if the version of this table is greater than
                    // that of the previous one. if so, replace the table
                    // id of last added dataElement.
                    if (tblVersion!=null)
                        if (prvTblVersion==null ||
                                    tblVersion.compareTo(prvTblVersion) >= 0)
                            dataElement.setTableID(tblID);
                    
                    // in any ways, continue to the next row right away
                    prvTblVersion = tblVersion;
                    continue;
                }
                else // moving to new element, so reset prvTblVersion
                    prvTblVersion = "";
                
                prvID = id;
                
                String shortName = rs.getString("DATAELEM.SHORT_NAME");
                String nsID = rs.getString("NAMESPACE.NAMESPACE_ID");
                String version = rs.getString("DATAELEM.VERSION");
                
                // if version of this element is greater than version
                // of previous one, remove the latter from the result.
                // If it's less then skip this element.
                // This shall not be done if looking for working copies only.
                if (v.size()>0 && !wrkCopies){
                    DataElement elm = (DataElement)v.get(v.size()-1);
                    
                    String _nsID = elm.getNamespace()==null ?
                                  "!" : elm.getNamespace().getID();
                    
                    if (elm.getShortName().equals(shortName) &&
                        _nsID!=null && _nsID.equals(nsID)){
                        
                        if (version.compareTo(elm.getVersion())>=0)
                            v.remove(v.size()-1);
                        else
                            continue;
                    }
                }
                
                // start constructing the new element
                
                dataElement = new DataElement(String.valueOf(id),
                                            rs.getString("DATAELEM.SHORT_NAME"),
                                            rs.getString("DATAELEM.TYPE"),
                                            null);
                
                dataElement.setVersion(rs.getString("DATAELEM.VERSION"));
                dataElement.setStatus(rs.getString("DATAELEM.REG_STATUS"));
                dataElement.setWorkingCopy(rs.getString("WORKING_COPY"));
                dataElement.setWorkingUser(rs.getString("WORKING_USER"));
                
                String childID = rs.getString("CONTENT.CHILD_ID");
                if (childID != null){
                    String childType = rs.getString("CONTENT.CHILD_TYPE");
                    if (childType != null && childType.equalsIgnoreCase("seq"))
                        dataElement.setSequence(childID);
                    else if (childType != null && childType.equalsIgnoreCase("chc"))
                        dataElement.setChoice(childID);
                }
                
                Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                            rs.getString("NAMESPACE.SHORT_NAME"),
                                            rs.getString("NAMESPACE.FULL_NAME"),
                                            null, //rs.getString("NAMESPACE.URL"),
                                            rs.getString("NAMESPACE.DEFINITION"));
                //ns.setTable(rs.getString("NAMESPACE.TABLE_ID"));
                //ns.setDataset(rs.getString("NAMESPACE.DATASET_ID"));
                dataElement.setNamespace(ns);
                
                dataElement.setTopNs(rs.getString("DATAELEM.TOP_NS"));

                dataElement.setExtension(rs.getString("DATAELEM.EXTENDS"));
                dataElement.setTableID(tblID);
                dataElement.setPosition(rs.getString("TBL2ELEM.POSITION"));

                if (bAttributes){
                    prepStmt.setInt(1, id);

                    ResultSet _rs = prepStmt.executeQuery();

                    while (_rs.next()){
                        DElemAttribute attr = dataElement.getAttributeById(_rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"));
                        if (attr==null){
                          attr =
                             new DElemAttribute(_rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                        _rs.getString("M_ATTRIBUTE.NAME"),
                                        _rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                        DElemAttribute.TYPE_SIMPLE,
                                        _rs.getString("ATTRIBUTE.VALUE"),
                                        _rs.getString("M_ATTRIBUTE.DEFINITION"),
                                        _rs.getString("M_ATTRIBUTE.OBLIGATION"),
                                        _rs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                            dataElement.addAttribute(attr);
                        }
                        else{
                          attr.addValue(_rs.getString("ATTRIBUTE.VALUE"));
                        }
                    }
                }

                v.add(dataElement);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    
    public DataElement getDataElement(String delem_id) throws SQLException {
        
        if (delem_id == null || delem_id.length()==0){
            // if delem_id==null, it means only metadata on attributes is wanted,
            // so we create an identity-less DataElement, and all found attributes to it
            DataElement dataElement = new DataElement();
            Vector attributes = getDElemAttributes();
            for (int i=0; i<attributes.size(); i++)
                dataElement.addAttribute(attributes.get(i));
            return dataElement;
        }
        
        String qry =
        "select DATAELEM.*, NAMESPACE.*, DS_TABLE.VERSION, " +
        " CLASS2ELEM.DATACLASS_ID, CONTENT.CHILD_ID,CONTENT.CHILD_TYPE,TBL2ELEM.TABLE_ID " +
        "from DATAELEM " +
        "left outer join CLASS2ELEM on DATAELEM.DATAELEM_ID=CLASS2ELEM.DATAELEM_ID " +
        "left outer join NAMESPACE on DATAELEM.PARENT_NS=NAMESPACE.NAMESPACE_ID " +
        "left outer join CONTENT on DATAELEM.DATAELEM_ID=CONTENT.PARENT_ID " +
        "left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
        "left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
        "where DATAELEM.DATAELEM_ID=" + delem_id +
        " order by DS_TABLE.VERSION desc";
        
        log(qry);
        
        Statement stmt = null;
        ResultSet rs = null;
        DataElement dataElement = null;
        
        try{
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);
        
            if (rs.next()){
                
                dataElement = new DataElement(rs.getString("DATAELEM_ID"),
                                            rs.getString("DATAELEM.SHORT_NAME"),
                                            rs.getString("TYPE"),
                                            null);

                dataElement.setTableID(rs.getString("TBL2ELEM.TABLE_ID"));
                dataElement.setVersion(rs.getString("DATAELEM.VERSION"));
                dataElement.setStatus(rs.getString("DATAELEM.REG_STATUS"));
                dataElement.setWorkingCopy(rs.getString("WORKING_COPY"));
                dataElement.setWorkingUser(rs.getString("WORKING_USER"));
                
                dataElement.setTopNs(rs.getString("DATAELEM.TOP_NS"));
                
                String childID = rs.getString("CONTENT.CHILD_ID");
                if (childID != null){
                    String childType = rs.getString("CONTENT.CHILD_TYPE");
                    if (childType != null && childType.equalsIgnoreCase("seq"))
                        dataElement.setSequence(childID);
                    else if (childType != null && childType.equalsIgnoreCase("chc"))
                        dataElement.setChoice(childID);
                }
                
                Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                            rs.getString("NAMESPACE.SHORT_NAME"),
                                            rs.getString("NAMESPACE.FULL_NAME"),
                                            null, //rs.getString("NAMESPACE.URL"),
                                            rs.getString("NAMESPACE.DEFINITION"));
                //ns.setTable(rs.getString("NAMESPACE.TABLE_ID"));
                //ns.setDataset(rs.getString("NAMESPACE.DATASET_ID"));
                dataElement.setNamespace(ns);
                
                dataElement.setDataClass(rs.getString("CLASS2ELEM.DATACLASS_ID"));
                
                dataElement.setExtension(rs.getString("DATAELEM.EXTENDS"));
            }
            else return null;
            
            qry =
            "select M_ATTRIBUTE.*, NAMESPACE.*, ATTRIBUTE.VALUE from " +
            "M_ATTRIBUTE left outer join NAMESPACE on " +
            "M_ATTRIBUTE.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID " +
            "left outer join ATTRIBUTE on M_ATTRIBUTE.M_ATTRIBUTE_ID=ATTRIBUTE.M_ATTRIBUTE_ID " +
            "where ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=" + delem_id;
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);
            
            while (rs.next()){
                DElemAttribute attr = dataElement.getAttributeById(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"));
                if (attr==null){
                    attr =
                        new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                    rs.getString("M_ATTRIBUTE.NAME"),
                                    rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                    DElemAttribute.TYPE_SIMPLE,
                                    rs.getString("ATTRIBUTE.VALUE"),
                                    rs.getString("M_ATTRIBUTE.DEFINITION"),
                                    rs.getString("M_ATTRIBUTE.OBLIGATION"),
                                    rs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                     dataElement.addAttribute(attr);
                }
                else{
                    attr.addValue(rs.getString("ATTRIBUTE.VALUE"));
                }


                Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                            rs.getString("NAMESPACE.SHORT_NAME"),
                                            rs.getString("NAMESPACE.FULL_NAME"),
                                            null, //rs.getString("NAMESPACE.URL"),
                                            rs.getString("NAMESPACE.DEFINITION"));
                //ns.setTable(rs.getString("NAMESPACE.TABLE_ID"));
                //ns.setDataset(rs.getString("NAMESPACE.DATASET_ID"));
                attr.setNamespace(ns);
                
   //             dataElement.addAttribute(attr);
            }
        }
        finally {
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return dataElement;
    }

    public Vector getDElemAttributes() throws SQLException {
        return getDElemAttributes(null,null,null);
    }

    public Vector getDElemAttributes(String type) throws SQLException {
        return getDElemAttributes(null,type,null);
    }
    
    public Vector getDElemAttributes(String attr_id, String type) throws SQLException {
        return getDElemAttributes(attr_id, type, null);
    }
    
    public Vector getDElemAttributes(String attr_id, String type, String orderBy) throws SQLException {
        
        if (type==null) type = DElemAttribute.TYPE_SIMPLE;
        
        String qry="";
        if (type.equals(DElemAttribute.TYPE_SIMPLE)){
            qry = "select distinct M_ATTRIBUTE_ID as ID, M_ATTRIBUTE.* from M_ATTRIBUTE";
            if (attr_id != null)
                qry = qry + " where M_ATTRIBUTE_ID=" + attr_id;
        }
        else{
            qry = "select distinct M_COMPLEX_ATTR_ID as ID, M_COMPLEX_ATTR.* from M_COMPLEX_ATTR";
            if (attr_id != null)
                qry = qry + " where M_COMPLEX_ATTR_ID=" + attr_id;
        }
        
        if (orderBy == null) orderBy = ORDER_BY_M_ATTR_NAME;
        qry = qry + " order by " + orderBy;
        
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
            
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);
            
            while (rs.next()){
                DElemAttribute attr = new DElemAttribute(rs.getString("ID"),
                                                        rs.getString("NAME"),
                                                        rs.getString("SHORT_NAME"),
                                                        type,
                                                        null,
                                                        rs.getString("DEFINITION"),
                                                        rs.getString("OBLIGATION"));
                
                Namespace ns = getNamespace(rs.getString("NAMESPACE_ID"));
                if (ns!=null)
                attr.setNamespace(ns);
               
                if (type.equals(DElemAttribute.TYPE_SIMPLE)){
                    attr.setDisplayProps(rs.getString("DISP_TYPE"),
                                        rs.getInt("DISP_ORDER"),
                                        rs.getInt("DISP_WHEN"),
                                        rs.getString("DISP_WIDTH"),
                                        rs.getString("DISP_HEIGHT"),
                                        rs.getString("DISP_MULTIPLE"));
                }
                else{
                    attr.setDisplayProps(null,
                                        rs.getInt("DISP_ORDER"),
                                        rs.getInt("DISP_WHEN"), null, null, null);
                }

                v.add(attr);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return v;
    }
    
    /*public Vector getSubElements(String delem_id) throws SQLException {
        
        StringBuffer buf =
            new StringBuffer("select distinct NAMESPACE.*, RELATION.*, DATAELEM.* from DATAELEM left outer join RELATION on DATAELEM_ID=CHILD_ID left outer join NAMESPACE on DATAELEM.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");
        buf.append("where ");
        buf.append("RELATION.PARENT_ID=");
        buf.append(delem_id);
        buf.append(" order by RELATION.POSITION");
        
        log(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        Vector v = new Vector();
        
        
        while (rs.next()){
            DataElement dataElement = new DataElement(rs.getString("RELATION.CHILD_ID"),
                                                      rs.getString("DATAELEM.SHORT_NAME"), null);
            
            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DEFINITION"));                                         
            dataElement.setNamespace(ns);
            
            dataElement.setRelation(rs.getString("RELATION.PARENT_ID"),
                                    rs.getString("RELATION.POSITION"),
                                    rs.getString("RELATION.MIN_OCCURS"),
                                    rs.getString("RELATION.MAX_OCCURS"));
            v.add(dataElement);
        }
        
        return v;
    }*/
    
    public Vector getSequence(String id) throws SQLException {
        return getSubElements(SEQUENCE_TYPE, id);
    }
    
    public Vector getChoice(String id) throws SQLException {
        return getSubElements(CHOICE_TYPE, id);
    }
    
    public Vector getSubElements(String structType, String structID) throws SQLException {
        
        Vector v = new Vector();
        if (structID == null) return v;
        
        String tableName   = structType.equals(CHOICE_TYPE) ? "CHOICE" : "SEQUENCE";
        String orderClause =
            structType.equals(CHOICE_TYPE) ? " order by DATAELEM.SHORT_NAME" : " order by SEQUENCE.POSITION";
        
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct NAMESPACE.*, DATAELEM.*, ");
        buf.append(tableName);
        buf.append(".* from ");
        buf.append(tableName);
        buf.append(" left outer join DATAELEM on ");
        buf.append(tableName);
        buf.append(".CHILD_ID=DATAELEM_ID ");
        buf.append("left outer join NAMESPACE on DATAELEM.PARENT_NS=NAMESPACE.NAMESPACE_ID ");
        buf.append("where ");
        buf.append(tableName);
        buf.append(".");
        buf.append(tableName);
        buf.append("_ID=");
        buf.append(structID);
        
        // prune out the working copies
        // (the business logic at edit view will lead the user eventually
        // to the working copy anyway)
        buf.append(" and DATAELEM.WORKING_COPY='N'");
        
        // order the result set
        buf.append(orderClause);
        
        log(buf.toString());
        // DBG
        System.out.println(buf.toString());
        
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            
            while (rs.next()){
                
                String childType = rs.getString(tableName + ".CHILD_TYPE");
                if (childType == null) continue;
                
                if (childType.equalsIgnoreCase("elm")){
                    
                    String shortName = rs.getString("DATAELEM.SHORT_NAME");
                    String nsID = rs.getString("DATAELEM.PARENT_NS");
                    String version = rs.getString("DATAELEM.VERSION");
                    
                    if (shortName.equals("Chemical"))
                        System.out.println(buf.toString());
                    
                    // if version of this element is greater than version
                    // of previous one, remove the latter from the result
                    if (v.size()>0){
                        DataElement elm = (DataElement)v.get(v.size()-1);
                        String _nsID = elm.getNamespace()==null ?
                                    "!" : elm.getNamespace().getID();
                        if (elm.getShortName().equals(shortName) &&
                            _nsID!=null && _nsID.equals(nsID)){
                            
                            if (version.compareTo(elm.getVersion())>=0)
                                v.remove(v.size()-1);
                        }
                    }
                
                    DataElement dataElement = new DataElement(rs.getString(tableName + ".CHILD_ID"),
                                                            shortName,
                                                            rs.getString("DATAELEM.TYPE"));

                    dataElement.setVersion(version);
                    dataElement.setStatus(rs.getString("DATAELEM.REG_STATUS"));
                    dataElement.setWorkingCopy(rs.getString("WORKING_COPY"));
                    dataElement.setWorkingUser(rs.getString("WORKING_USER"));
                    
                    dataElement.setTopNs(rs.getString("DATAELEM.TOP_NS"));
                    
                    Namespace ns = new Namespace(nsID,
                                                rs.getString("NAMESPACE.SHORT_NAME"),
                                                rs.getString("NAMESPACE.FULL_NAME"),
                                                null, //rs.getString("NAMESPACE.URL"),
                                                rs.getString("NAMESPACE.DEFINITION"));
                    ns.setTable(rs.getString("NAMESPACE.TABLE_ID"));
                    ns.setDataset(rs.getString("NAMESPACE.DATASET_ID"));                             
                    dataElement.setNamespace(ns);
                        
                    if (structType.equals(SEQUENCE_TYPE))
                        dataElement.setInSequence(rs.getString(tableName + "." + tableName + "_ID"),
                                                rs.getString(tableName + ".POSITION"),
                                                rs.getString(tableName + ".MIN_OCCURS"),
                                                rs.getString(tableName + ".MAX_OCCURS"));
                                                    
                    v.add(dataElement);
                }
                else{
                    Hashtable hash = new Hashtable();
                    hash.put("child_type", childType);
                    hash.put("child_id", rs.getString(tableName + ".CHILD_ID"));
                    if (structType.equals(SEQUENCE_TYPE)){
                        hash.put("child_min_occ", rs.getString(tableName + ".MIN_OCCURS"));
                        hash.put("child_max_occ", rs.getString(tableName + ".MAX_OCCURS"));
                        hash.put("child_pos", rs.getString(tableName + ".POSITION"));
                    }
                    
                    v.add(hash);
                }
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    
    public Vector getSubValues(String parent_csi) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct CHILD_CSI from CSI_RELATION ");
        buf.append(" left outer join CS_ITEM on CHILD_CSI=CSI_ID ");
        buf.append("where rel_type='taxonomy' and PARENT_CSI=");
        buf.append(parent_csi);
        buf.append(" ORDER BY CS_ITEM.POSITION");
        
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            v = new Vector();

            while (rs.next()){
                FixedValue fxv = getFixedValue(rs.getString("CHILD_CSI"));
                v.add(fxv);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return v;
    }

    public boolean hasSubValues(String parent_csi) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select count(*) from CSI_RELATION ");
        buf.append("where PARENT_CSI=");
        buf.append(parent_csi);
        
        Statement stmt = null;
        ResultSet rs = null;
            
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            
            if (rs.next()){
                if (rs.getInt(1)>0){
                    return true;
                }
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return false;
    }

    public boolean isChild(String csiID) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select count(*) from CSI_RELATION ");
        buf.append("where CHILD_CSI=");
        buf.append(csiID);
        
        Statement stmt = null;
        ResultSet rs = null;
            
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            
            if (rs.next()){
                if (rs.getInt(1)>0){
                    return true;
                }
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return false;
    }
    
    public Vector getFixedValues(String delem_id) throws SQLException {
        return getFixedValues(delem_id, "elem", true);
    }

    public Vector getFixedValues(String delem_id, String parent_type) throws SQLException {
        return getFixedValues(delem_id, parent_type, true);
    }

    public Vector getFixedValues(String delem_id, boolean topLevel) throws SQLException {
        return getFixedValues(delem_id, "elem", topLevel);
    }
    
    public Vector getFixedValues(String delem_id, String parent_type, boolean topLevel) throws SQLException {
        StringBuffer buf = new StringBuffer();
        /*buf.append("select * from FIXED_VALUE ");
        buf.append("where DATAELEM_ID=");
        buf.append(delem_id);
        buf.append(" and PARENT_TYPE=" + Util.strLiteral(parent_type));*/
        
        buf.append("select CS_ITEM.*");
        if (topLevel){
            buf.append(", CSI_RELATION.CHILD_CSI");
            buf.append(", CSI_RELATION.REL_TYPE");
        }
        buf.append(" from CS_ITEM");
        if (topLevel)
            buf.append(" left outer join CSI_RELATION on (CS_ITEM.CSI_ID=CSI_RELATION.CHILD_CSI and CSI_RELATION.REL_TYPE='taxonomy')");
        buf.append(" where COMPONENT_ID=");
        buf.append(delem_id);
        buf.append(" and CSI_TYPE='fxv'");
        buf.append(" and COMPONENT_TYPE=" + Util.strLiteral(parent_type));

        //buf.append(" ORDER BY CSI_VALUE");
        buf.append(" ORDER BY POSITION, CSI_VALUE");

        log(buf.toString());

        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            while (rs.next()){
                /*
                Hashtable hash = new Hashtable();
                hash.put("value", rs.getString("FIXED_VALUE.VALUE"));

                String reprElemName = rs.getString("DATAELEM.SHORT_NAME");
                String reprNsName = rs.getString("NAMESPACE.SHORT_NAME");
                String reprElem = "";
                if (reprElemName!=null && reprNsName!=null)
                    reprElem = reprNsName + ":" + reprElemName;

                hash.put("repr_elem", reprElem);
                v.add(hash);
                */
                //FixedValue fxv = getFixedValue(rs.getString("FIXED_VALUE.FIXED_VALUE_ID"));
                if (topLevel && !Util.nullString(rs.getString("CSI_RELATION.CHILD_CSI")))
                    continue;

    /*            String rel_type=rs.getString("CSI_RELATION.REL_TYPE");
                if (rel_type!=null)
                    if (!rel_type.equals("taxonomy")) continue;
    */
                FixedValue fxv = getFixedValue(rs.getString("CSI_ID"));
                v.add(fxv);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    public Vector getAttrFields(String attr_id) throws SQLException{
        return getAttrFields(attr_id, null);
    }
    public Vector getAttrFields(String attr_id, String priority) throws SQLException{

        StringBuffer buf = new StringBuffer();
        buf.append("select * from M_COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_ID=");
        buf.append(attr_id);

        if (priority != null){
            buf.append(" and PRIORITY='");
            buf.append(priority);
            buf.append("'");
        }

        buf.append(" order by POSITION");

        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            while (rs.next()){

                Hashtable hash = new Hashtable();
                hash.put("id", rs.getString("M_COMPLEX_ATTR_FIELD_ID"));
                hash.put("name", rs.getString("NAME"));
                hash.put("definition", rs.getString("DEFINITION"));
                hash.put("position", rs.getString("POSITION"));
                hash.put("priority", rs.getString("PRIORITY"));

                v.add(hash);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    public Hashtable getAttrField(String field_id) throws SQLException{

        StringBuffer buf = new StringBuffer();
        buf.append("select * from M_COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(field_id);

        Statement stmt = null;
        ResultSet rs = null;
        Hashtable hash = new Hashtable();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            while (rs.next()){

                hash.put("id", rs.getString("M_COMPLEX_ATTR_FIELD_ID"));
                hash.put("name", rs.getString("NAME"));
                hash.put("definition", rs.getString("DEFINITION"));
                hash.put("position", rs.getString("POSITION"));
                hash.put("priority", rs.getString("PRIORITY"));
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return hash;
    }
    
    public Vector getComplexAttribute(String attr_id, String parent_id, String parent_type) throws SQLException {
        return getComplexAttributes(parent_id, parent_type, attr_id);
    }
    
    public Vector getComplexAttributes(String parent_id, String parent_type) throws SQLException {
        return getComplexAttributes(parent_id, parent_type, null);
    }
    public Vector getComplexAttributes(String parent_id, String parent_type, String attr_id) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select ");
        buf.append("M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID as ATTR_ID, ");
        buf.append("M_COMPLEX_ATTR.SHORT_NAME as ATTR_NAME, ");
        buf.append("NAMESPACE.SHORT_NAME as NS, ");
        buf.append("COMPLEX_ATTR_ROW.POSITION as ROW_POS, ");
        buf.append("COMPLEX_ATTR_ROW.ROW_ID as ROW_ID, ");
        buf.append("COMPLEX_ATTR_FIELD.M_COMPLEX_ATTR_FIELD_ID as FIELD_ID, ");
        buf.append("COMPLEX_ATTR_FIELD.VALUE as FIELD_VALUE ");
        buf.append("from ");
        buf.append("COMPLEX_ATTR_ROW, ");
        buf.append("M_COMPLEX_ATTR left outer join NAMESPACE ");
        buf.append("on M_COMPLEX_ATTR.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID, ");
        buf.append("COMPLEX_ATTR_FIELD ");
        buf.append("where ");
        buf.append("COMPLEX_ATTR_ROW.PARENT_ID=");
        buf.append(parent_id);
        buf.append(" and COMPLEX_ATTR_ROW.PARENT_TYPE='");
        buf.append(parent_type);
        buf.append("'");

        if (attr_id != null){
            buf.append(" and COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=");
            buf.append(attr_id);
        }

        buf.append(" and COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID and ");
        buf.append("COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID ");
        buf.append("order by ATTR_ID,ROW_POS");
        log(buf.toString());
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        DElemAttribute attr = null;
        Hashtable rowHash = null;
        
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            Hashtable attrs  = new Hashtable();
            Hashtable fields = new Hashtable();

            int prvRow = -1;
            while (rs.next()){
                
                String attrID = rs.getString("ATTR_ID");
                
                if (attrs.containsKey(attrID))
                    attr = (DElemAttribute)attrs.get(attrID);
                else{
                    if (attr != null){
                        attr.addRow(rowHash);
                        v.add(attr);
                        rowHash = null;
                        prvRow = -1;
                    }
                    
                    attr = new DElemAttribute(attrID,
                                            null,
                                            rs.getString("ATTR_NAME"),
                                            DElemAttribute.TYPE_COMPLEX,
                                            null);
                    Namespace ns = new Namespace(null, rs.getString("NS"), null, null, null);
                    attr.setNamespace(ns);
                    
                    attrs.put(attrID, attr);
                }
                
                int row = rs.getInt("ROW_POS");
                if (row != prvRow){
                    if (prvRow != -1)
                        attr.addRow(rowHash);
                    
                    rowHash = new Hashtable();
                    rowHash.put("rowid", rs.getString("ROW_ID"));
                    rowHash.put("position", rs.getString("ROW_POS"));
                }
                
                rowHash.put(rs.getString("FIELD_ID"), rs.getString("FIELD_VALUE"));
                
                prvRow = row;
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        if (attr != null){
            attr.addRow(rowHash);
            v.add(attr);
        }
        
        return v;
    }
    
    public Vector getComplexAttributeValues(String attr_id) throws SQLException {

        if (attr_id==null) return null;

        StringBuffer buf = new StringBuffer();
        buf.append("select ");
        buf.append("M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID as ATTR_ID, ");
        buf.append("M_COMPLEX_ATTR.SHORT_NAME as ATTR_NAME, ");
        buf.append("NAMESPACE.SHORT_NAME as NS, ");
        buf.append("COMPLEX_ATTR_ROW.POSITION as ROW_POS, ");
        buf.append("COMPLEX_ATTR_ROW.ROW_ID as ROW_ID, ");
        buf.append("COMPLEX_ATTR_FIELD.M_COMPLEX_ATTR_FIELD_ID as FIELD_ID, ");
        buf.append("COMPLEX_ATTR_FIELD.VALUE as FIELD_VALUE ");
        buf.append("from ");
        buf.append("COMPLEX_ATTR_ROW, ");
        buf.append("M_COMPLEX_ATTR left outer join NAMESPACE ");
        buf.append("on M_COMPLEX_ATTR.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID, ");
        buf.append("COMPLEX_ATTR_FIELD ");
        buf.append("where ");
        buf.append("COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID and ");
        buf.append("COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID ");

        if (attr_id != null){
            buf.append(" and COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=");
            buf.append(attr_id);
        }

        buf.append(" order by ROW_ID");
        log(buf.toString());
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        Hashtable rowHash = null;
        boolean hasVal=false;
        boolean ok=true;
        String row="";

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            String prvRow = "-1";
            while (ok){
                ok = rs.next();
                if (ok) row = rs.getString("ROW_ID");

                if (!row.equals(prvRow) || !ok){
                    if (!prvRow.equals("-1")){
                        for (int i=0; i<v.size();i++){
                            Hashtable h = (Hashtable)v.get(i);
                            if (h.equals(rowHash)){
                              hasVal=true;
                              break;
                            }
                        }
                        if (!hasVal)
                            v.add(rowHash);
                        hasVal=false;
                    }

                    if (!ok) break;
                    rowHash = new Hashtable();
                }
                rowHash.put(rs.getString("FIELD_ID"), rs.getString("FIELD_VALUE"));
                prvRow = row;
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    public Vector getSimpleAttributeValues(String attr_id) throws SQLException {

        if (attr_id==null) return null;

        StringBuffer buf = new StringBuffer();
        buf.append("select distinct value from ATTRIBUTE where M_ATTRIBUTE_ID=");
        buf.append(attr_id);
        buf.append(" order by VALUE");
        log(buf.toString());
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            while (rs.next()){

                String value = rs.getString("value");
                if (value==null) continue;
                if (!value.equals("")){
                    v.add(value);
                }
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }

    public Vector getNamespaces() throws SQLException {
        return getNamespaces(null);
    }
    
    public Namespace getNamespace(String id) throws SQLException {
        Vector v = getNamespaces(id);
        if (v != null && v.size()!=0) return (Namespace)v.get(0);
        return null;
    }
    
    public Vector getNamespaces(String id) throws SQLException {

        StringBuffer buf = new StringBuffer("select * from NAMESPACE");
        if (id != null && id.length()!=0){
            buf.append(" where NAMESPACE_ID='");
            buf.append(id);
            buf.append("'");
        }
        
        //buf.append(" order by TABLE_ID, DATASET_ID");
        buf.append(" order by SHORT_NAME");

        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
            
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            while (rs.next()){
                Namespace namespace=new Namespace(rs.getString("NAMESPACE_ID"),
                                                  rs.getString("SHORT_NAME"),
                                                  rs.getString("FULL_NAME"),
                                                  null, //rs.getString("URL"),
                                                  rs.getString("DEFINITION"));
                //namespace.setTable(rs.getString("NAMESPACE.TABLE_ID"));
                //namespace.setDataset(rs.getString("NAMESPACE.DATASET_ID"));
				namespace.setWorkingUser(rs.getString("WORKING_USER"));
                v.add(namespace);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }

    public String getContentDefinitionUrl(String id) throws SQLException {

        StringBuffer buf = new StringBuffer("select * from CONTENT_DEFINITION where DATAELEM_ID=");
        buf.append(id);
        
        Statement stmt = null;
        ResultSet rs = null;
            
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            
            if (rs.next()){
                return rs.getString("URL");
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return null;
    }

    public Dataset getDataset(String datasetID) throws SQLException {
        Vector v = getDatasets(datasetID, false);
        if (v==null || v.size()==0)
            return null;
        else
            return (Dataset)v.get(0);
    }

    public Vector getDatasets() throws SQLException {
        return getDatasets(null, false);
    }
    
    public Vector getDatasets(boolean wrkCopies) throws SQLException {
        return getDatasets(null, wrkCopies);
    }
    
    private Vector getDatasets(String datasetID, boolean wrkCopies)
                                                throws SQLException {

        StringBuffer buf  = new StringBuffer();
        buf.append("select distinct DATASET.* ");
        buf.append("from DATASET ");
        buf.append("where CORRESP_NS is not null ");
        if (datasetID!=null && datasetID.length()!=0){
            buf.append(" and DATASET.DATASET_ID=");
            buf.append(datasetID);
            //buf.append(" and ");
        }
        //buf.append("NAMESPACE.TABLE_ID is null");
        
        // prune out the working copies
        // (the business logic at edit view will lead the user eventually
        // to the working copy anyway)
        // But only in case if the ID is not exolicitly specified
        if (Util.nullString(datasetID)){
            if (wrkCopies && (user==null || !user.isAuthentic()))
                wrkCopies = false;
            if (buf.length()!=0)
                buf.append(" and ");
            if (!wrkCopies)
                buf.append("DATASET.WORKING_COPY='N'");
            else
                buf.append("DATASET.WORKING_COPY='Y' and " +
                                "DATASET.WORKING_USER='" +
                                user.getUserName() + "'");
        }
                               
        buf.append(" order by DATASET.SHORT_NAME, DATASET.VERSION desc");

        log(buf.toString());
        System.out.println(buf.toString());

        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            Dataset ds = null;
            
            while (rs.next()){
                
                String shortName = rs.getString("SHORT_NAME");
                
                // make sure we get the latest version of the dataset
                if (ds!=null && shortName.equals(ds.getShortName()))
                    continue;
                
                ds = new Dataset(rs.getString("DATASET_ID"),
                                        rs.getString("SHORT_NAME"),
                                        rs.getString("VERSION"));
                                        
                ds.setWorkingCopy(rs.getString("WORKING_COPY"));
                ds.setStatus(rs.getString("REG_STATUS"));
                ds.setVisual(rs.getString("VISUAL"));
                ds.setDetailedVisual(rs.getString("DETAILED_VISUAL"));
                ds.setNamespaceID(rs.getString("CORRESP_NS"));
                
                v.add(ds);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    
    public Vector getDatasets(Vector params, String short_name, String version) throws SQLException {
        return getDatasets(params, short_name, version, null);
    }
    
    public Vector getDatasets(Vector params, String short_name, String version, String oper) throws SQLException {
        return getDatasets(params, short_name, version, oper, false);
    }
    
    /**
    * get datasets by params, control oper & working copies
    */
    public Vector getDatasets(Vector params,
                              String short_name,
                              String version,
                              String oper,
                              boolean wrkCopies) throws SQLException {
        
        // first get the id of simple attribute "Name"
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select M_ATTRIBUTE_ID from " +
                        "M_ATTRIBUTE where SHORT_NAME='Name'");
        String nameID = rs.next() ? rs.getString(1) : null;
        
        // no get working on datasets
        
        if (oper==null) oper=" like ";

        StringBuffer tables = new StringBuffer();
        tables.append("DATASET");

        StringBuffer constraints = new StringBuffer();

        if (short_name!=null && short_name.length()!=0){
            constraints.append("DATASET.SHORT_NAME");
            if (oper.trim().equalsIgnoreCase("match")) oper=" like "; //short_name is not fulltext index
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like"))
                constraints.append("'%" + short_name + "%'");
            else
                constraints.append("'" + short_name + "'");
        }

        if (version!=null && version.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DATASET.VERSION='");
            constraints.append(version);
            constraints.append("'");
        }

        for (int i=0; params!=null && i<params.size(); i++){ // if params==null, we ask for all

            String index = String.valueOf(i+1);
            DDSearchParameter param = (DDSearchParameter)params.get(i);

            String attrID  = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();

            tables.append(", ATTRIBUTE as ATTR" + index);

            if (constraints.length()!=0) constraints.append(" and ");

            constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + attrID);
            constraints.append(" and ");

            if (attrValues!=null && attrValues.size()!=0){
                constraints.append("(");
                for (int j=0; j<attrValues.size(); j++){
                    if (j>0) constraints.append(" or ");
                    if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH"))
                       constraints.append("match(ATTR" + index + ".VALUE) against(" + attrValues.get(j) +")");
                    else
                       constraints.append("ATTR" + index + ".VALUE" + valueOper + attrValues.get(j));
                }
                constraints.append(")");
            }

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DATASET.DATASET_ID");

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='DS'");
        }
        
        // prune out the working copies
        // (the business logic at edit view will lead the user eventually
        // to the working copy anyway)
        if (wrkCopies && (user==null || !user.isAuthentic()))
            wrkCopies = false;
        if (constraints.length()!=0)
            constraints.append(" and ");
        if (!wrkCopies)
            constraints.append("DATASET.WORKING_COPY='N'");
        else
            constraints.append("DATASET.WORKING_COPY='Y' and " +
                               "DATASET.WORKING_USER='" +
                               user.getUserName() + "'");

        StringBuffer buf = new StringBuffer("select DATASET.* from ");
        buf.append(tables.toString());
        if (constraints.length()!=0){
            buf.append(" where ");
            buf.append(constraints.toString());
        }

        buf.append(" order by DATASET.SHORT_NAME, DATASET.VERSION desc");

        log(buf.toString());

        stmt = null;
        rs = null;
        Vector v = new Vector();
        
        // preprare the statement for getting attributes
        PreparedStatement ps = null;
        if (nameID!=null){            
            String s = "select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID=" +
                       nameID + " and PARENT_TYPE='DS' and " +
                       "DATAELEM_ID=?";
            ps = conn.prepareStatement(s);
        }
        
        // execute the query for datasets            
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            Dataset ds = null;
            while (rs.next()){

                String shortName = rs.getString("DATASET.SHORT_NAME");
                
                // make sure we get the latest version of the dataset
                if (ds!=null && shortName.equals(ds.getShortName()))
                    continue;
                
                ds = new Dataset(rs.getString("DATASET_ID"),
                                        rs.getString("SHORT_NAME"),
                                        rs.getString("VERSION"));
                
                ds.setWorkingCopy(rs.getString("WORKING_COPY"));
                ds.setStatus(rs.getString("REG_STATUS"));
                ds.setVisual(rs.getString("VISUAL"));
                ds.setDetailedVisual(rs.getString("DETAILED_VISUAL"));
                ds.setNamespaceID(rs.getString("CORRESP_NS"));
                
                // set the name if nameID was previously successfully found
                if (nameID!=null){
                    ps.setInt(1, rs.getInt("DATASET.DATASET_ID"));
                    ResultSet rs2 = ps.executeQuery();
                    if (rs2.next()) ds.setName(rs2.getString(1));
                }
                
                v.add(ds);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }
    
    public Vector getDatasetTables(String dsID) throws SQLException {
        
        StringBuffer buf  = new StringBuffer();
        buf.append("select distinct DS_TABLE.* ");//, DATASET.* ");
        buf.append("from DS_TABLE ");
        
        // JH140803
        // there's now a many-to-many relation btw DS_TABLE & DATASET
        buf.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
        //buf.append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ");
            
        //buf.append("where DS_TABLE.CORRESP_NS is not null and DATASET.DATASET_ID=");
        buf.append("where DS_TABLE.CORRESP_NS is not null and DST2TBL.DATASET_ID=");
        buf.append(dsID);
        buf.append(" order by DS_TABLE.SHORT_NAME,DS_TABLE.VERSION desc");
        
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            DsTable dsTable = null;
            while (rs.next()){
                
                // make sure you get the latest version
                String shortName = rs.getString("DS_TABLE.SHORT_NAME");
                if (dsTable!=null){
                    if (shortName.equals(dsTable.getShortName()))
                        continue;
                }
                
                dsTable = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
                                            dsID,//rs.getString("DATASET.DATASET_ID"),
                                            shortName);
                
                dsTable.setName(rs.getString("DS_TABLE.NAME"));
                dsTable.setWorkingCopy(rs.getString("DS_TABLE.WORKING_COPY"));
				dsTable.setWorkingUser(rs.getString("DS_TABLE.WORKING_USER"));
                dsTable.setVersion(rs.getString("DS_TABLE.VERSION"));
                dsTable.setStatus(rs.getString("DS_TABLE.REG_STATUS"));
                dsTable.setDefinition(rs.getString("DS_TABLE.DEFINITION"));
                dsTable.setType(rs.getString("DS_TABLE.TYPE"));
                dsTable.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                dsTable.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
                
                v.add(dsTable);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
        
        return v;
    }
    
    /**
    *
    */
    public Vector getDatasetTables(Vector params, String short_name, String full_name, String definition) throws SQLException {
        return getDatasetTables(params, short_name, full_name, definition, null);
    }
    
    public Vector getDatasetTables(Vector params, String short_name, String full_name, String definition, String oper) throws SQLException {
        return getDatasetTables(params, short_name, full_name, definition, oper, false);
    }
    
    /**
    *
    */
    public Vector getDatasetTables(Vector params,
                                   String short_name,
                                   String full_name,
                                   String definition,
                                   String oper,
                                   boolean wrkCopies) throws SQLException {
        
        // first get the id of simple attribute "Name"
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select M_ATTRIBUTE_ID from " +
                        "M_ATTRIBUTE where SHORT_NAME='Name'");
        String nameID = rs.next() ? rs.getString(1) : null;
        
        
        // now let's work with tables
        
        if (oper==null) oper=" like ";

        StringBuffer tables = new StringBuffer();
        tables.append("DS_TABLE ");
        
        // JH140803
        // there's now a many-to-many relation btw DS_TABLE & DATASET
        tables.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
        tables.append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ");

        StringBuffer constraints = new StringBuffer();

        if (short_name!=null && short_name.length()!=0){
            constraints.append("DS_TABLE.SHORT_NAME");
            if (oper.trim().equalsIgnoreCase("match")) oper=" like "; //short_name is not fulltext index
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like"))
                constraints.append("'%" + short_name + "%'");
            else
                constraints.append("'" + short_name + "'");
        }

        if (full_name!=null && full_name.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DS_TABLE.NAME like '%");
            constraints.append(full_name);
            constraints.append("%'");
        }
        if (definition!=null && definition.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DS_TABLE.DEFINITION like '%");
            constraints.append(definition);
            constraints.append("%'");
        }
        
        // prune out working copies
        if (wrkCopies && (user==null || !user.isAuthentic()))
            wrkCopies = false;
        if (constraints.length()!=0)
            constraints.append(" and ");
        if (!wrkCopies)
            constraints.append("DS_TABLE.WORKING_COPY='N'");
        else
            constraints.append("DS_TABLE.WORKING_COPY='Y' and " +
                               "DS_TABLE.WORKING_USER='" +
                               user.getUserName() + "'");

        for (int i=0; params!=null && i<params.size(); i++){ // if params==null, we ask for all

            String index = String.valueOf(i+1);
            DDSearchParameter param = (DDSearchParameter)params.get(i);

            String attrID  = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();
            String attrName = param.getAttrShortName();

            tables.append(", ATTRIBUTE as ATTR" + index);

            if (constraints.length()!=0) constraints.append(" and ");

            constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + attrID);
            constraints.append(" and ");

            if (attrValues!=null && attrValues.size()!=0){
                constraints.append("(");
                for (int j=0; j<attrValues.size(); j++){
                    if (j>0) constraints.append(" or ");
                    if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH"))
                       constraints.append("match(ATTR" + index + ".VALUE) against(" + attrValues.get(j) +")");
                    else
                       constraints.append("ATTR" + index + ".VALUE" + valueOper + attrValues.get(j));
                }
                constraints.append(")");
            }

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DS_TABLE.TABLE_ID");
            
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='T'");
        }

        StringBuffer buf =
        new StringBuffer("select DS_TABLE.*, DATASET.* from ");
        buf.append(tables.toString());
        if (constraints.length()!=0){
            buf.append(" where ");
            buf.append(constraints.toString());
        }

        buf.append(" order by DS_TABLE.SHORT_NAME, " +
                   "DATASET.SHORT_NAME, DS_TABLE.VERSION desc, DATASET.VERSION desc");

        log(buf.toString());
        
        stmt = null;
        rs = null;
        Vector v = new Vector();
        
        // preprare the statement for getting attributes
        PreparedStatement ps = null;
        if (nameID!=null){            
            String s = "select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID=" +
                       nameID + " and PARENT_TYPE='T' and " +
                       "DATAELEM_ID=?";
            ps = conn.prepareStatement(s);
        }

        // execute the query for tables
        
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            String prvTblName = null;
            String prvDstName = null;
            
            while (rs.next()){

                String tblName = rs.getString("DS_TABLE.SHORT_NAME");
                String dstName = rs.getString("DATASET.SHORT_NAME");
                
                // make sure you get the latest version of each tbl/dst
                if (prvTblName!=null){
                    if (tblName.equals(prvTblName)){
                        if (prvDstName!=null && prvDstName.equals(dstName))
                            continue;
                        else if (prvDstName==null && dstName==null)
                            continue;
                    }
                }
                
                prvTblName = tblName;
                prvDstName = dstName;
                
                DsTable tbl = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
                                          rs.getString("DATASET.DATASET_ID"),
                                          rs.getString("DS_TABLE.SHORT_NAME"));

                tbl.setDefinition(rs.getString("DS_TABLE.DEFINITION"));
                tbl.setName(rs.getString("DS_TABLE.NAME"));         
                tbl.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                tbl.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
                tbl.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
                
                // set the name if nameID was previously successfully found
                if (nameID!=null){
                    ps.setInt(1, rs.getInt("DS_TABLE.TABLE_ID"));
                    ResultSet rs2 = ps.executeQuery();
                    if (rs2.next()) tbl.setName(rs2.getString(1));
                }
                
                v.add(tbl);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }

    public DsTable getDatasetTable(String tableID) throws SQLException {
        
        StringBuffer buf  = new StringBuffer();
        buf.append("select distinct DS_TABLE.*, DATASET.* ");
        buf.append("from DS_TABLE ");
        
        // JH140803
        // there's now a many-to-many relation btw DS_TABLE & DATASET
        buf.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
        buf.append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ");
        
        buf.append("where DS_TABLE.CORRESP_NS is not null and DS_TABLE.TABLE_ID=");
        buf.append(tableID);
        
        buf.append(" order by DATASET.VERSION desc");
        
        System.out.println(buf.toString());
        
        Statement stmt = null;
        ResultSet rs = null;
        DsTable dsTable = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            if (rs.next()){

                dsTable = new DsTable(rs.getString("TABLE_ID"),
                                            //rs.getString("DATASET_ID"),
                                            rs.getString("DATASET.DATASET_ID"),
                                            rs.getString("SHORT_NAME"));
                
                dsTable.setName(rs.getString("NAME"));
                dsTable.setWorkingCopy(rs.getString("DS_TABLE.WORKING_COPY"));
                dsTable.setVersion(rs.getString("DS_TABLE.VERSION"));
                dsTable.setStatus(rs.getString("DS_TABLE.REG_STATUS"));
                dsTable.setDefinition(rs.getString("DEFINITION"));
                dsTable.setType(rs.getString("TYPE"));
                
                dsTable.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                dsTable.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
                
                dsTable.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return dsTable;
    }

    public Vector getAttributes(String parentID, String parentType, String attrType) throws SQLException {
        if (attrType.equals(DElemAttribute.TYPE_SIMPLE))
            return getSimpleAttributes(parentID, parentType);
        else
            return getComplexAttributes(parentID, parentType);
    }

    public Vector getSimpleAttributes(String parentID, String parentType) throws SQLException {

        String qry =
        // JH - 120203, ATTRIBUTE.FIXED_VALUE_ID is no more present in the model
        //"select M_ATTRIBUTE.*, NAMESPACE.*, ATTRIBUTE.VALUE, ATTRIBUTE.FIXED_VALUE_ID from " +
        "select M_ATTRIBUTE.*, NAMESPACE.*, ATTRIBUTE.VALUE from " +
        "M_ATTRIBUTE left outer join NAMESPACE on " +
        "M_ATTRIBUTE.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID " +
        "left outer join ATTRIBUTE on M_ATTRIBUTE.M_ATTRIBUTE_ID=ATTRIBUTE.M_ATTRIBUTE_ID " +
        "where ATTRIBUTE.DATAELEM_ID='" + parentID + "' and ATTRIBUTE.PARENT_TYPE='" + parentType + "'";

        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);

            while (rs.next()){
                DElemAttribute attr = getAttributeById(v, rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"));
                if (attr==null){
                    attr =
                        new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                    rs.getString("M_ATTRIBUTE.NAME"),
                                    rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                    DElemAttribute.TYPE_SIMPLE,
                                    rs.getString("ATTRIBUTE.VALUE"),
                                    rs.getString("M_ATTRIBUTE.DEFINITION"),
                                    rs.getString("M_ATTRIBUTE.OBLIGATION"),
                                    rs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));

                    Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                            rs.getString("NAMESPACE.SHORT_NAME"),
                                            rs.getString("NAMESPACE.FULL_NAME"),
                                            null, //rs.getString("NAMESPACE.URL"),
                                            rs.getString("NAMESPACE.DEFINITION"));
                    //ns.setTable(rs.getString("NAMESPACE.TABLE_ID"));
                    //ns.setDataset(rs.getString("NAMESPACE.DATASET_ID"));
                    attr.setNamespace(ns);
                    // JH - 120203, ATTRIBUTE.FIXED_VALUE_ID is no more present in the model
                    //attr.setFixedValueID("ATTRIBUTE.FIXED_VALUE_ID");
                    v.add(attr);
                }
                else{
                    attr.addValue(rs.getString("ATTRIBUTE.VALUE"));
                }

            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;

    }

    public String getDataElementID(String ns_id, String short_name) throws SQLException {

        if(ns_id == null || ns_id.length()==0) return null;
        if(short_name == null || short_name.length()==0) return null;

        StringBuffer buf = new StringBuffer("select DATAELEM.DATAELEM_ID from DATAELEM where DATAELEM.SHORT_NAME='");
        buf.append(short_name);
        buf.append("' AND DATAELEM.PARENT_NS=");
        buf.append(ns_id);

        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            if (rs.next()){
                return rs.getString("DATAELEM_ID");
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return null;
    }

	public FixedValue getFixedValue(String fxv_id) throws SQLException {

        if (fxv_id == null || fxv_id.length()==0){
            FixedValue fxv = new FixedValue();
            Vector attributes = getDElemAttributes();
            for (int i=0; i<attributes.size(); i++)
                fxv.addAttribute(attributes.get(i));
            return fxv;
        }

        /*String qry =
        "select FIXED_VALUE.* from FIXED_VALUE " +
        "where FIXED_VALUE.FIXED_VALUE_ID=" + fxv_id;*/

        String qry =
        "select * from CS_ITEM " +
        "where CSI_ID=" + fxv_id;

        Statement stmt = null;
        ResultSet rs = null;
        FixedValue fxv = null;

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);

            if (rs.next()){
                /*fxv = new FixedValue(rs.getString("FIXED_VALUE.FIXED_VALUE_ID"),
                                            rs.getString("FIXED_VALUE.DATAELEM_ID"),
                                            rs.getString("FIXED_VALUE.VALUE"));*/

                fxv = new FixedValue(rs.getString("CSI_ID"),
                                            rs.getString("COMPONENT_ID"),
                                            rs.getString("CSI_VALUE"),
                                            rs.getString("POSITION"));

                String isDefault = rs.getString("IS_DEFAULT");
                if (!Util.nullString(isDefault) && isDefault.equalsIgnoreCase("Y"))
                    fxv.setDefault();

                fxv.setCsID(rs.getString("CS_ID"));
            }
            else return null;

            qry =
            "select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE " +
            "where " +
            //"ATTRIBUTE.PARENT_TYPE='FV' and ATTRIBUTE.DATAELEM_ID=" + fxv_id + " and " +
            "ATTRIBUTE.PARENT_TYPE='CSI' and ATTRIBUTE.DATAELEM_ID=" + fxv_id + " and " +
            "ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID";


            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);

            while (rs.next()){

                DElemAttribute attr =
                    new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                    rs.getString("M_ATTRIBUTE.NAME"),
                                    rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                    DElemAttribute.TYPE_SIMPLE,
                                    rs.getString("ATTRIBUTE.VALUE"),
                                    rs.getString("M_ATTRIBUTE.DEFINITION"),
                                    rs.getString("M_ATTRIBUTE.OBLIGATION"));
                fxv.addAttribute(attr);
            }

            qry = "select * from CS_ITEM" +
            " RIGHT OUTER JOIN CSI_RELATION ON CS_ITEM.CSI_ID=CSI_RELATION.CHILD_CSI" +
            " where CS_ITEM.CSI_TYPE='fxv' and CSI_RELATION.PARENT_CSI=" + fxv_id;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);

            while (rs.next()){

                CsiItem csiI = new CsiItem(rs.getString("CS_ITEM.CSI_ID"),
                                        rs.getString("CS_ITEM.CSI_VALUE"),
                                        rs.getString("CS_ITEM.COMPONENT_ID"),
                                        rs.getString("CS_ITEM.COMPONENT_TYPE"));
                csiI.setRelDescription(rs.getString("CSI_RELATION.REL_DESCRIPTION"));
                fxv.addItem(csiI);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return fxv;
    }
    public Vector getRelatedElements(String delem_id, String parent_type) throws SQLException {
        return getRelatedElements(delem_id, parent_type, null, null);
    }
    public Vector getRelatedElements(String delem_id, String parent_type, String childCsi) throws SQLException {
        return getRelatedElements(delem_id, parent_type, childCsi, null);
    }
    public Vector getRelatedElements(String delem_id, String parent_type, String childCsi, String elemType) throws SQLException {

        StringBuffer buf = new StringBuffer();

        buf.append("select CHILDITEM.*");
        buf.append(", CSI_RELATION.*, DATAELEM.SHORT_NAME");
        buf.append(" from CS_ITEM AS PARENTITEM");
        buf.append(" left outer join CSI_RELATION on PARENTITEM.CSI_ID=CSI_RELATION.PARENT_CSI");
        buf.append(" left outer join CS_ITEM AS CHILDITEM on CSI_RELATION.CHILD_CSI=CHILDITEM.CSI_ID");
        buf.append(" left outer join DATAELEM on CHILDITEM.COMPONENT_ID=DATAELEM.DATAELEM_ID");
        buf.append(" where PARENTITEM.COMPONENT_ID=");
        buf.append(delem_id);
        buf.append(" and PARENTITEM.COMPONENT_TYPE=" + Util.strLiteral(parent_type));
        buf.append(" and PARENTITEM.CSI_TYPE='elem'");
        if (childCsi!=null){
            buf.append(" and CHILDITEM.CSI_ID=");
            buf.append(childCsi);
        }
        if (elemType!=null){
            buf.append(" and DATAELEM.TYPE=");
            buf.append(Util.strLiteral(elemType));
        }
        buf.append(" ORDER BY CHILDITEM.POSITION, CSI_VALUE");

        log(buf.toString());
        
        Statement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
            
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            while (rs.next()){

                CsiItem csiI = new CsiItem(rs.getString("CHILDITEM.CSI_ID"),
                                        rs.getString("DATAELEM.SHORT_NAME"),
                                        rs.getString("CHILDITEM.COMPONENT_ID"),
                                        rs.getString("CHILDITEM.COMPONENT_TYPE"));
                csiI.setRelDescription(rs.getString("CSI_RELATION.REL_DESCRIPTION"));
                v.add(csiI);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }

        return v;
    }

    public Vector getAllFixedValues(String delem_id, String parent_type) throws SQLException {
        Vector v = new Vector();
        getAllFixedValues(delem_id, parent_type, null, 0, v);
        return v;
    }

    private void getAllFixedValues(String delem_id, String parent_type, String parent_id, int level, Vector v) throws SQLException {

        StringBuffer buf = new StringBuffer();

        buf.append("select CS_ITEM.*");
        buf.append(", CSI_RELATION.PARENT_CSI");
        buf.append(", CSI_RELATION.REL_TYPE");
        buf.append(" from CS_ITEM");
        buf.append(" left outer join CSI_RELATION on (CS_ITEM.CSI_ID=CSI_RELATION.CHILD_CSI and CSI_RELATION.REL_TYPE='taxonomy')");
        buf.append(" where COMPONENT_ID=");
        buf.append(delem_id);
        buf.append(" and CSI_TYPE='fxv'");
        buf.append(" and COMPONENT_TYPE=" + Util.strLiteral(parent_type));
        if(level>0 && parent_id != null){
          buf.append(" and CSI_RELATION.PARENT_CSI=");
          buf.append(parent_id);
        }

        //buf.append(" ORDER BY CSI_VALUE");
        buf.append(" ORDER BY POSITION, CSI_VALUE");

        log(buf.toString());

        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());

            level++;

            while (rs.next()){

                //String rel_type=rs.getString("CSI_RELATION.REL_TYPE");
                //if (rel_type == null) rel_type="";
                if (level==1 && !Util.nullString(rs.getString("CSI_RELATION.PARENT_CSI")))
                    continue;

                //if (level>1 && rel_type.equals("abstract")) continue;

                String id =rs.getString("CSI_ID");
                FixedValue fxv = getFixedValue(id);
                fxv.setLevel(level);
                v.add(fxv);
                getAllFixedValues(delem_id, "elem", id, level, v);
            }
        }
        finally{
            try{
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException sqle) {}
        }
    }
    private DElemAttribute getAttributeById(Vector v, String id){

        if (v == null) return null;
        if (id == null || id.length() == 0) return null;

        for (int i=0; i<v.size(); i++){
            DElemAttribute attribute = (DElemAttribute)v.get(i);
            if (attribute.getID().equalsIgnoreCase(id))
                return attribute;
        }

        return null;
    }
    
    /**
     * Get the last insert ID from database.
     */
    public String getLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        String id = null;
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);        
        rs.clearWarnings();
        if (rs.next())
            id = rs.getString(1);
            
        stmt.close();
        return id;
    }
    
    /**
    *
    */
    public String getMaxVersion(String tblName, String constraint)
        throws SQLException {
        
        String q =
        "select max(VERSION) from " + tblName + " where " + constraint;
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next())
            return rs.getString(1);
        
        return null;
    }
    
    /**
    *
    */
    public Vector getElmHistory(String elmID) throws SQLException {
        if (Util.nullString(elmID))
            return null;
        
        String q =
        "select PARENT_NS, SHORT_NAME, VERSION from DATAELEM " +
        "where DATAELEM_ID=" + elmID;
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        
        String shortName = null;
        String nsID = null;
        String version = null;
        
        if (rs.next()){
            shortName = rs.getString("SHORT_NAME");
            nsID = rs.getString("PARENT_NS");
            version = rs.getString("VERSION");
        }
        
        if (shortName==null || nsID==null)
            return null;
        
        q = "select VERSION, USER, DATE, DATAELEM_ID from DATAELEM " +
            "where PARENT_NS=" + nsID + " and " +
            "SHORT_NAME='" + shortName + "' and VERSION<" + version +
            " order by VERSION desc";
        
        rs = stmt.executeQuery(q);
        
        Vector v = new Vector();
        while (rs.next()){
            
            String user = rs.getString("USER");
            if (user==null) user = "";
            
            String date = rs.getString("DATE");
            if (date==null || date.equals("0"))
                date = "";
            else
                date = eionet.util.Util.historyDate(Long.parseLong(date));
            
            Hashtable hash = new Hashtable();
            hash.put("id", rs.getString("DATAELEM_ID"));
            hash.put("version", rs.getString("VERSION"));
            hash.put("date", date);
            hash.put("user", user);
            
            v.add(hash);
        }
        
        return v;
    }
    
    /**
    *
    */
    public Vector getTblHistory(String tblName, String dstName, String version)
                                                        throws SQLException {
        
        String q =
        "select distinct DS_TABLE.* from DS_TABLE " +
        "left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
        "left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
        "where " +
        "DS_TABLE.SHORT_NAME='" + tblName + "' and " +
        "DATASET.SHORT_NAME='" + dstName + "' and " +
        "DS_TABLE.VERSION<" + version +
        " order by VERSION desc";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        
        Vector v = new Vector();
        while (rs.next()){
            
            String user = rs.getString("USER");
            if (user==null) user = "";
            
            String date = rs.getString("DATE");
            if (date==null || date.equals("0"))
                date = "";
            else
                date = eionet.util.Util.historyDate(Long.parseLong(date));
            
            Hashtable hash = new Hashtable();
            hash.put("id", rs.getString("TABLE_ID"));
            hash.put("version", rs.getString("VERSION"));
            hash.put("date", date);
            hash.put("user", user);
            
            v.add(hash);
        }
        
        return v;
    }
    
    /**
    *
    */
    public Vector getDstHistory(String shortName, String version)
                                                throws SQLException {
        
        String q =
        "select * from DATASET " +
        "where " +
        "SHORT_NAME='" + shortName + "' and " +
        "VERSION<" + version +
        " order by VERSION desc";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        
        Vector v = new Vector();
        while (rs.next()){
            
            String user = rs.getString("USER");
            if (user==null) user = "";
            
            String date = rs.getString("DATE");
            if (date==null || date.equals("0"))
                date = "";
            else
                date = eionet.util.Util.historyDate(Long.parseLong(date));
            
            Hashtable hash = new Hashtable();
            hash.put("id", rs.getString("DATASET_ID"));
            hash.put("version", rs.getString("VERSION"));
            hash.put("date", date);
            hash.put("user", user);
            
            v.add(hash);
        }
        
        return v;
    }
    
    /**
    *
    */
    public boolean isWorkingCopy(String id, String type) throws Exception{
        
        if (type==null)
            throw new Exception("Type not specified!");
        
        String tblName = "";
        if (type.equals("elm"))
            tblName = "DATAELEM";
        else if (type.equals("tbl"))
            tblName = "DS_TABLE";
        else if (type.equals("dst"))
            tblName = "DATASET";
        else
            throw new Exception("Unknown type!");
        
        String idField = type.equals("tbl") ? "TABLE_ID" : tblName+"_ID";
        
        String q = "select WORKING_COPY from " + tblName +
                   " where " + idField + "=" + id;
        
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next()){
            if (rs.getString(1).equals("Y"))
                return true;
        }
        else throw new Exception("Could not find such an object!");
        
        return false;
    }
    
    /**
    *
    */
    public void log(String msg){
        if (ctx != null){
            ctx.log(msg);
        }
    }

    public static void main(String[] args){

        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            DataElement elm = searchEngine.getDataElement("11498");
            String tblID = elm.getTableID();
            DsTable tbl = searchEngine.getDatasetTable(tblID);
            
			AppUserIF testUser = new TestUser();
			testUser.authenticate("jaanus", "jaanus");
            VersionManager verMan = new VersionManager(conn, testUser);
            String latestTblID =   verMan.getLatestTblID(tbl);
            System.out.println(tblID + ", " + latestTblID);                     
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
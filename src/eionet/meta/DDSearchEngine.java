package eionet.meta;

import java.sql.*;
import javax.servlet.*;
import java.util.*;

import com.tee.util.Util;

public class DDSearchEngine {
    
    private final static String SEQUENCE_TYPE = "seq";
    private final static String CHOICE_TYPE   = "chc";
    private final static String ELEMENT_TYPE  = "elm";
    
    public final static String ORDER_BY_M_ATTR_NAME  = "SHORT_NAME";
    public final static String ORDER_BY_M_ATTR_DISP_ORDER  = "DISP_ORDER";
    
    private Connection conn = null;
    private ServletContext ctx = null;
    private String sessionID = "";
    
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
    
    public Vector getDataElements() throws SQLException {
        return getDataElements(null, null, null, null);
    }
    
    public Vector getDataElements(Vector params, String type, String namespace, String short_name) throws SQLException {
        return getDataElements(params, type, namespace, short_name, null);
    }
    
    public Vector getDataElements(Vector params, String type, String namespace, String short_name, String tableID) throws SQLException {

        boolean bAttributes=false;
        PreparedStatement prepStmt=null;
        
        StringBuffer tables = new StringBuffer();
        tables.append("DATAELEM left outer join NAMESPACE on ");
        tables.append("DATAELEM.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");
        tables.append("left outer join CONTENT on DATAELEM.DATAELEM_ID=CONTENT.PARENT_ID");
        tables.append(" left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID");
        
        StringBuffer constraints = new StringBuffer();

        if (type!=null && type.length()!=0){
            constraints.append("DATAELEM.TYPE='");
            constraints.append(type);
            constraints.append("'");
        }

        if (namespace!=null && namespace.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DATAELEM.NAMESPACE_ID='");
            constraints.append(namespace);
            constraints.append("'");
        }

        if (short_name!=null && short_name.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            constraints.append("DATAELEM.SHORT_NAME='");
            constraints.append(short_name);
            constraints.append("'");
        }
        
        if (tableID!=null && tableID.length()!=0){
            if (constraints.length()!=0) constraints.append(" and ");
            //constraints.append("DATAELEM.TABLE_ID=");
            constraints.append("TBL2ELEM.TABLE_ID=");
            constraints.append(tableID);
            bAttributes=true;
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
                    constraints.append("ATTR" + index + ".VALUE" + valueOper + attrValues.get(j));
                }
                constraints.append(")");
            }
            
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DATAELEM.DATAELEM_ID");

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='E'");
        }
        
        StringBuffer buf = new StringBuffer("select DATAELEM.*, NAMESPACE.*, ");
        buf.append("CONTENT.CHILD_ID,CONTENT.CHILD_TYPE, TBL2ELEM.TABLE_ID from ");
        buf.append(tables.toString());
        if (constraints.length()!=0){
            buf.append(" where ");
            buf.append(constraints.toString());
        }
        
        buf.append(" order by DATAELEM.SHORT_NAME");
        
        log(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        Vector v = new Vector();

        if (bAttributes){
            String prepQry =
            "select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE " +
            "where " +
            "ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and " +
            "ATTRIBUTE.DATAELEM_ID=?";

            prepStmt = conn.prepareStatement(prepQry);
        }
        
        int prvID = -1;
        while (rs.next()){
            
            int id = rs.getInt("DATAELEM.DATAELEM_ID");
            if (id == prvID) continue;
            prvID = id;
            DataElement dataElement = new DataElement(String.valueOf(id),
                                          rs.getString("DATAELEM.SHORT_NAME"),
                                          rs.getString("DATAELEM.TYPE"),
                                          null);
            
            String childID = rs.getString("CONTENT.CHILD_ID");
            if (childID != null){
                String childType = rs.getString("CONTENT.CHILD_TYPE");
                if (childType != null && childType.equalsIgnoreCase("seq"))
                    dataElement.setSequence(childID);
                else if (childType != null && childType.equalsIgnoreCase("chc"))
                    dataElement.setChoice(childID);
            }
            
            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));                                         
            dataElement.setNamespace(ns);
            
            dataElement.setExtension(rs.getString("DATAELEM.EXTENDS"));
            
            dataElement.setTableID(rs.getString("TBL2ELEM.TABLE_ID"));

            if (bAttributes){
                prepStmt.setInt(1, id);

                ResultSet _rs = prepStmt.executeQuery();

                while (_rs.next()){
                    DElemAttribute attr =
                        new DElemAttribute(_rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                    _rs.getString("M_ATTRIBUTE.NAME"),
                                    _rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                    DElemAttribute.TYPE_SIMPLE,
                                    _rs.getString("ATTRIBUTE.VALUE"),
                                    _rs.getString("M_ATTRIBUTE.DEFINITION"),
                                    _rs.getString("M_ATTRIBUTE.OBLIGATION"));
                    dataElement.addAttribute(attr);
                }
            }

            v.add(dataElement);
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
        "select DATAELEM.*, NAMESPACE.*, " +
        " CLASS2ELEM.DATACLASS_ID, CONTENT.CHILD_ID,CONTENT.CHILD_TYPE,TBL2ELEM.TABLE_ID " +
        "from DATAELEM " +
        "left outer join CLASS2ELEM on DATAELEM.DATAELEM_ID=CLASS2ELEM.DATAELEM_ID " +
        "left outer join NAMESPACE on DATAELEM.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID " +
        "left outer join CONTENT on DATAELEM.DATAELEM_ID=CONTENT.PARENT_ID " +
        "left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
        "where DATAELEM.DATAELEM_ID=" + delem_id;
        
        log(qry);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
        DataElement dataElement = null;
        
        if (rs.next()){
            
            dataElement = new DataElement(rs.getString("DATAELEM_ID"),
                                          rs.getString("DATAELEM.SHORT_NAME"),
                                          rs.getString("TYPE"),
                                          null);

            dataElement.setTableID(rs.getString("TBL2ELEM.TABLE_ID"));
            
            String childID = rs.getString("CONTENT.CHILD_ID");
            if (childID != null){
                String childType = rs.getString("CONTENT.CHILD_TYPE");
                if (childType != null && childType.equalsIgnoreCase("seq"))
                    dataElement.setSequence(childID);
                else if (childType != null && childType.equalsIgnoreCase("chc"))
                    dataElement.setChoice(childID);
            }
            
            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));
                                         
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

            DElemAttribute attr =
                new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                   rs.getString("M_ATTRIBUTE.NAME"),
                                   rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                   DElemAttribute.TYPE_SIMPLE,
                                   rs.getString("ATTRIBUTE.VALUE"),
                                   rs.getString("M_ATTRIBUTE.DEFINITION"),
                                   rs.getString("M_ATTRIBUTE.OBLIGATION"));
            
            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));
                                         
            attr.setNamespace(ns);
            dataElement.addAttribute(attr);
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
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
        Vector v = new Vector();
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
                                    rs.getString("DISP_HEIGHT"));
            }
            else{
                attr.setDisplayProps(null,
                                     rs.getInt("DISP_ORDER"),
                                     rs.getInt("DISP_WHEN"), null, null);
            }

            v.add(attr);
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
                                         rs.getString("NAMESPACE.DESCRIPTION"));                                         
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
        buf.append("left outer join NAMESPACE on DATAELEM.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");
        buf.append("where ");
        buf.append(tableName);
        buf.append(".");
        buf.append(tableName);
        buf.append("_ID=");
        buf.append(structID);
        buf.append(orderClause);
        
        log(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        while (rs.next()){
            
            String childType = rs.getString(tableName + ".CHILD_TYPE");
            if (childType == null) continue;
            
            if (childType.equalsIgnoreCase("elm")){
                DataElement dataElement = new DataElement(rs.getString(tableName + ".CHILD_ID"),
                                                          rs.getString("DATAELEM.SHORT_NAME"),
                                                          rs.getString("DATAELEM.TYPE"));
                    
                Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                             rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                             rs.getString("NAMESPACE.FULL_NAME"),
                                             null, //rs.getString("NAMESPACE.URL"),
                                             rs.getString("NAMESPACE.DESCRIPTION"));
                                             
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
        
        return v;
    }
    
    public Vector getSubValues(String parent_csi) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct CHILD_CSI from CSI_RELATION ");
        buf.append("where PARENT_CSI=");
        buf.append(parent_csi);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        Vector v = new Vector();
        while (rs.next()){
            FixedValue fxv = getFixedValue(rs.getString("CHILD_CSI"));
            v.add(fxv);
        }
        
        return v;
    }
    
    public boolean hasSubValues(String parent_csi) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select count(*) from CSI_RELATION ");
        buf.append("where PARENT_CSI=");
        buf.append(parent_csi);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        if (rs.next()){
            if (rs.getInt(1)>0){
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isChild(String csiID) throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select count(*) from CSI_RELATION ");
        buf.append("where CHILD_CSI=");
        buf.append(csiID);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        if (rs.next()){
            if (rs.getInt(1)>0){
                return true;
            }
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
        if (topLevel)
            buf.append(", CSI_RELATION.CHILD_CSI");
        buf.append(" from CS_ITEM");
        if (topLevel)
            buf.append(" left outer join CSI_RELATION on CS_ITEM.CSI_ID=CSI_RELATION.CHILD_CSI");
        buf.append(" where COMPONENT_ID=");
        buf.append(delem_id);
        buf.append(" and COMPONENT_TYPE=" + Util.strLiteral(parent_type));

        buf.append(" ORDER BY CSI_VALUE");

        log(buf.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();
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
            
            FixedValue fxv = getFixedValue(rs.getString("CSI_ID"));
            v.add(fxv);
        }
        
        return v;
    }
    
    public Vector getAttrFields(String attr_id) throws SQLException{
        
        StringBuffer buf = new StringBuffer();
        buf.append("select * from M_COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_ID=");
        buf.append(attr_id);
        buf.append(" order by POSITION");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        Vector v = new Vector();
        while (rs.next()){
            
            Hashtable hash = new Hashtable();
            hash.put("id", rs.getString("M_COMPLEX_ATTR_FIELD_ID"));
            hash.put("name", rs.getString("NAME"));
            hash.put("definition", rs.getString("DEFINITION"));
            hash.put("position", rs.getString("POSITION"));
            
            v.add(hash);
        }
        
        return v;
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
        //buf.append("NAMESPACE.SHORT_NAME as NS, ");
        buf.append("NAMESPACE.NAMESPACE_ID as NS, ");
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
        System.out.println(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Hashtable attrs  = new Hashtable();
        Hashtable fields = new Hashtable();
        
        Vector v = new Vector();
        
        DElemAttribute attr = null;
        int prvRow = -1;
        Hashtable rowHash = null;
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
        
        if (attr != null){
            attr.addRow(rowHash);
            v.add(attr);
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

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();
        while (rs.next()){
            Namespace namespace = new Namespace(rs.getString("NAMESPACE_ID"),
                                                rs.getString("NAMESPACE_ID"), //rs.getString("SHORT_NAME"),
                                                rs.getString("FULL_NAME"),
                                                null, //rs.getString("URL"),
                                                rs.getString("DESCRIPTION"));
            v.add(namespace);
        }

        return v;
    }

    public String getContentDefinitionUrl(String id) throws SQLException {

        StringBuffer buf = new StringBuffer("select * from CONTENT_DEFINITION where DATAELEM_ID=");
        buf.append(id);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        if (rs.next()){
            return rs.getString("URL");
        }
        
        return null;
    }
    public DataClass getDataClass(String class_id) throws SQLException {

        if (class_id == null || class_id.length()==0){
            DataClass dataClass = new DataClass();
            Vector attributes = getDElemAttributes();
            for (int i=0; i<attributes.size(); i++)
                dataClass.addAttribute(attributes.get(i));
            return dataClass;
        }

        String qry =
        "select DATA_CLASS.*, NAMESPACE.* from " +
        "DATA_CLASS left outer join NAMESPACE on DATA_CLASS.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID " +
        "where DATA_CLASS.DATACLASS_ID=" + class_id;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
        DataClass dataClass = null;

        if (rs.next()){
            dataClass = new DataClass(rs.getString("DATACLASS_ID"),
                                          rs.getString("DATA_CLASS.SHORT_NAME"));

            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));

            dataClass.setNamespace(ns);
        }
        else return null;

        qry =
        "select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE " +
        "where " +
        "ATTRIBUTE.PARENT_TYPE='C' and ATTRIBUTE.DATAELEM_ID=" + class_id + " and " +
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
            dataClass.addAttribute(attr);
        }

        return dataClass;
    }

    public Vector getDataClasses() throws SQLException {

        StringBuffer tables = new StringBuffer("DATA_CLASS left outer join NAMESPACE on DATA_CLASS.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID");

        StringBuffer buf = new StringBuffer("select DATA_CLASS.*, NAMESPACE.* from ");
        buf.append(tables.toString());

        buf.append(" order by DATA_CLASS.SHORT_NAME");
        
        log(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();

        while (rs.next()){
            int id = rs.getInt("DATA_CLASS.DATACLASS_ID");
            DataClass dataClass = new DataClass(String.valueOf(id),
                                          rs.getString("DATA_CLASS.SHORT_NAME"));

            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));

            dataClass.setNamespace(ns);

            v.add(dataClass);
        }

        return v;
    }

    public Vector getClass2Elems(String class_id) throws SQLException {

        StringBuffer buf =
            new StringBuffer("select distinct NAMESPACE.*, CLASS2ELEM.*, DATAELEM.* from DATAELEM left outer join CLASS2ELEM on DATAELEM.DATAELEM_ID=CLASS2ELEM.DATAELEM_ID left outer join NAMESPACE on DATAELEM.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");
        buf.append("where ");
        buf.append("CLASS2ELEM.DATACLASS_ID=");
        buf.append(class_id);
        //buf.append(" order by RELATION.POSITION");

        log(buf.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();

        while (rs.next()){
            DataElement dataElement = new DataElement(rs.getString("DATAELEM.DATAELEM_ID"),
                                                      rs.getString("DATAELEM.SHORT_NAME"), null);

            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));
            dataElement.setNamespace(ns);

            v.add(dataElement);
        }

        return v;
    }
    
    public Dataset getDataset(String datasetID) throws SQLException {
        Vector v = getDatasets(datasetID);
        if (v==null || v.size()==0)
            return null;
        else
            return (Dataset)v.get(0);
    }
        
    public Vector getDatasets() throws SQLException {
        return getDatasets(null);
    }
    
    private Vector getDatasets(String datasetID) throws SQLException {
        
        String qry = "select distinct * from DATASET";
        
        if (datasetID!=null && datasetID.length()!=0)
            qry += " where DATASET_ID='" + datasetID + "'";
        
        qry += " order by SHORT_NAME, VERSION";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        Vector v = new Vector();
        while (rs.next()){
            
            Dataset ds = new Dataset(rs.getString("DATASET_ID"),
                                     rs.getString("SHORT_NAME"),
                                     rs.getString("VERSION"));
            v.add(ds);
        }
        
        stmt.close();

        return v;
    }
    
    public Vector getDatasets(Vector params, String short_name, String version) throws SQLException {

        StringBuffer tables = new StringBuffer();
        tables.append("DATASET");

        StringBuffer constraints = new StringBuffer();

        if (short_name!=null && short_name.length()!=0){
            constraints.append("DATASET.SHORT_NAME='");
            constraints.append(short_name);
            constraints.append("'");
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
                    constraints.append("ATTR" + index + ".VALUE" + valueOper + attrValues.get(j));
                }
                constraints.append(")");
            }

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DATASET.DATASET_ID");

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='DS'");
        }

        StringBuffer buf = new StringBuffer("select DATASET.* from ");
        buf.append(tables.toString());
        if (constraints.length()!=0){
            buf.append(" where ");
            buf.append(constraints.toString());
        }

        buf.append(" order by DATASET.SHORT_NAME");

        log(buf.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();

        int prvID = -1;
        while (rs.next()){

            int id = rs.getInt("DATASET.DATASET_ID");
            if (id == prvID) continue;
            prvID = id;
            Dataset ds = new Dataset(rs.getString("DATASET_ID"),
                                     rs.getString("SHORT_NAME"),
                                     rs.getString("VERSION"));

            v.add(ds);
        }

        return v;
    }
    public Vector getDatasetTables(String dsID) throws SQLException {

        String qry =
        "select distinct * from DS_TABLE where DATASET_ID=" + dsID +
        " order by SHORT_NAME";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        Vector v = new Vector();
        while (rs.next()){
            
            DsTable dsTable = new DsTable(rs.getString("TABLE_ID"),
                                          rs.getString("DATASET_ID"),
                                          rs.getString("SHORT_NAME"));
            
            dsTable.setName(rs.getString("NAME"));
            dsTable.setDefinition(rs.getString("DEFINITION"));
            dsTable.setType(rs.getString("TYPE"));
            dsTable.setNamespace(rs.getString("NAMESPACE_ID"));
            
            v.add(dsTable);
        }
        
        stmt.close();

        return v;
    }
    public Vector getDatasetTables(Vector params, String short_name, String full_name, String definition) throws SQLException {

        StringBuffer tables = new StringBuffer();
        tables.append("DS_TABLE");

        StringBuffer constraints = new StringBuffer();

        if (short_name!=null && short_name.length()!=0){
            constraints.append("DS_TABLE.SHORT_NAME='");
            constraints.append(short_name);
            constraints.append("'");
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
                    constraints.append("ATTR" + index + ".VALUE" + valueOper + attrValues.get(j));
                }
                constraints.append(")");
            }

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DS_TABLE.TABLE_ID");

            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='T'");
        }

        StringBuffer buf = new StringBuffer("select DS_TABLE.* from ");
        buf.append(tables.toString());
        if (constraints.length()!=0){
            buf.append(" where ");
            buf.append(constraints.toString());
        }

        buf.append(" order by DS_TABLE.SHORT_NAME");

        log(buf.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();

        int prvID = -1;
        while (rs.next()){

            int id = rs.getInt("DS_TABLE.TABLE_ID");
            if (id == prvID) continue;
            prvID = id;
            DsTable tbl = new DsTable(rs.getString("TABLE_ID"),
                                     rs.getString("DATASET_ID"),
                                     rs.getString("SHORT_NAME"));

            tbl.setDefinition(rs.getString("DEFINITION"));
            tbl.setName(rs.getString("NAME"));

            v.add(tbl);
        }

        return v;
    }

    public DsTable getDatasetTable(String tableID) throws SQLException {
        
        String qry =
        "select * from DS_TABLE where TABLE_ID=" + tableID;
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        DsTable dsTable = null;
        if (rs.next()){
            
            dsTable = new DsTable(rs.getString("TABLE_ID"),
                                          rs.getString("DATASET_ID"),
                                          rs.getString("SHORT_NAME"));
            
            dsTable.setName(rs.getString("NAME"));
            dsTable.setDefinition(rs.getString("DEFINITION"));
            dsTable.setType(rs.getString("TYPE"));
            dsTable.setNamespace(rs.getString("NAMESPACE_ID"));
        }
        
        stmt.close();

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

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        Vector v = new Vector();
        while (rs.next()){
            
            DElemAttribute attr =
                new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
                                   rs.getString("M_ATTRIBUTE.NAME"),
                                   rs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                   DElemAttribute.TYPE_SIMPLE,
                                   rs.getString("ATTRIBUTE.VALUE"),
                                   rs.getString("M_ATTRIBUTE.DEFINITION"),
                                   rs.getString("M_ATTRIBUTE.OBLIGATION"));
            
            Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
                                         rs.getString("NAMESPACE.NAMESPACE_ID"), //rs.getString("NAMESPACE.SHORT_NAME"),
                                         rs.getString("NAMESPACE.FULL_NAME"),
                                         null, //rs.getString("NAMESPACE.URL"),
                                         rs.getString("NAMESPACE.DESCRIPTION"));
                                         
            attr.setNamespace(ns);            
            // JH - 120203, ATTRIBUTE.FIXED_VALUE_ID is no more present in the model
            //attr.setFixedValueID("ATTRIBUTE.FIXED_VALUE_ID");
            
            v.add(attr);
        }

        stmt.close();

        return v;
        
    }

    public String getDataElementID(String ns_id, String short_name) throws SQLException {

        if(ns_id == null || ns_id.length()==0) return null;
        if(short_name == null || short_name.length()==0) return null;

        StringBuffer buf = new StringBuffer("select DATAELEM.DATAELEM_ID from DATAELEM where DATAELEM.SHORT_NAME='");
        buf.append(short_name);
        buf.append("' AND DATAELEM.NAMESPACE_ID='");
        buf.append(ns_id);
        buf.append("'");

        log(buf.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        if (rs.next()){
            return rs.getString("DATAELEM_ID");
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
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        FixedValue fxv = null;

        if (rs.next()){
            /*fxv = new FixedValue(rs.getString("FIXED_VALUE.FIXED_VALUE_ID"),
                                          rs.getString("FIXED_VALUE.DATAELEM_ID"),
                                          rs.getString("FIXED_VALUE.VALUE"));*/
            
            fxv = new FixedValue(rs.getString("CSI_ID"),
                                          rs.getString("COMPONENT_ID"),
                                          rs.getString("CSI_VALUE"));
            
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

        return fxv;
    }
    public Vector getRelatedElements(String delem_id, String parent_type) throws SQLException {

        StringBuffer buf = new StringBuffer();

        buf.append("select CHILDITEM.*");
        buf.append(", CSI_RELATION.*");
        buf.append(" from CS_ITEM AS PARENTITEM");
        buf.append(" left outer join CSI_RELATION on PARENTITEM.CSI_ID=CSI_RELATION.PARENT_CSI");
        buf.append(" left outer join CS_ITEM AS CHILDITEM on CSI_RELATION.CHILD_CSI=CHILDITEM.CSI_ID");
        buf.append(" where PARENTITEM.COMPONENT_ID=");
        buf.append(delem_id);
        buf.append(" and PARENTITEM.COMPONENT_TYPE=" + Util.strLiteral(parent_type));
        buf.append(" and PARENTITEM.CSI_TYPE='elem_name'");

        buf.append(" ORDER BY CSI_VALUE");

        log(buf.toString());
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector v = new Vector();
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

            CsiItem csiI = new CsiItem(rs.getString("CHILDITEM.CSI_ID"),
                                    rs.getString("CHILDITEM.CSI_VALUE"),
                                    rs.getString("CHILDITEM.COMPONENT_ID"),
                                    rs.getString("CHILDITEM.COMPONENT_TYPE"));
            csiI.setRelDescription(rs.getString("CSI_RELATION.REL_DESCRIPTION"));
            v.add(csiI);
        }

        return v;
    }

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
            
            //Dataset ds = searchEngine.getDataset("1");
            Vector dsets = searchEngine.getDatasets();
            for (int i=0; dsets!=null && i<dsets.size(); i++){
                DataElement el = (DataElement)dsets.get(i);
                System.out.println(el.getID() + " " + el.getShortName());
            }

            /*Vector attrs = searchEngine.getComplexAttributes("138", "E");
            for (int i=0; attrs!=null && i<attrs.size(); i++){
                DElemAttribute attr = (DElemAttribute)attrs.get(i);
                System.out.println(attr.getShortName());
                Vector rows = attr.getRows();
                for (int j=0; rows!=null && j<rows.size(); j++){
                    Hashtable hash = (Hashtable)rows.get(j);
                    Enumeration enum = hash.keys();
                    while (enum!=null && enum.hasMoreElements()){
                        String fieldID = (String)enum.nextElement();
                        String fieldValue = (String)hash.get(fieldID);
                        System.out.println(fieldID + " = " + fieldValue);
                    }
                }
            }*/
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
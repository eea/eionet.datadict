
package eionet.meta;

import java.sql.*;

public class Namespace{
    
    private String id = null;
    private String shortName = null;
    private String fullName = null;
    private String url = null;
    private String description = null;
    
    private String tableID = null;
    private String dsID = null;
    
    private String workingUser = null;
    
    public Namespace(String id, String shortName, String fullName, String url, String description){
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
        this.url = url;
        this.description = description;
    }
    
    public String getID(){
        return id;
    }

	public String getPrefix(){
		return "dd" + id;
	}
    
    public String getShortName(){
        return shortName;
    }
    
    public String getFullName(){
        return fullName;
    }
    
    public String getUrl(){
        return url;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setUrl(String url){
        this.url = url;
    }
    
    public void setTable(String tableID){
        this.tableID = tableID;
    }
    
    public String getTable(){
        return tableID;
    }
    
    public void setDataset(String dsID){
        this.dsID = dsID;
    }
    
    public String getDataset(){
        return dsID;
    }
    
	public void setWorkingUser(String user){
		this.workingUser = user;
	}

	public String getWorkingUser(){
		return this.workingUser;
	}
    
    /**
    *
    */
    public static void main(String[] args){
        
        Connection conn = null;
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            if (conn != null){
                try{ conn.close(); }
                catch (Exception e) {}
            }
        }
    }
}
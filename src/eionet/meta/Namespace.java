
package eionet.meta;

public class Namespace{
    
    private String id = null;
    private String shortName = null;
    private String fullName = null;
    private String url = null;
    private String description = null;
    
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
}
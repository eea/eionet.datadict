package eionet.meta.dao.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * The domain object corresponding to table INFERENCE_RULE
 */

public class InferenceRule {
    
    public static enum RuleType{
        INVERSE("owl:inverseOf");
        
        private final String name;
        
        private RuleType(String ruleType){
            this.name = ruleType;
        }
        
        public String getName(){
            return this.name;
        }
        
        public static Set getAllNames(){
            Set ruleNames = new HashSet<String>();
            
            for(RuleType rule : RuleType.values()){
                ruleNames.add(rule.getName());
            }
            return ruleNames;
        }
        
        public static Map<String, RuleType> getAllMappings(){
            Map<String, RuleType> map = new HashMap();
            
            for(RuleType rule : RuleType.values()){
                map.put(rule.getName(), rule);
            }
            return map;
        }
        
        public static RuleType fromName(String name){
            for(RuleType type : RuleType.values()){
                if(type.getName().equals(name)){
                    return type;
                }
            }
            throw new IllegalArgumentException("Illegal rule name: " + name);
        }
    }
    
    private DataElement sourceDElement;
    private RuleType type;
    private DataElement targetDElement;
    
    public InferenceRule(DataElement source, RuleType type, DataElement target){
        this.sourceDElement = source;
        this.type = type;
        this.targetDElement = target;
    }
    
    public InferenceRule(){
    }
    
    public DataElement getSourceDElement(){
        return this.sourceDElement;
    }
    
    public void setSourceDElement(DataElement source){
        this.sourceDElement = source;
    }
    
    public DataElement getTargetDElement(){
        return this.targetDElement;
    }
    
    public void setTargetDElement(DataElement target){
        this.targetDElement = target;
    }
    
    public RuleType getType(){
        return this.type;
    }
    
    public void setType(RuleType type){
        this.type = type;
    }
    
    public String getTypeName(){
        return this.type.getName();
    }
    
    public static Set getAllRuleNames(){
        return RuleType.getAllNames();
    }
    
    public static Map<String, RuleType> getAllRuleMappings(){
        return RuleType.getAllMappings();
    }
    
    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof InferenceRule))
            return false;
        if (obj == this)
            return true;
        
        InferenceRule rule = (InferenceRule) obj;
        return ( (getSourceDElement().getId() == rule.getSourceDElement().getId()) && (getTargetDElement().getId() == rule.getTargetDElement().getId()) && (getType() == rule.getType()) );
    }
    
    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 37).append(this.getSourceDElement().getId()).append(this.getType().getName()).append(this.getTargetDElement().getId()).toHashCode();
    }
    
}

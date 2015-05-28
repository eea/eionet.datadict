package eionet.meta;

/*
 * The domain object corresponding to table INFERENCE_RULE
 */

public class InferenceRule {
    
    private static enum RuleType{
        INVERSE("owl:inverseOf");
        
        private String name;
        
        private RuleType(String ruleType){
            this.name = ruleType;
        }
        
        public String getName(){
            return this.name;
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
    private RuleType rule;
    private DataElement targetDElement;
    
    public InferenceRule(DataElement source, String rule, DataElement target){
        this.sourceDElement = source;
        this.rule = RuleType.fromName(rule);
        this.targetDElement = target;
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
    
    public RuleType getRule(){
        return this.rule;
    }
    
    public String getRuleName(){
        return this.rule.getName();
    }
    
}

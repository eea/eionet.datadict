/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Raptis Dimos
 */

package eionet.web.action;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.InferenceRule;
import eionet.meta.dao.domain.InferenceRule.RuleType;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;
import java.util.Collection;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;


@UrlBinding("/inference_rules/{parentElementId}/{$event}/{type}/{targetElementId}/{newType}/{newTargetElementId}/{pattern}")
public class InferenceRuleActionBean extends AbstractActionBean {
    
    private DataElement parentElement;     
    private Collection<InferenceRule> rules;
    
    int parentElementId;
    RuleType type;
    int targetElementId;
    
    RuleType newType;
    int newTargetElementId;
    
    String pattern;
    
    private static final String INFERENCE_RULES_VIEW_JSP = "/pages/inferenceRules/inference_rules.jsp";
    private static final String INFERENCE_RULE_NEW_JSP = "/pages/inferenceRules/new_inference_rule.jsp";
    private static final String INFERENCE_RULE_EXISTING_JSP = "/pages/inferenceRules/existing_inference_rule.jsp";
    private static final String INFERENCE_RULE_ACCESS_PAGE = "/pages/inferenceRules/inference_access.jsp";
    
    @SpringBean
    private IDataService dataService;
    
    @DefaultHandler
    public Resolution view() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }
        
        if(!dataService.dataElementExists(parentElementId) )
            addGlobalValidationError("There is no element with ID : " + Integer.toString(parentElementId));
        
        if(getGlobalValidationErrors().size() == 0){
            rules = dataService.listDataElementRules(parentElementId);
            parentElement = dataService.getDataElement(parentElementId);
        }
        return new ForwardResolution(INFERENCE_RULES_VIEW_JSP);
    }
    
    public Resolution newRule() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }
        
        if(!dataService.dataElementExists(parentElementId))
            addGlobalValidationError("There is no element with ID : " + Integer.toString(parentElementId));
        
        if(getGlobalValidationErrors().size() == 0)
            parentElement = dataService.getDataElement(parentElementId);
        
        return new ForwardResolution(INFERENCE_RULE_NEW_JSP);
    }
    
    public Resolution addRule() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }
        
        if(!dataService.dataElementExists(parentElementId))
            addCautionMessage("There is no source element with ID : " + Integer.toString(parentElementId));
        if(!dataService.dataElementExists(targetElementId))
            addCautionMessage("There is no target element with ID : " + Integer.toString(targetElementId));
        if( dataService.dataElementExists(parentElementId) && dataService.dataElementExists(targetElementId)){
            if( dataService.ruleExists(parentElementId, type, targetElementId) )
                addCautionMessage("The rule (" + type.getName() + "," + Integer.toString(targetElementId) + ") for element " + Integer.toString(parentElementId) + " already exists");
        }
        
        
        if(getCautionMessages().size() > 0){
            parentElement = dataService.getDataElement(parentElementId);
            return getContext().getSourcePageResolution();
        }
        else{
            dataService.createDataElementRule(parentElementId, type, targetElementId);
            addSystemMessage("Inference Rule (" + type.getName() + "," + Integer.toString(targetElementId) + ") for element " + Integer.toString(parentElementId) + " already exists");
            return new RedirectResolution(InferenceRuleActionBean.class).addParameter("parentElementId", parentElementId);
        }
        
    }
    
    public Resolution existingRule() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }
        
        if( !dataService.dataElementExists(parentElementId) )
            addGlobalValidationError("There is no source element with ID : " + Integer.toString(parentElementId));
        if( !dataService.dataElementExists(targetElementId) )
            addGlobalValidationError("There is no target element with ID : " + Integer.toString(targetElementId));
        
        if(getGlobalValidationErrors().size() == 0)
            parentElement = dataService.getDataElement(parentElementId);
        
        return new ForwardResolution(INFERENCE_RULE_EXISTING_JSP);
    }
    
    public Resolution editRule() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }

        if( !dataService.dataElementExists(parentElementId))
            addGlobalValidationError("The ID (" + Integer.toString(parentElementId) + ") of the parent element of the existing rule is not valid");
        if( !dataService.dataElementExists(targetElementId) )
            addGlobalValidationError("The ID (" + Integer.toString(targetElementId) + ") of the target element of the existing rule is not valid");         
        
        if( !dataService.dataElementExists(newTargetElementId) )
            addCautionMessage("There is no target element with ID : " + Integer.toString(newTargetElementId));
        if( dataService.dataElementExists(parentElementId) && dataService.dataElementExists(newTargetElementId)){
            if( dataService.ruleExists(parentElementId, newType, newTargetElementId) )
                addCautionMessage("The rule (" + newType.getName() + "," + Integer.toString(newTargetElementId) + ") for element " + Integer.toString(parentElementId) + " already exists");
        }
        
        if( (getGlobalValidationErrors().size() > 0) || (getCautionMessages().size() > 0) ){
            parentElement = dataService.getDataElement(parentElementId);
            return getContext().getSourcePageResolution();
        }
        else{
            dataService.updateDataElementRule(parentElementId, type, targetElementId, newType, newTargetElementId);
            addSystemMessage("Inference Rule (" + type.getName() + "," + Integer.toString(targetElementId) + ") for element " + Integer.toString(parentElementId) + " was successfully edited to (" + newType.getName() + "," + Integer.toString(newTargetElementId) + ")");
            return new RedirectResolution(InferenceRuleActionBean.class).addParameter("parentElementId", parentElementId);
        }
        
    }
    
    public Resolution deleteRule() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }
        
        if( !dataService.dataElementExists(parentElementId) || !dataService.dataElementExists(targetElementId) )
            addCautionMessage("The rule (" + Integer.toString(parentElementId) + "," + type.getName() + "," + Integer.toString(targetElementId) + ") could not be deleted, because it does not exist");
        else{
            dataService.deleteDataElementRule(parentElementId, type, targetElementId);
            addSystemMessage("Inference Rule (" + type.getName() + "," + Integer.toString(targetElementId) + ") for element " + Integer.toString(parentElementId) + "was successfully deleted");
        }
        return new RedirectResolution(InferenceRuleActionBean.class).addParameter("parentElementId", parentElementId);
    }
    
    @HandlesEvent("search")
    public Resolution grepElement() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access inference rules");
            return new ForwardResolution(INFERENCE_RULE_ACCESS_PAGE);
        }
        
        Collection<DataElement> elements = dataService.grepDataElement(pattern);
        
        JSONArray jsonObject = convertToJsonArray(elements, new String[] {"id", "shortName"});
        return new StreamingResolution("application/json", jsonObject.toString());
    }
    
    private JSONArray convertToJsonArray(Collection javaObject, final String[] includedFields){
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setJsonPropertyFilter( new PropertyFilter(){    
            @Override
            public boolean apply( Object source, String name, Object value) {
                boolean isFiltered = true;
                for (String includedField : includedFields){
                    if (name.equals(includedField))
                        isFiltered = false;
                }
                return isFiltered;
            }    
        });   
        
        JSONArray jsonObject = JSONArray.fromObject(javaObject, jsonConfig);
        return jsonObject;
    }
    
    public void setParentElement(DataElement element){
        this.parentElement = element;
    }
    
    public DataElement getParentElement(){
        return this.parentElement;
    }
    
    public Collection<InferenceRule> getRules(){
        return this.rules;
    }
    
    public void setRules(Collection rules){
        this.rules = rules;
    }
    
    public void setType(RuleType type){
        this.type = type;
    }

    public RuleType getType(){
        return type;
    }
    
    public int getTargetElementId(){
        return targetElementId;
    }
    
    public void setTargetElementId(int id){
        this.targetElementId = id;
    }
    
    public int getParentElementId(){
        return parentElementId;
    }
    
    public void setParentElementId(int id){
        this.parentElementId = id;
    }
    
    public RuleType getNewType(){
        return this.newType;
    }
    
    public void setNewType(RuleType type){
        this.newType = type;
    }
    
    public int getNewTargetElementId(){
        return this.targetElementId;
    }
    
    public void setNewTargetElementId(int id){
        this.newTargetElementId = id;
    }
    
    public String getPattern(){
        return this.pattern;
    }
    
    public void setPattern(String pattern){
        this.pattern = pattern;
    }
}

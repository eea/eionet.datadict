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

import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;
import java.util.Collection;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;


@UrlBinding("/fixed_values/{ownerType}/{ownerId}/{$event}/{fixedValue}")
public class FixedValuesActionBean extends AbstractActionBean {
    
    private Object owner;
    private Collection<FixedValue> fixedValues;
    private FixedValue fixedValue;
    
    private String ownerType;
    private int ownerId;
    
    private static final String ELEMENT_FIXED_VALUES_VIEW_JSP = "/pages/fixedValues/elements/fixed_values.jsp";
    private static final String ELEMENT_FIXED_VALUES_NEW_JSP = "/pages/fixedValues/elements/new_fixed_value.jsp";
    private static final String ELEMENT_FIXED_VALUES_EXISTING_JSP = "/pages/fixedValues/elements/existing_fixed_value.jsp";
    
    private static final String ATTRIBUTE_FIXED_VALUES_VIEW_JSP = "/pages/fixedValues/attributes/fixed_values.jsp";
    private static final String ATTRIBUTE_FIXED_VALUES_NEW_JSP = "/pages/fixedValues/attributes/new_fixed_value.jsp";
    private static final String ATTRIBUTE_FIXED_VALUES_EXISTING_JSP = "/pages/fixedValues/attributes/existing_fixed_value.jsp";
            
    private static final String ERROR_PAGE = "/pages/fixedValues/values_access.jsp";
    
    @SpringBean
    private IDataService dataService;
    
    @DefaultHandler
    public Resolution view() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access fixed values");
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(ownerType.equals("attr"))
            return attributeOwnerView();
        else if(ownerType.equals("elem"))
            return elementOwnerView();
        else{
            addGlobalValidationError("Invalid owner type ");
            return new ForwardResolution(ERROR_PAGE);
        }
        
    }
    
    public Resolution elementOwnerView() throws ServiceException {
        if(!dataService.dataElementExists(ownerId) ){
            addGlobalValidationError("There is no element with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        else {
            owner = dataService.getDataElement(ownerId);
            fixedValues = dataService.getDataElementFixedValues(ownerId);
        }
        return new ForwardResolution(ELEMENT_FIXED_VALUES_VIEW_JSP);
    }
    
    public Resolution attributeOwnerView() throws ServiceException {
        if(!dataService.attributeExists(ownerId)){
            addGlobalValidationError("There is no attribute with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        else{
            owner = dataService.getAttributeById(ownerId);
            fixedValues = dataService.getAttributeFixedValues(ownerId);
        }
        return new ForwardResolution(ATTRIBUTE_FIXED_VALUES_VIEW_JSP);
    }
    
    @HandlesEvent("new")
    public Resolution newValue() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access fixed values");
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(ownerType.equals("attr"))
            return attributeOwnerNew();
        else if(ownerType.equals("elem"))
            return elementOwnerNew();
        else{
            addGlobalValidationError("Invalid owner type ");
            return new ForwardResolution(ERROR_PAGE);
        }      
    }
    
    public Resolution elementOwnerNew() throws ServiceException {
        if(!dataService.dataElementExists(ownerId) ){
            addGlobalValidationError("There is no element with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        else
            owner = dataService.getDataElement(ownerId);
        return new ForwardResolution(ELEMENT_FIXED_VALUES_NEW_JSP);
    }
    
    public Resolution attributeOwnerNew() throws ServiceException {
        if(!dataService.attributeExists(ownerId)) {
            addGlobalValidationError("There is no attribute with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        else
            owner = dataService.getAttributeById(ownerId);
        return new ForwardResolution(ATTRIBUTE_FIXED_VALUES_NEW_JSP);
    }
    
    @HandlesEvent("add")
    public Resolution addValue() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access fixed values");
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(ownerType.equals("attr"))
            return attributeOwnerAdd();
        else if(ownerType.equals("elem"))
            return elementOwnerAdd();
        else{
            addGlobalValidationError("Invalid owner type ");
            return new ForwardResolution(ERROR_PAGE);
        }
    }
    
    public Resolution elementOwnerAdd() throws ServiceException {
        fixedValue.setOwnerType("elem");
        fixedValue.setOwnerId(ownerId);
        
        if(dataService.fixedValueExistsWithSameNameOwner(fixedValue)){
            addCautionMessage("Another fixed value with the same name exists for this element/attribute");
            owner = dataService.getDataElement(ownerId);
            return getContext().getSourcePageResolution();
        }
        
        dataService.createFixedValue(fixedValue);
        return new RedirectResolution(FixedValuesActionBean.class).addParameter("ownerType", ownerType).addParameter("ownerId", ownerId);
    }
    
    public Resolution attributeOwnerAdd() throws ServiceException {
        fixedValue.setOwnerType("attr");
        fixedValue.setOwnerId(ownerId);
        
        if(dataService.fixedValueExistsWithSameNameOwner(fixedValue)){
            addCautionMessage("Another fixed value with the same name exists for this element/attribute");
            owner = dataService.getDataElement(ownerId);
            return getContext().getSourcePageResolution();
        }
        
        dataService.createFixedValue(fixedValue);
        return new RedirectResolution(FixedValuesActionBean.class).addParameter("ownerType", ownerType).addParameter("ownerId", ownerId);
    }
    
    @HandlesEvent("delete")
    public Resolution deleteValue() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access fixed values");
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(!dataService.fixedValueExists(fixedValue.getId()) ){
            addGlobalValidationError("There is no fixed value with ID : " + Integer.toString(fixedValue.getId()));
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(getGlobalValidationErrors().size() == 0){
            fixedValue = dataService.getFixedValueById(fixedValue.getId());
            dataService.deleteFixedValue(fixedValue);
            addSystemMessage("Fixed Value " + fixedValue.getValue() + " was successfully deleted");
        }
        return new RedirectResolution(FixedValuesActionBean.class).addParameter("ownerType", ownerType).addParameter("ownerId", ownerId);   
    }
    
    @HandlesEvent("existing")
    public Resolution existingValue() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access fixed values");
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(ownerType.equals("attr"))
            return attributeOwnerExisting();
        else if(ownerType.equals("elem"))
            return elementOwnerExisting();
        else{
            addGlobalValidationError("Invalid owner type ");
            return new ForwardResolution(ERROR_PAGE);
        }
        
    }
    
    public Resolution attributeOwnerExisting() throws ServiceException {
        if(!dataService.attributeExists(ownerId) ){
            addGlobalValidationError("There is no attribute with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        owner = dataService.getAttributeById(ownerId);
        fixedValue = dataService.getFixedValueById(fixedValue.getId());
        return new ForwardResolution(ATTRIBUTE_FIXED_VALUES_EXISTING_JSP);
    }
    
    public Resolution elementOwnerExisting() throws ServiceException {
        if(!dataService.dataElementExists(ownerId) ){
            addGlobalValidationError("There is no element with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        owner = dataService.getDataElement(ownerId);
        fixedValue = dataService.getFixedValueById(fixedValue.getId());
        return new ForwardResolution(ELEMENT_FIXED_VALUES_EXISTING_JSP);
    }
    
    @HandlesEvent("edit")
    public Resolution editValue() throws ServiceException {
        if(!isUserLoggedIn()){
            addGlobalValidationError("You have to login to access fixed values");
            return new ForwardResolution(ERROR_PAGE);
        }
        
        if(ownerType.equals("attr"))
            return attributeOwnerEdit();
        else if(ownerType.equals("elem"))
            return elementOwnerEdit();
        else{
            addGlobalValidationError("Invalid owner type ");
            return new ForwardResolution(ERROR_PAGE);
        }
    }
    
    public Resolution elementOwnerEdit() throws ServiceException {
        if(!dataService.dataElementExists(ownerId) ){
            addGlobalValidationError("There is no element with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        fixedValue.setOwnerType("elem");
        fixedValue.setOwnerId(ownerId);
        
        if(dataService.fixedValueExistsWithSameNameOwner(fixedValue)){
            addCautionMessage("Another fixed value with the same name exists for this element/attribute");
            owner = dataService.getDataElement(ownerId);
            return getContext().getSourcePageResolution();
        }
        
        dataService.updateFixedValue(fixedValue);
        return new RedirectResolution(FixedValuesActionBean.class).addParameter("ownerType", ownerType).addParameter("ownerId", ownerId);
    }
    
    public Resolution attributeOwnerEdit() throws ServiceException {
        if(!dataService.attributeExists(ownerId) ){
            addGlobalValidationError("There is no attribute with ID : " + Integer.toString(ownerId));
            return new ForwardResolution(ERROR_PAGE);
        }
        fixedValue.setOwnerType("attr");
        fixedValue.setOwnerId(ownerId);
        
        if(dataService.fixedValueExistsWithSameNameOwner(fixedValue)){
            addCautionMessage("Another fixed value with the same name exists for this element/attribute");
            owner = dataService.getDataElement(ownerId);
            return getContext().getSourcePageResolution();
        }
        
        dataService.updateFixedValue(fixedValue);
        return new RedirectResolution(FixedValuesActionBean.class).addParameter("ownerType", ownerType).addParameter("ownerId", ownerId);
    }
    
    public Object getOwner() {
        return this.owner;
    }
    
    public void setOwner(Object owner) {
        this.owner = owner;
    }
    
    public String getOwnerType(){
        return this.ownerType;
    }
    
    public void setOwnerType(String type){
        this.ownerType = type;
    }
    
    public int getOwnerId(){
        return this.ownerId;
    }
    
    public void setOwnerId(int id){
        this.ownerId = id;
    }
    
    public Collection<FixedValue> getFixedValues(){
        return this.fixedValues;
    }
    
    public void setFixedValues(Collection<FixedValue> values){
        this.fixedValues = values;
    }
    
    public FixedValue getFixedValue(){
        return this.fixedValue;
    }
    
    public void setFixedValue(FixedValue value){
        this.fixedValue = value;
    }

}

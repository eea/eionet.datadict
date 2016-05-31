package eionet.datadict.controllers;

import eionet.datadict.model.Attribute;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.NamespaceDataService;
import eionet.datadict.services.data.RdfNamespaceDataService;
import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.web.action.AbstractActionBean;
import java.util.List;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

@UrlBinding("/attribute2/{$event}/{attribute.id}")
public class AttributeActionBean2 extends AbstractActionBean implements ValidationErrorHandler {

    @SpringBean
    private AclService aclService;
    @SpringBean
    private AttributeService attributeService;
    @SpringBean
    private RdfNamespaceDataService rdfNamespaceDataService;
    @SpringBean
    private NamespaceDataService namespaceDataService;
    
    private Attribute attribute;
    private List<Namespace> namespaces;
    private List<RdfNamespace> rdfNamespaces;
    
    public Resolution add() throws UserAuthenticationException, UserAuthorizationException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add/edit attributes.");
        }
        
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT)) {
            throw new UserAuthorizationException("You are not authorized to add new attributes.");
        }
        
        this.attribute = new Attribute();
        this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        
        return new ForwardResolution("/pages/attributes/attributeEditor.jsp");
    }
    
    public Resolution save() throws UserAuthenticationException, UserAuthorizationException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add/edit attributes.");
        }
        
        int attributeId = this.attributeService.save(attribute, user);
        
        return new RedirectResolution("/attribute/view/" + attributeId);
    }
    
    @Override
    public Resolution handleValidationErrors(ValidationErrors ve) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // ------ ActionBean bean access ---------
    
    public AclService getAclService() {
        return aclService;
    }

    public void setAclService(AclService aclService) {
        this.aclService = aclService;
    }

    public AttributeService getAttributeService() {
        return attributeService;
    }

    public void setAttributeService(AttributeService attributeService) {
        this.attributeService = attributeService;
    }
    
    public RdfNamespaceDataService getRdfNamespaceDataService() {
        return rdfNamespaceDataService;
    }

    public void setRdfNamespaceDataService(RdfNamespaceDataService rdfNamespaceDataService) {
        this.rdfNamespaceDataService = rdfNamespaceDataService;
    }

    public NamespaceDataService getNamespaceDataService() {
        return namespaceDataService;
    }

    public void setNamespaceDataService(NamespaceDataService namespaceDataService) {
        this.namespaceDataService = namespaceDataService;
    }
    
    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public List<RdfNamespace> getRdfNamespaces() {
        return rdfNamespaces;
    }

    public void setRdfNamespaces(List<RdfNamespace> rdfNamespaces) {
        this.rdfNamespaces = rdfNamespaces;
    }
    
}

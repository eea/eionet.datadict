package eionet.datadict.service.impl;

import eionet.datadict.dal.RdfNamespaceDAO;
import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eionet.datadict.dal.AttributeDefinitionDAO;

/**
 *
 * @author eworx-alk
 */
@Service
public class AttributeServiceImpl implements AttributeService{
    
    private final AttributeDefinitionDAO attributeDeclarationDAO;
    private final RdfNamespaceDAO rdfNamespaceDAO;
    
    @Autowired
    public AttributeServiceImpl(AttributeDefinitionDAO attributeDeclarationDAO, RdfNamespaceDAO rdfNamespaceDAO) {
        this.attributeDeclarationDAO = attributeDeclarationDAO;
        this.rdfNamespaceDAO = rdfNamespaceDAO;
    }

//    @Override
//    public AttributeDeclaration attributeDeclarationAndRdfNamespaceById(int attributeDeclarationId) {
//        AttributeDeclaration attrDecl = this.attributeDeclarationDAO.getAttributeDeclById(attributeDeclarationId);
//        attrDecl.setRdfNameSpace(this.rdfNamespaceDAO.getRdfNamespaceById(attrDecl.getRdfNameSpaceId()));
//        return attrDecl;
//    }
} 
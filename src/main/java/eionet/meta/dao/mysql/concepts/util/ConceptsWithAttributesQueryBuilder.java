package eionet.meta.dao.mysql.concepts.util;

import eionet.meta.dao.domain.StandardGenericStatus;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class ConceptsWithAttributesQueryBuilder {

    private final int vocabularyId;
    private final String conceptIdentifier;
    private final String conceptLabel;
    private final StandardGenericStatus conceptStatus;
    
    public ConceptsWithAttributesQueryBuilder(int vocabularyId, String conceptIdentifier, String conceptLabel, StandardGenericStatus conceptStatus) {
        this.vocabularyId = vocabularyId;
        this.conceptIdentifier = conceptIdentifier;
        this.conceptLabel = conceptLabel;
        this.conceptStatus = conceptStatus;
    }

    public int getVocabularyId() {
        return vocabularyId;
    }

    public String getConceptIdentifier() {
        return conceptIdentifier;
    }

    public String getConceptLabel() {
        return conceptLabel;
    }

    public StandardGenericStatus getConceptStatus() {
        return conceptStatus;
    }
    
    public String buildConceptsSqlQuery() {
        return
            "select \n" +
            "	c.VOCABULARY_CONCEPT_ID, \n" +
            "	c.IDENTIFIER, \n" +
            "	c.LABEL, \n" +
            "	c.DEFINITION, \n" +
            "	c.NOTATION, \n" +
            "	c.STATUS, \n" +
            "	c.ACCEPTED_DATE, \n" +
            "	c.NOT_ACCEPTED_DATE, \n" +
            "	c.STATUS_MODIFIED \n" +
            "from \n" +
            "	VOCABULARY_CONCEPT c \n" +
            this.append(
            "   left join ( \n" +
            "       select \n" +
            "           distinct vc.VOCABULARY_CONCEPT_ID \n" +
            "       from \n" +
            "           VOCABULARY2ELEM v2e \n" +
            "           inner join DATAELEM d on v2e.DATAELEM_ID = d.DATAELEM_ID \n" +
            "           inner join VOCABULARY_CONCEPT vc on v2e.VOCABULARY_ID = vc.VOCABULARY_ID \n" +
            "           inner join VOCABULARY_CONCEPT_ELEMENT vce on vc.VOCABULARY_CONCEPT_ID = vce.VOCABULARY_CONCEPT_ID and vce.DATAELEM_ID = v2e.DATAELEM_ID \n" +
            "       where\n" +
            "           v2e.VOCABULARY_ID = :vocabularyId and d.IDENTIFIER = 'skos:prefLabel' \n" +
            this.append(" and vc.STATUS & :conceptStatus = :conceptStatus", this.isStatusDefined()) + " \n" +
            "            and vce.ELEMENT_VALUE collate UTF8_GENERAL_CI like :conceptLabelPattern \n" +
            "	) preflabel on c.VOCABULARY_CONCEPT_ID = preflabel.VOCABULARY_CONCEPT_ID \n",
                    this.isConceptLabelDefined()
            ) +
            "where \n" +
            "	c.VOCABULARY_ID = :vocabularyId" + this.append(" and c.STATUS & :conceptStatus = :conceptStatus", this.isStatusDefined()) + " \n" +
            this.append(
                "   and c.IDENTIFIER collate UTF8_GENERAL_CI like :conceptIdentifierPattern \n", 
                this.isConceptIdentifierDefined()
            ) +
            this.append(
                "   and (preflabel.VOCABULARY_CONCEPT_ID is not null or c.LABEL collate UTF8_GENERAL_CI like :conceptLabelPattern) \n", 
                this.isConceptLabelDefined()
            ) +
            "order by \n" +
            "   c.VOCABULARY_CONCEPT_ID";
    }

    public Map<String, Object> buildConceptsSqlQueryParameters() {
        return this.buildParameters();
    }

    public String buildConceptAttributesSqlQuery() {
        if (this.isConceptLabelDefined()) {
            return this.buildConceptAttributesWithLabelFilteringSqlQuery();
        }
        
        return this.buildConceptAttributesWithoutLabelFilteringSqlQuery();
    }

    public Map<String, Object> buildConceptAttributesSqlQueryParameters() {
        return this.buildParameters();
    }
    
    protected String buildConceptAttributesWithoutLabelFilteringSqlQuery() {
        return
            "select\n" +
            "	c.VOCABULARY_CONCEPT_ID as ConceptId, \n" +
            "	v.DATAELEM_ID as AttributeId, \n" +
            "	v.ELEMENT_VALUE as AttributeValue, \n" +
            "	v.LANGUAGE as AttributeLanguage, \n" +
            "	rc.VOCABULARY_ID as RelatedConceptVocabularyId,\n" +
            "	v.RELATED_CONCEPT_ID RelatedConceptId,\n" +
            "	rc.IDENTIFIER as RelatedConceptIdentifier, \n" +
            "	rc.LABEL as RelatedConceptLabel, \n" +
            "	rc.NOTATION as RelatedConceptNotation, \n" +
            "	rc.DEFINITION as RelatedConceptDefinition \n" +
            "from \n" +
            "   VOCABULARY_CONCEPT c \n" +
            "	inner join VOCABULARY_CONCEPT_ELEMENT v on c.VOCABULARY_CONCEPT_ID = v.VOCABULARY_CONCEPT_ID \n" +
            "   left join VOCABULARY_CONCEPT rc on v.RELATED_CONCEPT_ID = rc.VOCABULARY_CONCEPT_ID \n" +
            "where \n" +
            "	c.VOCABULARY_ID = :vocabularyId" + this.append(" and c.STATUS & :conceptStatus = :conceptStatus", this.isStatusDefined()) + " \n" +
            this.append(
                "   and c.IDENTIFIER collate UTF8_GENERAL_CI like :conceptIdentifierPattern \n", 
                this.isConceptIdentifierDefined()
            ) +
            "order by \n" +
            "   c.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, v.LANGUAGE, v.ID";
    }
    
    protected String buildConceptAttributesWithLabelFilteringSqlQuery() {
        return
            "select\n" +
            "	c.VOCABULARY_CONCEPT_ID as ConceptId,\n" +
            "	v.DATAELEM_ID as AttributeId,\n" +
            "	d.IDENTIFIER as AttributeIdentifier,\n" +
            "	v.ELEMENT_VALUE as AttributeValue,\n" +
            "	v.LANGUAGE as AttributeLanguage,\n" +
            "	rc.VOCABULARY_ID as RelatedConceptVocabularyId,\n" +
            "	v.RELATED_CONCEPT_ID RelatedConceptId,\n" +
            "	rc.IDENTIFIER as RelatedConceptIdentifier,\n" +
            "	rc.LABEL as RelatedConceptLabel,\n" +
            "	rc.NOTATION as RelatedConceptNotation,\n" +
            "	rc.DEFINITION as RelatedConceptDefinition\n" +
            "from (\n" +
            "	select \n" +
            "       distinct vc.VOCABULARY_CONCEPT_ID\n" +
            "	from \n" +
            "       VOCABULARY_CONCEPT vc \n" +
            "       left join ( \n" +
            "           select \n" +
            "               vc2.VOCABULARY_CONCEPT_ID, vce.ELEMENT_VALUE as PrefLabel \n" +
            "		from \n" +
            "               VOCABULARY2ELEM v2e \n" +
            "               inner join DATAELEM d on v2e.DATAELEM_ID = d.DATAELEM_ID \n" +
            "               inner join VOCABULARY_CONCEPT vc2 on v2e.VOCABULARY_ID = vc2.VOCABULARY_ID \n" +
            "               inner join VOCABULARY_CONCEPT_ELEMENT vce on vc2.VOCABULARY_CONCEPT_ID = vce.VOCABULARY_CONCEPT_ID and vce.DATAELEM_ID = v2e.DATAELEM_ID \n" +
            "		where \n" +
            "               v2e.VOCABULARY_ID = :vocabularyId and d.IDENTIFIER = 'skos:prefLabel' \n" +
            "		) preflabel on vc.VOCABULARY_CONCEPT_ID = preflabel.VOCABULARY_CONCEPT_ID \n" +
            "	where \n" +
            "       vc.VOCABULARY_ID = :vocabularyId" + this.append(" and vc.STATUS & :conceptStatus = :conceptStatus", this.isStatusDefined()) + " \n" +
            this.append(
            "           and vc.IDENTIFIER collate UTF8_GENERAL_CI like :conceptIdentifierPattern \n", 
                this.isConceptIdentifierDefined()
            ) +
            "           and (vc.LABEL collate UTF8_GENERAL_CI like :conceptLabelPattern or preflabel.PrefLabel collate UTF8_GENERAL_CI like :conceptLabelPattern) \n" +
            ") c \n" +
            "	inner join VOCABULARY_CONCEPT_ELEMENT v on c.VOCABULARY_CONCEPT_ID = v.VOCABULARY_CONCEPT_ID \n" +
            "   inner join DATAELEM d on v.DATAELEM_ID = d.DATAELEM_ID \n" +
            "   left join VOCABULARY_CONCEPT rc on v.RELATED_CONCEPT_ID = rc.VOCABULARY_CONCEPT_ID \n" +
            "where \n" +
            "	d.IDENTIFIER <> 'skos:prefLabel' or v.ELEMENT_VALUE collate UTF8_GENERAL_CI like :conceptLabelPattern \n" +
            "order by \n" +
            "   c.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, v.LANGUAGE, v.ID";
    }
    
    protected String append(String sqlSegment, boolean append) {
        return append ? sqlSegment : "";
    }
    
    protected boolean isStatusDefined() {
        return this.conceptStatus != null;
    }
    
    protected boolean isConceptIdentifierDefined() {
        return !StringUtils.isBlank(this.conceptIdentifier);
    }
    
    protected boolean isConceptLabelDefined() {
        return !StringUtils.isBlank(this.conceptLabel);
    }
    
    protected Map<String, Object> buildParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyId", this.vocabularyId);
        
        if (this.isStatusDefined()) {
            parameters.put("conceptStatus", this.conceptStatus.getValue());
        }
        
        if (this.isConceptIdentifierDefined()) {
            parameters.put("conceptIdentifierPattern", this.conceptIdentifier + "%");
        }
        
        if (this.isConceptLabelDefined()) {
            parameters.put("conceptLabelPattern", this.conceptLabel + "%");
        }
        
        return parameters;
    }
    
}

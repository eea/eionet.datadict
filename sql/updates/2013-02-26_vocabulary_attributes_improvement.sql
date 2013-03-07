ALTER TABLE M_ATTRIBUTE ADD COLUMN RDF_PROPERTY varchar(100);

UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:altLabel' WHERE SHORT_NAME = 'altLabel';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:hiddenLabel' WHERE SHORT_NAME = 'hiddenLabel';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:scopeNote' WHERE SHORT_NAME = 'scopeNote';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:changeNote' WHERE SHORT_NAME = 'changeNote';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:editorialNote' WHERE SHORT_NAME = 'editorialNote';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:historyNote' WHERE SHORT_NAME = 'historyNote';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:example' WHERE SHORT_NAME = 'example';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:prefLabel' WHERE SHORT_NAME = 'label';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:definition' WHERE SHORT_NAME = 'multiLangDefinition';
UPDATE M_ATTRIBUTE SET RDF_PROPERTY = 'skos:notation' WHERE SHORT_NAME = 'notation';
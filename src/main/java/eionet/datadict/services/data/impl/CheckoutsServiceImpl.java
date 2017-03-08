package eionet.datadict.services.data.impl;

import eionet.datadict.services.data.CheckoutsService;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.ISchemaDAO;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckoutsServiceImpl implements CheckoutsService {

    private final IDataSetDAO dataSetDao;
    private final IDataElementDAO dataElementDao;
    private final ISchemaSetDAO schemaSetDao;
    private final ISchemaDAO schemaDao;
    private final IVocabularyFolderDAO vocabularyFolderDao;

    @Autowired
    public CheckoutsServiceImpl(IDataSetDAO dataSetDao, IDataElementDAO dataElementDao, 
            ISchemaSetDAO schemaSetDao, ISchemaDAO schemaDao, IVocabularyFolderDAO vocabularyFolderDao) {
        this.dataSetDao = dataSetDao;
        this.dataElementDao = dataElementDao;
        this.schemaSetDao = schemaSetDao;
        this.schemaDao = schemaDao;
        this.vocabularyFolderDao = vocabularyFolderDao;
    }

    @Override
    public List<DataSet> getDataSetsWorkingCopies(String userName) {
        return this.dataSetDao.getWorkingCopiesOf(userName);
    }

    @Override
    public List<DataElement> getCommonDataElementsWorkingCopies(String userName) {
        return this.dataElementDao.getCommonDataElementsWorkingCopiesOf(userName);
    }

    @Override
    public List<SchemaSet> getSchemaSetsWorkingCopies(String userName) {
        return this.schemaSetDao.getWorkingCopiesOf(userName);
    }

    @Override
    public List<Schema> getSchemasWorkingCopies(String userName) {
        return this.schemaDao.getWorkingCopiesOf(userName);
    }

    @Override
    public List<VocabularyFolder> getVocabulariesWorkingCopies(String userName) {
        return this.vocabularyFolderDao.getWorkingCopies(userName);
    }

}

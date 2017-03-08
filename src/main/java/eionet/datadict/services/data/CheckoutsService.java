package eionet.datadict.services.data;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.List;

public interface CheckoutsService {

    List<DataSet> getDataSetsWorkingCopies(String userName);

    List<DataElement> getCommonDataElementsWorkingCopies(String userName);

    List<SchemaSet> getSchemaSetsWorkingCopies(String userName);

    List<Schema> getSchemasWorkingCopies(String userName);

    List<VocabularyFolder> getVocabulariesWorkingCopies(String userName);

}

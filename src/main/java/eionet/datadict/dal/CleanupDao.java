package eionet.datadict.dal;

import java.util.List;

public interface CleanupDao {
 
    String TABLE_OWNER_TYPE = "tbl";
    String DATA_ELEMENT_OWNER_TYPE = "elm";
    String TABLE_PARENT_TYPE = "T";

    int deleteBrokenDatasetToTableRelations();

    int deleteBrokenTableToElementRelations();

    int deleteDatasetToTableRelations(List<Integer> ids);

    int deleteTableRelationsWithElements(List<Integer> tableIds);

    int deleteElementRelationsWithTables(List<Integer> elementIds);

    int deleteDocs(String ownerType, List<Integer> ids);

    int deleteForeignKeyRelations(List<Integer> ids);

    int deleteInferenceRules(List<Integer> ids);

    int deleteOrphanNamespaces();

    int deleteOrphanAcls();

}

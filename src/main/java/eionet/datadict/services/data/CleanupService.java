package eionet.datadict.services.data;

public interface CleanupService {
    
    int deleteBrokenDatasetToTableRelations();

    int deleteOrphanTables();

    int deleteBrokenTableToElementRelations();

    int deleteOrphanNonCommonDataElements();

    int deleteOrphanNamespaces();

    int deleteOrphanAcls();

}

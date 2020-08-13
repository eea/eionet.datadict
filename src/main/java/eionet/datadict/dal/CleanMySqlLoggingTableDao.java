package eionet.datadict.dal;

/**
 * Deletes entries of table mysql.general_log that are older than 1 month
 */
public interface CleanMySqlLoggingTableDao {

    void delete();
}

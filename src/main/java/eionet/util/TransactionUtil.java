package eionet.util;

import eionet.meta.dao.Transaction;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class TransactionUtil {

    /**
     * 
     * @param transaction
     */
    public static void rollback(Transaction transaction){

        if (transaction!=null){
            transaction.rollback();
        }
    }

    /**
     * 
     * @param transaction
     */
    public static void close(Transaction transaction){

        if (transaction!=null){
            transaction.close();
        }
    }
}

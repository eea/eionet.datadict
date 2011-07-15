package eionet.util;

import java.util.Comparator;

import eionet.meta.Dataset;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DatasetDateComparator implements Comparator{

    /** */
    private int sortOrder = -1;

    /**
     *
     */
    public DatasetDateComparator() {
    }

    /**
     *
     * @param sortOrder
     */
    public DatasetDateComparator(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     *
     */
    public int compare(Object o1, Object o2) {

        if ((o1 instanceof Dataset)==false || (o2 instanceof Dataset)==false)
            return 0;
        else
            return compare((Dataset)o1, (Dataset)o2) * sortOrder;
    }

    /**
     *
     * @param dst1
     * @param dst2
     * @return
     */
    private int compare(Dataset dst1, Dataset dst2) {

        String dateStr1 = dst1.getDate();
        String dateStr2 = dst2.getDate();

        if (dateStr1==null && dateStr2==null)
            return 0;
        else if (dateStr1==null && dateStr2!=null)
            return -1;
        else if (dateStr1!=null && dateStr2==null)
            return 1;

        return Long.valueOf(dateStr1).compareTo(Long.valueOf(dateStr2));
    }
}

/*
 * DataSetSort.java
 * 
 * Created on Feb 12, 2016
 *            www.eworx.gr
 */
package eionet.meta;

import eionet.util.Util;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author js
 */
public enum DataSetSort {

    NAME, SHORT_NAME, STATUS, ID;

    public Comparator<Dataset> getComparator(final boolean descending) {
        final Comparator<Dataset> comparator;
        switch (this) {
            case ID:
                comparator = Comparator.comparingInt(d -> Integer.parseInt(d.getID()));
                break;
            case STATUS:
                comparator = Comparator.comparing(d -> Util.getStatusSortString(d.getStatus()));
                break;
            case SHORT_NAME:
                comparator = Comparator.comparing(Dataset::getShortName, String.CASE_INSENSITIVE_ORDER);
                break;
            case NAME:
            default:
                comparator = Comparator.comparing(Dataset::getName, String.CASE_INSENSITIVE_ORDER);
                break;
        }
        return descending ? comparator.reversed() : comparator;
    }

    public static DataSetSort fromString(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (DataSetSort value : DataSetSort.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

}

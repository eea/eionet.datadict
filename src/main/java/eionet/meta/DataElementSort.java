/*
 * DataElementSort.java
 * 
 * Created on Apr 5, 2016
 *            www.eworx.gr
 */
package eionet.meta;

import eionet.util.Util;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Ioannis Stamos <js@eworx.gr>
 */
public enum DataElementSort {

    ID, SHORT_NAME, STATUS, TYPE;

    public Comparator<DataElement> getComparator(final boolean descending) {
        final Comparator<DataElement> comparator;
        switch (this) {
            case ID:
                comparator = Comparator.comparingInt(d -> Integer.parseInt(d.getID()));
                break;
            case TYPE:
                comparator = Comparator.comparing(DataElement::getType, String.CASE_INSENSITIVE_ORDER);
                break;
            case STATUS:
                comparator = Comparator.comparing(d -> Util.getStatusSortString(d.getStatus()));
                break;
            case SHORT_NAME:
            default:
                comparator = Comparator.comparing(DataElement::getShortName, String.CASE_INSENSITIVE_ORDER);
                break;
        }
        return descending ? comparator.reversed() : comparator;
    }

    public static DataElementSort fromString(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (DataElementSort value : DataElementSort.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}

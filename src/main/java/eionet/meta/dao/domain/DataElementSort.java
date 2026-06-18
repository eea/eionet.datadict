/*
 * DataElementSort.java
 * 
 * Created on Apr 5, 2016
 *            www.eworx.gr
 */
package eionet.meta.dao.domain;

import eionet.util.Util;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Ioannis Stamos <js@eworx.gr>
 */
public enum DataElementSort {

    IDENTIFIER, NAME, STATUS, TYPE, TABLE_NAME, DATASET_NAME;

    public Comparator<DataElement> getComparator(final boolean descending) {
        final Comparator<DataElement> comparator;
        switch (this) {
            case IDENTIFIER:
                comparator = Comparator.comparing(DataElement::getIdentifier, String.CASE_INSENSITIVE_ORDER);
                break;
            case STATUS:
                comparator = Comparator.comparing(d -> Util.getStatusSortString(d.getStatus()));
                break;
            case TYPE:
                comparator = Comparator.comparing(DataElement::getType, String.CASE_INSENSITIVE_ORDER);
                break;
            case TABLE_NAME:
                comparator = Comparator.comparing(DataElement::getTableName, String.CASE_INSENSITIVE_ORDER);
                break;
            case DATASET_NAME:
                comparator = Comparator.comparing(DataElement::getDataSetName, String.CASE_INSENSITIVE_ORDER);
                break;
            case NAME:
            default:
                comparator = Comparator.comparing(DataElement::getName, String.CASE_INSENSITIVE_ORDER);
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

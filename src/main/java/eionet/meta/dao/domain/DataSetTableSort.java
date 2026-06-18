/*
 * DataSetSort.java
 *
 * Created on Feb 12, 2016
 *            www.eworx.gr
 */
package eionet.meta.dao.domain;

import eionet.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 *
 * @author Ioannis Stamos <js@eworx.gr>
 */
public enum DataSetTableSort {

    NAME, SHORT_NAME, DATASET, DATASET_STATUS;

    public Comparator<DataSetTable> getComparator(final boolean descending) {
        final Comparator<DataSetTable> comparator;
        switch (this) {
            case SHORT_NAME:
                comparator = Comparator.comparing(DataSetTable::getShortName, String.CASE_INSENSITIVE_ORDER);
                break;
            case DATASET:
                comparator = Comparator.comparing(DataSetTable::getDataSetName, String.CASE_INSENSITIVE_ORDER);
                break;
            case DATASET_STATUS:
                comparator = Comparator.comparing(d -> Util.getStatusSortString(d.getDataSetStatus()));
                break;
            case NAME:
            default:
                comparator = Comparator.comparing(DataSetTable::getName, String.CASE_INSENSITIVE_ORDER);
                break;
        }
        return descending ? comparator.reversed() : comparator;
    }

    public static DataSetTableSort fromString(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (DataSetTableSort value : DataSetTableSort.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

}

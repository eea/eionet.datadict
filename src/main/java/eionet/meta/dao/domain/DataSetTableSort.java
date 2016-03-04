/*
 * DataSetSort.java
 * 
 * Created on Feb 12, 2016
 *            www.eworx.gr
 */
package eionet.meta.dao.domain;

import eionet.util.Util;
import java.util.Comparator;

/**
 *
 * @author Ioannis Stamos <js@eworx.gr>
 */
public enum DataSetTableSort {

    NAME, SHORT_NAME, DATASET, DATASET_STATUS;

    public Comparator<DataSetTable> getComparator(final boolean descending) {
        if (this == NAME) {
            return new Comparator<DataSetTable>() {
                @Override
                public int compare(DataSetTable d1, DataSetTable d2) {
                    return descending ? -d1.getName().compareToIgnoreCase(d2.getName()) : 
                            d1.getName().compareToIgnoreCase(d2.getName());
                }
            };
        }
        if (this == SHORT_NAME) {
            return new Comparator<DataSetTable>() {
                @Override
                public int compare(DataSetTable d1, DataSetTable d2) {
                    return descending ? -d1.getShortName().compareToIgnoreCase(d2.getShortName()) : 
                            d1.getShortName().compareToIgnoreCase(d2.getShortName());
                }
            };
        }
        if (this == DATASET) {
            return new Comparator<DataSetTable>() {
                @Override
                public int compare(DataSetTable d1, DataSetTable d2) {
                    return descending ? -d1.getDataSetName().compareToIgnoreCase(d2.getDataSetName()) : 
                            d1.getDataSetName().compareToIgnoreCase(d2.getDataSetName());
                }
            };
        }
        return new Comparator<DataSetTable>() {
            @Override
            public int compare(DataSetTable d1, DataSetTable d2) {
                return descending ? -Util.getStatusSortString(d1.getDataSetStatus()).compareTo(Util.getStatusSortString(d2.getDataSetStatus())) :
                        Util.getStatusSortString(d1.getDataSetStatus()).compareTo(Util.getStatusSortString(d2.getDataSetStatus()));
            }
        };
    }

    public static DataSetTableSort fromString(String name) {
        for (DataSetTableSort value : DataSetTableSort.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

}

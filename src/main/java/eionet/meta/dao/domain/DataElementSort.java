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
        if (this == IDENTIFIER) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -d1.getIdentifier().compareToIgnoreCase(d2.getIdentifier()) : 
                            d1.getIdentifier().compareToIgnoreCase(d2.getIdentifier());
                }
            };
        }
        if (this == NAME) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -d1.getName().compareToIgnoreCase(d2.getName()) :
                            d1.getName().compareToIgnoreCase(d2.getName());
                }
            };
        }
        if (this == TYPE) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -d1.getType().compareToIgnoreCase(d2.getType()) : 
                            d1.getType().compareToIgnoreCase(d2.getType());
                }
            };
        }
        if (this == STATUS) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -Util.getStatusSortString(d1.getStatus()).compareTo(Util.getStatusSortString(d2.getStatus())) :
                            Util.getStatusSortString(d1.getStatus()).compareTo(Util.getStatusSortString(d2.getStatus()));
                }
            };
        }
        if (this == TABLE_NAME) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -d1.getTableName().compareToIgnoreCase(d2.getTableName()) : 
                            d1.getTableName().compareToIgnoreCase(d2.getTableName());
                }
            };
        }
        if (this == DATASET_NAME) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -d1.getDataSetName().compareToIgnoreCase(d2.getDataSetName()) : 
                            d1.getDataSetName().compareToIgnoreCase(d2.getDataSetName());
                }
            };
        }
        return null;
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

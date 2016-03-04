/*
 * DataSetSort.java
 * 
 * Created on Feb 12, 2016
 *            www.eworx.gr
 */
package eionet.meta.dao.domain;

import eionet.meta.Dataset;
import eionet.util.Util;
import java.util.Comparator;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author js
 */
public enum DataSetSort {
    
    NAME, STATUS, ID;

    public Comparator<Dataset> getComparator(final boolean descending) {
        if (this == ID) {
            return new Comparator<Dataset>() {
                @Override
                public int compare(Dataset d1, Dataset d2) {
                    return descending ? -d1.getIdentifier().compareToIgnoreCase(d2.getIdentifier()) : 
                            d1.getIdentifier().compareToIgnoreCase(d2.getIdentifier());
                }
            };
        }
        if (this == STATUS) {
            return new Comparator<Dataset>() {
                @Override
                public int compare(Dataset d1, Dataset d2) {
                    return descending ? -Util.getStatusSortString(d1.getStatus()).compareTo(Util.getStatusSortString(d2.getStatus())) :
                            Util.getStatusSortString(d1.getStatus()).compareTo(Util.getStatusSortString(d2.getStatus()));
                }
            };
        }
        return new Comparator<Dataset>() {
            @Override
            public int compare(Dataset d1, Dataset d2) {
                return descending ? -d1.getName().compareToIgnoreCase(d2.getName()) :
                        d1.getName().compareToIgnoreCase(d2.getName());
            }
        };
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

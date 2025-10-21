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
        if (this == ID) {
            return new Comparator<Dataset>() {
                @Override
                public int compare(Dataset d1, Dataset d2) {
                    return descending ? -Integer.valueOf(d1.getID()).compareTo(Integer.valueOf(d2.getID())) : 
                            Integer.valueOf(d1.getID()).compareTo(Integer.valueOf(d2.getID()));
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
        if (this == SHORT_NAME) {
            return new Comparator<Dataset>() {
                @Override
                public int compare(Dataset d1, Dataset d2) {
                     return descending ? -d1.getShortName().compareToIgnoreCase(d2.getShortName()) :
                        d1.getShortName().compareToIgnoreCase(d2.getShortName());
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

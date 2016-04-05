/*
 * DataElementSort.java
 * 
 * Created on Apr 5, 2016
 *            www.eworx.gr
 */
package eionet.meta;

import eionet.util.Util;
import java.util.Comparator;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ioannis Stamos <js@eworx.gr>
 */
public enum DataElementSort {

    ID, SHORT_NAME, STATUS, TYPE;

    public Comparator<DataElement> getComparator(final boolean descending) {
        if (this == ID) {
            return new Comparator<DataElement>() {
                @Override
                public int compare(DataElement d1, DataElement d2) {
                    return descending ? -Integer.valueOf(d1.getID()).compareTo(Integer.valueOf(d2.getID())) : 
                            Integer.valueOf(d1.getID()).compareTo(Integer.valueOf(d2.getID()));
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
        return new Comparator<DataElement>() {
            @Override
            public int compare(DataElement d1, DataElement d2) {
                return descending ? -d1.getShortName().compareToIgnoreCase(d2.getShortName()) :
                        d1.getShortName().compareToIgnoreCase(d2.getShortName());
            }
        };
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

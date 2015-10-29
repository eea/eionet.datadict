/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.dao.domain.util;

import eionet.meta.FixedValue;
import eionet.util.StringOrdinalComparator;
import java.util.Comparator;

/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */
public class FixedValuesOrdinalComparator implements Comparator<FixedValue> {

    private final StringOrdinalComparator cmp = new StringOrdinalComparator();

    @Override
    public int compare(FixedValue val1, FixedValue val2) {
        return cmp.compare(val1.getValue(), val2.getValue());
    }

}

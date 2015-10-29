/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.dao.domain.util;

import eionet.meta.FixedValue;
import eionet.util.StringOrdinalComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */
public class FixedValueOrdinalComparator {
    
    public FixedValueOrdinalComparator () { }
    
    public ArrayList<FixedValue> getFixedValuesOrderedByCode(ArrayList unorderedFixedValuesVector) {
        

        Collections.sort(unorderedFixedValuesVector, new Comparator<FixedValue>() {
            private StringOrdinalComparator cmp = new StringOrdinalComparator();

            @Override
            public int compare(FixedValue o1, FixedValue o2) {
                return cmp.compare(o1.getValue(), o2.getValue());
            }
        });

        return unorderedFixedValuesVector;
    }
}

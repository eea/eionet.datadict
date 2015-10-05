/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class for filtering based on Predicate 
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class PredicateFiltering {
    
    protected PredicateFiltering(){}
    
    public static <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for (T element: target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }
}

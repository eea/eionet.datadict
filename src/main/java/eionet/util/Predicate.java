/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.util;

/**
 * Utility for filtering Collections based on predicates
 * @author Lena KARGIOTI eka@eworx.gr
 * @param <T>
 */
public interface Predicate<T> {
    boolean apply(T type);
}

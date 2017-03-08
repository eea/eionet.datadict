/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action.di;

import net.sourceforge.stripes.action.ActionBean;

/**
 *
 * @author exorx-alk
 */
public interface SpyDependencyInjector {
    
    boolean accepts(ActionBean bean);
    
    ActionBean getSpyActionBean(ActionBean executionContextActionBean);
}

package eionet.web.action.di;

import net.sourceforge.stripes.action.ActionBean;

/**
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ActionBeanDependencyInjector {

    boolean accepts(ActionBean bean);
    
    void injectDependencies(ActionBean bean);
}

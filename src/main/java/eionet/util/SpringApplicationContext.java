/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class SpringApplicationContext implements ApplicationContextAware {

  private static ApplicationContext context;

  /**
   * This method is called from within the ApplicationContext once it is 
   * done starting up, it will stick a reference to itself into this bean.
   * @param appContext a reference to the ApplicationContext.
   */
  @Override
  public void setApplicationContext(ApplicationContext appContext) throws BeansException {
    context = appContext;
  }

  /**
   * @param beanName the name of the bean to get.
   * @return an Object reference to the named bean.
   */
  public static Object getBean(String beanName) {
    return context.getBean(beanName);
  }
  /**
   * 
   * @param <T> the class of the bean to get
   * @param type
   * @return an Object of type T
   */
  public static <T extends Object> T getBean(Class<T> type){
      return context.getBean(type);
  }
    
}

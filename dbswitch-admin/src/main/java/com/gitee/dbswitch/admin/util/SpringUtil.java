// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SpringUtil.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return SpringUtil.applicationContext;
  }

  public static Object getBean(String name) {
    return SpringUtil.applicationContext.getBean(name);
  }

  public static <T> T getBean(Class<T> clazz) {
    return SpringUtil.applicationContext.getBean(clazz);
  }

  public static <T> T getBean(Class<T> clazz, Object... objects) {
    return SpringUtil.applicationContext.getBean(clazz, objects);
  }

  public static <T> T getBean(String name, Class<T> clazz) {
    return SpringUtil.applicationContext.getBean(name, clazz);
  }

}

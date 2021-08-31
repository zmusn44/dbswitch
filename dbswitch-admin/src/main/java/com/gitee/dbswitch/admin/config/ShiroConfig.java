// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.config;

import com.gitee.dbswitch.admin.config.shiro.UserAuthenticatingFilter;
import com.gitee.dbswitch.admin.config.shiro.SystemUserRealm;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {

  /**
   * 向Spring容器注册Shiro认证授权的Realm所需的密码匹配器CredentialsMatcher
   *
   * @return org.apache.shiro.authc.credential.CredentialsMatcher
   */
  @Bean
  public CredentialsMatcher getCredentialsMatcher() {
    //这里使用简单的密码匹配器
    SimpleCredentialsMatcher credentialsMatcher = new SimpleCredentialsMatcher();
    return credentialsMatcher;
  }

  /**
   * 向Spring容器注册Shiro认证授权所需的Realm
   *
   * @return org.apache.shiro.realm.Realm
   */
  @Bean
  public org.apache.shiro.realm.Realm getRealm() {
    org.apache.shiro.realm.AuthorizingRealm realm = new SystemUserRealm();
    realm.setCredentialsMatcher(getCredentialsMatcher());
    return realm;
  }

  /**
   * 向Spring容器注册Shiro的安全管理器SecurityManager
   *
   * @return org.apache.shiro.mgt.SecurityManager
   */
  @Bean
  public org.apache.shiro.mgt.SecurityManager getSecurityManager() {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    securityManager.setRealm(getRealm());
    return securityManager;
  }

  /**
   * 向Spring容器注册Shiro的过滤器ShiroFilterFactoryBean
   *
   * @return org.apache.shiro.spring.web.ShiroFilterFactoryBean
   */
  @Bean
  public ShiroFilterFactoryBean getShiroFilterFactoryBean() {
    ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
    shiroFilter.setSecurityManager(getSecurityManager());

    //auth过滤
    Map<String, Filter> filters = new HashMap<>();
    filters.put("auth", new UserAuthenticatingFilter());
    shiroFilter.setFilters(filters);

    Map<String, String> filterMap = new LinkedHashMap<>();
    // anno匿名访问  auth验证
    filterMap.put("/static/**", "anon");
    filterMap.put("/index.html", "anon");
    filterMap.put("/favicon.ico", "anon");
    filterMap.put("/webjars/**", "anon");
    filterMap.put(SwaggerConfig.API_V1 + "/authentication/login", "anon");
    filterMap.put("/swagger/**", "anon");
    filterMap.put("/v2/api-docs", "anon");
    filterMap.put("/swagger-ui.html", "anon");
    filterMap.put("/swagger-resources/**", "anon");
    // 除了以上路径，其他都需要权限验证
    filterMap.put("/**", "auth");
    shiroFilter.setFilterChainDefinitionMap(filterMap);

    return shiroFilter;
  }

  /**
   * 开启AOP注解支持
   */
  @Bean
  public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
    DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
    advisorAutoProxyCreator.setProxyTargetClass(true);
    return advisorAutoProxyCreator;
  }

  /**
   * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions)
   * <p>
   * 参考地址：https://blog.csdn.net/qq_2300688967/article/details/81195039
   * </p>
   */
  @Bean
  public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
    AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
    authorizationAttributeSourceAdvisor.setSecurityManager(getSecurityManager());
    return authorizationAttributeSourceAdvisor;
  }

}

// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.config.shiro;

import com.gitee.dbswitch.admin.dao.SystemUserDAO;
import com.gitee.dbswitch.admin.entity.SystemUserEntity;
import com.gitee.dbswitch.admin.util.CacheUtil;
import com.gitee.dbswitch.admin.util.ServletUtil;
import java.util.Collections;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shiro授权认证所需的Realm实现类
 * <p>
 * 说明：该类通过Spring的配置类注入到容器中
 * </p>
 *
 * @author tang
 */
public class SystemUserRealm extends AuthorizingRealm {

  @Autowired
  private SystemUserDAO systemUserDAO;

  /**
   * 这里覆盖掉org.apache.shiro.realm.AuthenticatingRealm的方法，以支持我们自定义的 io.gitee.inrgihc.sbvb.domain.shiro.UserAuthenticationToken类。
   */
  @Override
  public boolean supports(AuthenticationToken token) {
    return token != null && UserAuthenticationToken.class.isAssignableFrom(token.getClass());
  }

  /**
   * <p>
   * 授权实现函数
   * </p>
   * <p>
   * 代码内容为：配置用户的角色和权限
   * </p>
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    //根据缓存将token转换成User实体对象
    String accessToken = (String) principals.getPrimaryPrincipal();
    Object cache = CacheUtil.get(accessToken);
    if (null == cache) {
      throw new RuntimeException("token不存在或已经失效，请重新登录!");
    }

    SystemUserEntity systemUserEntity = (SystemUserEntity) cache;

    //根据用户的username配置角色和权限
    SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
    authorizationInfo.setRoles(Collections.singleton("all"));
    authorizationInfo.setStringPermissions(Collections.singleton("all"));

    //返回授权信息实例对象
    return authorizationInfo;
  }

  /**
   * <p>
   * 认证实现函数
   * </p>
   * <p>
   * 代码内容为：token的有效性验证,这里通过设置AuthenticationToken(实际实现类 为io.gitee.inrgihc.sbvb.domain.shiro.UserAuthenticationToken)
   * 的getCredentials()等于AuthenticationInfo(实际实现类为下面代码中new的 SimpleAuthenticationInfo)的getCredentials()以跳过了shiro的认证。
   * </p>
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
      throws AuthenticationException {
    String accessToken = (String) token.getPrincipal();

    //根据缓存将token转换成User实体对象
    Object cache = CacheUtil.get(accessToken);
    if (null == cache) {
      throw new RuntimeException("token不存在或已经失效，请重新登录!");
    }

    //判断数据库中的User实体对象的有效性
    SystemUserEntity systemUserEntity = (SystemUserEntity) cache;
    SystemUserEntity user = systemUserDAO.findByUsername(systemUserEntity.getUsername());
    if (null == user) {
      throw new RuntimeException("token所使用的认证用户不存在，或者已经被删除!");
    } else if (Boolean.TRUE.equals(user.getLocked())) {
      throw new LockedAccountException("token所使用的认证用户已经被锁定"); // 帐号锁定
    }

    ServletUtil.getHttpServletRequest().setAttribute("username",user.getUsername());

    return new SimpleAuthenticationInfo(accessToken, accessToken, this.getName());
  }

}

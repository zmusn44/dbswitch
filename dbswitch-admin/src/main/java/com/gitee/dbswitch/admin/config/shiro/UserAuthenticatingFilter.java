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

import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.util.JsonUtil;
import com.gitee.dbswitch.admin.util.ServletUtil;
import com.gitee.dbswitch.admin.util.TokenUtil;
import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * shiro过滤器
 *
 * @author tang
 */
public class UserAuthenticatingFilter extends AuthenticatingFilter {

  /**
   * 生成自定义UserAuthenticationToken类型的token
   */
  @Override
  protected AuthenticationToken createToken(ServletRequest request, ServletResponse response)
      throws Exception {
    String token = TokenUtil.getRequestToken((HttpServletRequest) request);
    return new UserAuthenticationToken(token);
  }

  /**
   * 步骤1.所有请求全部拒绝访问
   */
  @Override
  protected boolean isAccessAllowed(ServletRequest request, ServletResponse response,
      Object mappedValue) {
    if (((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name())) {
      return true;
    }

    return false;
  }

  /**
   * 步骤2，拒绝访问的请求，会调用onAccessDenied方法，onAccessDenied方法先获取 token，再调用executeLogin方法
   */
  @Override
  protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
      throws Exception {
    String path=((HttpServletRequest) request).getRequestURI();
    if("/".equals(path)){
      return true;
    }

    String token = TokenUtil.getRequestToken((HttpServletRequest) request);
    if (StringUtils.isEmpty(token)) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
      httpResponse.setHeader("Access-Control-Allow-Origin", ServletUtil.getOrigin());
      httpResponse.setCharacterEncoding("UTF-8");

      Result result = Result.failed(ResultCode.ERROR_ACCESS_FORBIDDEN, "请登录");
      String json = JsonUtil.toJsonString(result);
      httpResponse.getWriter().print(json);

      return false;
    }

    return executeLogin(request, response);
  }

  /**
   * token失效时候调用
   */
  @Override
  protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
      ServletRequest request,
      ServletResponse response) {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.setContentType("application/json;charset=utf-8");
    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
    httpResponse.setHeader("Access-Control-Allow-Origin", ServletUtil.getOrigin());
    httpResponse.setCharacterEncoding("UTF-8");
    try {
      Result result = Result.failed(ResultCode.ERROR_TOKEN_EXPIRED, "请登录");
      String json = JsonUtil.toJsonString(result);
      httpResponse.getWriter().print(json);
    } catch (IOException ex) {
    }

    return false;
  }

}

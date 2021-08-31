// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.controller.publicapi;

import com.gitee.dbswitch.admin.common.annotation.AccessLog;
import com.gitee.dbswitch.admin.common.annotation.NoToken;
import com.gitee.dbswitch.admin.common.annotation.OperateLog;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.config.SwaggerConfig;
import com.gitee.dbswitch.admin.service.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"账号认证接口"})
@RestController
@RequestMapping(value = SwaggerConfig.API_V1 + "/authentication")
public class AuthenticationController {

  @Resource
  private AuthenticationService authenticationService;

  @NoToken
  @AccessLog(value = "认证登陆", description = "'认证登陆的用户名为：'+#username")
  @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "账号登录", notes = "使用一个账号密码登录")
  @ApiImplicitParams({
      @ApiImplicitParam(paramType = "query", name = "username", value = "用户名", required = true, dataTypeClass = String.class),
      @ApiImplicitParam(paramType = "query", name = "password", value = "密码", required = true, dataTypeClass = String.class)})
  public Result<?> login(String username, String password) {
    return authenticationService.login(username, password);
  }

  @OperateLog(name = "退出登录")
  @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "认证登出", notes = "登出系统")
  @ApiImplicitParams({
      @ApiImplicitParam(paramType = "header", dataType = "String", name = "token", value = "token标记", required = true)})
  public Result logout() {
    return authenticationService.logout();
  }

}

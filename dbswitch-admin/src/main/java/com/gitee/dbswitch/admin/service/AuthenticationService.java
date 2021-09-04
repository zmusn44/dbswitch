// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.entity.SystemUserEntity;
import com.gitee.dbswitch.admin.model.response.AccessTokenResponse;
import com.gitee.dbswitch.admin.util.CacheUtil;
import com.gitee.dbswitch.admin.util.PasswordUtil;
import com.gitee.dbswitch.admin.util.ServletUtil;
import com.gitee.dbswitch.admin.util.TokenUtil;
import com.mchange.v2.lang.StringUtils;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {

  @Resource
  private SystemUserService systemUserService;

  public Result<AccessTokenResponse> login(String username, String password) {
    SystemUserEntity user = systemUserService.findByUsername(username);
    if (Objects.isNull(user)) {
      return Result.failed(ResultCode.ERROR_USER_NOT_EXISTS, username);
    }

    String encryptPassword = PasswordUtil.encryptPassword(password, user.getSalt());
    if (!encryptPassword.equals(user.getPassword())) {
      return Result.failed(ResultCode.ERROR_USER_PASSWORD_WRONG, username);
    }

    String token = TokenUtil.generateValue();
    CacheUtil.put(token, user);
    AccessTokenResponse accessTokenWrapper = new AccessTokenResponse(user.getRealName(), token,
        CacheUtil.CACHE_DURATION_SECONDS);
    return Result.success(accessTokenWrapper);
  }

  public Result logout() {
    String token = TokenUtil.getRequestToken(ServletUtil.getHttpServletRequest());
    if (StringUtils.nonEmptyString(token)) {
      CacheUtil.remove(token);
    }

    log.info("logout with token:{}", token);
    return Result.success();
  }

}

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

import org.apache.shiro.authc.AuthenticationToken;

public class UserAuthenticationToken implements AuthenticationToken {

  private static final long serialVersionUID = 97776669757414720L;

  private String accessToken;

  public UserAuthenticationToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public Object getPrincipal() {
    return accessToken;
  }

  @Override
  public Object getCredentials() {
    return accessToken;
  }

}

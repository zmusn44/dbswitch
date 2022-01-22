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

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public final class PasswordUtils {

  //hash算法名称
  private static String algorithmName = "md5";

  //hash迭代次数
  private static int hashIterations = 2;

  public static String encryptPassword(String password, String credentialsSalt) {
    return encryptPassword(password, credentialsSalt, algorithmName, hashIterations);
  }

  public static String encryptPassword(String password, String credentialsSalt,
      String algorithmName, int hashIterations) {
    String newPassword = new SimpleHash(algorithmName, password,
        ByteSource.Util.bytes(credentialsSalt),
        hashIterations).toHex();

    return newPassword;
  }

  public static void main(String[] args) {
    String password = "123456";
    String credentialsSalt = "test";
    String newPassword = encryptPassword(password, credentialsSalt);
    System.out.println(newPassword);
  }

}

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

import cn.hutool.crypto.digest.BCrypt;

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
    String newPassword = BCrypt.hashpw(password, credentialsSalt);
    //String newPassword = new SimpleHash(algorithmName, password,
    //    ByteSource.Util.bytes(credentialsSalt),
    //    hashIterations).toHex();

    return newPassword;
  }

  public static void main(String[] args) {
    String password = "123456";
    String credentialsSalt = "$2a$10$eUanVjvzV27BBxAb4zuBCu";//BCrypt.gensalt();
    String newPassword = encryptPassword(password, credentialsSalt);
    System.out.println(newPassword);
    System.out.println(credentialsSalt);
  }

}

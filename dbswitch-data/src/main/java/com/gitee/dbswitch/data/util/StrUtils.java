package com.gitee.dbswitch.data.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * 字符串工具类
 *
 * @author tang
 * @date 2021/6/8 20:55
 */
public final class StrUtils {

  /**
   * 根据逗号切分字符串为数组
   *
   * @param str 待切分的字符串
   * @return List
   */
  public static List<String> stringToList(String str) {
    if (!StringUtils.isEmpty(str)) {
      String[] strs = str.split(",");
      if (strs.length > 0) {
        return new ArrayList<>(Arrays.asList(strs));
      }
    }

    return new ArrayList<>();
  }

  private StrUtils() {
  }
}

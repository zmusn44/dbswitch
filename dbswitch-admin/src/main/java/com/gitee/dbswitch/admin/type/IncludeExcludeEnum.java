package com.gitee.dbswitch.admin.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IncludeExcludeEnum {
  INCLUDE("包含"),
  EXCLUDE("排除"),
  ;

  private String name;
}

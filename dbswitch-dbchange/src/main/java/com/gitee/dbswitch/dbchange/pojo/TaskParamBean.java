// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.dbchange.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.lang.Nullable;
import lombok.AllArgsConstructor;

/**
 * 任务参数实体类定义
 *
 * @author tang
 */
@Data
@Builder
@AllArgsConstructor
public class TaskParamBean {

  /**
   * 老表的数据源
   */
  @NonNull
  DataSource oldDataSource;

  /**
   * 老表的schema名
   */
  @NonNull
  private String oldSchemaName;

  /**
   * 老表的table名
   */
  @NonNull
  private String oldTableName;

  /**
   * 新表的数据源
   */
  @NonNull
  DataSource newDataSource;

  /**
   * 新表的schema名
   */
  @NonNull
  private String newSchemaName;

  /**
   * 新表的table名
   */
  @NonNull
  private String newTableName;

  /**
   * 字段列
   */
  @Nullable
  private List<String> fieldColumns;
}

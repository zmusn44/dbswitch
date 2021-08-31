// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gitee.dbswitch.admin.type.ScheduleModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@ApiModel("任务详情")
public class AssignmentDetailResponse {

  @ApiModelProperty("ID编号")
  private Long id;

  @ApiModelProperty("任务名")
  private String name;

  @ApiModelProperty("描述")
  private String description;

  @ApiModelProperty("调度模式")
  private ScheduleModeEnum scheduleMode;

  @ApiModelProperty("Cron表达式")
  private String cronExpression;

  @ApiModelProperty("是否已发布")
  private Boolean isPublished;

  @ApiModelProperty("配置信息")
  private Configuration configuration;

  @ApiModelProperty("创建时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Timestamp createTime;

  @ApiModelProperty("更新时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Timestamp updateTime;

  @NoArgsConstructor
  @Data
  @ApiModel("任务配置")
  public static class Configuration {

    @ApiModelProperty("源端连接ID")
    private Long sourceConnectionId;

    @ApiModelProperty("源端连接名称")
    private String sourceConnectionName;

    @ApiModelProperty("源端数据源的Schema")
    private List<String> sourceSchemas;

    @ApiModelProperty("目的端连接ID")
    private Long targetConnectionId;

    @ApiModelProperty("目的端连接名称")
    private String targetConnectionName;

    @ApiModelProperty("目的端数据源的Schema")
    private String targetSchema;

    @ApiModelProperty("目的端的表名前缀")
    private String tablePrefix;
  }
}